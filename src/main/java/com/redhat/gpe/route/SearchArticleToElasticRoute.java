package com.redhat.gpe.route;

import com.redhat.gpe.service.ElasticSearchService;
import org.apache.camel.Exchange;
import org.apache.camel.component.elasticsearch.ElasticsearchConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchArticleToElasticRoute extends OnExceptionRoute {

    final static Logger LOG = LoggerFactory.getLogger(ElasticSearchService.class);

    @Override
    public void configure() throws Exception {

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
                        .setBody().simple("No article has been retrieved from the ES DB for this id ${header.id}.")
                        .setHeader(Exchange.CONTENT_TYPE).constant("text/plain")
                .endChoice();                


        from("direct:searchByUser").id("searchbyuser-direct-route")
                .log("Search articles by user Service called !")
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_NAME).simple("{{indexname}}")
                .setHeader(ElasticsearchConfiguration.PARAM_INDEX_TYPE).simple("{{indextype}}")
                .beanRef("elasticSearchService", "getBlogs")
                .choice()
                    .when().simple("${body.isEmpty} == 'true'")
                        .setBody().simple("No articles have been retrieved from the ES DB for this user ${header.user}.")
                        .setHeader(Exchange.CONTENT_TYPE).constant("text/plain")
                .endChoice();

        from("direct:searchByUser2").id("searchbyuser2-direct-route")
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
