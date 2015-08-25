package com.redhat.gpe.route;

import com.google.gson.Gson;
import com.redhat.gpe.model.Blog;
import org.apache.camel.*;
import org.elasticsearch.action.index.IndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchService {

    final static Logger LOG = LoggerFactory.getLogger(ElasticSearchService.class);
    
    public IndexRequest addEntry(@Body Blog body,
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
    
}