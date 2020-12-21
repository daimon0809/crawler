package com.github.hcsp;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

public class ElasticsearchDataGenerator {
    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "db/mybatis/mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException();
        }

        List<News> newsFromMySql = getNewsFromMySql(sqlSessionFactory);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> writeSingleThread(newsFromMySql)).start();
            writeSingleThread(newsFromMySql);
        }
    }

    private static void writeSingleThread(List<News> newsFromMySql) {
        try (RestHighLevelClient client =
                     new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            BulkRequest bulkRequest = new BulkRequest();
            for (int i = 0; i < 1000; i++) {
                for (News news : newsFromMySql) {
                    IndexRequest request = new IndexRequest("news");

                    Map<String, Object> data = new HashMap<>();
                    data.put("content", news.getContent());
                    data.put("url", news.getUrl());
                    data.put("title", news.getTitle());
                    data.put("createdAt", news.getCreateAt());
                    data.put("modifiedAt", news.getModifiedAt());

                    request.source(data, XContentType.JSON);

                    bulkRequest.add(request);
                }
                BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                System.out.println("current thread" + Thread.currentThread().getName() + " finishes" + i + ": " + bulkResponse.status().getStatus());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<News> getNewsFromMySql(SqlSessionFactory sqlSessionFactory) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectList("com.github.hcsp.MockMapper.selectNews");
        }
    }
}
