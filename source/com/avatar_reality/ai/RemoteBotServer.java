package com.avatar_reality.ai;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

public class RemoteBotServer implements RemoteBotInterface
{
	private static Logger _logger = Logger.getRootLogger();
	
	@Override
    public String say(String name, String text) throws RemoteException
    {
		synchronized(BotCommandProcessor.getSingleton())
		{
			return BotCommandProcessor.getSingleton().receiveSayAction(name, text);
		}
    }

	public static void main(String[] args) 
	{
//		BasicConfigurator.configure();

		String botName = "Test.botl";
		if (args.length>0)
			botName = args[0];
			
//        if (System.getSecurityManager() == null) 
//        {
//            System.setSecurityManager(new SecurityManager());
//        }
        try 
        {
        	java.rmi.registry.LocateRegistry.createRegistry(1098);
        	_logger.info("RMI registry ready.");            
   		 	String name = "RemoteBot";
            RemoteBotServer server = new RemoteBotServer();
            RemoteBotInterface stub =
                (RemoteBotInterface) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.getRegistry(1098);
            registry.rebind(name, stub);
            _logger.info("RemoteBot bound");
            BotCommandProcessor.create("TestBot", new BotCommandPrinter());
            BotCommandProcessor.getSingleton().loadTriggers(botName);
            
            while (true)
            {
            	Thread.sleep(10000);
            	_logger.info("Heartbeat");
            }
        } 
        catch (Exception e) 
        {
            _logger.info("RemoteBot exception: "+e.getMessage());
            e.printStackTrace();
        }
    }
}
