package com.avatar_reality.ai;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteBotInterface extends Remote
{
	String say(String name, String text) throws RemoteException;
}
