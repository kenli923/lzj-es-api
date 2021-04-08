package com.lzj;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lzj.pojo.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class LzjEsApiApplicationTests {

	@Autowired
	@Qualifier("restHighLevelClient")
	private RestHighLevelClient client;

	@Test
	void testCreateIndex() {
		CreateIndexRequest request = new CreateIndexRequest("lzj_index_test");
		try {
			CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
			System.out.println(response.index());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testGetIndex() {
		GetIndexRequest request = new GetIndexRequest("lzj_index_test");
		try {
			if (client.indices().exists(request, RequestOptions.DEFAULT)) {
				GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);
				System.out.println(response.getAliases());
			} else {
				System.out.println(false);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    @Test
    void testIndexExist(){
        GetIndexRequest request = new GetIndexRequest("lzj_index_test");
        try {
            System.out.println(client.indices().exists(request, RequestOptions.DEFAULT));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	@Test
    void testDeleteIndex(){
        DeleteIndexRequest request = new DeleteIndexRequest("lzj_index_test");
        try {
            AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
            System.out.println(response.isAcknowledged());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testAddDocument(){
        User user = new User("张三",18);
        IndexRequest request = new IndexRequest("lzj_index_test");

        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));

        ObjectMapper mapper = new ObjectMapper();

        try {
            String s = mapper.writeValueAsString(user);
            System.out.println(s);
            request.source(s, XContentType.JSON);
            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
            System.out.println(response.status());
            System.out.println(response.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testDocumentExist(){
        GetRequest request = new GetRequest("lzj_index_test","1");

        // 不获取返回的 _source 的上下文
        request.fetchSourceContext(new FetchSourceContext(false));
        request.storedFields("_none_");

        try {
            boolean exists = client.exists(request, RequestOptions.DEFAULT);
            System.out.println(exists);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testGetDocument(){
        GetRequest request = new GetRequest("lzj_index_test","1");

        try {
            boolean exists = client.exists(request, RequestOptions.DEFAULT);
            if(exists){
                GetResponse response = client.get(request, RequestOptions.DEFAULT);
                System.out.println(response.getSource());
                System.out.println(response.getSourceAsString());
                System.out.println(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testUpdateDocument(){
        UpdateRequest request = new UpdateRequest("lzj_index_test","1");
        User user = new User("李四", 29);
        ObjectMapper mapper = new ObjectMapper();
        try {
            request.id("2").doc(mapper.writeValueAsString(user),XContentType.JSON);
            UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
            System.out.println(response.status());
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testBulkDocument() {
        BulkRequest request = new BulkRequest("lzj_index_test");
        ArrayList<User> userList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        for (int i = 1; i <= 10; i++) {
            userList.add(new User("user" + i, i + 18));
        }
        try {
            for (int i = 0; i < userList.size(); i++) {
                request.add(new IndexRequest("lzj_index_test")
                        .id("" + i)
                        .source(mapper.writeValueAsString(userList.get(i)), XContentType.JSON)
                );
            }
            BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
            System.out.println(response.hasFailures());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testSearch() {
        SearchRequest request = new SearchRequest("lzj_index_test");
        // 构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //设置一个match的name=user的搜索条件
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("name","user1");
        searchSourceBuilder.query(matchQueryBuilder);
        searchSourceBuilder.timeout(new TimeValue(30, TimeUnit.SECONDS));

        request.source(searchSourceBuilder);

        try {
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(response.getHits()));
            System.out.println("============================================================");
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
