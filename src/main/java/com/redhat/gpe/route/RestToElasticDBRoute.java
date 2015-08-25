package com.redhat.gpe.route;

import com.google.gson.Gson;
import com.redhat.gpe.model.Blog;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.elasticsearch.ElasticsearchConfiguration;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.elasticsearch.action.index.IndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestToElasticDBRoute
        extends RouteBuilder {

    final static Logger LOG = LoggerFactory.getLogger(ElasticSearchService.class);

    @Override
    public void configure() throws Exception {

        JacksonDataFormat jacksonFormat = new JacksonDataFormat(Blog.class);

        restConfiguration().component("jetty").host("0.0.0.0").port("9191").bindingMode(RestBindingMode.json);

        rest("/entries/")
                .get("/{id}")
                   .to("direct:findbyid")
                .put("/new/{id}").consumes("application/json").type(Blog.class)
                   .to("direct:new");


        from("direct:new")
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_NAME).simple("{{indexname}}")
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_TYPE).simple("{{indextype}}")
                .setHeader(ElasticsearchConfiguration.OPERATION_INDEX).constant("INDEX")
                
                .bean(ElasticSearchService.class, "addEntry")
                
                .to("elasticsearch://{{clustername}}?ip={{address}}")
                        //.to("elasticsearch://{{clustername}}?operation=INDEX&ip={{address}}&indexName={{indexname}}&indexType={{indextype}}")
                .log("Response received : ${body}");

        from("direct:findbyid")
                .to("elasticsearch://{{clustername}}?operation=GET_BY_ID&ip={{address}}&indexName={{indexname}}&indexType={{indextype}}")
                .log("Response received : ${body}");

    }
}
