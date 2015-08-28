# GPE JBoss Fuse Lab

This JBoss Fuse lab will demonstrate how the new Apache Camel REST DSL syntax can be used to design a CRUD service.
An Elasticsearch NoSQL database will be deployed into a Fuse Managed Container and the database will be populate with Blog JSon documents.
The CRUD service will allow to add, delete or search for articles published. The search function allow to retrieve a user using its id, name.

The CRUD services can be accessed using the new Camel REST component and the file component. To do a bulk import of articles, you will create a CSV file containing this
record structure `id,user,blog description,title`. All the records will be uploaded by the file endpoint, transformed using the `Apache Camel Bindy` Dataformat to a collection of Blog objects.
Next, each Blog object will be used as input to issue a request to insert a new record within the Elasticsearch database.

For the REST Service, a JSON article `{ "user": "cmoulliard" "postDate": "2015-12-12", "body": "Integration is hard.", "title": "On distributed search" }` message is expected by the Jetty REST endpoint `/blog/article/id` 
The body content will be used as input to also issue a request to the same Service used by the file endpoint to consume CSV records.. 

# Prerequisites

- [JBoss Fuse 6.2.GA](http://www.jboss.org/download-manager/file/jboss-fuse-6.2.0.GA-full_zip.zip)
- [JBoss Developer Studio](http://www.jboss.org/download-manager/file/jboss-devstudio-8.1.0.GA-standalone_jar.jar)
- [httpie](https://github.com/jkbrzt/httpie) 
- [curl](http://curl.haxx.se/download.html) (optional)
- JDK 1.7
- Apache Maven 3.x

# Getting started

Follow these instructions to install and configure JBoss Developer Studio 8.1.0.GA and JBoss Fuse 6.2.GA - https://www.jboss.org/products/fuse/get-started/

# Installation

In order to use the lab, we have to perform these steps :
 
 - [Download](https://github.com/gpe-mw-training/fuse-lab-emea-2015/archive/master.zip) and compile the project locally
 - Create a Fuse Fabric Server
 - Create 2 child containers; one will be used as the Elasticsearch Database and the other for the project itself where the Apache Camel Routes will be deployed.

Open a Windows or Unix Terminal and move to the installation directory of the JBoss Fuse 6.2 - GA distribution directory.
Run this command to launch first the JBoss Fuse Server and next within the Fuse Karaf console to issue the commands responsible to setup the environment.

````
$JBOSS_FUSE_INSTALL/bin/fuse

Please wait while JBoss Fuse is loading...
100% [========================================================================]

      _ ____                  ______
     | |  _ \                |  ____|
     | | |_) | ___  ___ ___  | |__ _   _ ___  ___
 _   | |  _ < / _ \/ __/ __| |  __| | | / __|/ _ \
| |__| | |_) | (_) \__ \__ \ | |  | |_| \__ \  __/
 \____/|____/ \___/|___/___/ |_|   \__,_|___/\___|

  JBoss Fuse (6.2.0.redhat-133)
  http://www.redhat.com/products/jbossenterprisemiddleware/fuse/

Hit '<tab>' for a list of available commands
and '[cmd] --help' for help on a specific command.

Open a browser to http://localhost:8181 to access the management console

Create a new Fabric via 'fabric:create'
or join an existing Fabric via 'fabric:join [someUrls]'

Hit '<ctrl-d>' or 'osgi:shutdown' to shutdown JBoss Fuse.

JBossFuse:karaf@root>shell:source mvn:com.redhat.gpe/fuse-lab/1.0/script/creation
Waiting for container: root
Waiting for container root to provision.

Creating new instance on SSH port 8102 and RMI ports 1100/44445 at: /Users/chmoulli/Fuse/Fuse-servers/jboss-fuse-6.2.0.redhat-133/instances/elasticsearch-node
The following containers have been created successfully:
	Container: elasticsearch-node.
Creating new instance on SSH port 8103 and RMI ports 1101/44446 at: /Users/chmoulli/Fuse/Fuse-servers/jboss-fuse-6.2.0.redhat-133/instances/lab
The following containers have been created successfully:
	Container: lab.
````

After a few moments, the environment will be ready. You can verify/control that the 2 containers are running

```
JBossFuse:karaf@root>fabric:container-list
[id]                 [version]  [type]  [connected]  [profiles]                       [provision status]
root*                 1.0        karaf   yes          fabric                           success
                                                      fabric-ensemble-0000-1
                                                      jboss-fuse-full
  elasticsearch-node  1.0        karaf   yes          insight-elasticsearch.datastore  success
  lab                 1.0        karaf   yes          feature-camel                    success
```

Open a second Windows or Unix terminal. Unzip the material of the lab and move to that directory `fuse-lab-emea-2015-master`

Build your maven project locally and deploy it to the JBoss Fuse Fabric Server

```
cd fuse-lab-emea-2015-master
mvn clean install
mvn fabric8:deploy
```

Now that the profile has been created and published on JBoss Fuse, we will install it into the Fuse Lab Managed container

```
JBosFuse:karaf@root>fabric:container-add-profile lab gpe-fuse
```

Remarks : 

If you change th code of this lab, then redeploy if after doing a maven installation and running this command `mvn fabric8:deploy`.
Next, the profile can updated on the container using theses commands

```
fabric:container-remove-profile lab gpe-fuse
fabric:container-add-profile lab gpe-fuse
```

If, for any reason, you would like to restart the lab from the beginning. Then, exist from the JBoss Fuse Console using the command `CTRL-D` or `osgi:shutdown` 
and run this script to clean and kill the jvm instances `./bin/deletefabric8`

# Minimal installation

You can also run the project locally using `mvn camel:run` at the condition that the Karaf feature `insight-elasticsearch` has been deployed into JBoss Fuse 6.2.
Additional OSGI parameters could be defined forthe elasticsearch database using the `io.fabric8.elasticsearch-insight.cfg` file deployed into the `etc` folder of JBoss Fuse.
That should also work if you deploy locally an Elasticsearch instance but this feature hasn't been tested.

# Play with the lab

Open a Windows or Unix Terminal and issue one of the following HTTP requests using curl or httpie tool within the lab project folder

## Add a user

Before to issue the HTTP GET request, you can change the content of the Blog Article that you will publish

````
http PUT http://127.0.0.1:9191/blog/article/1 < src/data/entry.json
````

## Search a user

````
http http://127.0.0.1:9191/blog/article/search/user/cmoulliard
````

## Search a user using its ID

````
http http://127.0.0.1:9191/blog/article/search/d/1
````

## Delete a user

````
http DELETE http://127.0.0.1:9191/blog/article/1
````

## All HTTPie requests

When you test your project, you can copy/paste this list of HTTPie queries to play with the CRUD scenario

````
http PUT http://127.0.0.1:9191/blog/article/1 < src/data/entry.json
http PUT http://127.0.0.1:9191/blog/article/2 < src/data/entry.json
http PUT http://127.0.0.1:9191/blog/article/3 < src/data/entry.json

http http://127.0.0.1:9191/blog/article/search/id/1
http http://127.0.0.1:9191/blog/article/search/id/4

http http://127.0.0.1:9191/blog/article/search/user/cmoulliard
http http://127.0.0.1:9191/blog/article/search/user/cmoullia

http DELETE http://127.0.0.1:9191/blog/article/1
http http://127.0.0.1:9191/blog/article/search/id/1

Using Servlet instead of Jetty

http http://127.0.0.1:8183/rest/blog/article/search/id/1
````

## Troubleshooting

- When the local Camel REST endpoints don't work, you can query directly the elasticsearch database using these HTTPie requests to check if it work.

  Remark : The hostname must be changed depending if you run locally or remotely the JBoss Fuse Server

  ```
  http http://127.0.0.1:9191/blog/post/1 pretty==true
  http http://192.168.1.80:9200/blog/post/_search q=="user:cmoulliard" pretty==true
  
  curl 'http://192.168.1.80:9200/blog/post/_search?q=user:cmoulliard&pretty=true'
  ```  
 

- Delete all entries

   http DELETE http://192.168.1.80:9200/blog
   
- Create Index

   http PUT http://192.168.1.80:9200/blog

- Add mapping
  
   http PUT http://192.168.1.80:9200/blog/_mapping/article < src/data/mapping.json
   
- Check mapping
   
   http http://192.168.1.80:9200/blog/_mapping/article
   
- Add user
   
   http PUT http://192.168.1.80:9200/blog/article/1 < src/data/entry.json
   
- Query
   
   http http://192.168.1.80:9200/blog/post/_search pretty==true < src/data/query.json 
   
- All together
   
```
http DELETE http://fusehost:9200/blog
http PUT http://fusehost:9200/blog
http PUT http://fusehost:9200/blog/_mapping/article < src/data/mapping.json
http http://fusehost:9200/blog/_mapping/article

http PUT http://fusehost:9200/blog/article/1 < src/data/entry.json
http PUT http://fusehost:9200/blog/article/2 < src/data/entry.json
http PUT http://fusehost:9200/blog/article/3 < src/data/entry.json
http PUT http://fusehost:9200/blog/article/4 < src/data/entry.json
http PUT http://fusehost:9200/blog/article/5 < src/data/entry.json
http PUT http://fusehost:9200/blog/article/6 < src/data/entry.json
http PUT http://fusehost:9200/blog/article/7 < src/data/entry.json
http PUT http://fusehost:9200/blog/article/8 < src/data/entry.json
http PUT http://fusehost:9200/blog/article/9 < src/data/entry.json
http PUT http://fusehost:9200/blog/article/10 < src/data/entry.json

http http://fusehost:9200/blog/article/1
http http://fusehost:9200/blog/article/2

```

- Create kibana_index, add dashboard & search about it

```
Delete and recreate kibana-int index

http DELETE http://fusehost:9200/kibana-int
http PUT http://fusehost:9200/kibana-int

Add fuse-lab dashboard

http PUT http://fusehost:9200/kibana-int/dashboard/fuse-lab < src/data/dashboard.json

Delete dashboard

http DELETE http://fusehost:9200/kibana-int/dashboard/fuse-lab

Export existing kibana dashboard from ES to a file

http http://fusehost:9200/kibana-int/dashboard/fuse-lab/_source > fuse-lab.json

Get Dashboards

http http://fusehost:9200/_search q=="dashboard:*"
http http://fusehost:9200/kibana-int/_search q=="title:fuse-lab" pretty==true
HTTP/1.1 200 OK
Content-Length: 4595
Content-Type: application/json; charset=UTF-8

{
    "_shards": {
        "failed": 0,
        "successful": 5,
        "total": 5
    },
    "hits": {
        "hits": [
            {
                "_id": "fuse-lab",
                "_index": "kibana-int",
                "_score": 0.2712221,
                "_source": {
                    "dashboard": "{\"title\":\"fuse-lab\",\"services\":{\"query\":{\"list\":{\"0\":{\"query\":\"*\",\"alias\":\"\",\"color\":\"#7EB26D\",\"id\":0,\"pin\":false,\"type\":\"lucene\",\"enable\":true}},\"ids\":[0]},\"filter\":{\"list\":{},\"ids\":[]}},\"rows\":[{\"title\":\"Options\",\"height\":\"50px\",\"editable\":true,\"collapse\":false,\"collapsable\":true,\"panels\":[{\"span\":8,\"editable\":true,\"type\":\"histogram\",\"loadingEditor\":false,\"mode\":\"count\",\"time_field\":\"postDate\",\"value_field\":null,\"x-axis\":true,\"y-axis\":true,\"scale\":1,\"y_format\":\"none\",\"grid\":{\"max\":null,\"min\":0},\"queries\":{\"mode\":\"all\",\"ids\":[0]},\"annotate\":{\"enable\":false,\"query\":\"*\",\"size\":20,\"field\":\"_type\",\"sort\":[\"_score\",\"desc\"]},\"auto_int\":true,\"resolution\":100,\"interval\":\"1y\",\"intervals\":[\"auto\",\"1s\",\"1m\",\"5m\",\"10m\",\"30m\",\"1h\",\"3h\",\"12h\",\"1d\",\"1w\",\"1y\"],\"lines\":false,\"fill\":0,\"linewidth\":3,\"points\":false,\"pointradius\":5,\"bars\":true,\"stack\":true,\"spyable\":true,\"zoomlinks\":true,\"options\":true,\"legend\":true,\"show_query\":true,\"interactive\":true,\"legend_counts\":true,\"timezone\":\"browser\",\"percentage\":false,\"zerofill\":true,\"derivative\":false,\"tooltip\":{\"value_type\":\"cumulative\",\"query_as_alias\":true},\"title\":\"Articles\"}],\"notice\":false},{\"title\":\"Graph\",\"height\":\"250px\",\"editable\":true,\"collapse\":false,\"collapsable\":true,\"panels\":[{\"error\":false,\"span\":3,\"editable\":true,\"group\":[\"default\"],\"type\":\"terms\",\"queries\":{\"mode\":\"all\",\"ids\":[0]},\"field\":\"user\",\"exclude\":[],\"missing\":true,\"other\":true,\"size\":100,\"order\":\"count\",\"style\":{\"font-size\":\"10pt\"},\"donut\":false,\"tilt\":false,\"labels\":true,\"arrangement\":\"horizontal\",\"chart\":\"pie\",\"counter_pos\":\"none\",\"title\":\"Document types\",\"spyable\":true,\"tmode\":\"terms\",\"tstat\":\"total\",\"valuefield\":\"\"},{\"error\":false,\"span\":3,\"editable\":true,\"group\":[\"default\"],\"type\":\"terms\",\"queries\":{\"mode\":\"all\",\"ids\":[0]},\"field\":\"_type\",\"exclude\":[],\"missing\":true,\"other\":true,\"size\":10,\"order\":\"count\",\"style\":{\"font-size\":\"10pt\"},\"donut\":false,\"tilt\":false,\"labels\":true,\"arrangement\":\"horizontal\",\"chart\":\"table\",\"counter_pos\":\"above\",\"spyable\":true,\"title\":\"Document Types\",\"tmode\":\"terms\",\"tstat\":\"total\",\"valuefield\":\"\"}],\"notice\":false},{\"title\":\"Events\",\"height\":\"650px\",\"editable\":true,\"collapse\":false,\"collapsable\":true,\"panels\":[{\"error\":false,\"span\":12,\"editable\":true,\"group\":[\"default\"],\"type\":\"table\",\"size\":100,\"pages\":5,\"offset\":0,\"sort\":[\"_score\",\"desc\"],\"style\":{\"font-size\":\"9pt\"},\"overflow\":\"min-height\",\"fields\":[],\"highlight\":[],\"sortable\":true,\"header\":true,\"paging\":true,\"spyable\":true,\"queries\":{\"mode\":\"all\",\"ids\":[0]},\"field_list\":true,\"status\":\"Stable\",\"trimFactor\":300,\"normTimes\":true,\"title\":\"Documents\",\"all_fields\":false,\"localTime\":false,\"timeField\":\"@timestamp\"}],\"notice\":false}],\"editable\":true,\"index\":{\"interval\":\"none\",\"pattern\":\"[logstash-]YYYY.MM.DD\",\"default\":\"blog\",\"warm_fields\":false},\"style\":\"dark\",\"failover\":false,\"panel_hints\":true,\"loader\":{\"save_gist\":false,\"save_elasticsearch\":true,\"save_local\":true,\"save_default\":true,\"save_temp\":true,\"save_temp_ttl_enable\":true,\"save_temp_ttl\":\"30d\",\"load_gist\":true,\"load_elasticsearch\":true,\"load_elasticsearch_size\":20,\"load_local\":true,\"hide\":false},\"pulldowns\":[{\"type\":\"query\",\"collapse\":false,\"notice\":false,\"query\":\"*\",\"pinned\":true,\"history\":[],\"remember\":10,\"enable\":true},{\"type\":\"filtering\",\"collapse\":true,\"notice\":false,\"enable\":true}],\"nav\":[{\"type\":\"timepicker\",\"collapse\":false,\"notice\":false,\"status\":\"Stable\",\"time_options\":[\"5m\",\"15m\",\"1h\",\"6h\",\"12h\",\"24h\",\"2d\",\"7d\",\"30d\"],\"refresh_intervals\":[\"5s\",\"10s\",\"30s\",\"1m\",\"5m\",\"15m\",\"30m\",\"1h\",\"2h\",\"1d\"],\"timefield\":\"postDate\",\"enable\":true}],\"refresh\":false}",
                    "group": "guest",
                    "title": "fuse-lab",
                    "user": "guest"
                },
                "_type": "dashboard"
            }
        ],
        "max_score": 0.2712221,
        "total": 1
    },
    "timed_out": false,
    "took": 2
}
```

- Backup and restore

https://www.elastic.co/guide/en/elasticsearch/reference/1.3/modules-snapshots.html
http:/chrissimpson.co.uk/elasticsearch-snapshot-restore-api.html
