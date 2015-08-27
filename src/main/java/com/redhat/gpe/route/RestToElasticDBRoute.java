package com.redhat.gpe.route;

import com.redhat.gpe.model.Blog;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.elasticsearch.ElasticsearchConfiguration;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestToElasticDBRoute extends RouteBuilder {

    final static Logger LOG = LoggerFactory.getLogger(ElasticSearchService.class);

    @Override
    public void configure() throws Exception {

        JacksonDataFormat jacksonFormat = new JacksonDataFormat(Blog.class);

        restConfiguration().component("jetty").host("0.0.0.0").port("9191").bindingMode(RestBindingMode.json).dataFormatProperty("prettyPrint", "true");

        rest("/blog/").produces("application/json").consumes("application/json")
                
                .get("/article/search/id/{id}").id("rest-searchbyid")
                    .to("direct:searchById")

                .get("/article/search/user/{user}").id("rest-searchbyuser").outTypeList(Blog.class)
                   .to("direct:searchByUser")

                .put("/article/{id}").id("rest-put-article").type(Blog.class)
                    .to("direct:add")
                
                .delete("/article/{id}").id("rest-deletearticle").type(Blog.class)
                   .to("direct:remove");
        
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

        from("direct:remove").id("remove-direct-route")
                .log("Remove a Blog entry service called !")
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_NAME).simple("{{indexname}}")
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_TYPE).simple("{{indextype}}")

                // We will search for the ID of the Blog Article
                .setHeader(ElasticsearchConfiguration.PARAM_OPERATION).constant(ElasticsearchConfiguration.OPERATION_GET_BY_ID)
                .setBody().simple("${header.id}")
                .to("elasticsearch://{{clustername}}?ip={{address}}")
                .beanRef("elasticSearchService", "getBlog")
                
                .choice()
                    .when()
                       // If No article has been retrieved, we generate a message for the HTTP Client
                       .simple("${body} == null")
                       .setBody().constant("No article has been retrieved from the ES DB.")
                       .setHeader(Exchange.CONTENT_TYPE).constant("text/plain")
                       .setHeader(Exchange.HTTP_RESPONSE_CODE).constant(200)
                       .endChoice()
                    .otherwise()
                       // We will delete now the article if a result has been retrieved
                       .setHeader(ElasticsearchConfiguration.PARAM_OPERATION).constant(ElasticsearchConfiguration.OPERATION_DELETE)
                       .beanRef("elasticSearchService", "remove")
                       .to("elasticsearch://{{clustername}}?ip={{address}}");
        

        from("direct:add").id("add-direct-route")
                .log("Add new Blog entry service called !")

                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_NAME).simple("{{indexname}}")
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_TYPE).simple("{{indextype}}")
                .setHeader(ElasticsearchConfiguration.PARAM_OPERATION).constant(ElasticsearchConfiguration.OPERATION_INDEX)

                .beanRef("elasticSearchService", "add")

                .to("elasticsearch://{{clustername}}?ip={{address}}")
                .log("Response received : ${body}");

        from("direct:searchById").id("searchbyid-direct-route")
                .log("Search article by ID Service called !")
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_NAME).simple("{{indexname}}")
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_TYPE).simple("{{indextype}}")
                .setHeader(ElasticsearchConfiguration.PARAM_OPERATION).constant(ElasticsearchConfiguration.OPERATION_GET_BY_ID)

                .setBody().simple("${header.id}")

                .to("elasticsearch://{{clustername}}?ip={{address}}")
                .beanRef("elasticSearchService", "getBlog")
                .choice()
                    .when().simple("${body} == null")
                        .setBody().constant("No article has been retrieved from the ES DB.")
                        .setHeader(Exchange.CONTENT_TYPE).constant("text/plain")
                .endChoice();                


        from("direct:searchByUser").id("searchbyuser-direct-route")
                .log("Search articles by user Service called !")
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_NAME).simple("{{indexname}}")
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_TYPE).simple("{{indextype}}")
                .beanRef("elasticSearchService", "getBlogs");

        from("direct:searchByUser2")
                .log("Search Blogs Service called !")
                .setHeader(Exchange.HTTP_QUERY, constant("q=user:cmoulliard&pretty=true"))
                .setHeader(Exchange.HTTP_PATH, constant("/blog/post/_search"))
                .to("http4:{{address}}:{{port}}/?bridgeEndpoint=true")
                .beanRef("elasticSearchService", "getBlogs2");
        
        /* 
         *  Code works with Camel 2.16 as SEARCH operation wasn't implemented for 2.15
         *
        from("direct:search")
                .log("Search Blogs Service called !")
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_NAME).simple("{{indexname}}")
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_TYPE).simple("{{indextype}}")
                .setHeader(ElasticsearchConfiguration.PARAM_OPERATION).constant("SEARCH")

                .beanRef("elasticSearchService", "searchUser")

                .doTry()
                  .to("elasticsearch://{{clustername}}?ip={{address}}")
                  .beanRef("elasticSearchService", "generateUsersResponse")
                .doCatch(org.elasticsearch.client.transport.NoNodeAvailableException.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getIn().setBody("ElasticSearch server is not available, not started, network issue , ... !");
                        exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "text/plain");
                        exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
                    }
                })
                .endDoTry();
         */


    }
}
