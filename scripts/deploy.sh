#! /bin/bash

# set -u                          # Treat unset variables as an error
# set -e #  Exit immediately if a simple command exits with a non-zero status



READLINK=readlink
if which greadlink > /dev/null; then
    READLINK=greadlink
fi

ABSOLUTE_PATH=$(${READLINK} -f $0)
REMOTE_SERVERS="all"


log() {
    echo "`date "+%Y-%m-%d %H:%M:%S"` INFO -- $@"
}
function remote_run {
    server=$1
    shift
    command=$@
    log "exec $server in $command"
    ssh "$server" "$command"
}

function deploy_to_remote() {
    server=$1
    dir='~/tools'
    remote_run ${server} "mkdir -p ${dir}"

    cp target/tools-1.0.jar .
    cp -r target/lib .

    rsync --rsh='ssh' -vr --delete --delete-excluded --exclude-from conf/exclude.txt . "${server}:${dir}"
    rm -rf tools-1.0.jar
    rm -rf lib
}

usage () {
    cat <<-EOF
USAGE:
deploy [options] [command]

Options:
  -s --server       remote server to deploy [all]

command:
    deploy
    build
    run
    kill
EOF
}

ALL_SERVERS=("fetcher1" "fetcher2" "fetcher3" "fetcher4" "dev01" "vpn")
ALL_SERVERS=("fetcher4" "fetcher5" "vpn")
ALL_SERVERS=("aliyun9" "aliyun8" "gfw")

function deploy() {
    build

    if [ "all" = ${REMOTE_SERVERS} ]; then
        for server in "${ALL_SERVERS[@]}"; do
            deploy_to_remote ${server}
        done
    else
        deploy_to_remote ${REMOTE_SERVERS}
    fi
}

function run_in_remote() {
    if [ "all" = ${REMOTE_SERVERS} ]; then
        for server in "${ALL_SERVERS[@]}"; do
            remote_run ${server} $@
        done
    else
        remote_run ${REMOTE_SERVERS} $@
    fi
}

function install_packages() {
    remote_run ${REMOTE_SERVERS} "apt-get update && apt-get install openjdk-7-jdk -y"
    deploy
}

function build() {
    ./scripts/gen.sh
    rm -rf target
    mvn package
}

while test $# -ne 0; do
    arg=$1; shift
    case ${arg} in
        -h|--help) usage; exit;;
        -s|--server) REMOTE_SERVERS=$1; shift;;
        build) build; exit;;
        deploy) deploy ; exit;;
        kill) run_in_remote 'cat ~/dfetcher/pid.pid | xargs kill' ; exit;;
        run) run_in_remote '~/dfetcher/remote_run.sh'; exit;;
        install) install_packages; exit;;
        *) usage; exit;;
    esac
done

# reach hear, no args is given
usage

