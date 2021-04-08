package com.lzj.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lzj.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    @GetMapping("/setData/{keyword}")
    public String setData(@PathVariable("keyword") String keyword){
        if(goodsService.addGoods(keyword)){
            return "true";
        }
        return "false";
    }

    @GetMapping({"/search/{keyword}/{currentPage}/{pageSize}","/search/{keyword}"})
    public List<Map<String,Object>> search(@PathVariable("keyword") String keyword,
                         @PathVariable(value = "currentPage",required = false) Integer currentPage,
                         @PathVariable(value = "pageSize", required = false) Integer pageSize) {
        if (currentPage == null) {
            currentPage = 1;
        }
        if (pageSize == null) {
            pageSize = 20;
        }
        return goodsService.getGoodsListHighlight(keyword, currentPage, pageSize);
        //return goodsService.getGoodsList(keyword,currentPage,pageSize);
    }
}
