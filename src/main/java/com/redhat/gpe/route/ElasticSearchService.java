package com.redhat.gpe.route;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.redhat.gpe.model.Blog;
import org.apache.camel.*;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.PlainActionFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

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
        GetResponse response = (GetResponse) future.get();
        String result = response.getSourceAsString().replace("\\", "");
/*        ObjectMapper mapper = new ObjectMapper();
        String output = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);*/
        return result;
    }

}