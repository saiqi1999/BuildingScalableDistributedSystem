HOW TO START:

1. Deploy or start [Server] locally on a tomcat instance, RMQ, Redis ip hardcoded need to change possibly
2. Redis server need to start and accept connection remotely to get data

If you want to populate or provide data in future, the steps:
1. Ensure Redis server started and protected mode off, firewall added your ip access, port 6379
2. Use "remove" class in [Client] and purge all data in Redis, Redis ip hardcoded
3. Start [Consumer], firewall add consumer ip to Redis's security group, port 6379
4. Start [Server] locally or remote, some ip hardcoded need to change possibly
5. Use [Client] reach [Server] by giving proper arguments, run populate method



------------------------------------------------------------
STARTING PARAMETERS:

When starting [Client]: -i localhost:8080 -n 20000 -t 128 -m 10\
Means server ip = localhost, simulate 20000 skiers, each skier take 10 run. Record going to server in 128 threads

When starting [Consumer]: -ip 54.69.36.225 -MQip 34.213.38.180 -m ForResort\
Means target Redis db server ip = 54.69.36.225, build index on that for resort kind search on that Redis, rabbitMQ that queues post data on ip 34.213.38.180

When starting [Consumer]: -ip 54.69.36.225 -MQip 34.213.38.180 -m ForSkier\
Means target Redis db server ip = 54.69.36.225, build index on that for skier kind search on that Redis, rabbitMQ that queues post data on ip 34.213.38.180



------------------------------------------------------------
HOW TO ACCESS: suppose server ip is localhost

GET http://localhost:8080/HW3_server/resorts/1/seasons/2/day/1/skiers \
Get the number of unique skiers in resort 1, season 2, day 1. (Might be slow)

GET http://localhost:8080/HW3_server/skiers/1/seasons/2/days/3/skiers/15237 \
Get the total vertices for skier 15237 in resort 1, season 2, day 3.

GET http://localhost:8080/HW3_server/skiers/15237/vertical?resortID=2 \
Get the total vertices for skier 15237 in resort 2

GET http://localhost:8080/HW3_server/skiers/15237/vertical?resortID=1&season=2 \
Get the total vertices for skier 15237 in resort 1, season 2

POST http://localhost:8080/HW3_server/skiers/3/seasons/2/days/1/skiers/2?time=222121&liftId=22&waitTime=33330 \
Provide such a piece of record to database.
