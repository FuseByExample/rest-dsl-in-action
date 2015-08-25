package com.redhat.gpe.route;

import com.redhat.gpe.model.Blog;
import org.apache.camel.CamelContext;
import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.model.rest.RestBindingMode;

public class ElasticSearchService extends RouteBuilder {

    // @PropertyInject("clustername")
    // String clustername;
    
    @Override
    public void configure() throws Exception {
        
        JacksonDataFormat jacksonFormat = new JacksonDataFormat(Blog.class);

        restConfiguration().component("jetty").host("0.0.0.0").port("9191").bindingMode(RestBindingMode.json);

        rest("/entries/")
            .post("new").consumes("application/json").type(Blog.class)
            .to("direct:add");

        from("direct:add")
            .marshal(jacksonFormat)
            .to("elasticsearch://{{clustername}}?operation=INDEX&ip={{address}}&indexName={{indexname}}&indexType={{indextype}}")
            .log("Response received : ${body}");
        
    }
}
