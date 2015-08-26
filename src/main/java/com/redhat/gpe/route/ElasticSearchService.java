package com.redhat.gpe.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redhat.gpe.model.Blog;
import org.apache.camel.*;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.PlainActionFuture;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.io.stream.InputStreamStreamInput;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ElasticSearchService {

    final static Logger LOG = LoggerFactory.getLogger(ElasticSearchService.class);
    Client client;
    
    public void init() {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", "insight")
                .put("client.transport.sniff", true)
                .build();
        client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress("192.168.1.80", 9300));
    }
    
    public void shutdown() {
        client.close();
    }

    public IndexRequest add(@Body Blog body,
                            @Header("indexname") String indexname,
                            @Header("indextype") String indextype,
                            @Header("id") String id) {

        String source = new Gson().toJson(body);
        LOG.info("Id : " + id + ", indexname : " + indexname + ", indextype : " + indextype);
        LOG.info("Source : " + source);

        IndexRequest req = new IndexRequest(indexname, indextype, id);
        req.source(source);
        return req;
    }

    public String findById(@Header("id") String id) {
        return id;
    }

    public String generateResponse(@Body PlainActionFuture future) throws Exception {
        GetResponse getResponse = (GetResponse) future.get();
        String response = getResponse.getSourceAsString();
        if (response == null) {
            LOG.info("No result found for the id - " + getResponse.getId());
            response = emptyFieldsJson("user","title","body","postDate");
        }
        return response;
    }
    
    public List<Blog> getBlogs(@Body String result) throws Exception {

        List<Blog> blogs = new ArrayList<Blog>();
        LOG.info("Result received : " + result);
        
        /*InputStream is = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
        InputStreamStreamInput stream = new InputStreamStreamInput(is);
        SearchResponse searchResponse = SearchResponse.readSearchResponse(stream);*/

/*        XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON);
        builder.startObject();
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.toXContent(builder, ToXContent.EMPTY_PARAMS);
        builder.endObject();

        if (searchResponse.getHits().totalHits() != 0) {
            SearchHits sHits = searchResponse.getHits();
            SearchHit[] results = sHits.hits();
            for(SearchHit hit : results) {
                LOG.info("Result : " + hit.getSourceAsString());
                Blog blog = new ObjectMapper().readValue( hit.getSourceAsString(), Blog.class);
                blogs.add(blog);
            }
        }*/

        JSONObject json = new JSONObject(result);
        
        JSONObject hits = json.getJSONObject("hits");
        Integer total = (Integer) hits.get("total");
        
        JSONArray results = hits.getJSONArray("hits");
        for (int i = 0; i < results.length(); i++) {
            JSONObject source = results.getJSONObject(i).getJSONObject("_source");
            String id = (String) results.getJSONObject(i).get("_id");
            Blog blog = new ObjectMapper().readValue( source.toString(), Blog.class);
            blog.setId(id);
            blogs.add(blog);
        }
        return blogs;
    }

    public List<Blog> getBlogs2(@Header("user") String user,
                                @Header("indexname") String indexname,
                                @Header("indextype") String indextype) throws Exception {

        List<Blog> blogs = new ArrayList<Blog>();

        SearchResponse response = client.prepareSearch(indexname)
                .setTypes(indextype)
                .setQuery(QueryBuilders.termQuery("user",user))
                .setFrom(0).setSize(60).setExplain(true)
                .execute()
                .actionGet();

        long totalHits = response.getHits().getTotalHits();
        if (totalHits == 0) {
            LOG.info("No result found for the search request");
        } else {
            SearchHits searchHits = response.getHits();
            SearchHit[] hits = searchHits.hits();
            for(SearchHit searchHit : hits) {
                LOG.info("Result : " + searchHit.getSourceAsString());
                Blog blog = new ObjectMapper().readValue( searchHit.getSourceAsString(), Blog.class);
                blog.setId(searchHit.getId());
                blogs.add(blog);
            }
        }
        
        return blogs;
    }
    
    /*
    public SearchRequest searchUser(@Header("user") String user,
                         @Header("indexname") String indexname,
                         @Header("indextype") String indextype) {
        
        SearchRequest request = new SearchRequest(indexname);
        request.types(indextype);
        request.searchType(SearchType.DFS_QUERY_THEN_FETCH);
        request.source("{\"query\":{\"match\":{\"user\":\"" + user + "\"}}}");
        
        return request;
    }

    public String generateUsersResponse(@Body PlainActionFuture future) throws Exception {
        SearchResponse searchResponse = (SearchResponse) future.get();
        long hits = searchResponse.getHits().getTotalHits();
        if (hits == 0) {
            LOG.info("No result found for the search request");
        } else {
            SearchHits sHits = searchResponse.getHits();
            SearchHit[] results = sHits.hits();
            for(SearchHit result : results) {
                LOG.info("Result : " + result.getSourceAsString());
            }
        }
        return "";
    }
    */

    /**
     * Generate JSON String using fields passed as parameter and assign the content to an empty string*
     */
    public static String emptyFieldsJson(String... fields) {

        final String DQ = "\"";
        final String DQCOLONDQ = "\": \"";
        final String DQCOMA = "\", ";
        final String PREFIX = "{";
        final String SUFFIX = "}";

        StringBuffer buffer = new StringBuffer();
        buffer.append(PREFIX);

        for (String field : fields) {
            buffer.append(DQ);
            buffer.append(field);
            buffer.append(DQCOLONDQ);
            buffer.append(DQCOMA);
        }
        
        // Remove last coma added
        buffer.setLength(buffer.length() - 2);

        buffer.append(SUFFIX);
        return buffer.toString();
    }

    /**
     * Convert JSON string to pretty print version
     *
     * @param jsonString
     * @return
     */
    public static String toPrettyFormat(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }

}