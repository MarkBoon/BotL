#Transfer files
cp *.botl botl.server
cp BotServer.jar botl.server
rsync -avPe 'ssh -i me.pem' botl.server ec2-user@ec2-23-20-17-195.compute-1.amazonaws.com:~/
rsync -tve 'ssh -i me.pem' *.war ec2-user@ec2-23-20-17-195.compute-1.amazonaws.com:/var/lib/tomcat6/webapps
