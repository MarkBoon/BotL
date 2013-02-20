package com.avatar_reality.ai.lstm;

public class Connection
{
	int _index;
	Neuron _source;
	Neuron _target;
	boolean _propagate;
	
	public Connection(Neuron source, Neuron target, int targetIndex, boolean propagate)
	{
		_source = source;
		_target = target;
		_index = targetIndex;
		_propagate = propagate;
	}
	
	public Connection(Neuron source, Neuron target, int targetIndex)
	{
		this(source,target,targetIndex,true);
	}
	
	public void fire(double value)
	{
		_target.adjustInput(value,_index,_propagate);
	}
}
