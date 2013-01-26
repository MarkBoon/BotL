package com.avatar_reality.ai.lstm;

public class SigmaNeuron extends WeightedNeuron 
{
	private static final double THRESHOLD = 0.01;

	synchronized public void adjustInput(double value, int index)
	{
		double oldValue = input[index];
		if (Math.abs(value-oldValue)>THRESHOLD)
		{
			double oldSum = output;
			input[index] = value;
			output -= weights[index]*oldValue;
			output += weights[index]*value;
			if (Math.abs(output-oldSum)>THRESHOLD)
				fireChange();
		}
	}
}
