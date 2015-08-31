package org.jboss.fuse.route;

import org.jboss.fuse.model.Blog;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.elasticsearch.client.transport.NoNodeAvailableException;

public class FileToAddServiceRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        BindyCsvDataFormat csv = new BindyCsvDataFormat(Blog.class);
        
        from("{{fileUri}}").id(("file-marshal-split-service"))
            .onException(NoNodeAvailableException.class).maximumRedeliveries(2).to("direct://error").handled(true).end()
            .log(LoggingLevel.DEBUG, "Records received : ${body}")
            .unmarshal(csv)
            .split(body())
                .setHeader("id").simple("${body.id}")
                .to("direct:add");
        
        from("direct://error")
           .log("No node Elasticsearch server is available");

    }
}
