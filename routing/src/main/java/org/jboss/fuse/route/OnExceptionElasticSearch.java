package org.jboss.fuse.route;

import org.jboss.fuse.service.ElasticSearchService;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OnExceptionElasticSearch extends RouteBuilder {

    final static Logger LOG = LoggerFactory.getLogger(ElasticSearchService.class);
    
    @Override
    public void configure() throws Exception {

        onException(org.elasticsearch.client.transport.NoNodeAvailableException.class)
             .handled(true)
             .setBody().constant("ElasticSearch server is not available, not started, network issue , ... ")
             .setHeader(Exchange.CONTENT_TYPE).constant("text/plain")
             .setHeader(Exchange.HTTP_RESPONSE_CODE).constant(400)
                .log(">> Exception message : ${exception.message}")
             .log(">> Stack trace : ${exception.stacktrace}");
        
        onException(org.elasticsearch.indices.IndexMissingException.class)
             .handled(true)
             .setBody().constant("The [blog] index is missing into the Elasticsearch Database")
             .setHeader(Exchange.CONTENT_TYPE).constant("text/plain")
             .setHeader(Exchange.HTTP_RESPONSE_CODE).constant(400)
             .log(">> Exception message : ${exception.message}")
             .log(">> Stack trace : ${exception.stacktrace}");

    }
}
