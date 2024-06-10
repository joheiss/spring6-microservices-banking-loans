### build image using maven (buildpack/paketo)
mvn spring-boot:build-image

### run docker container
docker run -d -p 8092:8092 joheiss/sb3-loans:v1
