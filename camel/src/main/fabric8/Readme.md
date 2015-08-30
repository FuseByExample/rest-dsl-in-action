# GPE JBoss Fuse Lab

This JBoss Fuse lab will demonstrate how the new Apache Camel REST DSL syntax can be used to design a CRUD service.
An Elasticsearch NoSQL database will be deployed into a Fuse Managed Container and the database will be populate with Blog JSon documents.
The CRUD service will allow to add, delete or search for articles published. The search function allow to retrieve a user using its id, name.

The CRUD services can be accessed using the new Camel REST component and the file component. To do a bulk import of articles, you will create a CSV file containing this
record structure `id,user,blog description,title`. All the records will be uploaded by the file endpoint, transformed using the `Apache Camel Bindy` Dataformat to a collection of Blog objects.
Next, each Blog object will be used as input to issue a request to insert a new record within the Elasticsearch database.

For the REST Service, a JSON article `{ "user": "cmoulliard" "postDate": "2015-12-12", "body": "Integration is hard.", "title": "On distributed search" }` message is expected by the Jetty REST endpoint `/blog/article/id` 
The body content will be used as input to also issue a request to the same Service used by the file endpoint to consume CSV records.. 