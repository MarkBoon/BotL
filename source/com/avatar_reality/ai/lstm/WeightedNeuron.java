package com.avatar_reality.ai.lstm;

public abstract class WeightedNeuron
	extends Neuron
{
	double[] weights;
	double[] input;

	void createConnection(Neuron source)
	{
		double[] newInput = new double[input.length+1];
		double[] newWeights = new double[weights.length+1];

		if (input.length>0)
		{
			System.arraycopy(input, 0, newInput, 0, input.length);
			System.arraycopy(weights, 0, newWeights, 0, weights.length);
		}
		input = newInput;
		weights = newWeights;
		weights[weights.length-1] = Math.random();
		
		source.addOutputConnection(new Connection(source,this,input.length-1));
	}
}
