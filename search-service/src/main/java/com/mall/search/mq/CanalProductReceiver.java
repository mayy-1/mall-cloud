package com.mall.search.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.search.config.CanalRabbitMQConfig;
import com.mall.search.domain.EsProduct;
import com.mall.search.service.IEsProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Canal 商品变更消息消费者
 * <p>
 * 监听 Canal 通过 RabbitMQ 投递的 binlog 变更（flatMessage JSON），把商品变更同步到 ES：
 * - pms_product 变更 → 重新查 MySQL 组装完整 ES 文档 upsert；DELETE 或已下架/逻辑删除 → 删除 ES 文档
 */
@Component
@RequiredArgsConstructor
public class CanalProductReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(CanalProductReceiver.class);

    private final IEsProductService esProductService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = CanalRabbitMQConfig.CANAL_ES_SYNC_QUEUE)
    public void onCanalMessage(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root == null || root.path("isDdl").asBoolean(false)) {
                return;
            }
            String database = root.path("database").asText("");
            String table = root.path("table").asText("");
            String type = root.path("type").asText("");
            // 只处理商品库的 binlog
            if (!"mall_product".equals(database)) {
                return;
            }
            JsonNode data = root.path("data");
            if (!data.isArray() || data.size() == 0) {
                return;
            }
            JsonNode row = data.get(0);
            handleRow(table, type, row);
        } catch (Exception e) {
            // 单条失败不影响后续消息；可在此接入死信/重试
            LOGGER.error("处理 Canal 商品变更消息失败, msg={}", json, e);
        }
    }

    private void handleRow(String table, String type, JsonNode row) {
        if ("pms_product".equals(table)) {
            handleProduct(type, row);
        }
    }

    /** 商品主表变更 */
    private void handleProduct(String type, JsonNode row) {
        Long productId = Long.valueOf(row.path("id").asText());
        if ("DELETE".equals(type)) {
            esProductService.delete(productId);
            LOGGER.info("Canal DELETE pms_product → 删除ES文档, productId={}", productId);
            return;
        }
        // INSERT / UPDATE：重新查 MySQL 组装完整文档 upsert
        EsProduct esProduct = esProductService.create(productId);
        if (esProduct == null) {
            // 商品已逻辑删除(delete_status=1)或下架(publish_status=0) → ES 同步删除
            esProductService.delete(productId);
            LOGGER.info("Canal {} pms_product → 商品不可搜索, 删除ES文档, productId={}", type, productId);
        } else {
            LOGGER.info("Canal {} pms_product → 同步ES文档, productId={}", type, productId);
        }
    }
}
