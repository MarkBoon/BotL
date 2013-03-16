package com.avatar_reality.ai.lstm;

public class SigmaNeuron extends WeightedNeuron 
{
	private static final double THRESHOLD = 0.0;

	synchronized public void adjustInput(double value, int index, boolean propagate, Connection source)
	{
		double oldValue = input[index];
		if (Math.abs(value-oldValue)>THRESHOLD)
		{
			double oldSum = getOutput();
			input[index] = value;
			setOutput(getOutput() - weights[index]*oldValue);
			setOutput(getOutput() + weights[index]*value);
			if (propagate && Math.abs(getOutput()-oldSum)>THRESHOLD)
				fireChange();
		}
	}
}
