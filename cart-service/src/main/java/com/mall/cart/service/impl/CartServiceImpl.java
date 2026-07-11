package com.mall.cart.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.mall.api.client.MemberClient;
import com.mall.api.client.PromotionClient;
import com.mall.api.dto.CartItemDTO;
import com.mall.api.dto.CartPromotionItemDTO;
import com.mall.api.dto.MemberDTO;
import com.mall.cart.domain.dto.CartProduct;
import com.mall.cart.mapper.OmsCartItemMapper;
import com.mall.cart.mapper.PortalProductMapper;
import com.mall.cart.model.OmsCartItem;
import com.mall.cart.service.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车管理Service实现类
 * Created by macro on 2018/8/2.
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {

    /** 购物车Mapper */
    private final OmsCartItemMapper cartItemMapper;
    /** 前台商品Mapper */
    private final PortalProductMapper productMapper;
    /** 促销服务Feign调用 */
    private final PromotionClient promotionClient;
    /** 会员服务Feign调用 */
    private final MemberClient memberClient;

    @Override
    public int add(OmsCartItem cartItem) {
        int count;
        MemberDTO currentMember = memberClient.getCurrentMember().getData();
        cartItem.setMemberId(currentMember.getId());
        cartItem.setMemberNickname(currentMember.getNickname());
        cartItem.setDeleteStatus(0);
        OmsCartItem existCartItem = getCartItem(cartItem);
        if (existCartItem == null) {
            cartItem.setCreateDate(new Date());
            count = cartItemMapper.insert(cartItem);
        } else {
            cartItem.setModifyDate(new Date());
            existCartItem.setQuantity(existCartItem.getQuantity() + cartItem.getQuantity());
            count = cartItemMapper.updateByPrimaryKey(existCartItem);
        }
        return count;
    }

    /**
     * 根据会员id,商品id和规格获取购物车中商品
     */
    private OmsCartItem getCartItem(OmsCartItem cartItem) {
        OmsCartItem condition = new OmsCartItem();
        condition.setMemberId(cartItem.getMemberId());
        condition.setProductId(cartItem.getProductId());
        condition.setDeleteStatus(0);
        if (cartItem.getProductSkuId() != null) {
            condition.setProductSkuId(cartItem.getProductSkuId());
        }
        List<OmsCartItem> cartItemList = cartItemMapper.selectByCondition(condition);
        if (!CollectionUtils.isEmpty(cartItemList)) {
            return cartItemList.get(0);
        }
        return null;
    }

    @Override
    public List<OmsCartItem> list(Long memberId) {
        OmsCartItem condition = new OmsCartItem();
        condition.setDeleteStatus(0);
        condition.setMemberId(memberId);
        return cartItemMapper.selectByCondition(condition);
    }

    @Override
    public List<CartPromotionItemDTO> listPromotion(Long memberId, List<Long> cartIds) {
        List<OmsCartItem> cartItemList = list(memberId);
        if(CollUtil.isNotEmpty(cartIds)){
            cartItemList = cartItemList.stream().filter(item->cartIds.contains(item.getId())).collect(Collectors.toList());
        }
        List<CartPromotionItemDTO> cartPromotionItemList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(cartItemList)){
            List<CartItemDTO> cartItemDTOList = cartItemList.stream().map(item -> {
                CartItemDTO dto = new CartItemDTO();
                BeanUtil.copyProperties(item, dto);
                return dto;
            }).collect(Collectors.toList());
            cartPromotionItemList = promotionClient.calcCartPromotion(cartItemDTOList).getData();
        }
        return cartPromotionItemList;
    }

    @Override
    public int updateQuantity(Long id, Long memberId, Integer quantity) {
        OmsCartItem record = new OmsCartItem();
        record.setQuantity(quantity);
        OmsCartItem condition = new OmsCartItem();
        condition.setDeleteStatus(0);
        condition.setId(id);
        condition.setMemberId(memberId);
        return cartItemMapper.updateSelectiveByCondition(record, condition);
    }

    @Override
    public int delete(Long memberId, List<Long> ids) {
        // 使用 selectByCondition + 逐条删除代替 updateSelectiveByCondition + andIdIn
        OmsCartItem record = new OmsCartItem();
        record.setDeleteStatus(1);
        for (Long id : ids) {
            OmsCartItem condition = new OmsCartItem();
            condition.setId(id);
            condition.setMemberId(memberId);
            cartItemMapper.updateSelectiveByCondition(record, condition);
        }
        return ids.size();
    }

    @Override
    public CartProduct getCartProduct(Long productId) {
        return productMapper.getCartProduct(productId);
    }

    @Override
    public int updateAttr(OmsCartItem cartItem) {
        //删除原购物车信息
        OmsCartItem updateCart = new OmsCartItem();
        updateCart.setId(cartItem.getId());
        updateCart.setModifyDate(new Date());
        updateCart.setDeleteStatus(1);
        cartItemMapper.updateByPrimaryKeySelective(updateCart);
        cartItem.setId(null);
        add(cartItem);
        return 1;
    }

    @Override
    public int clear(Long memberId) {
        OmsCartItem record = new OmsCartItem();
        record.setDeleteStatus(1);
        OmsCartItem condition = new OmsCartItem();
        condition.setMemberId(memberId);
        return cartItemMapper.updateSelectiveByCondition(record, condition);
    }
}
