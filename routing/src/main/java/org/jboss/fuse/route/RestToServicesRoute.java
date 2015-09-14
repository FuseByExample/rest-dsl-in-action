package org.jboss.fuse.route;

import org.jboss.fuse.model.Blog;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

public class RestToServicesRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        restConfiguration().component("netty-http").host("0.0.0.0").port("9191")
           .enableCORS(true)
           .bindingMode(RestBindingMode.json)
           .dataFormatProperty("prettyPrint", "true")
           .endpointProperty("matchOnUriPrefix", "true"); ;

        rest("/blog/").id("rest-blog-service").produces("application/json").consumes("application/json")

           .get("/article/search/id/{id}").id("rest-searchbyid")
               .to("direct:searchById")

           .get("/article/search/user/{user}").id("rest-searchbyuser").outTypeList(Blog.class)
                .to("direct:searchByUser")

           .put("/article/").id("rest-put-article").type(Blog.class)
                .to("direct:add")

           .delete("/article/{id}").id("rest-deletearticle").type(Blog.class)
               .to("direct:remove");

           /*
            * Workaround to support HTTP OPTIONS request required for Swagger API when CORS is enabled
            * The Allow Header reports the operations supported.
            * A new verb has been added - https://issues.apache.org/jira/browse/CAMEL-9129 but it will be available for 2.16 or 2.15.x
            */
           rest("/").id("rest-options")
             .verb("options","/blog/article")
             .route()
             .setHeader("Access-Control-Allow-Origin", constant("*"))
             .setHeader("Access-Control-Allow-Methods", constant("GET, HEAD, POST, PUT, DELETE, OPTIONS"))
             .setHeader("Access-Control-Allow-Headers", constant("Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers"))
             .setHeader("Allow", constant("GET, HEAD, POST, PUT, DELETE, OPTIONS"));

    }
}
