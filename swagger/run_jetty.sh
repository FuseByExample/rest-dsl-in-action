#!/usr/bin/env bash

# rm -rf src/main/webapp/*
mvn clean
mvn package
mvn install

mvn jetty:run

