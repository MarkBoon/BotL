package com.avatar_reality.ai.lstm;

public class Connection
{
	int _index;
	Neuron _source;
	Neuron _target;
	boolean _propagate;
	
	public Connection(Neuron source, Neuron target, int index, boolean propagate)
	{
		_source = source;
		_target = target;
		_index = index;
		_propagate = propagate;
	}
	
	public Connection(Neuron source, Neuron target, int index)
	{
		this(source,target,index,true);
	}
	
	public void fire(double value)
	{
		_target.adjustInput(value,_index,_propagate);
	}
}
