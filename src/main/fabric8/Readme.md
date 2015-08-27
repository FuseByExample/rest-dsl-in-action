# GPE JBoss Fuse Lab

This is a GPE JBoss Fuse Lab

# Installation

In order to use the lab, we have to create a Fuse Fabric Server and next 2 child containers. One will be used as the Elasticsearch Database
repository and the other for the project itself where the Apache Camel Routes will be deployed.

Open a Windows or Unix Terminal and move to the installation directory of the JBoss Fuse 6.2 - GA distribution directory
Run this command to launch first the JBoss Fuse Server and next within the Fuse Karaf console to issue the commands responsible to setup the environement

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

Build your maven project locally and deploy it to the JBoss Fuse Fabric Server

```
mvn clean install
mvn fabric8:deploy
```

Now that the profile has been created and published on JBoss Fuse, we will install it into the Fuse Lab Managed container and watch the profile

```
JBosFuse:karaf@root>fabric:container-add-profile lab gpe-fuse
fabric:profile-refresh gpe-fuse
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
  
    Request to call Elasticsearch directly
    curl 'http://192.168.1.80:9200/blog/post/_search?q=user:cmoulliard&pretty=true'
    or
    http http://192.168.1.80:9200/blog/post/_search q=="user:cmoulliard" pretty==true
````

## Search a user using its ID

````
  http http://127.0.0.1:9191/blog/article/search/id/1
  
  Request to call Elasticsearch directly
  
  http http://127.0.0.1:9191/blog/post/1 pretty==true
````

## Delete a user

````
http DELETE http://127.0.0.1:9191/blog/article/1
````

## All requests

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
````

