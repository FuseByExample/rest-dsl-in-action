package org.jboss.fuse.route;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.jboss.fuse.model.Blog;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

public class RestToServicesRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet")
           .enableCORS(true)
           .bindingMode(RestBindingMode.json)
           .dataFormatProperty("prettyPrint", "true");

        rest("/blog/article/").id("rest-blog-service").description("Blog Article REST Endpoint").produces("application/json").consumes("application/json")

           .get("search/id/{id}").description("Search for a blog article / id").id("rest-searchbyid")
               .to("direct:searchById")

           .get("search/user/{user}").description("Search for a blog article / user").id("rest-searchbyuser").outTypeList(Blog.class)
                .to("direct:searchByUser")

           .put().id("rest-put-article").description("Put a blog article").type(Blog.class)
                .to("direct:add")

           .delete("{id}").id("rest-deletearticle").description("Delete a blog article").type(Blog.class)
               .to("direct:remove");

           /*
            * Workaround to support HTTP OPTIONS request required for Swagger API when CORS is enabled
            * The Allow Header reports the operations supported.
            * A new verb has been added - https://issues.apache.org/jira/browse/CAMEL-9129 but it will be available for 2.16 or 2.15.x
            * */
           rest("/blog/article").id("rest-options")
             .verb("options").route()
             .setHeader("Access-Control-Allow-Origin", constant("*"))
             .setHeader("Access-Control-Allow-Methods", constant("GET, HEAD, POST, PUT, DELETE, OPTIONS"))
             .setHeader("Access-Control-Allow-Headers", constant("Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers"))
             .setHeader("Allow", constant("GET, HEAD, POST, PUT, DELETE, OPTIONS"));
             
    }
}
