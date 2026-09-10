package com.mall.marketing.service.impl;

import com.mall.api.dto.SeckillProductDetailDTO;
import com.mall.api.dto.SeckillHoldInfo;
import com.mall.api.dto.SmsFlashPromotion;
import com.mall.marketing.domain.QueueEnum;
import com.mall.marketing.domain.dto.SeckillHoldMessage;
import com.mall.marketing.domain.dto.SeckillOrderParam;
import com.mall.marketing.mapper.SmsFlashPromotionMapper;
import com.mall.marketing.mapper.SmsFlashPromotionProductRelationMapper;
import com.mall.marketing.model.SmsFlashPromotionProductRelation;
import com.mall.marketing.service.SeckillService;
import com.mall.api.client.product.ProductClient;
import com.mall.api.client.product.SkuStockClient;
import com.mall.api.dto.ProductDTO;
import com.mall.api.dto.SkuStockDTO;
import com.mym.mall.common.api.CommonResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 秒杀服务实现
 * 核心流程：Lua 脚本原子扣减 + 写占坑标记 → 发占坑超时延时消息 → 超时未确认则回滚
 */
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillServiceImpl.class);

    private static final String SECKILL_STOCK_KEY = "seckill:stock:%d:%d";
    private static final String SECKILL_USERS_KEY = "seckill:users:%d:%d";
    /** 占坑标记 key：抢到但未确认支付的占位 */
    private static final String SECKILL_HOLD_KEY = "seckill:hold:%d:%d:%d";

    /** 占坑时长（秒）：抢到后 5 分钟内未确认支付则回滚库存 */
    private static final long SECKILL_HOLD_TTL_SECONDS = 5 * 60L;

    /** 秒杀 key 过期缓冲（秒）：活动结束后额外保留 1 天，便于对账与查询 */
    private static final long SECKILL_KEY_TTL_BUFFER = 24 * 60 * 60L;
    /** 秒杀 key 兜底过期时间（秒）：活动未配置结束时间或已结束时给 1 天 */
    private static final long SECKILL_KEY_TTL_FALLBACK = 24 * 60 * 60L;

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
    /** SKU Feign（用于秒杀详情查规格 spData） */
    private final SkuStockClient skuStockClient;

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
            return 0;
        }
        // 2. 校验秒杀商品关联
        SmsFlashPromotionProductRelation condition = new SmsFlashPromotionProductRelation();
        condition.setFlashPromotionId(promotionId);
        condition.setProductId(productId);
        List<SmsFlashPromotionProductRelation> relations = productRelationMapper.selectByCondition(condition);
        if (relations.isEmpty()) {
            LOGGER.warn("秒杀商品关联不存在, promotionId={}, productId={}", promotionId, productId);
            return 0;
        }
        SmsFlashPromotionProductRelation relation = relations.get(0);

        // 3. 构造 Redis Key
        String stockKey = String.format(SECKILL_STOCK_KEY, promotionId, productId);
        String usersKey = String.format(SECKILL_USERS_KEY, promotionId, productId);
        String holdKey = String.format(SECKILL_HOLD_KEY, promotionId, productId, memberId);

        // 4. 执行 Lua 原子脚本：判断用户是否已购买 + 判断库存 + 扣减
        Long result = redisTemplate.execute(
                seckillScript,
                List.of(stockKey, usersKey),
                memberId.toString(),
                String.valueOf(relation.getFlashPromotionLimit())
        );
        int seckillResult = result != null ? result.intValue() : SECKILL_STOCK_EMPTY;

        if (seckillResult == SECKILL_SUCCESS) {
            // 4.1 给已购买用户 Set 设置过期时间（活动结束后 + 1 天，避免长期堆积）
            redisTemplate.expire(usersKey, calcSeckillKeyTtl(promotion), TimeUnit.SECONDS);

            // 5. 写占坑标记（含确认建单所需信息，5min 内未确认则回滚）
            SeckillHoldInfo holdInfo = new SeckillHoldInfo();
            holdInfo.setSkuId(relation.getSkuId());
            holdInfo.setSeckillPrice(relation.getFlashPromotionPrice());
            holdInfo.setQuantity(1);
            holdInfo.setProductName(getProductName(productId));
            redisTemplate.opsForValue().set(holdKey, holdInfo, SECKILL_HOLD_TTL_SECONDS, TimeUnit.SECONDS);

            // 6. 发送「占坑超时检查」延时消息（5min），超时未确认支付则回滚库存
            SeckillHoldMessage holdMessage = SeckillHoldMessage.builder()
                    .promotionId(promotionId)
                    .productId(productId)
                    .memberId(memberId)
                    .quantity(1)
                    .build();

            rabbitTemplate.convertAndSend(
                    QueueEnum.QUEUE_TTL_SECKILL_HOLD.getExchange(),
                    QueueEnum.QUEUE_TTL_SECKILL_HOLD.getRouteKey(),
                    holdMessage,
                    message -> {
                        message.getMessageProperties().setExpiration(
                                String.valueOf(SECKILL_HOLD_TTL_SECONDS * 1000));
                        return message;
                    }
            );
            LOGGER.info("秒杀占坑成功, memberId={}, productId={}", memberId, productId);
        } else if (seckillResult == SECKILL_STOCK_EMPTY) {
            LOGGER.info("秒杀失败，库存不足, memberId={}, productId={}", memberId, productId);
        } else if (seckillResult == SECKILL_REPEAT) {
            LOGGER.info("秒杀失败，用户重复购买, memberId={}, productId={}", memberId, productId);
        }
        return seckillResult;
    }

    @Override
    public void preWarmNearbyPromotions(long leadMs) {
        Date now = new Date();
        List<SmsFlashPromotion> promotions = flashPromotionMapper.selectByCondition(null);

        for (SmsFlashPromotion promotion : promotions) {
            if (promotion.getStartDate() == null || promotion.getEndDate() == null) continue;
            long timeUntilStart = promotion.getStartDate().getTime() - now.getTime();

            // 距离开始还有 leadMs 以内，且还未开始，且已启用 → 预热
            if (timeUntilStart > 0 && timeUntilStart <= leadMs
                    && promotion.getStatus() != null && promotion.getStatus() == 1) {
                String preWarmedKey = String.format("seckill:prewarmed:%d", promotion.getId());
                if (!redisTemplate.hasKey(preWarmedKey)) {
                    warmUpStock(promotion.getId());
                    // 标记已预热，避免重复执行
                    redisTemplate.opsForValue().set(preWarmedKey, "1");
                    LOGGER.info("秒杀活动库存已预热(距开始{}分钟), promotionId={}, title={}",
                            timeUntilStart / 60000, promotion.getId(), promotion.getTitle());
                }
            }
        }
    }

    @Override
    public void warmUpStock(Long promotionId) {
        SmsFlashPromotion promotion = flashPromotionMapper.selectByPrimaryKey(promotionId);
        long ttl = calcSeckillKeyTtl(promotion);

        SmsFlashPromotionProductRelation condition = new SmsFlashPromotionProductRelation();
        condition.setFlashPromotionId(promotionId);
        List<SmsFlashPromotionProductRelation> relations = productRelationMapper.selectByCondition(condition);

        for (SmsFlashPromotionProductRelation relation : relations) {
            String stockKey = String.format(SECKILL_STOCK_KEY, promotionId, relation.getProductId());
            String usersKey = String.format(SECKILL_USERS_KEY, promotionId, relation.getProductId());

            // 仅在 key 不存在时设置（避免覆盖已运行的秒杀数据），并带上过期时间
            Boolean stockExists = redisTemplate.hasKey(stockKey);
            if (Boolean.FALSE.equals(stockExists)) {
                redisTemplate.opsForValue().set(stockKey, relation.getFlashPromotionCount(), ttl, TimeUnit.SECONDS);
                LOGGER.info("预热库存, promotionId={}, productId={}, stock={}, ttl={}s",
                        promotionId, relation.getProductId(), relation.getFlashPromotionCount(), ttl);
            }

            // 初始化已购买用户 Set（如果不存在，设置过期时间为活动结束后 1 天）
            Boolean usersExists = redisTemplate.hasKey(usersKey);
            if (Boolean.FALSE.equals(usersExists)) {
                // 空 Set 无法在 Redis 中单独存在；首次购买时由 Lua 脚本 sadd 创建并设置过期时间
                LOGGER.info("初始化用户记录 key, promotionId={}, productId={}", promotionId, relation.getProductId());
            }
        }
    }

    @Override
    public void reconcileStock(Long promotionId) {
        SmsFlashPromotion promotion = flashPromotionMapper.selectByPrimaryKey(promotionId);
        long ttl = calcSeckillKeyTtl(promotion);

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
                // 以 DB 为准修正 Redis（DB 已通过 MQ 消费者扣减过），并重新带上过期时间
                redisTemplate.opsForValue().set(stockKey, dbStock, ttl, TimeUnit.SECONDS);
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
        List<Map<String, Object>> rows = productRelationMapper.selectCurrentSeckillProducts();
        if (rows.isEmpty()) return List.of();

        List<Long> productIds = rows.stream()
                .map(r -> ((Number) r.get("productId")).longValue())
                .distinct().collect(Collectors.toList());
        Map<Long, ProductDTO> productMap = productClient.getByIds(productIds).getData()
                .stream().collect(Collectors.toMap(ProductDTO::getId, p -> p));

        List<SeckillProductDetailDTO> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Long pid = ((Number) row.get("productId")).longValue();
            ProductDTO product = productMap.get(pid);
            SeckillProductDetailDTO dto = new SeckillProductDetailDTO();
            dto.setPromotionId(((Number) row.get("promotionId")).longValue());
            dto.setPromotionTitle((String) row.get("promotionTitle"));
            dto.setProductId(pid);
            dto.setProductName(product != null ? product.getName() : null);
            dto.setProductPic(product != null ? product.getPic() : null);
            dto.setOriginalPrice(product != null ? product.getPrice() : BigDecimal.ZERO);
            dto.setSeckillPrice(new BigDecimal(row.get("seckillPrice").toString()));
            dto.setSeckillStock(((Number) row.get("seckillStock")).intValue());
            dto.setLimitPerUser(row.get("limitPerUser") != null ? ((Number) row.get("limitPerUser")).intValue() : 1);
            dto.setStartTime((String) row.get("startTime"));
            dto.setEndTime((String) row.get("endTime"));
            dto.setStatus(1);
            result.add(dto);
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
        // 场次ID + 秒杀SKU（单SKU秒杀，规格固定）
        dto.setSessionId(relation.getFlashPromotionSessionId());
        dto.setSkuId(relation.getSkuId());
        if (relation.getSkuId() != null) {
            try {
                CommonResult<SkuStockDTO> skuRes = skuStockClient.getSkuStockBySkuId(relation.getSkuId());
                if (skuRes != null && skuRes.getData() != null) {
                    dto.setSkuSpData(skuRes.getData().getSpData());
                }
            } catch (Exception e) {
                // SKU 查不到不影响主流程
            }
        }
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

    /**
     * 计算秒杀相关 key 的过期时间（秒）：
     * 活动结束后再保留 1 天缓冲，便于对账与查询；
     * 活动未配置结束时间或已结束时给 1 天兜底，避免 key 永久堆积。
     */
    private long calcSeckillKeyTtl(SmsFlashPromotion promotion) {
        if (promotion == null || promotion.getEndDate() == null) {
            return SECKILL_KEY_TTL_FALLBACK;
        }
        long ttl = (promotion.getEndDate().getTime() - System.currentTimeMillis()) / 1000
                + SECKILL_KEY_TTL_BUFFER;
        return Math.max(ttl, SECKILL_KEY_TTL_FALLBACK);
    }

    private static DefaultRedisScript<Long> createSeckillScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/seckill.lua"));
        script.setResultType(Long.class);
        return script;
    }
}
