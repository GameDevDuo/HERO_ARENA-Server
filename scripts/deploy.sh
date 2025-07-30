git pull origin main

chmod +x ./gradlew

./gradlew clean build

docker build . -t heroarena/heroarena-server:latest

docker stop heroarena-server
docker rm heroarena-server

docker run -d -p 8080:8080 --name heroarena-server --env-file ./.env heroarena/heroarena-server:latest