#Transfer files
cp *.botl transfer
cp BotServer.jar transfer
cp BotL/botl.server.war transfer
rsync -avPe 'ssh -i tetris_stars.pem' transfer ubuntu@ec2-107-21-174-126.compute-1.amazonaws.com:~/
