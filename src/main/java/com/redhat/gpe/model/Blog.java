package com.redhat.gpe.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

@CsvRecord(skipFirstLine = true, separator = ",", quote = "\"")
public class Blog {
    
    @DataField(pos = 2) String user;
    @DataField(pos = 3) String postDate;
    @DataField(pos = 4) String body;
    @DataField(pos = 5) String title;

    @DataField(pos = 1) @JsonProperty(required = false)
    String id;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}
