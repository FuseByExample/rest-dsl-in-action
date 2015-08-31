package org.jboss.fuse.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

@CsvRecord(skipFirstLine = true, separator = ",", quote = "\"")
public class Blog {
    
    @DataField(pos = 2) String user;
    @DataField(pos = 3, pattern = "yyyy-MM-dd'T'HH:mm") @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm") Date postDate; // yyyy-MM-dd'T'HH:mm:ss
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

    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date postDate) {
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

    public String convertTime(long time){
        Date date = new Date(time);
        Format format = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa");
        return format.format(date);
    }


}
