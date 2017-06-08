FROM java:8-jre
VOLUME /tmp
EXPOSE 9091
ADD cms-kotlin.jar kotlin-app.jar
RUN sh -c 'touch /kotlin-app.jar'
ENTRYPOINT ["java", "-jar", "/kotlin-app.jar"]