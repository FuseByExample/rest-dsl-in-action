# GPE JBoss Fuse Lab

This JBoss Fuse lab will demonstrate how the new Apache Camel REST DSL syntax can be used to expose REST Services that we will use to add, search or delete
articles for a blog into a nosql Elasticsearch database. The use case has been enriched to expose a file endpoint, as you can see within the following picture, which
is responsible to poll a folder, to consumes csv files and insert all the articles into the database. The Apache Camel Elasticsearch component is called from different routes
and will communicate with the ElasticSearch Database to perform the CRUD operations.

[](fuse-lab1.png)

The Elasticsearch database and the Apache Camel projects will be deployed into different Fuse Managed Containers operated by a JBoss Fuse Fabric Server.

The CRUD services can be accessed using the new Camel REST component and the file component. To do a bulk import of articles, you will create a CSV file containing this
record structure `id,user,blog description,title`. All the records will be uploaded by the file endpoint, transformed using the `Apache Camel Bindy` Dataformat to a collection of Blog objects.
Next, each Blog object will be used as input to issue a request to insert a new record within the Elasticsearch database.

For the REST Service, a JSON article `{ "user": "cmoulliard" "postDate": "2015-12-12", "body": "Integration is hard.", "title": "On distributed search" }` message is expected by the Jetty REST endpoint `/blog/article/id`
The body content will be used as input to also issue a request to the same Service used by the file endpoint to consume CSV records.