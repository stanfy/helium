#!/usr/bin/env bash

# Helium start script for Unix.
#
# Respects the following optional env variables:
# - HELIUM_VERSION - version of Helium to use
# - HELIUM_HOME - directory used to store Helium executables
# - JAVA_HOME - to determine what Java to use
# - JAVA_OPTS - to pass Java options (like heap and stack size) when needed
#
# Use --refresh option to update a snapshot version.

if [ -z "$HELIUM_VERSION" ]; then
    HELIUM_VERSION="0.8.3-SNAPSHOT"
fi

if [ -z "$HELIUM_HOME" ]; then
    HELIUM_HOME="$HOME/.helium"
fi

HELIUM_JAR="$HELIUM_HOME/command-line-$HELIUM_VERSION-nodeps.jar"

FIRST_ARG=$1
if [ "$FIRST_ARG" == "--refresh" ]; then
    ARGS=${@:2}
    FORCE_DOWNLOAD=1
else
    ARGS=$@
    FORCE_DOWNLOAD=0
fi

warn ( ) {
    echo "$*"
}

die ( ) {
    echo
    echo "$*"
    echo
    exit 1
}

# Ensure Helium jar exists.
if [ ! -f $HELIUM_JAR ] || [ $FORCE_DOWNLOAD == 1 ] ; then
    mkdir -p $HELIUM_HOME &2>/dev/null
    NEXUS_QUERY="g=com.stanfy.helium&a=command-line&v=$HELIUM_VERSION&c=nodeps"
    NEXUS_ENDPOINT="service/local/artifact/maven/content"
    if [[ $HELIUM_VERSION == *"-SNAPSHOT" ]] ; then
        DOWNLOAD_URL="https://oss.sonatype.org/$NEXUS_ENDPOINT?r=snapshots&$NEXUS_QUERY"
    else
        DOWNLOAD_URL="https://repository.sonatype.org/$NEXUS_ENDPOINT?r=central-proxy&$NEXUS_QUERY"
    fi
    echo "Downloading Helium from $DOWNLOAD_URL"

    CURLCMD="curl -sLf $DOWNLOAD_URL -o $HELIUM_JAR"
    $CURLCMD || ( rm $HELIUM_JAR &2>/dev/null; die "Cannot download Helium jar using $CURLCMD" ) || exit 1

    echo "Download successful"
    echo
fi

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Run
$JAVACMD $JAVA_OPTS -jar $HELIUM_JAR $ARGS
