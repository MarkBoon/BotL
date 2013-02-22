package com.avatar_reality.ai.lstm;

public class IdentityNeuron
	extends Neuron
{
	synchronized public void adjustInput(double value, int index, boolean propagate)
	{
		setOutput(value);
		if (propagate)
			fireChange();
	}

	@Override
	void createConnection(Neuron source)
	{
		source.addOutputConnection(new Connection(source,this,-1));
	}
}
