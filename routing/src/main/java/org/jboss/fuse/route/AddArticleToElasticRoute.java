package org.jboss.fuse.route;

import org.apache.camel.Exchange;
import org.jboss.fuse.model.Blog;
import org.jboss.fuse.service.ElasticSearchService;
import org.apache.camel.LoggingLevel;
import org.apache.camel.component.elasticsearch.ElasticsearchConfiguration;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddArticleToElasticRoute extends OnExceptionElasticSearch {

    final static Logger LOG = LoggerFactory.getLogger(ElasticSearchService.class);

    @Override
    public void configure() throws Exception {

        JacksonDataFormat jacksondf = new JacksonDataFormat(Blog.class);
        final String ID = "id";

        from("direct:add").id("add-direct-route")
                .log(LoggingLevel.INFO,"Add new Blog entry service called !")
                
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_NAME).simple("{{indexname}}")
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_TYPE).simple("{{indextype}}")
                .setHeader(ElasticsearchConfiguration.PARAM_OPERATION).constant(ElasticsearchConfiguration.OPERATION_INDEX)
                // Transform Java Blog Object to JSON String. IT will be used as Source Body content to insert the record in ElasticSearch
                .setHeader(ID).simple("${body.id}")
                .marshal(jacksondf)
                
                // Call the add service of the elasticsearchService POJO to generate the IndexRequest object
                .beanRef("elasticSearchService", "add")

                // Call the elasticsearch Service to add/insert an entry within the index
                .to("elasticsearch://{{clustername}}?ip={{address}}")
                .log("Response received : ${body}");
    }
}
