package com.avatar_reality.ai;

public class Util 
{

	public static String decapitalize(String input)
	{
		StringBuilder b = new StringBuilder(input);
		b.setCharAt(0, Character.toLowerCase(input.charAt(0)));
		return b.toString();
	}
}
