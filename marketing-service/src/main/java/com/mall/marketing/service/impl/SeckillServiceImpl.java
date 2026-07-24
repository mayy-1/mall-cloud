package com.mall.marketing.service.impl;

import com.mall.marketing.domain.QueueEnum;
import com.mall.marketing.domain.dto.SeckillOrderMessage;
import com.mall.marketing.domain.dto.SeckillOrderParam;
import com.mall.marketing.domain.dto.SeckillProductDetailDTO;
import com.mall.marketing.mapper.SmsFlashPromotionMapper;
import com.mall.marketing.mapper.SmsFlashPromotionProductRelationMapper;
import com.mall.marketing.model.SmsFlashPromotion;
import com.mall.marketing.model.SmsFlashPromotionProductRelation;
import com.mall.marketing.service.SeckillService;
import com.mall.api.client.product.ProductClient;
import com.mall.api.dto.ProductDTO;
import com.mym.mall.common.api.CommonResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 秒杀服务实现
 * 核心流程：Lua 脚本原子扣减 → 发送 MQ 消息 → 异步创建订单
 */
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillServiceImpl.class);

    private static final String SECKILL_STOCK_KEY = "seckill:stock:%d:%d";
    private static final String SECKILL_USERS_KEY = "seckill:users:%d:%d";

    /** Lua 脚本返回值：秒杀成功 */
    private static final int SECKILL_SUCCESS = 1;
    /** Lua 脚本返回值：库存不足 */
    private static final int SECKILL_STOCK_EMPTY = 0;
    /** Lua 脚本返回值：用户已购买 */
    private static final int SECKILL_REPEAT = -1;

    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final SmsFlashPromotionMapper flashPromotionMapper;
    private final SmsFlashPromotionProductRelationMapper productRelationMapper;
    /** 商品服务 Feign */
    private final ProductClient productClient;

    /** 预加载 Lua 脚本 */
    private final DefaultRedisScript<Long> seckillScript = createSeckillScript();

    @Override
    public int executeSeckill(SeckillOrderParam param) {
        Long promotionId = param.getPromotionId();
        Long productId = param.getProductId();
        Long memberId = param.getMemberId();

        // 1. 校验秒杀活动状态
        SmsFlashPromotion promotion = flashPromotionMapper.selectByPrimaryKey(promotionId);
        if (promotion == null || promotion.getStatus() != 1) {
            LOGGER.warn("秒杀活动不存在或已下线, promotionId={}", promotionId);
            return SECKILL_STOCK_EMPTY;
        }

        // 2. 校验秒杀商品关联
        SmsFlashPromotionProductRelation condition = new SmsFlashPromotionProductRelation();
        condition.setFlashPromotionId(promotionId);
        condition.setProductId(productId);
        List<SmsFlashPromotionProductRelation> relations = productRelationMapper.selectByCondition(condition);
        if (relations.isEmpty()) {
            LOGGER.warn("秒杀商品关联不存在, promotionId={}, productId={}", promotionId, productId);
            return SECKILL_STOCK_EMPTY;
        }

        SmsFlashPromotionProductRelation relation = relations.get(0);

        // 3. 构造 Redis Key
        String stockKey = String.format(SECKILL_STOCK_KEY, promotionId, productId);
        String usersKey = String.format(SECKILL_USERS_KEY, promotionId, productId);

        // 4. 执行 Lua 原子脚本：判断用户是否已购买 + 判断库存 + 扣减
        Long result = redisTemplate.execute(
                seckillScript,
                List.of(stockKey, usersKey),
                memberId.toString(),
                String.valueOf(relation.getFlashPromotionLimit())
        );

        int seckillResult = result != null ? result.intValue() : SECKILL_STOCK_EMPTY;

        if (seckillResult == SECKILL_SUCCESS) {
            // 5. Lua 扣减成功 → 发送 MQ 异步消息
            SeckillOrderMessage message = SeckillOrderMessage.builder()
                    .promotionId(promotionId)
                    .sessionId(param.getSessionId())
                    .productId(productId)
                    .productName(getProductName(productId))
                    .memberId(memberId)
                    .seckillPrice(relation.getFlashPromotionPrice())
                    .quantity(1)
                    .createTime(new Date())
                    .build();

            rabbitTemplate.convertAndSend(
                    QueueEnum.QUEUE_SECKILL_ORDER.getExchange(),
                    QueueEnum.QUEUE_SECKILL_ORDER.getRouteKey(),
                    message
            );
            LOGGER.info("秒杀成功，已发送MQ消息, memberId={}, productId={}", memberId, productId);
        } else if (seckillResult == SECKILL_STOCK_EMPTY) {
            LOGGER.info("秒杀失败，库存不足, memberId={}, productId={}", memberId, productId);
        } else if (seckillResult == SECKILL_REPEAT) {
            LOGGER.info("秒杀失败，用户重复购买, memberId={}, productId={}", memberId, productId);
        }

        return seckillResult;
    }

    @Override
    public void warmUpStock(Long promotionId) {
        SmsFlashPromotionProductRelation condition = new SmsFlashPromotionProductRelation();
        condition.setFlashPromotionId(promotionId);
        List<SmsFlashPromotionProductRelation> relations = productRelationMapper.selectByCondition(condition);

        for (SmsFlashPromotionProductRelation relation : relations) {
            String stockKey = String.format(SECKILL_STOCK_KEY, promotionId, relation.getProductId());
            String usersKey = String.format(SECKILL_USERS_KEY, promotionId, relation.getProductId());

            // 仅在 key 不存在时设置（避免覆盖已运行的秒杀数据）
            Boolean stockExists = redisTemplate.hasKey(stockKey);
            if (Boolean.FALSE.equals(stockExists)) {
                redisTemplate.opsForValue().set(stockKey, relation.getFlashPromotionCount());
                LOGGER.info("预热库存, promotionId={}, productId={}, stock={}",
                        promotionId, relation.getProductId(), relation.getFlashPromotionCount());
            }

            // 初始化已购买用户 Set（如果不存在，设置过期时间为活动结束后 1 天）
            Boolean usersExists = redisTemplate.hasKey(usersKey);
            if (Boolean.FALSE.equals(usersExists)) {
                // 空 Set 无需初始化；购买时 sadd 会自动创建
                LOGGER.info("初始化用户记录 key, promotionId={}, productId={}", promotionId, relation.getProductId());
            }
        }
    }

    @Override
    public void reconcileStock(Long promotionId) {
        SmsFlashPromotionProductRelation condition = new SmsFlashPromotionProductRelation();
        condition.setFlashPromotionId(promotionId);
        List<SmsFlashPromotionProductRelation> relations = productRelationMapper.selectByCondition(condition);

        for (SmsFlashPromotionProductRelation relation : relations) {
            String stockKey = String.format(SECKILL_STOCK_KEY, promotionId, relation.getProductId());
            Object redisStockObj = redisTemplate.opsForValue().get(stockKey);
            int redisStock = redisStockObj != null ? Integer.parseInt(redisStockObj.toString()) : 0;

            int dbStock = relation.getFlashPromotionCount();

            if (redisStock != dbStock) {
                LOGGER.warn("秒杀库存不一致, promotionId={}, productId={}, redisStock={}, dbStock={}",
                        promotionId, relation.getProductId(), redisStock, dbStock);
                // 以 DB 为准修正 Redis（DB 已通过 MQ 消费者扣减过）
                redisTemplate.opsForValue().set(stockKey, dbStock);
            }
        }
    }

    @Override
    public Long getSeckillStock(Long promotionId, Long productId) {
        String stockKey = String.format(SECKILL_STOCK_KEY, promotionId, productId);
        Object stock = redisTemplate.opsForValue().get(stockKey);
        return stock != null ? Long.parseLong(stock.toString()) : 0L;
    }

    @Override
    public List<SeckillProductDetailDTO> getCurrentSeckillProducts() {
        List<SeckillProductDetailDTO> result = new ArrayList<>();
        Date now = new Date();

        // 查询所有启用状态的秒杀活动
        List<SmsFlashPromotion> promotions = flashPromotionMapper.selectByCondition(null);
        for (SmsFlashPromotion promotion : promotions) {
            if (promotion.getStatus() != 1) continue;
            if (promotion.getStartDate() == null || promotion.getEndDate() == null) continue;
            if (now.before(promotion.getStartDate()) || now.after(promotion.getEndDate())) continue;

            // 查询活动下的商品
            SmsFlashPromotionProductRelation condition = new SmsFlashPromotionProductRelation();
            condition.setFlashPromotionId(promotion.getId());
            List<SmsFlashPromotionProductRelation> relations = productRelationMapper.selectByCondition(condition);

            for (SmsFlashPromotionProductRelation relation : relations) {
                ProductDTO product = productClient.getById(relation.getProductId()).getData();
                SeckillProductDetailDTO dto = new SeckillProductDetailDTO();
                dto.setPromotionId(promotion.getId());
                dto.setPromotionTitle(promotion.getTitle());
                dto.setProductId(relation.getProductId());
                dto.setProductName(product != null ? product.getName() : null);
                dto.setOriginalPrice(product != null ? product.getPrice() : BigDecimal.ZERO);
                dto.setSeckillPrice(relation.getFlashPromotionPrice());
                dto.setSeckillStock(relation.getFlashPromotionCount());
                dto.setLimitPerUser(relation.getFlashPromotionLimit());
                dto.setStartTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(promotion.getStartDate()));
                dto.setEndTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(promotion.getEndDate()));
                dto.setStatus(1); // 进行中
                result.add(dto);
            }
        }
        return result;
    }

    @Override
    public SeckillProductDetailDTO getSeckillProductDetail(Long promotionId, Long productId) {
        SmsFlashPromotion promotion = flashPromotionMapper.selectByPrimaryKey(promotionId);
        if (promotion == null) return null;

        SmsFlashPromotionProductRelation condition = new SmsFlashPromotionProductRelation();
        condition.setFlashPromotionId(promotionId);
        condition.setProductId(productId);
        List<SmsFlashPromotionProductRelation> relations = productRelationMapper.selectByCondition(condition);
        if (relations.isEmpty()) return null;

        SmsFlashPromotionProductRelation relation = relations.get(0);
        ProductDTO product = productClient.getById(productId).getData();

        SeckillProductDetailDTO dto = new SeckillProductDetailDTO();
        dto.setPromotionId(promotionId);
        dto.setPromotionTitle(promotion.getTitle());
        dto.setProductId(productId);
        dto.setProductName(product != null ? product.getName() : null);
        dto.setProductPic(product != null ? product.getPic() : null);
        dto.setOriginalPrice(product != null ? product.getPrice() : BigDecimal.ZERO);
        dto.setSeckillPrice(relation.getFlashPromotionPrice());
        dto.setSeckillStock(relation.getFlashPromotionCount());
        dto.setLimitPerUser(relation.getFlashPromotionLimit());
        dto.setStartTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(promotion.getStartDate()));
        dto.setEndTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(promotion.getEndDate()));
        dto.setStatus(promotion.getStatus());
        return dto;
    }

    @Override
    public void autoStartStopPromotions() {
        Date now = new Date();
        List<SmsFlashPromotion> promotions = flashPromotionMapper.selectByCondition(null);

        for (SmsFlashPromotion promotion : promotions) {
            if (promotion.getStartDate() == null || promotion.getEndDate() == null) continue;

            // 到了开始时间且未启用，自动上线
            if (now.after(promotion.getStartDate()) && now.before(promotion.getEndDate())
                    && (promotion.getStatus() == null || promotion.getStatus() != 1)) {
                promotion.setStatus(1);
                flashPromotionMapper.updateByPrimaryKeySelective(promotion);
                LOGGER.info("秒杀活动已自动上线, promotionId={}, title={}", promotion.getId(), promotion.getTitle());
                // 自动预热库存
                warmUpStock(promotion.getId());
            }

            // 到了结束时间且已启用，自动下线
            if (now.after(promotion.getEndDate()) && promotion.getStatus() != 0) {
                promotion.setStatus(0);
                flashPromotionMapper.updateByPrimaryKeySelective(promotion);
                LOGGER.info("秒杀活动已自动下线, promotionId={}, title={}", promotion.getId(), promotion.getTitle());
            }
        }
    }

    private String getProductName(Long productId) {
        CommonResult<ProductDTO> result = productClient.getById(productId);
        ProductDTO product = result != null ? result.getData() : null;
        return product != null ? product.getName() : null;
    }

    private static DefaultRedisScript<Long> createSeckillScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/seckill.lua"));
        script.setResultType(Long.class);
        return script;
    }
}
