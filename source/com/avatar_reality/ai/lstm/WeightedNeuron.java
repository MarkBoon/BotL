package com.avatar_reality.ai.lstm;

public abstract class WeightedNeuron
	extends Neuron
{
	double[] weights;
	double[] input;

	void createConnection(Neuron source)
	{
		createConnection(source, 0.1); //Math.random()*0.2-0.1;
	}
	
	void createConnection(Neuron source, double initialWeight)
	{
		addWeight(initialWeight);		
		Connection c = new Connection(source, this, input.length-1);
		addInputConnection(c);
		source.addOutputConnection(c);
	}
	
	void createInternalConnection(Neuron source, double initialWeight)
	{
		addWeight(initialWeight);		
		Connection c = new InternalConnection(source, this, input.length-1, true);
		addInputConnection(c);
		source.addOutputConnection(c);
	}
	
	void addWeight(double initialWeight)
	{
		int length = 1;
		if (input!=null)
			length = input.length+1;
		double[] newInput = new double[length];
		double[] newWeights = new double[length];

		if (input!=null && input.length>0)
		{
			System.arraycopy(input, 0, newInput, 0, input.length);
			System.arraycopy(weights, 0, newWeights, 0, weights.length);
		}
		input = newInput;
		weights = newWeights;
		weights[weights.length-1] = initialWeight;
	}
}
