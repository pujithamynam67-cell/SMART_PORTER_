#!/bin/sh
set -e

# Default to port 10000 on Render or 8080 locally if PORT is not passed
PORT="${PORT:-10000}"

echo "[SmartPorter] Setting Tomcat HTTP port to $PORT..."
if [ -f /usr/local/tomcat/conf/server.xml ]; then
    sed -i "s/port=\"8080\"/port=\"$PORT\"/g" /usr/local/tomcat/conf/server.xml
fi

echo "[SmartPorter] Launching Apache Tomcat on port $PORT..."
exec catalina.sh run
