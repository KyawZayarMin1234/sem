FROM amazoncorretto:17
COPY ./target/devops.jar /tmp/devops.jar
WORKDIR /tmp
# add a default role as arg 3 so it never prompts
ENTRYPOINT ["java","-jar","devops.jar","db:3306","30000","Engineer"]