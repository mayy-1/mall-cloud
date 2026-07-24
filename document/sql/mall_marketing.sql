/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80034 (8.0.34)
 Source Host           : localhost:3306
 Source Schema         : mall_marketing

 Target Server Type    : MySQL
 Target Server Version : 80034 (8.0.34)
 File Encoding         : 65001

 Date: 21/07/2026 16:11:02
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for cms_help
-- ----------------------------
DROP TABLE IF EXISTS `cms_help`;
CREATE TABLE `cms_help`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_id` bigint NULL DEFAULT NULL,
  `icon` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `title` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `show_status` int NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `read_count` int NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '帮助表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cms_help
-- ----------------------------

-- ----------------------------
-- Table structure for cms_help_category
-- ----------------------------
DROP TABLE IF EXISTS `cms_help_category`;
CREATE TABLE `cms_help_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `icon` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '分类图标',
  `help_count` int NULL DEFAULT NULL COMMENT '专题数量',
  `show_status` int NULL DEFAULT NULL,
  `sort` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '帮助分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cms_help_category
-- ----------------------------

-- ----------------------------
-- Table structure for cms_member_report
-- ----------------------------
DROP TABLE IF EXISTS `cms_member_report`;
CREATE TABLE `cms_member_report`  (
  `id` bigint NULL DEFAULT NULL,
  `report_type` int NULL DEFAULT NULL COMMENT '举报类型：0->商品评价；1->话题内容；2->用户评论',
  `report_member_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '举报人',
  `create_time` datetime NULL DEFAULT NULL,
  `report_object` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `report_status` int NULL DEFAULT NULL COMMENT '举报状态：0->未处理；1->已处理',
  `handle_status` int NULL DEFAULT NULL COMMENT '处理结果：0->无效；1->有效；2->恶意',
  `note` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '用户举报表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cms_member_report
-- ----------------------------

-- ----------------------------
-- Table structure for cms_prefrence_area
-- ----------------------------
DROP TABLE IF EXISTS `cms_prefrence_area`;
CREATE TABLE `cms_prefrence_area`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `sub_title` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `pic` varbinary(500) NULL DEFAULT NULL COMMENT '展示图片',
  `sort` int NULL DEFAULT NULL,
  `show_status` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '优选专区' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cms_prefrence_area
-- ----------------------------
INSERT INTO `cms_prefrence_area` VALUES (1, '让音质更出众', '音质不打折 完美现场感', NULL, NULL, 1);
INSERT INTO `cms_prefrence_area` VALUES (2, '让音质更出众22', '让音质更出众22', NULL, NULL, NULL);
INSERT INTO `cms_prefrence_area` VALUES (3, '让音质更出众33', NULL, NULL, NULL, NULL);
INSERT INTO `cms_prefrence_area` VALUES (4, '让音质更出众44', NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for cms_prefrence_area_product_relation
-- ----------------------------
DROP TABLE IF EXISTS `cms_prefrence_area_product_relation`;
CREATE TABLE `cms_prefrence_area_product_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `prefrence_area_id` bigint NULL DEFAULT NULL,
  `product_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '优选专区和产品关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cms_prefrence_area_product_relation
-- ----------------------------
INSERT INTO `cms_prefrence_area_product_relation` VALUES (1, 1, 12);
INSERT INTO `cms_prefrence_area_product_relation` VALUES (2, 1, 13);
INSERT INTO `cms_prefrence_area_product_relation` VALUES (3, 1, 14);
INSERT INTO `cms_prefrence_area_product_relation` VALUES (4, 1, 18);
INSERT INTO `cms_prefrence_area_product_relation` VALUES (5, 1, 7);
INSERT INTO `cms_prefrence_area_product_relation` VALUES (6, 2, 7);
INSERT INTO `cms_prefrence_area_product_relation` VALUES (7, 1, 22);
INSERT INTO `cms_prefrence_area_product_relation` VALUES (24, 1, 23);

-- ----------------------------
-- Table structure for cms_subject
-- ----------------------------
DROP TABLE IF EXISTS `cms_subject`;
CREATE TABLE `cms_subject`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_id` bigint NULL DEFAULT NULL,
  `title` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `pic` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '专题主图',
  `product_count` int NULL DEFAULT NULL COMMENT '关联产品数量',
  `recommend_status` int NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `collect_count` int NULL DEFAULT NULL,
  `read_count` int NULL DEFAULT NULL,
  `comment_count` int NULL DEFAULT NULL,
  `album_pics` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '画册图片用逗号分割',
  `description` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `show_status` int NULL DEFAULT NULL COMMENT '显示状态：0->不显示；1->显示',
  `content` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL,
  `forward_count` int NULL DEFAULT NULL COMMENT '转发数',
  `category_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '专题分类名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '专题表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cms_subject
-- ----------------------------
INSERT INTO `cms_subject` VALUES (1, 1, 'polo衬衫的也时尚', NULL, NULL, NULL, '2018-11-11 13:26:55', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '服装专题');
INSERT INTO `cms_subject` VALUES (2, 2, '大牌手机低价秒', NULL, NULL, NULL, '2018-11-12 13:27:00', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '手机专题');
INSERT INTO `cms_subject` VALUES (3, 2, '晓龙845新品上市', NULL, NULL, NULL, '2018-11-13 13:27:05', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '手机专题');
INSERT INTO `cms_subject` VALUES (4, 1, '夏天应该穿什么', NULL, NULL, NULL, '2018-11-01 13:27:09', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '服装专题');
INSERT INTO `cms_subject` VALUES (5, 1, '夏季精选', NULL, NULL, NULL, '2018-11-06 13:27:18', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '服装专题');
INSERT INTO `cms_subject` VALUES (6, 2, '品牌手机降价', NULL, NULL, NULL, '2018-11-07 13:27:21', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '手机专题');

-- ----------------------------
-- Table structure for cms_subject_category
-- ----------------------------
DROP TABLE IF EXISTS `cms_subject_category`;
CREATE TABLE `cms_subject_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `icon` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '分类图标',
  `subject_count` int NULL DEFAULT NULL COMMENT '专题数量',
  `show_status` int NULL DEFAULT NULL,
  `sort` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '专题分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cms_subject_category
-- ----------------------------
INSERT INTO `cms_subject_category` VALUES (1, '服装专题', NULL, NULL, NULL, NULL);
INSERT INTO `cms_subject_category` VALUES (2, '手机专题', NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for cms_subject_comment
-- ----------------------------
DROP TABLE IF EXISTS `cms_subject_comment`;
CREATE TABLE `cms_subject_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `subject_id` bigint NULL DEFAULT NULL,
  `member_nick_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `member_icon` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `content` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `show_status` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '专题评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cms_subject_comment
-- ----------------------------

-- ----------------------------
-- Table structure for cms_subject_product_relation
-- ----------------------------
DROP TABLE IF EXISTS `cms_subject_product_relation`;
CREATE TABLE `cms_subject_product_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `subject_id` bigint NULL DEFAULT NULL,
  `product_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 71 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '专题商品关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cms_subject_product_relation
-- ----------------------------
INSERT INTO `cms_subject_product_relation` VALUES (1, 1, 12);
INSERT INTO `cms_subject_product_relation` VALUES (2, 1, 13);
INSERT INTO `cms_subject_product_relation` VALUES (3, 1, 14);
INSERT INTO `cms_subject_product_relation` VALUES (4, 1, 18);
INSERT INTO `cms_subject_product_relation` VALUES (5, 1, 7);
INSERT INTO `cms_subject_product_relation` VALUES (6, 2, 7);
INSERT INTO `cms_subject_product_relation` VALUES (7, 1, 22);
INSERT INTO `cms_subject_product_relation` VALUES (29, 1, 23);
INSERT INTO `cms_subject_product_relation` VALUES (30, 4, 23);
INSERT INTO `cms_subject_product_relation` VALUES (31, 5, 23);
INSERT INTO `cms_subject_product_relation` VALUES (68, 2, 26);
INSERT INTO `cms_subject_product_relation` VALUES (69, 3, 26);
INSERT INTO `cms_subject_product_relation` VALUES (70, 6, 26);

-- ----------------------------
-- Table structure for cms_topic
-- ----------------------------
DROP TABLE IF EXISTS `cms_topic`;
CREATE TABLE `cms_topic`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_id` bigint NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `start_time` datetime NULL DEFAULT NULL,
  `end_time` datetime NULL DEFAULT NULL,
  `attend_count` int NULL DEFAULT NULL COMMENT '参与人数',
  `attention_count` int NULL DEFAULT NULL COMMENT '关注人数',
  `read_count` int NULL DEFAULT NULL,
  `award_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '奖品名称',
  `attend_type` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '参与方式',
  `content` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '话题内容',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '话题表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cms_topic
-- ----------------------------

-- ----------------------------
-- Table structure for cms_topic_category
-- ----------------------------
DROP TABLE IF EXISTS `cms_topic_category`;
CREATE TABLE `cms_topic_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `icon` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '分类图标',
  `subject_count` int NULL DEFAULT NULL COMMENT '专题数量',
  `show_status` int NULL DEFAULT NULL,
  `sort` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '话题分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cms_topic_category
-- ----------------------------

-- ----------------------------
-- Table structure for cms_topic_comment
-- ----------------------------
DROP TABLE IF EXISTS `cms_topic_comment`;
CREATE TABLE `cms_topic_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_nick_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `topic_id` bigint NULL DEFAULT NULL,
  `member_icon` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `content` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `show_status` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '专题评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cms_topic_comment
-- ----------------------------

-- ----------------------------
-- Table structure for sms_coupon
-- ----------------------------
DROP TABLE IF EXISTS `sms_coupon`;
CREATE TABLE `sms_coupon`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` int NULL DEFAULT NULL COMMENT '优惠券类型；0->全场赠券；1->会员赠券；2->购物赠券；3->注册赠券',
  `name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `platform` int NULL DEFAULT NULL COMMENT '使用平台：0->全部；1->移动；2->PC',
  `count` int NULL DEFAULT NULL COMMENT '数量',
  `amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '金额',
  `per_limit` int NULL DEFAULT NULL COMMENT '每人限领张数',
  `min_point` decimal(10, 2) NULL DEFAULT NULL COMMENT '使用门槛；0表示无门槛',
  `start_time` datetime NULL DEFAULT NULL,
  `end_time` datetime NULL DEFAULT NULL,
  `use_type` int NULL DEFAULT NULL COMMENT '使用类型：0->全场通用；1->指定分类；2->指定商品',
  `note` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '备注',
  `publish_count` int NULL DEFAULT NULL COMMENT '发行数量',
  `use_count` int NULL DEFAULT NULL COMMENT '已使用数量',
  `receive_count` int NULL DEFAULT NULL COMMENT '领取数量',
  `enable_time` datetime NULL DEFAULT NULL COMMENT '可以领取的日期',
  `code` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '优惠码',
  `member_level` int NULL DEFAULT NULL COMMENT '可领取的会员类型：0->无限时',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '优惠券表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sms_coupon
-- ----------------------------
INSERT INTO `sms_coupon` VALUES (27, 0, '全品类通用券', 0, 94, 10.00, 10, 100.00, '2022-11-08 00:00:00', '2023-11-30 00:00:00', 0, NULL, 100, 0, 6, '2022-11-08 00:00:00', NULL, NULL);
INSERT INTO `sms_coupon` VALUES (28, 0, '手机分类专用券', 0, 995, 100.00, 5, 1000.00, '2022-11-08 00:00:00', '2023-11-30 00:00:00', 1, NULL, 1000, 0, 5, '2022-11-08 00:00:00', NULL, NULL);
INSERT INTO `sms_coupon` VALUES (29, 0, '苹果手机专用券', 0, 998, 600.00, 1, 4000.00, '2022-11-08 00:00:00', '2023-11-30 00:00:00', 2, NULL, 1000, 0, 2, '2022-11-08 00:00:00', NULL, NULL);
INSERT INTO `sms_coupon` VALUES (30, 0, '小米手机专用券', 0, 998, 200.00, 1, 2000.00, '2022-11-08 00:00:00', '2023-11-30 00:00:00', 2, NULL, 1000, 0, 2, '2022-11-08 00:00:00', NULL, NULL);
INSERT INTO `sms_coupon` VALUES (31, 0, '限时优惠券', 0, 999, 20.00, 5, 500.00, '2022-12-01 00:00:00', '2022-12-22 00:00:00', 0, NULL, 1000, 0, 1, '2022-12-23 00:00:00', NULL, NULL);

-- ----------------------------
-- Table structure for sms_coupon_history
-- ----------------------------
DROP TABLE IF EXISTS `sms_coupon_history`;
CREATE TABLE `sms_coupon_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `coupon_id` bigint NULL DEFAULT NULL,
  `member_id` bigint NULL DEFAULT NULL,
  `coupon_code` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `member_nickname` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '领取人昵称',
  `get_type` int NULL DEFAULT NULL COMMENT '获取类型：0->后台赠送；1->主动获取',
  `create_time` datetime NULL DEFAULT NULL,
  `use_status` int NULL DEFAULT NULL COMMENT '使用状态：0->未使用；1->已使用；2->已过期',
  `use_time` datetime NULL DEFAULT NULL COMMENT '使用时间',
  `order_id` bigint NULL DEFAULT NULL COMMENT '订单编号',
  `order_sn` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '订单号码',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_member_coupon`(`member_id` ASC, `coupon_id` ASC) USING BTREE,
  INDEX `idx_member_id`(`member_id` ASC) USING BTREE,
  INDEX `idx_coupon_id`(`coupon_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 53 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '优惠券使用、领取历史表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sms_coupon_history
-- ----------------------------

-- ----------------------------
-- Table structure for sms_coupon_product_category_relation
-- ----------------------------
DROP TABLE IF EXISTS `sms_coupon_product_category_relation`;
CREATE TABLE `sms_coupon_product_category_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `coupon_id` bigint NULL DEFAULT NULL,
  `product_category_id` bigint NULL DEFAULT NULL,
  `product_category_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '产品分类名称',
  `parent_category_name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '父分类名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '优惠券和产品分类关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sms_coupon_product_category_relation
-- ----------------------------
INSERT INTO `sms_coupon_product_category_relation` VALUES (11, 28, 19, '手机通讯', '手机数码');

-- ----------------------------
-- Table structure for sms_coupon_product_relation
-- ----------------------------
DROP TABLE IF EXISTS `sms_coupon_product_relation`;
CREATE TABLE `sms_coupon_product_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `coupon_id` bigint NULL DEFAULT NULL,
  `product_id` bigint NULL DEFAULT NULL,
  `product_name` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '商品名称',
  `product_sn` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '商品编码',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '优惠券和产品的关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sms_coupon_product_relation
-- ----------------------------
INSERT INTO `sms_coupon_product_relation` VALUES (18, 29, 37, 'Apple iPhone 14 (A2884) 128GB 支持移动联通电信5G 双卡双待手机', '100038005189');
INSERT INTO `sms_coupon_product_relation` VALUES (19, 29, 29, 'Apple iPhone 8 Plus 64GB 红色特别版 移动联通电信4G手机', '7437799');
INSERT INTO `sms_coupon_product_relation` VALUES (21, 30, 41, 'Redmi K50 天玑8100 2K柔性直屏 OIS光学防抖 67W快充 5500mAh大电量 墨羽 12GB+256GB 5G智能手机 小米 红米', '100035246702');
INSERT INTO `sms_coupon_product_relation` VALUES (22, 30, 40, '小米12 Pro 天玑版 天玑9000+处理器 5000万疾速影像 2K超视感屏 120Hz高刷 67W快充 12GB+256GB 黑色 5G手机', '100027789721');

-- ----------------------------
-- Table structure for sms_flash_promotion
-- ----------------------------
DROP TABLE IF EXISTS `sms_flash_promotion`;
CREATE TABLE `sms_flash_promotion`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '秒杀时间段名称',
  `start_date` date NULL DEFAULT NULL COMMENT '开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '结束日期',
  `status` int NULL DEFAULT NULL COMMENT '上下线状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '限时购表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sms_flash_promotion
-- ----------------------------
INSERT INTO `sms_flash_promotion` VALUES (14, '双11特卖活动', '2022-11-09', '2023-12-31', 1, '2022-11-09 14:56:48');

-- ----------------------------
-- Table structure for sms_flash_promotion_log
-- ----------------------------
DROP TABLE IF EXISTS `sms_flash_promotion_log`;
CREATE TABLE `sms_flash_promotion_log`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `member_id` int NULL DEFAULT NULL,
  `product_id` bigint NULL DEFAULT NULL,
  `member_phone` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `product_name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `subscribe_time` datetime NULL DEFAULT NULL COMMENT '会员订阅时间',
  `send_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '限时购通知记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sms_flash_promotion_log
-- ----------------------------

-- ----------------------------
-- Table structure for sms_flash_promotion_product_relation
-- ----------------------------
DROP TABLE IF EXISTS `sms_flash_promotion_product_relation`;
CREATE TABLE `sms_flash_promotion_product_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `flash_promotion_id` bigint NULL DEFAULT NULL,
  `flash_promotion_session_id` bigint NULL DEFAULT NULL COMMENT '编号',
  `product_id` bigint NULL DEFAULT NULL,
  `flash_promotion_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '限时购价格',
  `flash_promotion_count` int NULL DEFAULT NULL COMMENT '限时购数量',
  `flash_promotion_limit` int NULL DEFAULT NULL COMMENT '每人限购数量',
  `sort` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_promotion_product`(`flash_promotion_id` ASC, `product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 52 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '商品限时购与商品关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sms_flash_promotion_product_relation
-- ----------------------------

-- ----------------------------
-- Table structure for sms_flash_promotion_session
-- ----------------------------
DROP TABLE IF EXISTS `sms_flash_promotion_session`;
CREATE TABLE `sms_flash_promotion_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '场次名称',
  `start_time` time NULL DEFAULT NULL COMMENT '每日开始时间',
  `end_time` time NULL DEFAULT NULL COMMENT '每日结束时间',
  `status` int NULL DEFAULT NULL COMMENT '启用状态：0->不启用；1->启用',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '限时购场次表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sms_flash_promotion_session
-- ----------------------------
INSERT INTO `sms_flash_promotion_session` VALUES (1, '8:00', '08:00:00', '10:00:00', 1, '2018-11-16 13:22:17');
INSERT INTO `sms_flash_promotion_session` VALUES (2, '10:00', '10:00:00', '12:00:00', 1, '2018-11-16 13:22:34');
INSERT INTO `sms_flash_promotion_session` VALUES (3, '12:00', '12:00:00', '14:00:00', 1, '2018-11-16 13:22:37');
INSERT INTO `sms_flash_promotion_session` VALUES (4, '14:00', '14:00:00', '16:00:00', 1, '2018-11-16 13:22:41');
INSERT INTO `sms_flash_promotion_session` VALUES (5, '16:00', '16:00:00', '18:00:00', 1, '2018-11-16 13:22:45');
INSERT INTO `sms_flash_promotion_session` VALUES (6, '18:00', '18:00:00', '20:00:00', 1, '2018-11-16 13:21:34');
INSERT INTO `sms_flash_promotion_session` VALUES (7, '20:00', '20:00:00', '22:00:00', 1, '2018-11-16 13:22:55');

-- ----------------------------
-- Table structure for sms_home_advertise
-- ----------------------------
DROP TABLE IF EXISTS `sms_home_advertise`;
CREATE TABLE `sms_home_advertise`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `type` int NULL DEFAULT NULL COMMENT '轮播位置：0->PC首页轮播；1->app首页轮播',
  `pic` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `start_time` datetime NULL DEFAULT NULL,
  `end_time` datetime NULL DEFAULT NULL,
  `status` int NULL DEFAULT NULL COMMENT '上下线状态：0->下线；1->上线',
  `click_count` int NULL DEFAULT NULL COMMENT '点击数',
  `order_count` int NULL DEFAULT NULL COMMENT '下单数',
  `url` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '链接地址',
  `note` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '备注',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '首页轮播广告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sms_home_advertise
-- ----------------------------
INSERT INTO `sms_home_advertise` VALUES (2, '夏季大热促销', 1, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20190525/ad1.jpg', '2018-11-01 14:01:37', '2018-11-15 14:01:37', 0, 0, 0, NULL, '夏季大热促销', 0);
INSERT INTO `sms_home_advertise` VALUES (3, '夏季大热促销1', 1, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20190525/ad1.jpg', '2018-11-13 14:01:37', '2018-11-13 14:01:37', 0, 0, 0, NULL, '夏季大热促销1', 0);
INSERT INTO `sms_home_advertise` VALUES (4, '夏季大热促销2', 1, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20190525/ad2.jpg', '2018-11-13 14:01:37', '2018-11-13 14:01:37', 0, 0, 0, NULL, '夏季大热促销2', 0);
INSERT INTO `sms_home_advertise` VALUES (9, '电影推荐广告', 1, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20181113/movie_ad.jpg', '2018-11-01 00:00:00', '2018-11-24 00:00:00', 0, 0, 0, 'www.baidu.com', '电影推荐广告', 100);
INSERT INTO `sms_home_advertise` VALUES (10, '汽车促销广告', 1, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20181113/car_ad.jpg', '2018-11-13 00:00:00', '2018-11-24 00:00:00', 0, 0, 0, 'xxx', NULL, 99);
INSERT INTO `sms_home_advertise` VALUES (11, '汽车推荐广告', 1, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20181113/car_ad2.jpg', '2018-11-13 00:00:00', '2018-11-30 00:00:00', 0, 0, 0, 'xxx', NULL, 98);
INSERT INTO `sms_home_advertise` VALUES (12, '小米推荐广告', 1, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221108/xiaomi_banner_01.png', '2022-11-08 17:04:03', '2023-11-08 17:04:05', 1, 0, 0, '/pages/brand/brandDetail?id=6', NULL, 0);
INSERT INTO `sms_home_advertise` VALUES (13, '华为推荐广告', 1, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221108/huawei_banner_01.png', '2022-11-08 17:10:27', '2023-11-08 17:10:28', 1, 0, 0, '/pages/brand/brandDetail?id=3', NULL, 0);
INSERT INTO `sms_home_advertise` VALUES (14, '苹果推荐广告', 1, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221108/apple_banner_01.png', '2022-11-08 17:12:54', '2023-11-08 17:12:55', 1, 0, 0, '/pages/brand/brandDetail?id=51', NULL, 0);
INSERT INTO `sms_home_advertise` VALUES (15, '三星推荐广告', 1, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221108/sanxing_banner_01.png', '2022-11-08 17:15:38', '2023-11-08 17:15:39', 1, 0, 0, '/pages/brand/brandDetail?id=2', NULL, 0);
INSERT INTO `sms_home_advertise` VALUES (16, 'OPPO推荐广告', 1, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221108/oppo_banner_01.png', '2022-11-08 17:20:10', '2023-11-08 17:20:11', 1, 0, 0, '/pages/brand/brandDetail?id=21', NULL, 0);

-- ----------------------------
-- Table structure for sms_home_brand
-- ----------------------------
DROP TABLE IF EXISTS `sms_home_brand`;
CREATE TABLE `sms_home_brand`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `brand_id` bigint NULL DEFAULT NULL,
  `brand_name` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `recommend_status` int NULL DEFAULT NULL,
  `sort` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '首页推荐品牌表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sms_home_brand
-- ----------------------------
INSERT INTO `sms_home_brand` VALUES (6, 6, '小米', 1, 300);
INSERT INTO `sms_home_brand` VALUES (32, 50, '海澜之家', 1, 198);
INSERT INTO `sms_home_brand` VALUES (33, 51, '苹果', 1, 199);
INSERT INTO `sms_home_brand` VALUES (34, 2, '三星', 1, 195);
INSERT INTO `sms_home_brand` VALUES (35, 3, '华为', 1, 200);
INSERT INTO `sms_home_brand` VALUES (39, 21, 'OPPO', 1, 197);
INSERT INTO `sms_home_brand` VALUES (45, 1, '万和', 1, 0);
INSERT INTO `sms_home_brand` VALUES (46, 5, '方太', 1, 0);
INSERT INTO `sms_home_brand` VALUES (47, 4, '格力', 1, 0);

-- ----------------------------
-- Table structure for sms_home_new_product
-- ----------------------------
DROP TABLE IF EXISTS `sms_home_new_product`;
CREATE TABLE `sms_home_new_product`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NULL DEFAULT NULL,
  `product_name` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `recommend_status` int NULL DEFAULT NULL,
  `sort` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '新鲜好物表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sms_home_new_product
-- ----------------------------
INSERT INTO `sms_home_new_product` VALUES (19, 37, 'Apple iPhone 14 (A2884) 128GB 支持移动联通电信5G 双卡双待手机', 1, 197);
INSERT INTO `sms_home_new_product` VALUES (20, 38, 'Apple iPad 10.9英寸平板电脑 2022年款（64GB WLAN版/A14芯片/1200万像素/iPadOS MPQ03CH/A ）', 1, 0);
INSERT INTO `sms_home_new_product` VALUES (21, 39, '小米 Xiaomi Book Pro 14 2022 锐龙版 2.8K超清大师屏 高端轻薄笔记本电脑(新R5-6600H标压 16G 512G win11)', 1, 198);
INSERT INTO `sms_home_new_product` VALUES (22, 40, '小米12 Pro 天玑版 天玑9000+处理器 5000万疾速影像 2K超视感屏 120Hz高刷 67W快充 12GB+256GB 黑色 5G手机', 1, 200);
INSERT INTO `sms_home_new_product` VALUES (23, 41, 'Redmi K50 天玑8100 2K柔性直屏 OIS光学防抖 67W快充 5500mAh大电量 墨羽 12GB+256GB 5G智能手机 小米 红米', 1, 199);
INSERT INTO `sms_home_new_product` VALUES (24, 42, 'HUAWEI Mate 50 直屏旗舰 超光变XMAGE影像 北斗卫星消息 低电量应急模式 128GB曜金黑华为鸿蒙手机', 1, 0);
INSERT INTO `sms_home_new_product` VALUES (25, 44, '三星（SAMSUNG）500GB SSD固态硬盘 M.2接口(NVMe协议) 980（MZ-V8V500BW）', 1, 0);
INSERT INTO `sms_home_new_product` VALUES (26, 45, 'OPPO Reno8 8GB+128GB 鸢尾紫 新配色上市 80W超级闪充 5000万水光人像三摄 3200万前置索尼镜头 5G手机', 1, 0);
INSERT INTO `sms_home_new_product` VALUES (27, 43, '万和（Vanward)燃气热水器天然气家用四重防冻直流变频节能全新升级增压水伺服恒温高抗风 JSQ30-565W16【16升】【恒温旗舰款】', 1, 0);

-- ----------------------------
-- Table structure for sms_home_recommend_product
-- ----------------------------
DROP TABLE IF EXISTS `sms_home_recommend_product`;
CREATE TABLE `sms_home_recommend_product`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NULL DEFAULT NULL,
  `product_name` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `recommend_status` int NULL DEFAULT NULL,
  `sort` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '人气推荐商品表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sms_home_recommend_product
-- ----------------------------
INSERT INTO `sms_home_recommend_product` VALUES (10, 38, 'Apple iPad 10.9英寸平板电脑 2022年款（64GB WLAN版/A14芯片/1200万像素/iPadOS MPQ03CH/A ）', 1, 0);
INSERT INTO `sms_home_recommend_product` VALUES (11, 39, '小米 Xiaomi Book Pro 14 2022 锐龙版 2.8K超清大师屏 高端轻薄笔记本电脑(新R5-6600H标压 16G 512G win11)', 1, 0);
INSERT INTO `sms_home_recommend_product` VALUES (12, 44, '三星（SAMSUNG）500GB SSD固态硬盘 M.2接口(NVMe协议) 980（MZ-V8V500BW）', 1, 0);
INSERT INTO `sms_home_recommend_product` VALUES (13, 43, '万和（Vanward)燃气热水器天然气家用四重防冻直流变频节能全新升级增压水伺服恒温高抗风 JSQ30-565W16【16升】【恒温旗舰款】', 1, 0);
INSERT INTO `sms_home_recommend_product` VALUES (14, 45, 'OPPO Reno8 8GB+128GB 鸢尾紫 新配色上市 80W超级闪充 5000万水光人像三摄 3200万前置索尼镜头 5G手机', 1, 0);

-- ----------------------------
-- Table structure for sms_home_recommend_subject
-- ----------------------------
DROP TABLE IF EXISTS `sms_home_recommend_subject`;
CREATE TABLE `sms_home_recommend_subject`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `subject_id` bigint NULL DEFAULT NULL,
  `subject_name` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `recommend_status` int NULL DEFAULT NULL,
  `sort` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '首页推荐专题表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sms_home_recommend_subject
-- ----------------------------
INSERT INTO `sms_home_recommend_subject` VALUES (14, 1, 'polo衬衫的也时尚', 1, 0);
INSERT INTO `sms_home_recommend_subject` VALUES (15, 2, '大牌手机低价秒', 1, 0);
INSERT INTO `sms_home_recommend_subject` VALUES (16, 3, '晓龙845新品上市', 1, 0);
INSERT INTO `sms_home_recommend_subject` VALUES (17, 4, '夏天应该穿什么', 1, 0);
INSERT INTO `sms_home_recommend_subject` VALUES (18, 5, '夏季精选', 1, 100);
INSERT INTO `sms_home_recommend_subject` VALUES (19, 6, '品牌手机降价', 1, 0);

-- ----------------------------
-- Table structure for sms_seckill_order
-- ----------------------------
DROP TABLE IF EXISTS `sms_seckill_order`;
CREATE TABLE `sms_seckill_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `promotion_id` bigint NOT NULL COMMENT '秒杀活动ID',
  `session_id` bigint NOT NULL COMMENT '秒杀场次ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `order_id` bigint NOT NULL COMMENT '关联订单ID（oms_order.id）',
  `order_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单编号',
  `seckill_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '秒杀价',
  `quantity` int NULL DEFAULT 1 COMMENT '购买数量',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_member_product_promotion`(`member_id` ASC, `product_id` ASC, `promotion_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_promotion_id`(`promotion_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '秒杀订单记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sms_seckill_order
-- ----------------------------

-- ----------------------------
-- Table structure for undo_log
-- ----------------------------
DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `branch_id` bigint NOT NULL COMMENT '分支事务ID',
  `xid` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '全局事务ID',
  `context` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '上下文',
  `rollback_info` longblob NOT NULL COMMENT '回滚信息',
  `log_status` int NOT NULL COMMENT '状态：0正常 1回滚中',
  `log_created` datetime NOT NULL COMMENT '创建时间',
  `log_modified` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `ux_undo_log`(`xid` ASC, `branch_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Seata AT undo_log' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of undo_log
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
