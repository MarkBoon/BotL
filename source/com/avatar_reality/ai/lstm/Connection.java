package com.avatar_reality.ai.lstm;

public class Connection
{
	int _index;
	Neuron _source;
	Neuron _target;
	
	public Connection(Neuron source, Neuron target, int index)
	{
		_source = source;
		_target = target;
		_index = index;
	}
	
	public void fire(double value)
	{
		_target.adjustInput(value,_index);
	}
}
