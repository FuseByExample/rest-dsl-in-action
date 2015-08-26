# GPE JBoss Fuse Lab

This is a GPE JBoss Fuse Lab

# Installation

  fabric:create --clean root
  
  fabric:container-create-child --profile insight-elasticsearch.datastore root elasticsearch-node
  fabric:container-create-child --profile feature-camel root lab
  
  fabric:container-add-profile lab gpe-fuse
  fabric:container-remove-profile lab gpe-fuse

# HTTPie request

## ADD A user

  http PUT http://127.0.0.1:9191/entries/new/1 < src/data/entry.json

## SEARCH

  curl 'http://192.168.1.80:9200/blog/post/_search?q=user:cmoulliard&pretty=true'
  or
  http http://192.168.1.80:9200/blog/post/_search q=="user:cmoulliard" pretty==true

  http http://127.0.0.1:9191/entries/searchuser/cmoulliard

## GET A USER

  http http://127.0.0.1:9191/blog/post/1 pretty==true

  http http://127.0.0.1:9191/entries/searchid/1

