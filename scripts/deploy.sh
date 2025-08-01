git pull origin main

chmod +x ./gradlew

./gradlew clean build

docker build . -t heroarena/heroarena-server:latest

docker stop heroarena-server
docker rm heroarena-server

docker run -d --name heroarena-server --network heroarena-net -p 80:8080 --env-file .env heroarena/heroarena-server:latest