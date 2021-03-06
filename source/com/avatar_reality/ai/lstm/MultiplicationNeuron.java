package com.avatar_reality.ai.lstm;

public class MultiplicationNeuron
	extends Neuron
{
	private static final double THRESHOLD = 0.0;

	double[] input;
	
	synchronized public void adjustInput(double value, int index, boolean propagate, Connection sources)
	{
		input[index] = value;
		double oldOutput = getOutput();
		double newOutput = input[0];
		for (int i=1; i<input.length; i++)
			newOutput *= input[i];
		setOutput(newOutput);
		if (propagate && Math.abs(getOutput()-oldOutput)>THRESHOLD)
			fireChange();
	}

	@Override
	void createConnection(Neuron source)
	{
		createConnection(source, true);
	}
	
	void createConnection(Neuron source, boolean propagate)
	{
		int length = 1;
		if (input!=null)
			length = input.length+1;
		double[] newInput = new double[length];

		if (input!=null && input.length>0)
			System.arraycopy(input, 0, newInput, 0, input.length);
		input = newInput;
		
		source.addOutputConnection(new Connection(source,this,input.length-1,propagate));
	}
}
