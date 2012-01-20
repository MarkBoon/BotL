cp Documents/workspace/BotL/*.botl transfer
cp Documents/workspace/BotL/BotServer.jar transfer
cp Documents/workspace/BotL/botl.server.war transfer
rsync -avPe 'ssh -i tetris_stars.pem' transfer ubuntu@ec2-107-21-174-126.compute-1.amazonaws.com:~/
