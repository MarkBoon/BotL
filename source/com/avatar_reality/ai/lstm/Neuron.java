package com.avatar_reality.ai.lstm;

import java.util.ArrayList;
import java.util.List;

public abstract class Neuron
{
	static final double THRESHOLD = 0.01;
	
	double output = 0.0;
	List<Connection> outputConnections = new ArrayList<Connection>();
	
	abstract void adjustInput(double value, int index, boolean propagate);
	abstract void createConnection(Neuron source);
	
	private int _x;
	private int _y;
	
	protected void fireChange()
	{
		for (Connection c : outputConnections)
			c.fire(output);
	}
	
	public void addOutputConnection(Connection c)
	{
		outputConnections.add(c);
	}
	
	public int getX() 
	{
		return _x;
	}
	public void setX(int x) 
	{
		this._x = x;
	}
	public int getY() 
	{
		return _y;
	}
	public void setY(int y) 
	{
		this._y = y;
	}
}
