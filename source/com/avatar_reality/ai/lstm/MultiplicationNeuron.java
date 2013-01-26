package com.avatar_reality.ai.lstm;

public class MultiplicationNeuron
	extends Neuron
{
	private static final double THRESHOLD = 0.01;

	double output;
	double[] input;
	
	synchronized public void adjustInput(double value, int index)
	{
		double oldOutput = output;
		output /= input[index];
		output *= value;
		input[index] = value;
		if (Math.abs(output-oldOutput)>THRESHOLD)
			fireChange();
	}

	@Override
	void createConnection(Neuron source)
	{
		double[] newInput = new double[input.length+1];

		if (input.length>0)
			System.arraycopy(input, 0, newInput, 0, input.length);
		input = newInput;
		
		source.addOutputConnection(new Connection(source,this,input.length-1));
	}
}
