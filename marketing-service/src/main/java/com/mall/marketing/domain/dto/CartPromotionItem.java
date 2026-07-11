package com.mall.marketing.domain.dto;

import java.math.BigDecimal;

/**
 * 购物车促销商品项 DTO
 */
public class CartPromotionItem {
    private Long productId;
    private Long productCategoryId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal reduceAmount;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getProductCategoryId() { return productCategoryId; }
    public void setProductCategoryId(Long productCategoryId) { this.productCategoryId = productCategoryId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getReduceAmount() { return reduceAmount; }
    public void setReduceAmount(BigDecimal reduceAmount) { this.reduceAmount = reduceAmount; }
}
