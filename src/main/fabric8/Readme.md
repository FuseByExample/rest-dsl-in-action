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

JBossFuse:karaf@root>shell:source mvn:com.redhat.gpe/fuse-lab/1.0/script/installer

````
# HTTPie request

## ADD A user
````
  http PUT http://127.0.0.1:9191/blog/article/1 < src/data/entry.json
````
## SEARCH
````
  curl 'http://192.168.1.80:9200/blog/post/_search?q=user:cmoulliard&pretty=true'
  or
  http http://192.168.1.80:9200/blog/post/_search q=="user:cmoulliard" pretty==true

  http http://127.0.0.1:9191/blog/article/search/user/cmoulliard
````
## GET A USER
````
  http http://127.0.0.1:9191/blog/post/1 pretty==true

  http http://127.0.0.1:9191/blog/article/search/id/1
````
