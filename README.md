# A simple JDBC Java Application with Docker
A very simple simple JDBC App to illustrate docker.

## Run postgresql
* --rm the container is removed after stop
* -d it runs as a daemon
* -e an environement variable is passed to the container
* -v a named volume is associated to the container to persist databases
    (change the name of the volume if you launch several databases)
* -p maps a host or to the postgresql port in the container
    (change the first one if you already avec postgresql or if you want to lanch more than one ).
```bash
docker run --rm -d \
    --name postgres1 \
    -e POSTGRES_PASSWORD=mysecretpassword \
    -p 5432:5432 \
    -v data-jdbc-simple-docker-pg1:/var/lib/postgresql/data \
    postgres:12.2-alpine
```
## Check that postgresql is running
```docker ps```
The port of the postgresql server in the container is mapped to the same port on the host. 
It is possible to connect with localhost:5432

## Simple Java build with Docker
It is possible to build the app without Java and Maven installed on the host (with maven in a docker image).  

The build is done in a container with a standard image. 
The container is interactive and will be removed at the end. 
The local directory in mounted as a volume. 
The local maven repository (~/.m2) too to persist artefact downloads between builds.
The build is done with maven 3.6.3 on top on jdk 11 openj9.

```bash
docker run -it --rm \
    -u $UID \
    -v "$PWD":/build \
    -v ~/.m2:/var/maven/.m2 \
    -e MAVEN_CONFIG=/var/maven/.m2 \
    -w /build maven:3.6.3-jdk-11-openj9 mvn -Duser.home=/var/maven clean package
```

## Build Docker Image for the Java App
* replace brunoe with you docker hub login
* look at Dockerfile
```
docker build -t brunoe/jdbc-simple-docker .
```

## Run java app in docker
Note the use of --link and the name of the first container (postgres1) as a machine name in the jdbc URL.
```bash
docker run --link postgres1 brunoe/jdbc-simple-docker \
  "jdbc:postgresql://postgres1:5432/postgres?user=postgres&password=mysecretpassword"
```

## All in one step
* Its is two stage build
* A temporary image is used to build the .jar (with all dependencies see pom.xml).
```
docker build -t brunoe/jdbc-simple-docker -f Dockerfile-full .
```

## Push the image to dockerhub.
After this step the image is shared with other docker users.
```
docker push brunoe/jdbc-simple-docker
```

## Stop (and rm) the pg container
```
docker stop postgres1
```

## Restart another container with the same volume
```bash
docker run --rm -d \
    --name postgres1 \
    -e POSTGRES_PASSWORD=mysecretpassword \
    -p 5432:5432 \
    -v data-jdbc-simple-docker-pg1:/var/lib/postgresql/data \
    postgres:12.2-alpine
```
## Rerun the java app, the table still exists
```bash
docker run --link postgres1 brunoe/jdbc-simple-docker \
  "jdbc:postgresql://postgres1:5432/postgres?user=postgres&password=mysecretpassword"
```
## Launch another version of postgresql in parallel
Note the different : name, host port and volume name.
```bash
docker run --rm -d \
    --name postgres2 \
    -e POSTGRES_PASSWORD=mysecretpassword \
    -p 5433:5432 \
    -v data-jdbc-simple-docker-pg2:/var/lib/postgresql/data \
    postgres:11.7-alpine
```
## Connect the java app to new postgres (note the new port in the URL)
* Note that the database does not exist and is created
* Note that the postgres version is not the same
* with --link it is also possible to define an alias
```bash
docker run --link postgres2 brunoe/jdbc-simple-docker \
  "jdbc:postgresql://postgres2:5432/postgres?user=postgres&password=mysecretpassword"
```

## Orchestration
The execution of several containers can be orchestrated. 
An easy solution is to use docker-compose

* Look at docker-compose.yml

```bash
docker-compose up
```
stop with ctrl-c. 
Relaunch it and you will see that the database still exits.

Usually its started as a daemon with -d

```bash
docker-compose up -d
``` 

It is stopped with ```down``` in the same directory (warning ```-v``` will destroy the volumes). 
