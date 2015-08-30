package com.redhat.gpe.route;

import com.redhat.gpe.model.Blog;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.model.dataformat.CsvDataFormat;

import java.util.List;

public class FileToAddServiceRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        BindyCsvDataFormat csv = new BindyCsvDataFormat(Blog.class);
        
        from("{{fileUri}}").id(("file-marshal-split-service"))
            .log(LoggingLevel.DEBUG,"Records received : ${body}")
            .unmarshal(csv)
            .split(body())
                .setHeader("id").simple("${body.id}")
                .to("direct:add");

    }
}
