package com.avatar_reality.ai.lstm;

public class SigmaNeuron extends WeightedNeuron 
{
	private static final double THRESHOLD = 0.0;

	synchronized public void adjustInput(double value, int index, boolean propagate)
	{
		double oldValue = input[index];
		if (Math.abs(value-oldValue)>THRESHOLD)
		{
			double oldSum = output;
			input[index] = value;
			output -= weights[index]*oldValue;
			output += weights[index]*value;
			if (propagate && Math.abs(output-oldSum)>THRESHOLD)
				fireChange();
		}
	}
}
