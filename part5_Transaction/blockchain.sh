#!/bin/bash

set -e

# Check if the jar has been built.
if [ ! -e target/part5_Transaction-jar-with-dependencies.jar ]; then
  echo "Compiling blockchain project to a JAR"
  mvn package -DskipTests
fi

java -jar target/part5_Transaction-jar-with-dependencies.jar "$@"

