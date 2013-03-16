package com.avatar_reality.ai.lstm;

public class InternalConnection extends Connection
{
	public InternalConnection(Neuron source, Neuron target, int targetIndex, boolean propagate)
	{
		super(source,target,targetIndex,propagate);
	}

}
