FROM adoptopenjdk/openjdk12
VOLUME /tmp
EXPOSE 8080 8000

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} app.jar

#ENTRYPOINT ["java","-agentlib:jdwp=transport=dt_socket,address=*:8000,server=y,suspend=n", "-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]