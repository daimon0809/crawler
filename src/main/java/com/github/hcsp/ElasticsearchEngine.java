package com.github.hcsp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class ElasticsearchEngine {
    public static void main(String[] args) throws IOException {
        while (true) {
            System.out.println("pls input a search keywords:");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            String keyWord = reader.readLine();

            search(keyWord);
        }
    }

    private static void search(String keyWord) throws IOException {
        try (RestHighLevelClient client =
                     new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            SearchRequest request = new SearchRequest("news");
            request.source(new SearchSourceBuilder().query(new MultiMatchQueryBuilder(keyWord, "content", "title")));
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            response.getHits().forEach(hit -> System.out.println(hit.getSourceAsString()));
        }
    }
}
