package com.kkz.gmall.item.service;

import java.util.HashMap;

public interface ItemService {
    /**
     * 获取商品详情
     * @param skuId
     * @return
     */
    HashMap<String, Object> getItem(Long skuId);
}
