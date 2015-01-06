#! /bin/bash

./scripts/gen.sh

mvn clean compile assembly:single
java -cp target/tools-1.0-jar-with-dependencies.jar me.shenfeng.proxy.Checker -timeout 20 -threads 40 -db 'root@|jdbc:mysql://127.0.0.1:3306/tools'

#java -cp target/tools-1.0-jar-with-dependencies.jar me.shenfeng.proxy.Crawler -db 'root@|jdbc:mysql://127.0.0.1:3306/tools'

#java -cp target/tools-1.0-jar-with-dependencies.jar me.shenfeng.ApiServer -db 'root@|jdbc:mysql://127.0.0.1:3306/tools'

