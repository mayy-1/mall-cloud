-- ============================================================
-- Database: mall_order -- order-service + trade-service database
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for oms_company_address
-- ----------------------------
DROP TABLE IF EXISTS `oms_company_address`;
CREATE TABLE `oms_company_address`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '地址名称',
  `send_status` int(1) NULL DEFAULT NULL COMMENT '默认发货地址：0->否；1->是',
  `receive_status` int(1) NULL DEFAULT NULL COMMENT '是否默认收货地址：0->否；1->是',
  `name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '收发货人姓名',
  `phone` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '收货人电话',
  `province` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '省/直辖市',
  `city` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '市',
  `region` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '区',
  `detail_address` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '详细地址',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '公司收发货地址表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_company_address
-- ----------------------------
INSERT INTO `oms_company_address` VALUES (1, '深圳发货点', 1, 1, '大梨', '18000000000', '广东省', '深圳市', '南山区', '科兴科学园');
INSERT INTO `oms_company_address` VALUES (2, '北京发货点', 0, 0, '大梨', '18000000000', '北京市', NULL, '南山区', '科兴科学园');
INSERT INTO `oms_company_address` VALUES (3, '南京发货点', 0, 0, '大梨', '18000000000', '江苏省', '南京市', '南山区', '科兴科学园');

-- ----------------------------
-- Table structure for oms_order
-- ----------------------------
DROP TABLE IF EXISTS `oms_order`;
CREATE TABLE `oms_order`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单id',
  `member_id` bigint(20) NOT NULL,
  `coupon_id` bigint(20) NULL DEFAULT NULL,
  `order_sn` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '订单编号',
  `create_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `member_username` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户帐号',
  `total_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '订单总金额',
  `pay_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '应付金额（实际支付金额）',
  `freight_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '运费金额',
  `promotion_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '促销优化金额（促销价、满减、阶梯价）',
  `integration_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '积分抵扣金额',
  `coupon_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '优惠券抵扣金额',
  `discount_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '管理员后台调整订单使用的折扣金额',
  `pay_type` int(1) NULL DEFAULT NULL COMMENT '支付方式：0->未支付；1->支付宝；2->微信',
  `source_type` int(1) NULL DEFAULT NULL COMMENT '订单来源：0->PC订单；1->app订单',
  `status` int(1) NULL DEFAULT NULL COMMENT '订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单',
  `order_type` int(1) NULL DEFAULT NULL COMMENT '订单类型：0->正常订单；1->秒杀订单',
  `delivery_company` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '物流公司(配送方式)',
  `delivery_sn` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '物流单号',
  `auto_confirm_day` int(11) NULL DEFAULT NULL COMMENT '自动确认时间（天）',
  `integration` int(11) NULL DEFAULT NULL COMMENT '可以获得的积分',
  `growth` int(11) NULL DEFAULT NULL COMMENT '可以活动的成长值',
  `promotion_info` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '活动信息',
  `bill_type` int(1) NULL DEFAULT NULL COMMENT '发票类型：0->不开发票；1->电子发票；2->纸质发票',
  `bill_header` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '发票抬头',
  `bill_content` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '发票内容',
  `bill_receiver_phone` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '收票人电话',
  `bill_receiver_email` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '收票人邮箱',
  `receiver_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '收货人电话',
  `receiver_post_code` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '收货人邮编',
  `receiver_province` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '省份/直辖市',
  `receiver_city` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '城市',
  `receiver_region` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '区',
  `receiver_detail_address` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '详细地址',
  `note` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '订单备注',
  `confirm_status` int(1) NULL DEFAULT NULL COMMENT '确认收货状态：0->未确认；1->已确认',
  `delete_status` int(1) NOT NULL DEFAULT 0 COMMENT '删除状态：0->未删除；1->已删除',
  `use_integration` int(11) NULL DEFAULT NULL COMMENT '下单时使用的积分',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `delivery_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime NULL DEFAULT NULL COMMENT '确认收货时间',
  `comment_time` datetime NULL DEFAULT NULL COMMENT '评价时间',
  `modify_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 77 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_order
