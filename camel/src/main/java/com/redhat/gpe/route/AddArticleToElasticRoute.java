package com.redhat.gpe.route;

import com.redhat.gpe.model.Blog;
import com.redhat.gpe.service.ElasticSearchService;
import org.apache.camel.component.elasticsearch.ElasticsearchConfiguration;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddArticleToElasticRoute extends OnExceptionElasticSearch {

    final static Logger LOG = LoggerFactory.getLogger(ElasticSearchService.class);

    @Override
    public void configure() throws Exception {

        JacksonDataFormat jacksondf = new JacksonDataFormat(Blog.class);

        from("direct:add").id("add-direct-route")
                .log("Add new Blog entry service called !")
                
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_NAME).simple("{{indexname}}")
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_TYPE).simple("{{indextype}}")
                .setHeader(ElasticsearchConfiguration.PARAM_OPERATION).constant(ElasticsearchConfiguration.OPERATION_INDEX)

                // Transform Java Object to JSON
                .marshal(jacksondf)
                
                .beanRef("elasticSearchService", "add")

                .to("elasticsearch://{{clustername}}?ip={{address}}")
                .log("Response received : ${body}");
    }
}
