#!/bin/bash
# Start the process inside a docker container with java installed
# Some settings can be overriden by environment variables as shown below

ulimit -n 65536


export SERVICENAME=soundwave-worker
export JAVA_MAIN=com.pinterest.cmp.soundwave.WorkerMain
LOG4J_CONFIG_FILE=${LOG4J_CONFIG_FILE:=config/log4j.properties}
CONFIG_FILE=${CONFIG_FILE:=config/soundwaveworker.properties}
HEAP_SIZE= ${HEAP_SIZE:=512m}
NEW_SIZE= ${NEW_SIZE:=256m}

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
export PARENT_DIR="$(dirname $DIR)"

LOG_DIR=/var/log/soundwave-worker
CP=${PARENT_DIR}:${PARENT_DIR}/*:${PARENT_DIR}/lib/*

java -server -Xmx${HEAP_SIZE} -Xms${HEAP_SIZE} -XX:NewSize=${NEW_SIZE} -XX:MaxNewSize=${NEW_SIZE} \
-verbosegc -Xloggc:${LOG_DIR}/gc.log \
-XX:+UnlockDiagnosticVMOptions -XX:ParGCCardsPerStrideChunk=4096 \
-XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=100 -XX:GCLogFileSize=2M \
-XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintClassHistogram \
-XX:+HeapDumpOnOutOfMemoryError -XX:+UseParNewGC \
-XX:OnOutOfMemoryError="kill -9 %p" \
-XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=60 -XX:+UseCMSInitiatingOccupancyOnly \
-XX:ErrorFile=${LOG_DIR}/jvm_error.log \
-cp ${CP} -Dlog4j.configuration=${LOG4J_CONFIG_FILE} -Dconfig.file=${CONFIG_FILE} \
-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=10102 \
-Dfile.encoding=UTF-8 \
${JAVA_MAIN}
