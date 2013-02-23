package com.avatar_reality.ai.lstm;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public abstract class Neuron
{
	public static final String OUTPUT_PROPERTY = "output";
	
	static final double THRESHOLD = 0.01;
	
	protected double output = 0.0;
	List<Connection> outputConnections = new ArrayList<Connection>();
	
	abstract void adjustInput(double value, int index, boolean propagate);
	abstract void createConnection(Neuron source);
	
	private int _x;
	private int _y;
	private boolean _hasChanged = true;
	
	private PropertyChangeSupport _propertyChangeSupport = new PropertyChangeSupport(this);
	
	protected String _label = "";
	
	public String toString()
	{
		return _label+": "+output;
	}
	
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
	
	public double getOutput()
	{
		return output;
	}
	
	public void setOutput(double newValue)
	{
		double oldValue = output;
		output = newValue;
		if (oldValue!=newValue)
		{
			_propertyChangeSupport.firePropertyChange(OUTPUT_PROPERTY, oldValue, newValue);
			setHasChanged(true);
		}
	}
	
	public boolean hasOutputConnections()
	{
		return !outputConnections.isEmpty();
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		_propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		_propertyChangeSupport.removePropertyChangeListener(listener);
	}
	
	public boolean hasChanged() 
	{
		return _hasChanged;
	}
	
	public void setHasChanged(boolean hasChanged) 
	{
		_hasChanged = hasChanged;
	}
}
