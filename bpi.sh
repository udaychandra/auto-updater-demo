#!/bin/sh

BPI_SHELL_DIR=`pwd`

start() {
    CURRENT_VERSION=$(<"${BPI_SHELL_DIR}/version.txt")
    chmod -R a+x ${BPI_SHELL_DIR}/${CURRENT_VERSION}
    cd ${BPI_SHELL_DIR}/${CURRENT_VERSION}/bin
    ./bpi
}

start
EXIT_STATUS=$?

if [ ${EXIT_STATUS} -eq 100 ]; then
    start
fi
