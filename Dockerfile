# Use the official Tomcat image
FROM tomcat:10.1.31-jdk17-temurin-jammy

# Remove the default webapps (optional)
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy your web application directory
COPY trivia /usr/local/tomcat/webapps/trivia

# Expose the port Tomcat runs on
EXPOSE 8081

# Start Tomcat
CMD ["catalina.sh", "run"]
