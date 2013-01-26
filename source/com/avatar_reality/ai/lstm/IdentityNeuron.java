package com.avatar_reality.ai.lstm;

public class IdentityNeuron
	extends Neuron
{
	synchronized public void adjustInput(double value, int index)
	{
		output = value;
		fireChange();
	}

	@Override
	void createConnection(Neuron source)
	{
		source.addOutputConnection(new Connection(source,this,-1));
	}
}
