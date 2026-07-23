#!/bin/bash

# Default to 8080 if PORT environment variable is not set (e.g. for local Docker testing)
export PORT=${PORT:-8080}

echo "========================================================="
echo "Starting VotingApp Initialization..."
echo "Configuring Tomcat HTTP Connector to listen on port: $PORT"
echo "========================================================="

# Replace the default Connector port="8080" with the dynamic port assigned by Render in server.xml
sed -i "s/port=\"8080\"/port=\"$PORT\"/g" /usr/local/tomcat/conf/server.xml

echo "Starting Apache Tomcat 10.1 server..."
# Use exec to ensure catalina.sh runs as PID 1, allowing correct signal propagation (like SIGTERM for graceful shutdown)
exec catalina.sh run
