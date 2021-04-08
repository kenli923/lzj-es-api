package com.lzj.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lzj.pojo.Goods;
import com.lzj.utils.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean addGoods(String keywords) {
        ArrayList<Goods> goodsList = new HtmlParseUtil().getData(keywords);
        if(goodsList == null || goodsList.size() <= 0){
            return false;
        }
        BulkRequest request = new BulkRequest("jd_search_test")
                .timeout(new TimeValue(2, TimeUnit.MINUTES));
        ObjectMapper mapper = new ObjectMapper();
        try {
            for (Goods goods : goodsList) {
                request.add(new IndexRequest("jd_search_test")
                        .source(mapper.writeValueAsString(goods), XContentType.JSON)
                );
            }
            BulkResponse responses = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
            return !responses.hasFailures();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getGoodsList(String keywords, int currentPage, int pageSize) {
        if (currentPage < 1) {
            currentPage = 1;
        }
        if (pageSize < 1) {
            pageSize = 20;
        }
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        try {
            SearchRequest request = new SearchRequest("jd_search_test");

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("title", keywords);
            searchSourceBuilder.query(matchQueryBuilder)
                    .timeout(new TimeValue(10, TimeUnit.SECONDS))
                    .from((currentPage - 1) * pageSize)
                    .size(pageSize)
            ;
            request.source(searchSourceBuilder);
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            for (SearchHit hit : response.getHits().getHits()) {
                results.add(hit.getSourceAsMap());
            }
            return results;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> getGoodsListHighlight(String keywords, int currentPage, int pageSize) {
        if (currentPage < 1) {
            currentPage = 1;
        }
        if (pageSize < 1) {
            pageSize = 20;
        }
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        try {
            SearchRequest request = new SearchRequest("jd_search_test");

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("title", keywords);
            searchSourceBuilder.highlighter(new HighlightBuilder()
                    .field("title")//需要高亮的字段
                    .preTags("<span style='color:red;'>")//高亮字段前置文本
                    .postTags("</span>")//高亮字段后置文本
                    .requireFieldMatch(true)//多个高亮显示
            );
            searchSourceBuilder.query(matchQueryBuilder)
                    .timeout(new TimeValue(10, TimeUnit.SECONDS))
                    .from((currentPage - 1) * pageSize)
                    .size(pageSize)
            ;
            request.source(searchSourceBuilder);
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                HighlightField title = highlightFields.get("title");
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                if(title!=null){
                    Text[] fragments = title.fragments();
                    String n_title = "";
                    for (Text text : fragments) {
                        n_title += text;
                    }
                    sourceAsMap.put("title",n_title);
                }

                results.add(sourceAsMap);
            }
            return results;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
