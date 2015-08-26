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
                
                .get("/searchby-id/{id}")
                    .to("direct:findbyid")
                
                .get("/searchby-user/{user}").outTypeList(Blog.class)
                     .to("direct:search2")
                
                .put("/add/{id}")
                    .type(Blog.class)
                    .to("direct:new");
        
        onException(org.elasticsearch.client.transport.NoNodeAvailableException.class)
             .handled(true)
             .setBody().constant("ElasticSearch server is not available, not started, network issue , ... ")
             .setHeader(Exchange.CONTENT_TYPE).constant("text/plain")
             .setHeader(Exchange.HTTP_RESPONSE_CODE).constant(400)
             .log(">> Exception message : ${exception.message}")
             .log(">> Stack trace : ${exception.stacktrace}");


        from("direct:new")
                .log("Add new Blog entry service called !")
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_NAME).simple("{{indexname}}")
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_TYPE).simple("{{indextype}}")
                .setHeader(ElasticsearchConfiguration.PARAM_OPERATION).constant(ElasticsearchConfiguration.OPERATION_INDEX)

                .beanRef("elasticSearchService", "add")

                .to("elasticsearch://{{clustername}}?ip={{address}}")
                .log("Response received : ${body}");

        from("direct:findbyid")
                .log("Find By ID Service called !")
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_NAME).simple("{{indexname}}")
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_TYPE).simple("{{indextype}}")
                .setHeader(ElasticsearchConfiguration.PARAM_OPERATION).constant(ElasticsearchConfiguration.OPERATION_GET_BY_ID)

                .beanRef("elasticSearchService", "findById")

                .doTry()
                .to("elasticsearch://{{clustername}}?ip={{address}}")
                    .beanRef("elasticSearchService", "generateResponse")
                    .unmarshal(jacksonFormat)
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

        from("direct:search")
                .log("Search Blogs Service called !")
                .setHeader(Exchange.HTTP_QUERY, constant("q=user:cmoulliard&pretty=true"))
                .setHeader(Exchange.HTTP_PATH, constant("/blog/post/_search"))
                .to("http4:{{address}}:{{port}}/?bridgeEndpoint=true")
                .beanRef("elasticSearchService", "getBlogs");

        from("direct:search2")
                .log("Search Blogs Service called !")
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_NAME).simple("{{indexname}}")
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_TYPE).simple("{{indextype}}")
                .beanRef("elasticSearchService", "getBlogs2");

        /* Code working with Camel 2.16 */
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
