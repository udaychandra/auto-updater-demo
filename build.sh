#!/bin/sh

if [[ -z "${JAVA_HOME}" ]]; then
    echo "Please set JAVA_HOME environment variable"
    exit 1
fi

JAVA_LINK=$JAVA_HOME/bin/jlink

if [[ -z "${BPI_CLI_VERSION}" ]]; then
    BPI_CLI_VERSION=1
    echo "Using default BPI_CLI_VERSION: ${BPI_CLI_VERSION}"
fi

BPI_DIR=build/bpi

# Build the jars
./gradlew clean build copyToLib

# Prepare bpi distribution layout
mkdir ${BPI_DIR}

cat <<EOF >${BPI_DIR}/version.txt
${BPI_CLI_VERSION}
EOF

cp bpi.sh ${BPI_DIR}/bpi
chmod a+x ${BPI_DIR}/bpi

${JAVA_LINK} --module-path build/libs/:${JAVA_HOME}/jmods \
      --add-modules ud.bpi.cli,org.glassfish.java.json \
      --launcher bpi=ud.bpi.cli/ud.bpi.cli.Launcher \
      --output ${BPI_DIR}/${BPI_CLI_VERSION}

GREEN='\033[0;32m'
NC='\033[0m'

echo "\n${GREEN}BPI CLI custom runtime image built${NC}\n"
