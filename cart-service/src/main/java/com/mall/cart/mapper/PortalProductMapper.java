package com.mall.cart.mapper;

import com.mall.cart.domain.dto.CartProduct;
import org.apache.ibatis.annotations.Param;

/**
 * 前台商品DAO
 */
public interface PortalProductMapper {

    /**
     * 获取购物车商品信息（包含属性和SKU）
     */
    CartProduct getCartProduct(@Param("id") Long productId);
}
