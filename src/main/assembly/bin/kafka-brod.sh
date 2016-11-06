#!/usr/bin/env bash

if [[ "${KB_HOME}" == "" ]]; then
  KB_HOME=$(cd $(dirname $0) && cd .. && pwd)
fi

KB_CLASSPATH="${KB_HOME}/lib/*"
KB_CLASS=com.github.gquintana.kafka.brod.Main

KB_JAVA="java"
if [[ "${JAVA_HOME}" != "" ]]; then
  KB_JAVA="${JAVA_HOME}/bin/java"
fi

if [[ "${KB_CONFIG}" == "" ]]; then
  KB_CONFIG="${KB_HOME}/config/brod.properties"
fi

if [[ "${KB_LOG_CONFIG}" == "" ]]; then
  KB_LOG_CONFIG="${KB_HOME}/config/log4j.xml"
fi

if [[ "${KB_HEAP}" == "" ]]; then
  KB_HEAP="512m"
fi

KB_JAVA_OPTS="-Xmx${KB_HEAP} -Xms${KB_HEAP} -Dlog4.configuration=${KB_LOG_CONFIG}"

${KB_JAVA} ${KB_JAVA_OPTS} -cp "${KB_CLASSPATH}" ${KB_CLASS} ${KB_CONFIG}
