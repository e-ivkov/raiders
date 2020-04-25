Raiders
==============
About
--------
A generic matchmaking server, that you can use for your game.
1. Set up the DB
2. Configure game parameters in `application.conf`
3. Start the server
4. Tell the matchmaker about your players
5. Ask to create a new match

Support for game modes
----------------------
Currently only 1vs1 game mode is supported

Matchmaking formula
--------------------

How to run
-----------
### DB setup
```shell script
sudo docker run --rm --name pg-docker -e POSTGRES_PASSWORD=docker -d -p 5432:5432 -v $HOME/docker/volumes/postgres:/var/lib/postgresql/data  postgres
curl -d '{"skill":99}' http://localhost:8080/player/add
```
https://hackernoon.com/dont-install-postgres-docker-pull-postgres-bee20e200198

Future plans
------------
Support custom matchmaking params and formulas