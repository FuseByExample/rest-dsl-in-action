package com.redhat.gpe.route;

import com.redhat.gpe.model.Blog;
import com.redhat.gpe.service.ElasticSearchService;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestToServicesRoute extends RouteBuilder {

    final static Logger LOG = LoggerFactory.getLogger(ElasticSearchService.class);

    @Override
    public void configure() throws Exception {

        restConfiguration().component("jetty").host("0.0.0.0").port("9191").bindingMode(RestBindingMode.json).dataFormatProperty("prettyPrint", "true");

        rest("/blog/").id("rest-blog-services").produces("application/json").consumes("application/json")
                
                .get("/article/search/id/{id}").id("rest-searchbyid")
                    .to("direct:searchById")

                .get("/article/search/user/{user}").id("rest-searchbyuser").outTypeList(Blog.class)
                   .to("direct:searchByUser")

                .put("/article/{id}").id("rest-put-article").type(Blog.class)
                    .to("direct:add")
                
                .delete("/article/{id}").id("rest-deletearticle").type(Blog.class)
                   .to("direct:remove");

    }
}
