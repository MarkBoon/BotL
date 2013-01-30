package com.avatar_reality.ai.lstm;

public class SigmoidNeuron
	extends WeightedNeuron
{
	private static final double THRESHOLD = 0.01;

	double weightedSum;
	
	synchronized public void adjustInput(double value, int index, boolean propagate)
	{
		double oldValue = input[index];
		if (Math.abs(value-oldValue)>THRESHOLD)
		{
			input[index] = value;
			weightedSum -= weights[index]*oldValue;
			weightedSum += weights[index]*value;
			double newOutput = sigmoid(weightedSum);
			if (propagate && Math.abs(newOutput-output)>THRESHOLD)
			//if (propagate && (newOutput>=0.5 && output<0.5) || (newOutput<0.5 && output>=0.5))
			{
				output = newOutput;
				fireChange();
			}
			output = newOutput;
		}
	}
	
	private double sigmoid(double x)
	{
		return 1.0 / (1.0 - Math.pow(Math.E,x));
	}	
}
