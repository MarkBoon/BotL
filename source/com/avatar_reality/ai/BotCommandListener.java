package com.avatar_reality.ai;

public interface BotCommandListener
{
	public void say(String text);
	public void faceTo(String userID);
	public void walkTo(String location);
	public void runTo(String location);
	public String chooseRandomDestination(String locationString, double radius);
	public double getDistance(String location1, String location2);
}
