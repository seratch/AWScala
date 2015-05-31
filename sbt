#!/bin/bash

SBT_VERSION=0.13.8
sbtjar=sbt-launch.jar

set -e

# If sbtjar is present it is assumed to be ok
function CHECK_SBT {
  if [ ! -f $sbtjar ]; then
    SBT_URL=http://typesafe.artifactoryonline.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/$SBT_VERSION
    echo "Downloading $sbtjar v$SBT_VERSION" 1>&2
    curl -O $SBT_URL/$sbtjar

    MD5=$(curl -s $SBT_URL/jars/sbt-launch.jar.md5)
    test -f $sbtjar || exit 1
    sbtjar_md5=$(openssl md5 < $sbtjar|cut -f2 -d'='|awk '{print $1}')
    if [ "${sbtjar_md5}" != $MD5 ]; then
      echo "$sbtjar MD5 mismatch. This script expects SBT v$SBT_VERSION. If you have a different version, delete $sbtjar and try again." 1>&2
      exit -1
    fi
  fi
}

# Not sure why this is here, perhaps it should be deleted:
root=$(
  cd $(dirname $(readlink $0 || echo $0))/..
  /bin/pwd
)

CHECK_SBT

test -f ~/.sbtconfig && . ~/.sbtconfig

java -ea                          \
  $SBT_OPTS                       \
  $JAVA_OPTS                      \
  -Djava.net.preferIPv4Stack=true \
  -XX:+AggressiveOpts             \
  -XX:+UseParNewGC                \
  -XX:+UseConcMarkSweepGC         \
  -XX:+CMSParallelRemarkEnabled   \
  -XX:+CMSClassUnloadingEnabled   \
  -XX:MaxPermSize=1024m           \
  -XX:SurvivorRatio=128           \
  -XX:MaxTenuringThreshold=0      \
  -XX:ReservedCodeCacheSize=128m  \
  -Xss8M                          \
  -Xms512M                        \
  -Xmx1G                          \
  -server                         \
  -jar $sbtjar "$@"

