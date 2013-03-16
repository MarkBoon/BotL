package com.avatar_reality.ai.lstm;

public class IdentityNeuron
	extends Neuron
{
	synchronized public void adjustInput(double value, int index, boolean propagate, Connection source)
	{
		setOutput(value);
		if (propagate)
			fireChange();
	}

	@Override
	void createConnection(Neuron source)
	{
		Connection c = new Connection(source,this,-1);
		addInputConnection(c);
		source.addOutputConnection(c);
	}
}
