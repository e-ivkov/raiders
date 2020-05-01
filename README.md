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

Matchmaking behavior
--------------------
In the configuration you specify the desired maximum time limit, tolerance and maximum skill.
Let's discuss how these parameters are used to provide the most suitable matchmaking behavior.

### Tolerance
The tolerance should be between 0 and 1 and controls your preference on speed and accuracy of matchmaking.

Values closer to 1 favour the speed, so matchmaking will be fast but difference in skill can be huge.

Values closer to 0 let the matchmaking wait a little bit more and select the match with minimum skill difference.

### Time limit
The time limit param specifies the time by which we should definitely find a match.
So the closer to time limit the more tolerant matchmaking will become to huge skill differences.

### Max skill
The maximum possible value of skill in your game. Used to properly calculate ratios of differences in skill.

How to run
-----------
### Example configuration
Configuration should be put in `application.conf` in `resources` folder.
```
matchmaking = {
    tolerance = 0.5,
    time-limit-sec = 10,
    max-skill = 100
}
db = {
    username = "postgres",
    password = "docker"
}
```
### DB setup
Here is an example DB setup with docker containers. Pay attention that db name should be `raiders`.
And also the database engine should be Postgres.
```shell script
docker pull postgres
mkdir -p $HOME/docker/volumes/postgres
docker run --rm --name pg-docker -e POSTGRES_PASSWORD=docker -d -p 5432:5432 -v $HOME/docker/volumes/postgres:/var/lib/postgresql/data  postgres
```

You can find a helpful article on DB setup with docker here:
https://hackernoon.com/dont-install-postgres-docker-pull-postgres-bee20e200198
### Usage example
```shell script
curl -d '{"skill":99}' http://localhost:8080/player/add
# Output: {"id":1}

curl -d '{"skill":98}' http://localhost:8080/player/add
# Output: {"id":2}

curl http://localhost:8080/player/1/search/match/1vs1
# Output: Started searching

curl http://localhost:8080/player/2/search/match/1vs1
# Output: Started searching

curl http://localhost:8080/player/1/search/status
# Output: {"matchId":1,"status":"FOUND"}

curl http://localhost:8080/match/1/players
# Output: {"players":[1,2]}

curl http://localhost:8080/match/1/accept
# Output: Match 1 have been accepted, the data about this match will be deleted.
```
Future plans
------------
Support custom matchmaking params and formulas.
Support other game modes.