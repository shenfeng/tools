#!/bin/sh

mvn clean compile assembly:single

scp target/tools-1.0-jar-with-dependencies.jar vpn:~/tools

scp target/tools-1.0-jar-with-dependencies.jar techwolf-ks-01:/data/nfetcher_bak/downloader