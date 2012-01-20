package com.avatar_reality.ai;

public class BotCommandPrinter
	implements BotCommandListener
{
	@Override
	public void say(String text)
	{
		System.err.println(text);
	}

	@Override
	public void faceTo(String userID)
	{
		System.err.println("Turn towards "+userID);
	}

	@Override
	public void walkTo(String location)
	{
		System.err.println("Walk to "+location);
	}

	@Override
	public void runTo(String location)
	{
		System.err.println("Run to "+location);
	}

	@Override
	public String chooseRandomDestination(String locationString, double radius)
	{
		return locationString;
	}

	@Override
	public double getDistance(String location1, String location2)
	{
		return 1.0;
	}
}
