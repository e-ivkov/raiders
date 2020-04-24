```shell script
sudo docker run --rm --name pg-docker -e POSTGRES_PASSWORD=docker -d -p 5432:5432 -v $HOME/docker/volumes/postgres:/var/lib/postgresql/data  postgres
curl -d '{"skill":99}' http://localhost:8080/player/add
```
https://hackernoon.com/dont-install-postgres-docker-pull-postgres-bee20e200198