package com.redhat.gpe.route;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redhat.gpe.model.Blog;
import org.apache.camel.*;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.PlainActionFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchService {

    final static Logger LOG = LoggerFactory.getLogger(ElasticSearchService.class);

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