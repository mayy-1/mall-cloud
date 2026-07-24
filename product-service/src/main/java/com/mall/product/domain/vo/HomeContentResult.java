package com.mall.product.domain.vo;

import com.mall.api.dto.BrandDTO;
import com.mall.api.dto.HomeFlashPromotionDTO;
import com.mall.api.dto.HomeAdvertiseDTO;
import com.mall.api.dto.ProductDTO;
import com.mall.api.dto.SubjectDTO;
import lombok.Data;

import java.util.List;

/**
 * 首页内容聚合结果
 */
@Data
public class HomeContentResult {
    private List<BrandDTO> brandList;
    private List<ProductDTO> newProductList;
    private List<ProductDTO> hotProductList;
    private List<SubjectDTO> subjectList;
    private HomeFlashPromotionDTO homeFlashPromotion;  // DTO from mall-api
    private List<HomeAdvertiseDTO> advertiseList;
}
