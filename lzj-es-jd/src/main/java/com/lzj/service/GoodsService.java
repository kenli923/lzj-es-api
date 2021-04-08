package com.lzj.service;

import com.lzj.pojo.Goods;

import java.util.List;
import java.util.Map;

public interface GoodsService {
    public boolean addGoods(String keywords);

    public List<Map<String, Object>> getGoodsList(String keywords, int currentPage, int pageSize);

    public List<Map<String, Object>> getGoodsListHighlight(String keywords, int currentPage, int pageSize);
}
