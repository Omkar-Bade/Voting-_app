# Use the official Tomcat 10.1 image with Eclipse Temurin JDK 21 (matching Java 21 compiler target compliance)
FROM tomcat:10.1-jdk21-temurin

# Metadata
LABEL maintainer="Omkar Bade"
LABEL description="Docker image for VotingApp Servlet Application on Tomcat 10.1"

# Set environmental variables
ENV CATALINA_HOME=/usr/local/tomcat
ENV PATH=$CATALINA_HOME/bin:$PATH

# Remove the default Tomcat web applications to avoid conflicts and keep deployment clean
RUN rm -rf $CATALINA_HOME/webapps/*

# Copy the pre-built VotingApp.war from the host context and rename it to ROOT.war
# This deploys the application at the root context path (/) of the Tomcat server
COPY VotingApp.war $CATALINA_HOME/webapps/ROOT.war

# Copy the startup configuration script to Tomcat bin
COPY start.sh $CATALINA_HOME/bin/start.sh

# Ensure the startup script has executable permissions
RUN chmod +x $CATALINA_HOME/bin/start.sh

# Critical Fix: Convert Windows CRLF line endings to Linux LF in case the script is edited or built on Windows.
# This prevents shell interpreter errors (e.g. '\r: command not found') inside the Docker container.
RUN sed -i 's/\r$//' $CATALINA_HOME/bin/start.sh

# Expose port 8080 (documentation only, start.sh dynamically overrides this based on Render's assigned port)
EXPOSE 8080

# Execute the startup script which modifies Tomcat's server.xml with the assigned port and starts the server
CMD ["/usr/local/tomcat/bin/start.sh"]
