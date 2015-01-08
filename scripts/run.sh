#!/bin/sh

READLINK=readlink
if which greadlink > /dev/null; then
    READLINK=greadlink
fi

TOOLS=$(${READLINK} -f ~/tools)
CP="$TOOLS/tools-1.0.jar:$TOOLS/lib/*"

usage () {
    cat <<-EOF
USAGE:
run [options] [command]

Options:
  -h -help       print this help and exit

command:
    checker
    crawler
    server
    downloader
EOF
}

run() {
    main=$1
    shift
    java -cp $CP $main $@
}

while test $# -ne 0; do
    arg=$1; shift
    case ${arg} in
        -h|--help) usage; exit;;
        checker) run me.shenfeng.proxy.Checker $@; exit;;
        crawler) run me.shenfeng.proxy.Crawler $@ ; exit;;
        server) run me.shenfeng.ApiServer $@ ; exit;;
        downloader) run me.shenfeng.download.Downloader $@; exit;;
        *) usage; exit;;
    esac
done