-- ----------------------------
INSERT INTO `oms_order` VALUES (12, 1, 2, '201809150101000001', '2018-09-15 12:24:27', 'test', 18732.00, 16377.75, 20.00, 2344.25, 0.00, 10.00, 10.00, 0, 1, 4, 0, '', '', 15, 13284, 13284, '单品促销,打折优惠：满3件，打7.50折,满减优惠：满1000.00元，减120.00元,满减优惠：满1000.00元，减120.00元,无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '江苏省', '常州市', '天宁区', '东晓街道', '111', 0, 0, NULL, NULL, NULL, NULL, NULL, '2019-11-09 16:50:28');
INSERT INTO `oms_order` VALUES (13, 1, 2, '201809150102000002', '2018-09-15 14:24:29', 'test', 18732.00, 16377.75, 0.00, 2344.25, 0.00, 10.00, 0.00, 1, 1, 1, 0, '', '', 15, 13284, 13284, '单品促销,打折优惠：满3件，打7.50折,满减优惠：满1000.00元，减120.00元,满减优惠：满1000.00元，减120.00元,无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, 1000, '2018-10-11 14:04:19', NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (14, 1, 2, '201809130101000001', '2018-09-13 16:57:40', 'test', 18732.00, 16377.75, 0.00, 2344.25, 0.00, 10.00, 0.00, 2, 1, 3, 0, '顺丰快递', '201707196398345', 15, 13284, 13284, '单品促销,打折优惠：满3件，打7.50折,满减优惠：满1000.00元，减120.00元,满减优惠：满1000.00元，减120.00元,无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 1, 0, NULL, '2018-10-13 13:44:04', '2018-10-16 13:43:41', '2022-11-11 16:19:34', NULL, NULL);
INSERT INTO `oms_order` VALUES (15, 1, 2, '201809130102000002', '2018-09-13 17:03:00', 'test', 18732.00, 16377.75, 0.00, 2344.25, 0.00, 10.00, 0.00, 1, 1, 3, 0, '顺丰快递', '201707196398346', 15, 13284, 13284, '单品促销,打折优惠：满3件，打7.50折,满减优惠：满1000.00元，减120.00元,满减优惠：满1000.00元，减120.00元,无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 1, 1, NULL, '2018-10-13 13:44:54', '2018-10-16 13:45:01', '2018-10-18 14:05:31', NULL, NULL);
INSERT INTO `oms_order` VALUES (16, 1, 2, '201809140101000001', '2018-09-14 16:16:16', 'test', 18732.00, 16377.75, 0.00, 2344.25, 0.00, 10.00, 0.00, 2, 1, 4, 0, NULL, NULL, 15, 13284, 13284, '单品促销,打折优惠：满3件，打7.50折,满减优惠：满1000.00元，减120.00元,满减优惠：满1000.00元，减120.00元,无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (17, 1, 2, '201809150101000003', '2018-09-15 12:24:27', 'test', 18732.00, 16377.75, 0.00, 2344.25, 0.00, 10.00, 0.00, 0, 1, 4, 0, '顺丰快递', '201707196398345', 15, NULL, NULL, '单品促销,打折优惠：满3件，打7.50折,满减优惠：满1000.00元，减120.00元,满减优惠：满1000.00元，减120.00元,无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 1, NULL, NULL, '2018-10-12 14:01:28', NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (18, 1, 2, '201809150102000004', '2018-09-15 14:24:29', 'test', 18732.00, 16377.75, 0.00, 2344.25, 0.00, 10.00, 0.00, 1, 1, 1, 0, '圆通快递', 'xx', 15, NULL, NULL, '单品促销,打折优惠：满3件，打7.50折,满减优惠：满1000.00元，减120.00元,满减优惠：满1000.00元，减120.00元,无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 1, 1000, NULL, '2018-10-16 14:42:17', NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (19, 1, 2, '201809130101000003', '2018-09-13 16:57:40', 'test', 18732.00, 16377.75, 0.00, 2344.25, 0.00, 10.00, 0.00, 2, 1, 2, 0, NULL, NULL, 15, NULL, NULL, '单品促销,打折优惠：满3件，打7.50折,满减优惠：满1000.00元，减120.00元,满减优惠：满1000.00元，减120.00元,无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (20, 1, 2, '201809130102000004', '2018-09-13 17:03:00', 'test', 18732.00, 16377.75, 0.00, 2344.25, 0.00, 10.00, 0.00, 1, 1, 3, 0, NULL, NULL, 15, NULL, NULL, '单品促销,打折优惠：满3件，打7.50折,满减优惠：满1000.00元，减120.00元,满减优惠：满1000.00元，减120.00元,无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (21, 1, 2, '201809140101000002', '2018-09-14 16:16:16', 'test', 18732.00, 16377.75, 0.00, 2344.25, 0.00, 10.00, 0.00, 2, 1, 4, 0, NULL, NULL, 15, 18682, 18682, '单品促销,打折优惠：满3件，打7.50折,满减优惠：满1000.00元，减120.00元,满减优惠：满1000.00元，减120.00元,无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (22, 1, 2, '201809150101000005', '2018-09-15 12:24:27', 'test', 18732.00, 16377.75, 0.00, 2344.25, 0.00, 10.00, 0.00, 0, 1, 4, 0, '顺丰快递', '201707196398345', 15, 0, 0, '单品促销,打折优惠：满3件，打7.50折,满减优惠：满1000.00元，减120.00元,满减优惠：满1000.00元，减120.00元,无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 1, NULL, NULL, '2018-10-12 14:01:28', NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (23, 1, 2, '201809150102000006', '2018-09-15 14:24:29', 'test', 18732.00, 16377.75, 0.00, 2344.25, 0.00, 10.00, 0.00, 1, 1, 1, 0, '顺丰快递', 'xxx', 15, 0, 0, '单品促销,打折优惠：满3件，打7.50折,满减优惠：满1000.00元，减120.00元,满减优惠：满1000.00元，减120.00元,无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 1, 1000, NULL, '2018-10-16 14:41:28', NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (24, 1, 2, '201809130101000005', '2018-09-13 16:57:40', 'test', 18732.00, 16377.75, 0.00, 2344.25, 0.00, 10.00, 0.00, 2, 1, 2, 0, NULL, NULL, 15, 18682, 18682, '单品促销,打折优惠：满3件，打7.50折,满减优惠：满1000.00元，减120.00元,满减优惠：满1000.00元，减120.00元,无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (25, 1, 2, '201809130102000006', '2018-09-13 17:03:00', 'test', 18732.00, 16377.75, 10.00, 2344.25, 0.00, 10.00, 5.00, 1, 1, 4, 0, NULL, NULL, 15, 18682, 18682, '单品促销,打折优惠：满3件，打7.50折,满减优惠：满1000.00元，减120.00元,满减优惠：满1000.00元，减120.00元,无优惠', NULL, NULL, NULL, NULL, NULL, '大梨22', '18033441849', '518000', '北京市', '北京城区', '东城区', '东城街道', 'xxx', 0, 1, NULL, NULL, NULL, NULL, NULL, '2018-10-30 15:08:31');
INSERT INTO `oms_order` VALUES (26, 1, 2, '201809140101000003', '2018-09-14 16:16:16', 'test', 18732.00, 16377.75, 0.00, 2344.25, 0.00, 10.00, 0.00, 2, 1, 4, 0, NULL, NULL, 15, 18682, 18682, '单品促销,打折优惠：满3件，打7.50折,满减优惠：满1000.00元，减120.00元,满减优惠：满1000.00元，减120.00元,无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (27, 1, NULL, '202002250100000001', '2020-02-25 15:59:20', 'test', 540.00, 540.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0, 1, 4, 0, NULL, NULL, NULL, 0, 0, '无优惠,无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '南山区', '科兴科学园', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (28, 1, NULL, '202002250100000002', '2020-02-25 16:05:47', 'test', 540.00, 540.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0, 1, 4, 0, NULL, NULL, NULL, 0, 0, '无优惠,无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '南山区', '科兴科学园', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (29, 1, NULL, '202002250100000003', '2020-02-25 16:07:58', 'test', 540.00, 540.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0, 1, 4, 0, NULL, NULL, NULL, 0, 0, '无优惠,无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '南山区', '科兴科学园', NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (30, 1, NULL, '202002250100000004', '2020-02-25 16:50:13', 'test', 240.00, 240.00, 20.00, 0.00, 0.00, 0.00, 10.00, 0, 1, 3, 0, '顺丰快递', '12333333', NULL, 0, 0, '无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '南山区', '科兴科学园', NULL, 1, 0, NULL, '2020-02-25 16:53:29', '2020-02-25 16:54:03', '2020-05-17 19:38:15', NULL, '2020-02-25 16:52:51');
INSERT INTO `oms_order` VALUES (31, 1, 26, '202005160100000001', '2020-05-16 15:16:54', 'test', 13623.00, 11842.40, 0.00, 1629.60, 1.00, 150.00, 0.00, 0, 1, 4, 0, NULL, NULL, 15, 13623, 13623, '满减优惠：满5000.00元，减500.00元;打折优惠：满2件，打8.00折;满减优惠：满500.00元，减50.00元', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (32, 1, NULL, '202005170100000001', '2020-05-17 15:00:38', 'test', 6487.00, 6187.00, 0.00, 300.00, 0.00, 0.00, 0.00, 1, 1, 1, 0, NULL, NULL, 15, 6487, 6487, '满减优惠：满3000.00元，减300.00元;无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, '2020-05-17 15:33:28', NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (33, 1, NULL, '202005170100000002', '2020-05-17 15:14:18', 'test', 3788.00, 3488.00, 0.00, 300.00, 0.00, 0.00, 0.00, 0, 1, 4, 0, NULL, NULL, 15, 3788, 3788, '满减优惠：满3000.00元，减300.00元', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (34, 1, NULL, '202005170100000003', '2020-05-17 15:20:10', 'test', 3788.00, 3488.00, 0.00, 300.00, 0.00, 0.00, 0.00, 0, 1, 4, 0, NULL, NULL, 15, 3788, 3788, '满减优惠：满3000.00元，减300.00元', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (35, 1, NULL, '202005170100000004', '2020-05-17 15:22:03', 'test', 3788.00, 3488.00, 0.00, 300.00, 0.00, 0.00, 0.00, 2, 1, 3, 0, '顺丰快递', '123', 15, 3788, 3788, '满减优惠：满3000.00元，减300.00元', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 1, 0, NULL, '2020-05-17 15:29:07', '2020-05-17 15:30:24', '2020-05-17 15:41:45', NULL, NULL);
INSERT INTO `oms_order` VALUES (36, 1, NULL, '202005170100000005', '2020-05-17 16:59:26', 'test', 10275.00, 9775.00, 0.00, 500.00, 0.00, 0.00, 0.00, 0, 1, 4, 0, NULL, NULL, 15, 10275, 10275, '满减优惠：满5000.00元，减500.00元;无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (37, 1, NULL, '202005170100000006', '2020-05-17 19:33:48', 'test', 6487.00, 6187.00, 0.00, 300.00, 0.00, 0.00, 0.00, 1, 1, 3, 0, '顺丰快递', 'aadd', 15, 6487, 6487, '满减优惠：满3000.00元，减300.00元;无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 1, 0, NULL, '2020-05-17 19:33:59', '2020-05-17 19:34:59', '2020-05-17 19:35:50', NULL, NULL);
INSERT INTO `oms_order` VALUES (38, 1, NULL, '202005170100000007', '2020-05-17 19:39:10', 'test', 3788.00, 3488.00, 0.00, 300.00, 0.00, 0.00, 0.00, 0, 1, 4, 0, NULL, NULL, 15, 3788, 3788, '满减优惠：满3000.00元，减300.00元', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (39, 1, NULL, '202005170100000008', '2020-05-17 19:41:30', 'test', 3788.00, 3488.00, 0.00, 300.00, 0.00, 0.00, 0.00, 1, 1, 3, 0, '顺丰快递', 'sdf', 15, 3788, 3788, '满减优惠：满3000.00元，减300.00元', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 1, 1, NULL, '2020-05-17 19:41:41', '2020-05-17 19:42:07', '2020-05-17 19:42:36', NULL, NULL);
INSERT INTO `oms_order` VALUES (40, 1, NULL, '202005180100000001', '2020-05-18 16:50:03', 'test', 3788.00, 3488.00, 0.00, 300.00, 0.00, 0.00, 0.00, 2, 1, 1, 0, NULL, NULL, 15, 3788, 3788, '满减优惠：满3000.00元，减300.00元', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, '2020-05-18 16:50:29', NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (41, 1, 26, '202005180100000002', '2020-05-18 20:22:24', 'test', 6487.00, 6037.00, 0.00, 300.00, 0.00, 150.00, 0.00, 1, 1, 3, 0, '顺丰快递', '12313', 15, 6487, 6487, '满减优惠：满3000.00元，减300.00元;无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '清水河街道', NULL, 1, 0, NULL, '2020-05-18 20:22:29', '2020-05-18 20:23:03', '2020-05-18 20:23:20', NULL, NULL);
INSERT INTO `oms_order` VALUES (42, 1, NULL, '202005230100000001', '2020-05-23 16:21:27', 'test', 5398.00, 4318.40, 0.00, 1079.60, 0.00, 0.00, 0.00, 2, 1, 1, 0, NULL, NULL, 15, 5398, 5398, '打折优惠：满2件，打8.00折', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '清水河街道', NULL, 0, 0, NULL, '2020-05-23 16:21:30', NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (43, 1, NULL, '202005230100000002', '2020-05-23 17:01:33', 'test', 5398.00, 4318.40, 0.00, 1079.60, 0.00, 0.00, 0.00, 0, 1, 4, 0, NULL, NULL, 15, 5398, 5398, '打折优惠：满2件，打8.00折', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (44, 1, NULL, '202005240100000001', '2020-05-24 09:37:07', 'test', 7576.00, 7076.00, 0.00, 500.00, 0.00, 0.00, 0.00, 0, 1, 4, 0, NULL, NULL, 15, 7576, 7576, '满减优惠：满5000.00元，减500.00元', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (45, 1, 25, '202006070100000001', '2020-06-07 17:02:04', 'test', 10275.00, 9674.90, 0.00, 500.00, 0.00, 100.10, 0.00, 1, 1, 1, 0, NULL, NULL, 15, 10275, 10275, '满减优惠：满5000.00元，减500.00元;无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '清水河街道', NULL, 0, 0, NULL, '2020-06-07 17:02:17', NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (46, 1, 24, '202006210100000001', '2020-06-21 14:27:34', 'test', 9186.00, 7796.40, 0.00, 1379.60, 0.00, 10.00, 0.00, 2, 1, 1, 0, NULL, NULL, 15, 9186, 9186, '满减优惠：满3000.00元，减300.00元;打折优惠：满2件，打8.00折', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, '2020-06-21 14:27:38', NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (47, 1, NULL, '202006210100000002', '2020-06-21 15:13:06', 'test', 6487.00, 6187.00, 0.00, 300.00, 0.00, 0.00, 0.00, 1, 1, 3, 0, '顺丰快递', '123131', 15, 6487, 6487, '满减优惠：满3000.00元，减300.00元;无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '清水河街道', NULL, 1, 0, NULL, '2020-06-21 15:13:12', '2020-06-21 15:13:44', '2020-06-21 15:13:58', NULL, NULL);
INSERT INTO `oms_order` VALUES (48, 1, 26, '202006210100000003', '2020-06-21 15:15:18', 'test', 3788.00, 3338.00, 0.00, 300.00, 0.00, 150.00, 0.00, 2, 1, 3, 0, '圆通快递', '12313', 15, 3788, 3788, '满减优惠：满3000.00元，减300.00元', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 1, 0, NULL, '2020-06-21 15:15:20', '2020-06-21 15:15:48', '2020-06-21 15:15:58', NULL, NULL);
INSERT INTO `oms_order` VALUES (49, 1, NULL, '202006270100000001', '2020-06-27 10:27:56', 'test', 2699.00, 2699.00, 0.00, 0.00, 0.00, 0.00, 0.00, 2, 1, 1, 0, NULL, NULL, 15, 2699, 2699, '无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '清水河街道', NULL, 0, 0, NULL, '2020-06-27 10:27:58', NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (50, 1, NULL, '202210280100000001', '2022-10-28 14:50:58', 'test', 2699.00, 2699.00, 0.00, 0.00, 0.00, 0.00, 0.00, 2, 1, 1, 0, NULL, NULL, 15, 2699, 2699, '无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '清水河街道', NULL, 0, 0, NULL, '2022-10-28 14:51:02', NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (51, 1, NULL, '202210280100000002', '2022-10-28 15:27:41', 'test', 5999.00, 5999.00, 0.00, 0.00, 0.00, 0.00, 0.00, 2, 1, 1, 0, NULL, NULL, 15, 0, 0, '无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, '2022-10-28 15:27:44', NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (52, 1, 30, '202211090100000001', '2022-11-09 15:14:58', 'test', 2999.00, 2799.00, 0.00, 0.00, 0.00, 200.00, 0.00, 2, 1, 3, 0, '顺丰快递', '1233', 15, 0, 0, '无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 1, 0, NULL, '2022-11-09 15:15:00', '2022-11-09 15:16:12', '2022-11-09 15:16:31', NULL, NULL);
INSERT INTO `oms_order` VALUES (53, 1, 27, '202211090100000002', '2022-11-09 15:25:38', 'test', 3599.00, 3589.00, 0.00, 0.00, 0.00, 10.00, 0.00, 2, 1, 1, 0, NULL, NULL, 15, 0, 0, '无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, '2022-11-09 15:25:41', NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (54, 1, 29, '202211090100000003', '2022-11-09 15:26:11', 'test', 5999.00, 5399.00, 0.00, 0.00, 0.00, 600.00, 0.00, 2, 1, 1, 0, NULL, NULL, 15, 0, 0, '无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, '2022-11-09 15:26:13', NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (55, 1, NULL, '202211100100000001', '2022-11-10 16:57:59', 'test', 11998.00, 11998.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0, 1, 4, 0, NULL, NULL, 15, 0, 0, '无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (56, 1, 28, '202211110100000001', '2022-11-11 16:12:42', 'test', 2999.00, 2899.00, 0.00, 0.00, 0.00, 100.00, 0.00, 2, 1, 1, 0, NULL, NULL, 15, 0, 0, '无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, '2022-11-11 16:12:48', NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (57, 1, NULL, '202211110100000002', '2022-11-11 16:13:14', 'test', 2999.00, 2999.00, 0.00, 0.00, 0.00, 0.00, 0.00, 2, 1, 1, 0, NULL, NULL, 15, 0, 0, '无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, '2022-11-11 16:13:21', NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (58, 1, NULL, '202211110100000003', '2022-11-11 16:15:08', 'test', 5999.00, 5999.00, 0.00, 0.00, 0.00, 0.00, 0.00, 2, 1, 1, 0, NULL, NULL, 15, 0, 0, '无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, '2022-11-11 16:17:46', NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (59, 1, NULL, '202211110100000004', '2022-11-11 16:21:12', 'test', 649.00, 599.00, 0.00, 50.00, 0.00, 0.00, 0.00, 0, 1, 4, 0, NULL, NULL, 15, 649, 649, '满减优惠：满500.00元，减50.00元', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (60, 1, NULL, '202211160100000001', '2022-11-16 10:36:08', 'test', 11097.00, 11097.00, 0.00, 0.00, 0.00, 0.00, 0.00, 2, 1, 3, 0, '顺丰快递', '1234555', 15, 0, 0, '无优惠;无优惠;无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 1, 0, NULL, '2022-11-16 10:37:25', '2022-11-16 10:42:50', '2022-11-16 10:44:40', NULL, NULL);
INSERT INTO `oms_order` VALUES (61, 1, NULL, '202212210100000001', '2022-12-21 15:49:08', 'test', 2999.00, 2999.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0, 1, 4, 0, NULL, NULL, 15, 0, 0, '无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (62, 1, NULL, '202212210100000002', '2022-12-21 15:49:57', 'test', 8098.00, 8098.00, 0.00, 0.00, 0.00, 0.00, 0.00, 2, 1, 3, 0, '顺丰快递', 'SDFERR7845', 15, 0, 0, '无优惠;无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 1, 0, NULL, '2022-12-21 15:50:00', '2022-12-21 15:50:23', '2022-12-21 15:50:33', NULL, NULL);
INSERT INTO `oms_order` VALUES (63, 1, NULL, '202212210100000003', '2022-12-21 15:51:09', 'test', 2999.00, 2999.00, 0.00, 0.00, 0.00, 0.00, 0.00, 2, 1, 2, 0, '顺丰快递', '112', 15, 0, 0, '无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, '2022-12-21 15:51:11', '2023-01-10 10:14:12', NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (64, 1, NULL, '202212210100000004', '2022-12-21 15:51:35', 'test', 2099.00, 2099.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0, 1, 4, 0, NULL, NULL, 15, 0, 0, '无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (65, 1, 28, '202212210100000005', '2022-12-21 16:53:07', 'test', 5098.00, 4788.00, 0.00, 200.00, 10.00, 100.00, 0.00, 2, 1, 2, 0, '圆通快递', '115', 15, 0, 0, '满减优惠：满2000.00元，减200.00元;无优惠', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, '2022-12-21 16:53:58', '2023-01-10 10:14:12', NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (66, 1, NULL, '202301100100000001', '2023-01-10 15:34:59', 'test', 5998.00, 5798.00, 0.00, 200.00, 0.00, 0.00, 0.00, 0, 1, 4, 0, NULL, NULL, 15, 0, 0, '满减优惠：满2000.00元，减200.00元', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (67, 1, NULL, '202301100100000002', '2023-01-10 15:39:07', 'test', 3788.00, 3488.00, 0.00, 300.00, 0.00, 0.00, 0.00, 2, 1, 1, 0, NULL, NULL, 15, 3788, 3788, '满减优惠：满3000.00元，减300.00元', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, '2023-01-10 15:39:16', NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (68, 1, NULL, '202301100100000003', '2023-01-10 16:58:19', 'test', 3999.00, 3899.00, 0.00, 100.00, 0.00, 0.00, 0.00, 2, 1, 1, 0, NULL, NULL, 15, 3788, 3788, '单品促销', NULL, NULL, NULL, NULL, NULL, '大梨', '18033441849', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, '2023-01-10 16:58:21', NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (69, 11, 30, '202305110100000001', '2023-05-11 15:28:56', 'member', 5098.00, 4698.00, 0.00, 200.00, 0.00, 200.00, 0.00, 2, 1, 3, 0, '顺丰快递', '1231313', 15, 0, 0, '满减优惠：满2000.00元，减200.00元;无优惠', NULL, NULL, NULL, NULL, NULL, '小李', '18961511111', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 1, 0, NULL, '2023-05-11 15:28:59', '2023-05-11 15:30:08', '2023-05-11 15:30:16', NULL, NULL);
INSERT INTO `oms_order` VALUES (70, 11, NULL, '202305110100000002', '2023-05-11 15:30:36', 'member', 3599.00, 3599.00, 0.00, 0.00, 0.00, 0.00, 0.00, 2, 1, 3, 0, '顺丰快递', '232342', 15, 0, 0, '无优惠', NULL, NULL, NULL, NULL, NULL, '小李', '18961511111', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 1, 0, NULL, '2023-05-11 15:30:40', '2023-05-11 15:31:22', '2023-05-11 15:31:30', NULL, NULL);
INSERT INTO `oms_order` VALUES (71, 11, NULL, '202305110100000003', '2023-05-11 15:31:55', 'member', 5999.00, 5999.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0, 1, 4, 0, NULL, NULL, 15, 0, 0, '无优惠', NULL, NULL, NULL, NULL, NULL, '小李', '18961511111', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (72, 11, NULL, '202305110100000004', '2023-05-11 15:33:13', 'member', 5368.00, 5368.00, 0.00, 0.00, 0.00, 0.00, 0.00, 2, 1, 2, 0, '圆通快递', '1231434', 15, 0, 0, '无优惠;无优惠', NULL, NULL, NULL, NULL, NULL, '小李', '18961511111', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, '2023-05-11 15:33:21', '2023-05-11 15:33:43', NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (73, 11, NULL, '202305110100000005', '2023-05-11 15:34:39', 'member', 5999.00, 5999.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0, 1, 0, 0, NULL, NULL, 15, 0, 0, '无优惠', NULL, NULL, NULL, NULL, NULL, '小李', '18961511111', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (74, 11, NULL, '202305110100000006', '2023-05-11 15:35:05', 'member', 2999.00, 2799.00, 0.00, 200.00, 0.00, 0.00, 0.00, 2, 1, 2, 0, '顺丰快递', '123131', 15, 0, 0, '满减优惠：满2000.00元，减200.00元', NULL, NULL, NULL, NULL, NULL, '小李', '18961511111', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 0, 0, NULL, '2023-05-11 15:35:08', '2023-05-11 15:36:00', NULL, NULL, NULL);
INSERT INTO `oms_order` VALUES (75, 11, NULL, '202305110100000007', '2023-05-11 15:35:24', 'member', 2099.00, 2099.00, 0.00, 0.00, 0.00, 0.00, 0.00, 2, 1, 3, 0, '顺丰快递', '123131311', 15, 0, 0, '无优惠', NULL, NULL, NULL, NULL, NULL, '小李', '18961511111', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 1, 0, NULL, '2023-05-11 15:35:26', '2023-05-11 15:36:11', '2023-05-11 15:36:34', NULL, NULL);
INSERT INTO `oms_order` VALUES (76, 11, 28, '202305110100000008', '2023-05-11 15:37:16', 'member', 8998.00, 8698.00, 0.00, 200.00, 0.00, 100.00, 0.00, 2, 1, 3, 0, '顺丰快递', '1231313', 15, 0, 0, '无优惠;满减优惠：满2000.00元，减200.00元', NULL, NULL, NULL, NULL, NULL, '小李', '18961511111', '518000', '广东省', '深圳市', '福田区', '东晓街道', NULL, 1, 0, NULL, '2023-05-11 15:37:18', '2023-05-11 15:37:33', '2023-05-11 15:37:48', NULL, NULL);

-- ----------------------------
-- Table structure for oms_order_item
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_item`;
CREATE TABLE `oms_order_item`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NULL DEFAULT NULL COMMENT '订单id',
  `order_sn` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '订单编号',
  `product_id` bigint(20) NULL DEFAULT NULL,
  `product_pic` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `product_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `product_brand` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `product_sn` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `product_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '销售价格',
  `product_quantity` int(11) NULL DEFAULT NULL COMMENT '购买数量',
  `product_sku_id` bigint(20) NULL DEFAULT NULL COMMENT '商品sku编号',
  `product_sku_code` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商品sku条码',
  `product_category_id` bigint(20) NULL DEFAULT NULL COMMENT '商品分类id',
  `promotion_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商品促销名称',
  `promotion_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '商品促销分解金额',
  `coupon_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '优惠券优惠分解金额',
  `integration_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '积分优惠分解金额',
  `real_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '该商品经过优惠后的分解金额',
  `gift_integration` int(11) NULL DEFAULT 0,
  `gift_growth` int(11) NULL DEFAULT 0,
  `product_attr` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商品销售属性:[{\"key\":\"颜色\",\"value\":\"颜色\"},{\"key\":\"容量\",\"value\":\"4G\"}]',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 115 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '订单中所包含的商品' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_order_item
-- ----------------------------
INSERT INTO `oms_order_item` VALUES (21, 12, '201809150101000001', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20', '华为', '6946605', 3788.00, 1, 90, '201806070026001', 19, '单品促销', 200.00, 2.02, 0.00, 3585.98, 3788, 3788, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (22, 12, '201809150101000001', 27, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8', '小米', '7437788', 2699.00, 3, 98, '201808270027001', 19, '打折优惠：满3件，打7.50折', 674.75, 1.44, 0.00, 2022.81, 2699, 2699, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (23, 12, '201809150101000001', 28, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5a9d248cN071f4959.jpg', '红米5A', '小米', '7437789', 649.00, 1, 102, '201808270028001', 19, '满减优惠：满1000.00元，减120.00元', 57.60, 0.35, 0.00, 591.05, 649, 649, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (24, 12, '201809150101000001', 28, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5a9d248cN071f4959.jpg', '红米5A', '小米', '7437789', 699.00, 1, 103, '201808270028001', 19, '满减优惠：满1000.00元，减120.00元', 62.40, 0.37, 0.00, 636.23, 649, 649, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (25, 12, '201809150101000001', 29, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5acc5248N6a5f81cd.jpg', 'Apple iPhone 8 Plus', '苹果', '7437799', 5499.00, 1, 106, '201808270029001', 19, '无优惠', 0.00, 2.94, 0.00, 5496.06, 5499, 5499, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (26, 13, '201809150102000002', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20', '华为', '6946605', 3788.00, 1, 90, '201806070026001', 19, '单品促销', 200.00, 2.02, 0.00, 3585.98, 3788, 3788, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (27, 13, '201809150102000002', 27, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8', '小米', '7437788', 2699.00, 3, 98, '201808270027001', 19, '打折优惠：满3件，打7.50折', 674.75, 1.44, 0.00, 2022.81, 2699, 2699, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (28, 13, '201809150102000002', 28, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5a9d248cN071f4959.jpg', '红米5A', '小米', '7437789', 649.00, 1, 102, '201808270028001', 19, '满减优惠：满1000.00元，减120.00元', 57.60, 0.35, 0.00, 591.05, 649, 649, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (29, 13, '201809150102000002', 28, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5a9d248cN071f4959.jpg', '红米5A', '小米', '7437789', 699.00, 1, 103, '201808270028001', 19, '满减优惠：满1000.00元，减120.00元', 62.40, 0.37, 0.00, 636.23, 649, 649, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (30, 13, '201809150102000002', 29, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5acc5248N6a5f81cd.jpg', 'Apple iPhone 8 Plus', '苹果', '7437799', 5499.00, 1, 106, '201808270029001', 19, '无优惠', 0.00, 2.94, 0.00, 5496.06, 5499, 5499, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (31, 14, '201809130101000001', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20', '华为', '6946605', 3788.00, 1, 90, '201806070026001', 19, '单品促销', 200.00, 2.02, 0.00, 3585.98, 3788, 3788, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (32, 14, '201809130101000001', 27, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8', '小米', '7437788', 2699.00, 3, 98, '201808270027001', 19, '打折优惠：满3件，打7.50折', 674.75, 1.44, 0.00, 2022.81, 2699, 2699, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (33, 14, '201809130101000001', 28, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5a9d248cN071f4959.jpg', '红米5A', '小米', '7437789', 649.00, 1, 102, '201808270028001', 19, '满减优惠：满1000.00元，减120.00元', 57.60, 0.35, 0.00, 591.05, 649, 649, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (34, 14, '201809130101000001', 28, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5a9d248cN071f4959.jpg', '红米5A', '小米', '7437789', 699.00, 1, 103, '201808270028001', 19, '满减优惠：满1000.00元，减120.00元', 62.40, 0.37, 0.00, 636.23, 649, 649, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (35, 14, '201809130101000001', 29, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5acc5248N6a5f81cd.jpg', 'Apple iPhone 8 Plus', '苹果', '7437799', 5499.00, 1, 106, '201808270029001', 19, '无优惠', 0.00, 2.94, 0.00, 5496.06, 5499, 5499, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (36, 15, '201809130101000001', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20', '华为', '6946605', 3788.00, 1, 90, '201806070026001', 19, '单品促销', 200.00, 2.02, 0.00, 3585.98, 3788, 3788, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (37, 15, '201809130101000001', 27, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8', '小米', '7437788', 2699.00, 3, 98, '201808270027001', 19, '打折优惠：满3件，打7.50折', 674.75, 1.44, 0.00, 2022.81, 2699, 2699, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (38, 15, '201809130101000001', 28, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5a9d248cN071f4959.jpg', '红米5A', '小米', '7437789', 649.00, 1, 102, '201808270028001', 19, '满减优惠：满1000.00元，减120.00元', 57.60, 0.35, 0.00, 591.05, 649, 649, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (39, 15, '201809130101000001', 28, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5a9d248cN071f4959.jpg', '红米5A', '小米', '7437789', 699.00, 1, 103, '201808270028001', 19, '满减优惠：满1000.00元，减120.00元', 62.40, 0.37, 0.00, 636.23, 649, 649, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (40, 15, '201809130101000001', 29, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5acc5248N6a5f81cd.jpg', 'Apple iPhone 8 Plus', '苹果', '7437799', 5499.00, 1, 106, '201808270029001', 19, '无优惠', 0.00, 2.94, 0.00, 5496.06, 5499, 5499, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (41, 16, '201809140101000001', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20', '华为', '6946605', 3788.00, 1, 90, '201806070026001', 19, '单品促销', 200.00, 2.02, 0.00, 3585.98, 3788, 3788, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (42, 16, '201809140101000001', 27, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8', '小米', '7437788', 2699.00, 3, 98, '201808270027001', 19, '打折优惠：满3件，打7.50折', 674.75, 1.44, 0.00, 2022.81, 2699, 2699, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (43, 16, '201809140101000001', 28, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5a9d248cN071f4959.jpg', '红米5A', '小米', '7437789', 649.00, 1, 102, '201808270028001', 19, '满减优惠：满1000.00元，减120.00元', 57.60, 0.35, 0.00, 591.05, 649, 649, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (44, 16, '201809140101000001', 28, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5a9d248cN071f4959.jpg', '红米5A', '小米', '7437789', 699.00, 1, 103, '201808270028001', 19, '满减优惠：满1000.00元，减120.00元', 62.40, 0.37, 0.00, 636.23, 649, 649, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (45, 16, '201809140101000001', 29, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5acc5248N6a5f81cd.jpg', 'Apple iPhone 8 Plus', '苹果', '7437799', 5499.00, 1, 106, '201808270029001', 19, '无优惠', 0.00, 2.94, 0.00, 5496.06, 5499, 5499, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (46, 27, '202002250100000001', 36, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5b19403eN9f0b3cb8.jpg', '耐克NIKE 男子 气垫 休闲鞋 AIR MAX 90 ESSENTIAL 运动鞋 AJ1285-101白色41码', 'NIKE', '6799345', 100.00, 3, 163, '202002210036001', 29, '无优惠', 0.00, 0.00, 0.00, 100.00, 0, 0, NULL);
INSERT INTO `oms_order_item` VALUES (47, 27, '202002250100000001', 36, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5b19403eN9f0b3cb8.jpg', '耐克NIKE 男子 气垫 休闲鞋 AIR MAX 90 ESSENTIAL 运动鞋 AJ1285-101白色41码', 'NIKE', '6799345', 120.00, 2, 164, '202002210036001', 29, '无优惠', 0.00, 0.00, 0.00, 120.00, 0, 0, NULL);
INSERT INTO `oms_order_item` VALUES (48, 28, '202002250100000002', 36, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5b19403eN9f0b3cb8.jpg', '耐克NIKE 男子 气垫 休闲鞋 AIR MAX 90 ESSENTIAL 运动鞋 AJ1285-101白色41码', 'NIKE', '6799345', 100.00, 3, 163, '202002210036001', 29, '无优惠', 0.00, 0.00, 0.00, 100.00, 0, 0, NULL);
INSERT INTO `oms_order_item` VALUES (49, 28, '202002250100000002', 36, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5b19403eN9f0b3cb8.jpg', '耐克NIKE 男子 气垫 休闲鞋 AIR MAX 90 ESSENTIAL 运动鞋 AJ1285-101白色41码', 'NIKE', '6799345', 120.00, 2, 164, '202002210036001', 29, '无优惠', 0.00, 0.00, 0.00, 120.00, 0, 0, NULL);
INSERT INTO `oms_order_item` VALUES (50, 29, '202002250100000003', 36, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5b19403eN9f0b3cb8.jpg', '耐克NIKE 男子 气垫 休闲鞋 AIR MAX 90 ESSENTIAL 运动鞋 AJ1285-101白色41码', 'NIKE', '6799345', 100.00, 3, 163, '202002210036001', 29, '无优惠', 0.00, 0.00, 0.00, 100.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"红色\"},{\"key\":\"尺寸\",\"value\":\"38\"},{\"key\":\"风格\",\"value\":\"秋季\"}]');
INSERT INTO `oms_order_item` VALUES (51, 29, '202002250100000003', 36, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5b19403eN9f0b3cb8.jpg', '耐克NIKE 男子 气垫 休闲鞋 AIR MAX 90 ESSENTIAL 运动鞋 AJ1285-101白色41码', 'NIKE', '6799345', 120.00, 2, 164, '202002210036001', 29, '无优惠', 0.00, 0.00, 0.00, 120.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"红色\"},{\"key\":\"尺寸\",\"value\":\"38\"},{\"key\":\"风格\",\"value\":\"夏季\"}]');
INSERT INTO `oms_order_item` VALUES (52, 30, '202002250100000004', 36, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5b19403eN9f0b3cb8.jpg', '耐克NIKE 男子 气垫 休闲鞋 AIR MAX 90 ESSENTIAL 运动鞋 AJ1285-101白色41码', 'NIKE', '6799345', 120.00, 2, 164, '202002210036001', 29, '无优惠', 0.00, 0.00, 0.00, 120.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"红色\"},{\"key\":\"尺寸\",\"value\":\"38\"},{\"key\":\"风格\",\"value\":\"夏季\"}]');
INSERT INTO `oms_order_item` VALUES (53, 31, '202005160100000001', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20 ', '华为', '6946605', 3788.00, 2, 110, '201806070026001', 19, '满减优惠：满5000.00元，减500.00元', 250.00, 75.00, 0.28, 3462.72, 0, 0, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (54, 31, '202005160100000001', 27, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8 全面屏游戏智能手机 6GB+64GB 黑色 全网通4G 双卡双待', '小米', '7437788', 2699.00, 2, 98, '201808270027001', 19, '打折优惠：满2件，打8.00折', 539.80, 0.00, 0.20, 2159.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (55, 31, '202005160100000001', 28, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5a9d248cN071f4959.jpg', '小米 红米5A 全网通版 3GB+32GB 香槟金 移动联通电信4G手机 双卡双待', '小米', '7437789', 649.00, 1, 102, '201808270028001', 19, '满减优惠：满500.00元，减50.00元', 50.00, 0.00, 0.05, 598.95, 0, 0, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (56, 32, '202005170100000001', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20 ', '华为', '6946605', 3788.00, 1, 110, '201806070026001', 19, '满减优惠：满3000.00元，减300.00元', 300.00, 0.00, 0.00, 3488.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (57, 32, '202005170100000001', 27, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8 全面屏游戏智能手机 6GB+64GB 黑色 全网通4G 双卡双待', '小米', '7437788', 2699.00, 1, 98, '201808270027001', 19, '无优惠', 0.00, 0.00, 0.00, 2699.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (58, 33, '202005170100000002', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20 ', '华为', '6946605', 3788.00, 1, 110, '201806070026001', 19, '满减优惠：满3000.00元，减300.00元', 300.00, 0.00, 0.00, 3488.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (59, 34, '202005170100000003', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20 ', '华为', '6946605', 3788.00, 1, 110, '201806070026001', 19, '满减优惠：满3000.00元，减300.00元', 300.00, 0.00, 0.00, 3488.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (60, 35, '202005170100000004', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20 ', '华为', '6946605', 3788.00, 1, 110, '201806070026001', 19, '满减优惠：满3000.00元，减300.00元', 300.00, 0.00, 0.00, 3488.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (61, 36, '202005170100000005', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20 ', '华为', '6946605', 3788.00, 2, 110, '201806070026001', 19, '满减优惠：满5000.00元，减500.00元', 250.00, 0.00, 0.00, 3538.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (62, 36, '202005170100000005', 27, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8 全面屏游戏智能手机 6GB+64GB 黑色 全网通4G 双卡双待', '小米', '7437788', 2699.00, 1, 98, '201808270027001', 19, '无优惠', 0.00, 0.00, 0.00, 2699.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (63, 37, '202005170100000006', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20 ', '华为', '6946605', 3788.00, 1, 110, '201806070026001', 19, '满减优惠：满3000.00元，减300.00元', 300.00, 0.00, 0.00, 3488.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (64, 37, '202005170100000006', 27, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8 全面屏游戏智能手机 6GB+64GB 黑色 全网通4G 双卡双待', '小米', '7437788', 2699.00, 1, 98, '201808270027001', 19, '无优惠', 0.00, 0.00, 0.00, 2699.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (65, 38, '202005170100000007', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20 ', '华为', '6946605', 3788.00, 1, 110, '201806070026001', 19, '满减优惠：满3000.00元，减300.00元', 300.00, 0.00, 0.00, 3488.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (66, 39, '202005170100000008', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20 ', '华为', '6946605', 3788.00, 1, 110, '201806070026001', 19, '满减优惠：满3000.00元，减300.00元', 300.00, 0.00, 0.00, 3488.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (67, 40, '202005180100000001', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20 ', '华为', '6946605', 3788.00, 1, 110, '201806070026001', 19, '满减优惠：满3000.00元，减300.00元', 300.00, 0.00, 0.00, 3488.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (68, 41, '202005180100000002', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20 ', '华为', '6946605', 3788.00, 1, 110, '201806070026001', 19, '满减优惠：满3000.00元，减300.00元', 300.00, 150.00, 0.00, 3338.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (69, 41, '202005180100000002', 27, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8 全面屏游戏智能手机 6GB+64GB 黑色 全网通4G 双卡双待', '小米', '7437788', 2699.00, 1, 98, '201808270027001', 19, '无优惠', 0.00, 0.00, 0.00, 2699.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (70, 42, '202005230100000001', 27, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8 全面屏游戏智能手机 6GB+64GB 黑色 全网通4G 双卡双待', '小米', '7437788', 2699.00, 2, 98, '201808270027001', 19, '打折优惠：满2件，打8.00折', 539.80, 0.00, 0.00, 2159.20, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (71, 43, '202005230100000002', 27, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8 全面屏游戏智能手机 6GB+64GB 黑色 全网通4G 双卡双待', '小米', '7437788', 2699.00, 2, 98, '201808270027001', 19, '打折优惠：满2件，打8.00折', 539.80, 0.00, 0.00, 2159.20, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (72, 44, '202005240100000001', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20 ', '华为', '6946605', 3788.00, 2, 110, '201806070026001', 19, '满减优惠：满5000.00元，减500.00元', 250.00, 0.00, 0.00, 3538.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (73, 45, '202006070100000001', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20 ', '华为', '6946605', 3788.00, 2, 110, '201806070026001', 19, '满减优惠：满5000.00元，减500.00元', 250.00, 36.90, 0.00, 3501.10, 0, 0, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (74, 45, '202006070100000001', 27, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8 全面屏游戏智能手机 6GB+64GB 黑色 全网通4G 双卡双待', '小米', '7437788', 2699.00, 1, 98, '201808270027001', 19, '无优惠', 0.00, 26.30, 0.00, 2672.70, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (75, 46, '202006210100000001', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20 ', '华为', '6946605', 3788.00, 1, 110, '201806070026001', 19, '满减优惠：满3000.00元，减300.00元', 300.00, 4.12, 0.00, 3483.88, 0, 0, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (76, 46, '202006210100000001', 27, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8 全面屏游戏智能手机 6GB+64GB 黑色 全网通4G 双卡双待', '小米', '7437788', 2699.00, 2, 98, '201808270027001', 19, '打折优惠：满2件，打8.00折', 539.80, 2.94, 0.00, 2156.26, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (77, 47, '202006210100000002', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20 ', '华为', '6946605', 3788.00, 1, 110, '201806070026001', 19, '满减优惠：满3000.00元，减300.00元', 300.00, 0.00, 0.00, 3488.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (78, 47, '202006210100000002', 27, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8 全面屏游戏智能手机 6GB+64GB 黑色 全网通4G 双卡双待', '小米', '7437788', 2699.00, 1, 98, '201808270027001', 19, '无优惠', 0.00, 0.00, 0.00, 2699.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (79, 48, '202006210100000003', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20 ', '华为', '6946605', 3788.00, 1, 110, '201806070026001', 19, '满减优惠：满3000.00元，减300.00元', 300.00, 150.00, 0.00, 3338.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (80, 49, '202006270100000001', 27, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8 全面屏游戏智能手机 6GB+64GB 黑色 全网通4G 双卡双待', '小米', '7437788', 2699.00, 1, 98, '201808270027001', 19, '无优惠', 0.00, 0.00, 0.00, 2699.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (81, 50, '202210280100000001', 27, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8 全面屏游戏智能手机 6GB+64GB 黑色 全网通4G 双卡双待', '小米', '7437788', 2699.00, 1, 98, '201808270027001', 19, '无优惠', 0.00, 0.00, 0.00, 2699.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (82, 51, '202210280100000002', 37, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221028/iphone14_001.jpg', 'Apple iPhone 14 (A2884) 128GB 紫色 支持移动联通电信5G 双卡双待手机', '苹果', '100038005189', 5999.00, 1, 201, '202210280037001', 19, '无优惠', 0.00, 0.00, 0.00, 5999.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"午夜色\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (83, 52, '202211090100000001', 40, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221104/xiaomi_12_pro_01.jpg', '小米12 Pro 天玑版 天玑9000+处理器 5000万疾速影像 2K超视感屏 120Hz高刷 67W快充 12GB+256GB 黑色 5G手机', '小米', '100027789721', 2999.00, 1, 221, '202211040040001', 19, '无优惠', 0.00, 200.00, 0.00, 2799.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (84, 53, '202211090100000002', 38, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221028/ipad_001.jpg', 'Apple iPad 10.9英寸平板电脑 2022年款（64GB WLAN版/A14芯片/1200万像素/iPadOS MPQ03CH/A ）', '苹果', '100044025833', 3599.00, 1, 213, '202210280038001', 53, '无优惠', 0.00, 10.00, 0.00, 3589.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"银色\"},{\"key\":\"容量\",\"value\":\"64G\"}]');
INSERT INTO `oms_order_item` VALUES (85, 54, '202211090100000003', 37, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221028/iphone14_001.jpg', 'Apple iPhone 14 (A2884) 128GB 支持移动联通电信5G 双卡双待手机', '苹果', '100038005189', 5999.00, 1, 201, '202210280037001', 19, '无优惠', 0.00, 600.00, 0.00, 5399.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"午夜色\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (86, 55, '202211100100000001', 37, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221028/iphone14_001.jpg', 'Apple iPhone 14 (A2884) 128GB 支持移动联通电信5G 双卡双待手机', '苹果', '100038005189', 5999.00, 2, 201, '202210280037001', 19, '无优惠', 0.00, 0.00, 0.00, 5999.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"午夜色\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (87, 56, '202211110100000001', 40, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221104/xiaomi_12_pro_01.jpg', '小米12 Pro 天玑版 天玑9000+处理器 5000万疾速影像 2K超视感屏 120Hz高刷 67W快充 12GB+256GB 黑色 5G手机', '小米', '100027789721', 2999.00, 1, 221, '202211040040001', 19, '无优惠', 0.00, 100.00, 0.00, 2899.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (88, 57, '202211110100000002', 40, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221104/xiaomi_12_pro_01.jpg', '小米12 Pro 天玑版 天玑9000+处理器 5000万疾速影像 2K超视感屏 120Hz高刷 67W快充 12GB+256GB 黑色 5G手机', '小米', '100027789721', 2999.00, 1, 221, '202211040040001', 19, '无优惠', 0.00, 0.00, 0.00, 2999.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (89, 58, '202211110100000003', 37, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221028/iphone14_001.jpg', 'Apple iPhone 14 (A2884) 128GB 支持移动联通电信5G 双卡双待手机', '苹果', '100038005189', 5999.00, 1, 201, '202210280037001', 19, '无优惠', 0.00, 0.00, 0.00, 5999.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"午夜色\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (90, 59, '202211110100000004', 28, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5a9d248cN071f4959.jpg', '小米 红米5A 全网通版 3GB+32GB 香槟金 移动联通电信4G手机 双卡双待', '小米', '7437789', 649.00, 1, 102, '201808270028001', 19, '满减优惠：满500.00元，减50.00元', 50.00, 0.00, 0.00, 599.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (91, 60, '202211160100000001', 37, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221028/iphone14_001.jpg', 'Apple iPhone 14 (A2884) 128GB 支持移动联通电信5G 双卡双待手机', '苹果', '100038005189', 5999.00, 1, 201, '202210280037001', 19, '无优惠', 0.00, 0.00, 0.00, 5999.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"午夜色\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (92, 60, '202211160100000001', 40, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221104/xiaomi_12_pro_01.jpg', '小米12 Pro 天玑版 天玑9000+处理器 5000万疾速影像 2K超视感屏 120Hz高刷 67W快充', '小米', '100027789721', 2999.00, 1, 221, '202211040040001', 19, '无优惠', 0.00, 0.00, 0.00, 2999.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (93, 60, '202211160100000001', 41, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221104/redmi_k50_01.jpg', 'Redmi K50 天玑8100 2K柔性直屏 OIS光学防抖 67W快充 5500mAh大电量', '小米', '100035246702', 2099.00, 1, 225, '202211040041001', 19, '无优惠', 0.00, 0.00, 0.00, 2099.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"墨羽\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (94, 61, '202212210100000001', 40, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221104/xiaomi_12_pro_01.jpg', '小米12 Pro 天玑版 天玑9000+处理器 5000万疾速影像 2K超视感屏 120Hz高刷 67W快充', '小米', '100027789721', 2999.00, 1, 221, '202211040040001', 19, '无优惠', 0.00, 0.00, 0.00, 2999.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (95, 62, '202212210100000002', 37, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221028/iphone14_001.jpg', 'Apple iPhone 14 (A2884) 128GB 支持移动联通电信5G 双卡双待手机', '苹果', '100038005189', 5999.00, 1, 201, '202210280037001', 19, '无优惠', 0.00, 0.00, 0.00, 5999.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"午夜色\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (96, 62, '202212210100000002', 41, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221104/redmi_k50_01.jpg', 'Redmi K50 天玑8100 2K柔性直屏 OIS光学防抖 67W快充 5500mAh大电量', '小米', '100035246702', 2099.00, 1, 225, '202211040041001', 19, '无优惠', 0.00, 0.00, 0.00, 2099.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"墨羽\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (97, 63, '202212210100000003', 40, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221104/xiaomi_12_pro_01.jpg', '小米12 Pro 天玑版 天玑9000+处理器 5000万疾速影像 2K超视感屏 120Hz高刷 67W快充', '小米', '100027789721', 2999.00, 1, 221, '202211040040001', 19, '无优惠', 0.00, 0.00, 0.00, 2999.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (98, 64, '202212210100000004', 41, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221104/redmi_k50_01.jpg', 'Redmi K50 天玑8100 2K柔性直屏 OIS光学防抖 67W快充 5500mAh大电量', '小米', '100035246702', 2099.00, 1, 225, '202211040041001', 19, '无优惠', 0.00, 0.00, 0.00, 2099.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"墨羽\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (99, 65, '202212210100000005', 40, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221104/xiaomi_12_pro_01.jpg', '小米12 Pro 天玑版 天玑9000+处理器 5000万疾速影像 2K超视感屏 120Hz高刷 67W快充', '小米', '100027789721', 2999.00, 1, 221, '202211040040001', 19, '满减优惠：满2000.00元，减200.00元', 200.00, 58.80, 5.88, 2734.32, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (100, 65, '202212210100000005', 41, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221104/redmi_k50_01.jpg', 'Redmi K50 天玑8100 2K柔性直屏 OIS光学防抖 67W快充 5500mAh大电量', '小米', '100035246702', 2099.00, 1, 225, '202211040041001', 19, '无优惠', 0.00, 41.20, 4.12, 2053.68, 0, 0, '[{\"key\":\"颜色\",\"value\":\"墨羽\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (101, 66, '202301100100000001', 40, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221104/xiaomi_12_pro_01.jpg', '小米12 Pro 天玑版 天玑9000+处理器 5000万疾速影像 2K超视感屏 120Hz高刷 67W快充', '小米', '100027789721', 2999.00, 2, 221, '202211040040001', 19, '满减优惠：满2000.00元，减200.00元', 100.00, 0.00, 0.00, 2899.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (102, 67, '202301100100000002', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20 ', '华为', '6946605', 3788.00, 1, 110, '201806070026001', 19, '满减优惠：满3000.00元，减300.00元', 300.00, 0.00, 0.00, 3488.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"16G\"}]');
INSERT INTO `oms_order_item` VALUES (103, 68, '202301100100000003', 26, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20 ', '华为', '6946605', 3999.00, 1, 111, '201806070026002', 19, '单品促销', 100.00, 0.00, 0.00, 3899.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"金色\"},{\"key\":\"容量\",\"value\":\"32G\"}]');
INSERT INTO `oms_order_item` VALUES (104, 69, '202305110100000001', 40, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221104/xiaomi_12_pro_01.jpg', '小米12 Pro 天玑版 天玑9000+处理器 5000万疾速影像 2K超视感屏 120Hz高刷 67W快充', '小米', '100027789721', 2999.00, 1, 221, '202211040040001', 19, '满减优惠：满2000.00元，减200.00元', 200.00, 117.60, 0.00, 2681.40, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (105, 69, '202305110100000001', 41, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221104/redmi_k50_01.jpg', 'Redmi K50 天玑8100 2K柔性直屏 OIS光学防抖 67W快充 5500mAh大电量', '小米', '100035246702', 2099.00, 1, 225, '202211040041001', 19, '无优惠', 0.00, 82.40, 0.00, 2016.60, 0, 0, '[{\"key\":\"颜色\",\"value\":\"墨羽\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (106, 70, '202305110100000002', 38, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221028/ipad_001.jpg', 'Apple iPad 10.9英寸平板电脑 2022年款', '苹果', '100044025833', 3599.00, 1, 213, '202210280038001', 53, '无优惠', 0.00, 0.00, 0.00, 3599.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"银色\"},{\"key\":\"容量\",\"value\":\"64G\"}]');
INSERT INTO `oms_order_item` VALUES (107, 71, '202305110100000003', 39, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221028/xiaomi_computer_001.jpg', '小米 Xiaomi Book Pro 14 2022 锐龙版 2.8K超清大师屏 高端轻薄笔记本电脑', '小米', '100023207945', 5999.00, 1, 217, '202210280039001', 54, '无优惠', 0.00, 0.00, 0.00, 5999.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"新小米Pro 14英寸 2.8K屏\"},{\"key\":\"版本\",\"value\":\"R7 16G 512\"}]');
INSERT INTO `oms_order_item` VALUES (108, 72, '202305110100000004', 42, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221104/huawei_mate50_01.jpg', 'HUAWEI Mate 50 直屏旗舰 超光变XMAGE影像 北斗卫星消息', '华为', '100035295081', 4999.00, 1, 229, '202211040042001', 19, '无优惠', 0.00, 0.00, 0.00, 4999.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"曜金黑\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (109, 72, '202305110100000004', 44, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221108/sanxing_ssd_02.jpg', '三星（SAMSUNG）500GB SSD固态硬盘 M.2接口(NVMe协议)', '三星', '100018768480', 369.00, 1, 235, '202211080044001', 55, '无优惠', 0.00, 0.00, 0.00, 369.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"新品980｜NVMe PCIe3.0*4\"},{\"key\":\"版本\",\"value\":\"512GB\"}]');
INSERT INTO `oms_order_item` VALUES (110, 73, '202305110100000005', 37, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221028/iphone14_001.jpg', 'Apple iPhone 14 (A2884) 128GB 支持移动联通电信5G 双卡双待手机', '苹果', '100038005189', 5999.00, 1, 201, '202210280037001', 19, '无优惠', 0.00, 0.00, 0.00, 5999.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"午夜色\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (111, 74, '202305110100000006', 40, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221104/xiaomi_12_pro_01.jpg', '小米12 Pro 天玑版 天玑9000+处理器 5000万疾速影像 2K超视感屏 120Hz高刷 67W快充', '小米', '100027789721', 2999.00, 1, 221, '202211040040001', 19, '满减优惠：满2000.00元，减200.00元', 200.00, 0.00, 0.00, 2799.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (112, 75, '202305110100000007', 41, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221104/redmi_k50_01.jpg', 'Redmi K50 天玑8100 2K柔性直屏 OIS光学防抖 67W快充 5500mAh大电量', '小米', '100035246702', 2099.00, 1, 225, '202211040041001', 19, '无优惠', 0.00, 0.00, 0.00, 2099.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"墨羽\"},{\"key\":\"容量\",\"value\":\"128G\"}]');
INSERT INTO `oms_order_item` VALUES (113, 76, '202305110100000008', 39, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221028/xiaomi_computer_001.jpg', '小米 Xiaomi Book Pro 14 2022 锐龙版 2.8K超清大师屏 高端轻薄笔记本电脑', '小米', '100023207945', 5999.00, 1, 217, '202210280039001', 54, '无优惠', 0.00, 0.00, 0.00, 5999.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"新小米Pro 14英寸 2.8K屏\"},{\"key\":\"版本\",\"value\":\"R7 16G 512\"}]');
INSERT INTO `oms_order_item` VALUES (114, 76, '202305110100000008', 40, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221104/xiaomi_12_pro_01.jpg', '小米12 Pro 天玑版 天玑9000+处理器 5000万疾速影像 2K超视感屏 120Hz高刷 67W快充', '小米', '100027789721', 2999.00, 1, 221, '202211040040001', 19, '满减优惠：满2000.00元，减200.00元', 200.00, 100.00, 0.00, 2699.00, 0, 0, '[{\"key\":\"颜色\",\"value\":\"黑色\"},{\"key\":\"容量\",\"value\":\"128G\"}]');

-- ----------------------------
-- Table structure for oms_order_operate_history
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_operate_history`;
CREATE TABLE `oms_order_operate_history`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NULL DEFAULT NULL COMMENT '订单id',
  `operate_man` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '操作人：用户；系统；后台管理员',
  `create_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  `order_status` int(1) NULL DEFAULT NULL COMMENT '订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单',
  `note` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '订单操作历史记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_order_operate_history
-- ----------------------------
INSERT INTO `oms_order_operate_history` VALUES (5, 12, '后台管理员', '2018-10-12 14:01:29', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (6, 13, '后台管理员', '2018-10-12 14:01:29', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (7, 12, '后台管理员', '2018-10-12 14:13:10', 4, '订单关闭:买家退货');
INSERT INTO `oms_order_operate_history` VALUES (8, 13, '后台管理员', '2018-10-12 14:13:10', 4, '订单关闭:买家退货');
INSERT INTO `oms_order_operate_history` VALUES (9, 22, '后台管理员', '2018-10-15 16:31:48', 4, '订单关闭:xxx');
INSERT INTO `oms_order_operate_history` VALUES (10, 22, '后台管理员', '2018-10-15 16:35:08', 4, '订单关闭:xxx');
INSERT INTO `oms_order_operate_history` VALUES (11, 22, '后台管理员', '2018-10-15 16:35:59', 4, '订单关闭:xxx');
INSERT INTO `oms_order_operate_history` VALUES (12, 17, '后台管理员', '2018-10-15 16:43:40', 4, '订单关闭:xxx');
INSERT INTO `oms_order_operate_history` VALUES (13, 25, '后台管理员', '2018-10-15 16:52:14', 4, '订单关闭:xxx');
INSERT INTO `oms_order_operate_history` VALUES (14, 26, '后台管理员', '2018-10-15 16:52:14', 4, '订单关闭:xxx');
INSERT INTO `oms_order_operate_history` VALUES (15, 23, '后台管理员', '2018-10-16 14:41:28', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (16, 13, '后台管理员', '2018-10-16 14:42:17', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (17, 18, '后台管理员', '2018-10-16 14:42:17', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (18, 26, '后台管理员', '2018-10-30 14:37:44', 4, '订单关闭:关闭订单');
INSERT INTO `oms_order_operate_history` VALUES (19, 25, '后台管理员', '2018-10-30 15:07:01', 0, '修改收货人信息');
INSERT INTO `oms_order_operate_history` VALUES (20, 25, '后台管理员', '2018-10-30 15:08:13', 0, '修改费用信息');
INSERT INTO `oms_order_operate_history` VALUES (21, 25, '后台管理员', '2018-10-30 15:08:31', 0, '修改备注信息：xxx');
INSERT INTO `oms_order_operate_history` VALUES (22, 25, '后台管理员', '2018-10-30 15:08:39', 4, '订单关闭:2222');
INSERT INTO `oms_order_operate_history` VALUES (23, 12, '后台管理员', '2019-11-09 16:50:28', 4, '修改备注信息：111');
INSERT INTO `oms_order_operate_history` VALUES (24, 30, '后台管理员', '2020-02-25 16:52:37', 0, '修改费用信息');
INSERT INTO `oms_order_operate_history` VALUES (25, 30, '后台管理员', '2020-02-25 16:52:51', 0, '修改费用信息');
INSERT INTO `oms_order_operate_history` VALUES (26, 30, '后台管理员', '2020-02-25 16:54:03', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (27, 35, '后台管理员', '2020-05-17 15:30:24', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (28, 37, '后台管理员', '2020-05-17 19:35:00', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (29, 39, '后台管理员', '2020-05-17 19:42:08', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (30, 41, '后台管理员', '2020-05-18 20:23:04', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (31, 47, '后台管理员', '2020-06-21 15:13:44', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (32, 48, '后台管理员', '2020-06-21 15:15:49', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (33, 52, '后台管理员', '2022-11-09 15:16:13', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (34, 60, '后台管理员', '2022-11-16 10:42:50', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (35, 62, '后台管理员', '2022-12-21 15:50:24', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (36, 63, '后台管理员', '2023-01-10 10:08:34', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (37, 65, '后台管理员', '2023-01-10 10:08:34', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (38, 69, '后台管理员', '2023-05-11 15:30:08', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (39, 70, '后台管理员', '2023-05-11 15:31:22', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (40, 72, '后台管理员', '2023-05-11 15:33:43', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (41, 74, '后台管理员', '2023-05-11 15:36:00', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (42, 75, '后台管理员', '2023-05-11 15:36:11', 2, '完成发货');
INSERT INTO `oms_order_operate_history` VALUES (43, 76, '后台管理员', '2023-05-11 15:37:34', 2, '完成发货');

-- ----------------------------
-- Table structure for oms_order_return_apply
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_return_apply`;
CREATE TABLE `oms_order_return_apply`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NULL DEFAULT NULL COMMENT '订单id',
  `company_address_id` bigint(20) NULL DEFAULT NULL COMMENT '收货地址表id',
  `product_id` bigint(20) NULL DEFAULT NULL COMMENT '退货商品id',
  `order_sn` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '订单编号',
  `create_time` datetime NULL DEFAULT NULL COMMENT '申请时间',
  `member_username` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '会员用户名',
  `return_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '退款金额',
  `return_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '退货人姓名',
  `return_phone` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '退货人电话',
  `status` int(1) NULL DEFAULT NULL COMMENT '申请状态：0->待处理；1->退货中；2->已完成；3->已拒绝',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `product_pic` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商品图片',
  `product_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商品名称',
  `product_brand` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商品品牌',
  `product_attr` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '商品销售属性：颜色：红色；尺码：xl;',
  `product_count` int(11) NULL DEFAULT NULL COMMENT '退货数量',
  `product_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '商品单价',
  `product_real_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '商品实际支付单价',
  `reason` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '原因',
  `description` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  `proof_pics` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '凭证图片，以逗号隔开',
  `handle_note` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '处理备注',
  `handle_man` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '处理人员',
  `receive_man` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '收货人',
  `receive_time` datetime NULL DEFAULT NULL COMMENT '收货时间',
  `receive_note` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '收货备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '订单退货申请' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_order_return_apply
-- ----------------------------
INSERT INTO `oms_order_return_apply` VALUES (3, 12, 1, 26, '201809150101000001', '2018-10-17 14:34:57', 'test', 0.00, '大梨', '18000000000', 2, '2022-11-11 10:16:18', 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20', '华为', '颜色：金色;内存：16G', 1, 3788.00, 3585.98, '质量问题', '老是卡', 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg,http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '111', 'admin', 'admin', '2022-11-11 10:16:26', NULL);
INSERT INTO `oms_order_return_apply` VALUES (4, 12, 2, 27, '201809150101000001', '2018-10-17 14:40:21', 'test', 3585.98, '大梨', '18000000000', 1, '2018-10-18 13:54:10', 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8', '小米', '颜色：黑色;内存：32G', 1, 2699.00, 2022.81, '质量问题', '不够高端', '', '已经处理了', 'admin', NULL, NULL, NULL);
INSERT INTO `oms_order_return_apply` VALUES (5, 12, 3, 28, '201809150101000001', '2018-10-17 14:44:18', 'test', 3585.98, '大梨', '18000000000', 2, '2018-10-18 13:55:28', 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5a9d248cN071f4959.jpg', '红米5A', '小米', '颜色：金色;内存：16G', 1, 649.00, 591.05, '质量问题', '颜色太土', '', '已经处理了', 'admin', 'admin', '2018-10-18 13:55:58', '已经处理了');
INSERT INTO `oms_order_return_apply` VALUES (8, 13, NULL, 28, '201809150102000002', '2018-10-17 14:44:18', 'test', NULL, '大梨', '18000000000', 3, '2018-10-18 13:57:12', 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5a9d248cN071f4959.jpg', '红米5A', '小米', '颜色：金色;内存：16G', 1, 649.00, 591.05, '质量问题', '颜色太土', '', '理由不够充分', 'admin', NULL, NULL, NULL);
INSERT INTO `oms_order_return_apply` VALUES (9, 14, 2, 26, '201809130101000001', '2018-10-17 14:34:57', 'test', 3500.00, '大梨', '18000000000', 2, '2018-10-24 15:44:56', 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20', '华为', '颜色：金色;内存：16G', 1, 3788.00, 3585.98, '质量问题', '老是卡', '', '呵呵', 'admin', 'admin', '2018-10-24 15:46:35', '收货了');
INSERT INTO `oms_order_return_apply` VALUES (10, 14, NULL, 27, '201809130101000001', '2018-10-17 14:40:21', 'test', NULL, '大梨', '18000000000', 3, '2018-10-24 15:46:57', 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8', '小米', '颜色：黑色;内存：32G', 1, 2699.00, 2022.81, '质量问题', '不够高端', '', '就是不退', 'admin', NULL, NULL, NULL);
INSERT INTO `oms_order_return_apply` VALUES (11, 14, 2, 28, '201809130101000001', '2018-10-17 14:44:18', 'test', 591.05, '大梨', '18000000000', 1, '2018-10-24 17:09:04', 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5a9d248cN071f4959.jpg', '红米5A', '小米', '颜色：金色;内存：16G', 1, 649.00, 591.05, '质量问题', '颜色太土', '', '可以退款', 'admin', NULL, NULL, NULL);
INSERT INTO `oms_order_return_apply` VALUES (12, 15, 3, 26, '201809130102000002', '2018-10-17 14:34:57', 'test', 3500.00, '大梨', '18000000000', 2, '2018-10-24 17:22:54', 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20', '华为', '颜色：金色;内存：16G', 1, 3788.00, 3585.98, '质量问题', '老是卡', '', '退货了', 'admin', 'admin', '2018-10-24 17:23:06', '收货了');
INSERT INTO `oms_order_return_apply` VALUES (13, 15, NULL, 27, '201809130102000002', '2018-10-17 14:40:21', 'test', NULL, '大梨', '18000000000', 3, '2018-10-24 17:23:30', 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8', '小米', '颜色：黑色;内存：32G', 1, 2699.00, 2022.81, '质量问题', '不够高端', '', '无法退货', 'admin', NULL, NULL, NULL);
INSERT INTO `oms_order_return_apply` VALUES (15, 16, NULL, 26, '201809140101000001', '2018-10-17 14:34:57', 'test', NULL, '大梨', '18000000000', 0, NULL, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20', '华为', '颜色：金色;内存：16G', 1, 3788.00, 3585.98, '质量问题', '老是卡', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order_return_apply` VALUES (16, 16, NULL, 27, '201809140101000001', '2018-10-17 14:40:21', 'test', NULL, '大梨', '18000000000', 0, NULL, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8', '小米', '颜色：黑色;内存：32G', 1, 2699.00, 2022.81, '质量问题', '不够高端', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order_return_apply` VALUES (17, 16, NULL, 28, '201809140101000001', '2018-10-17 14:44:18', 'test', NULL, '大梨', '18000000000', 0, NULL, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5a9d248cN071f4959.jpg', '红米5A', '小米', '颜色：金色;内存：16G', 1, 649.00, 591.05, '质量问题', '颜色太土', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order_return_apply` VALUES (18, 17, NULL, 26, '201809150101000003', '2018-10-17 14:34:57', 'test', NULL, '大梨', '18000000000', 0, NULL, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20', '华为', '颜色：金色;内存：16G', 1, 3788.00, 3585.98, '质量问题', '老是卡', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order_return_apply` VALUES (19, 17, NULL, 27, '201809150101000003', '2018-10-17 14:40:21', 'test', NULL, '大梨', '18000000000', 0, NULL, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8', '小米', '颜色：黑色;内存：32G', 1, 2699.00, 2022.81, '质量问题', '不够高端', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order_return_apply` VALUES (20, 17, NULL, 28, '201809150101000003', '2018-10-17 14:44:18', 'test', NULL, '大梨', '18000000000', 0, NULL, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5a9d248cN071f4959.jpg', '红米5A', '小米', '颜色：金色;内存：16G', 1, 649.00, 591.05, '质量问题', '颜色太土', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order_return_apply` VALUES (21, 18, NULL, 26, '201809150102000004', '2018-10-17 14:34:57', 'test', NULL, '大梨', '18000000000', 0, NULL, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20', '华为', '颜色：金色;内存：16G', 1, 3788.00, 3585.98, '质量问题', '老是卡', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order_return_apply` VALUES (22, 18, NULL, 27, '201809150102000004', '2018-10-17 14:40:21', 'test', NULL, '大梨', '18000000000', 0, NULL, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8', '小米', '颜色：黑色;内存：32G', 1, 2699.00, 2022.81, '质量问题', '不够高端', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order_return_apply` VALUES (23, 18, NULL, 28, '201809150102000004', '2018-10-17 14:44:18', 'test', NULL, '大梨', '18000000000', 0, NULL, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5a9d248cN071f4959.jpg', '红米5A', '小米', '颜色：金色;内存：16G', 1, 649.00, 591.05, '质量问题', '颜色太土', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order_return_apply` VALUES (24, 19, NULL, 26, '201809130101000003', '2018-10-17 14:34:57', 'test', NULL, '大梨', '18000000000', 0, NULL, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/5ac1bf58Ndefaac16.jpg', '华为 HUAWEI P20', '华为', '颜色：金色;内存：16G', 1, 3788.00, 3585.98, '质量问题', '老是卡', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order_return_apply` VALUES (25, 19, NULL, 27, '201809130101000003', '2018-10-17 14:40:21', 'test', NULL, '大梨', '18000000000', 0, NULL, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/xiaomi.jpg', '小米8', '小米', '颜色：黑色;内存：32G', 1, 2699.00, 2022.81, '质量问题', '不够高端', '', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oms_order_return_apply` VALUES (26, 19, NULL, 28, '201809130101000003', '2018-10-17 14:44:18', 'test', NULL, '大梨', '18000000000', 0, NULL, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180615/5a9d248cN071f4959.jpg', '红米5A', '小米', '颜色：金色;内存：16G', 1, 649.00, 591.05, '质量问题', '颜色太土', '', NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for oms_order_return_reason
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_return_reason`;
CREATE TABLE `oms_order_return_reason`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '退货类型',
  `sort` int(11) NULL DEFAULT NULL,
  `status` int(1) NULL DEFAULT NULL COMMENT '状态：0->不启用；1->启用',
  `create_time` datetime NULL DEFAULT NULL COMMENT '添加时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '退货原因表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_order_return_reason
-- ----------------------------
INSERT INTO `oms_order_return_reason` VALUES (1, '质量问题', 1, 1, '2018-10-17 10:00:45');
INSERT INTO `oms_order_return_reason` VALUES (2, '尺码太大', 1, 1, '2018-10-17 10:01:03');
INSERT INTO `oms_order_return_reason` VALUES (3, '颜色不喜欢', 1, 1, '2018-10-17 10:01:13');
INSERT INTO `oms_order_return_reason` VALUES (4, '7天无理由退货', 1, 1, '2018-10-17 10:01:47');
INSERT INTO `oms_order_return_reason` VALUES (5, '价格问题', 1, 0, '2018-10-17 10:01:57');
INSERT INTO `oms_order_return_reason` VALUES (12, '发票问题', 0, 1, '2018-10-19 16:28:36');
INSERT INTO `oms_order_return_reason` VALUES (13, '其他问题', 0, 1, '2018-10-19 16:28:51');
INSERT INTO `oms_order_return_reason` VALUES (14, '物流问题', 0, 1, '2018-10-19 16:29:01');
INSERT INTO `oms_order_return_reason` VALUES (15, '售后问题', 0, 1, '2018-10-19 16:29:11');

-- ----------------------------
-- Table structure for oms_order_setting
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_setting`;
CREATE TABLE `oms_order_setting`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `flash_order_overtime` int(11) NULL DEFAULT NULL COMMENT '秒杀订单超时关闭时间(分)',
  `normal_order_overtime` int(11) NULL DEFAULT NULL COMMENT '正常订单超时时间(分)',
  `confirm_overtime` int(11) NULL DEFAULT NULL COMMENT '发货后自动确认收货时间（天）',
  `finish_overtime` int(11) NULL DEFAULT NULL COMMENT '自动完成交易时间，不能申请售后（天）',
  `comment_overtime` int(11) NULL DEFAULT NULL COMMENT '订单完成后自动好评时间（天）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '订单设置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of oms_order_setting
-- ----------------------------
INSERT INTO `oms_order_setting` VALUES (1, 60, 120, 15, 7, 7);

SET FOREIGN_KEY_CHECKS = 1;