### build image using maven (buildpack/paketo)
mvn spring-boot:build-image

### run docker container
docker run -d -p 8092:8092 joheiss/sb3-loans:v1

### push docker image to docker hub
docker image push docker.io/joheiss/sb3-loans:v1

### create mysql container for loansdb
docker run -p 3307:3306 --name loansdb -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=loansdb -d mysql