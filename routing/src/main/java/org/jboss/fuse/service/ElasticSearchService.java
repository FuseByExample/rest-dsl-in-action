package org.jboss.fuse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.fuse.model.Blog;
import org.apache.camel.Body;
import org.apache.camel.Header;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.PlainActionFuture;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ElasticSearchService {

    final static Logger LOG = LoggerFactory.getLogger(ElasticSearchService.class);
    Client client;
    
    public void init() {
        Settings settings = ImmutableSettings.settingsBuilder()
                .classLoader(Settings.class.getClassLoader())
                .put("cluster.name", "insight")
                .put("client.transport.sniff", false)
                .build();
        client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress("localhost",9300));
    }
    
    public void shutdown() {
        client.close();
    }

    public IndexRequest add(@Body String blog,
                            @Header("indexname") String indexname,
                            @Header("indextype") String indextype,
                            @Header("id") String id) throws IOException {
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Id : " + id + ", indexname : " + indexname + ", indextype : " + indextype);
            LOG.debug("Source : " + blog);
        }

        IndexRequest req = new IndexRequest(indexname, indextype, id);
        req.source(blog);
        return req;
    }

    public DeleteRequest remove(@Body Blog body,
                               @Header("indexname") String indexname,
                               @Header("indextype") String indextype,
                               @Header("id") String id) {

        DeleteRequest deleteRequest = new DeleteRequest(indexname,indextype,id);
        return deleteRequest;
    }

    public Blog getBlog(@Body PlainActionFuture future) throws Exception {
        Blog blog = null;
        GetResponse getResponse = (GetResponse) future.get();
        String response = getResponse.getSourceAsString();
        if (response != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm"));
            blog = objectMapper.readValue(response, Blog.class);
            blog.setId(getResponse.getId());
        }
        return blog;
    }

    public List<Blog> getBlogs(@Header("user") String user,
                               @Header("indexname") String indexname,
                               @Header("indextype") String indextype) throws Exception {

        List<Blog> blogs = new ArrayList<Blog>();

        SearchResponse response = client.prepareSearch(indexname)
                .setTypes(indextype)
                .setQuery(QueryBuilders.termQuery("user", user))
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

    public List<Blog> getBlogs2(@Body String result) throws Exception {

        List<Blog> blogs = new ArrayList<Blog>();
        LOG.info("Result received : " + result);

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
    
    /*
     *  Code works with Camel 2.16 as SEARCH operation wasn't implemented for 2.15
     *
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
     */

    /**
     * Convert JSON string to pretty print version
     *
     * @param jsonString
     * @return
     
    public static String toPrettyFormat(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }
    */

}