FROM adoptopenjdk:11-jre-hotspot
ENTRYPOINT ["java","-jar","/app/my-app.jar"]
ADD target/*jar-with-dependencies.jar /app/my-app.jar

