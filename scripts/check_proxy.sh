#! /bin/bash

./scripts/gen.sh

mvn clean compile assembly:single
java -cp target/tools-1.0-jar-with-dependencies.jar me.shenfeng.ProxyCheck -timeout 20 -threads 20
