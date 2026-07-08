package com.mall.cart.mapper;

import com.mall.cart.domain.dto.CartProduct;
import org.apache.ibatis.annotations.Param;

/**
 * 前台商品DAO
 * Created by macro on 2018/8/2.
 */
public interface PortalProductMapperCustom {

    /**
     * 获取购物车商品信息（包含属性和SKU）
     */
    CartProduct getCartProduct(@Param("id") Long productId);
}
