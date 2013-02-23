package com.avatar_reality.ai.lstm;

public class SigmoidNeuron
	extends WeightedNeuron
{
	private static final double THRESHOLD = 0.01;

	double _weightedSum;
	double _scale = 1.0;
	double _offset = 0.0;
	
	public SigmoidNeuron(String label, double scale, double offset)
	{
		_scale = scale;
		_offset = offset;
		_label = label;
	}
	
	public SigmoidNeuron(String label)
	{
		this(label, 1.0, 0.0);
	}
	
	synchronized public void adjustInput(double value, int index, boolean propagate)
	{
		double oldValue = input[index];
		input[index] = value;
		_weightedSum -= weights[index]*oldValue;
		_weightedSum += weights[index]*value;
		double newOutput = sigmoid(_weightedSum);
		if (propagate /*&& Math.abs(newOutput-output)>THRESHOLD*/)
		{
			setOutput(newOutput);
//			fireChange();
		}
		else
		{
			output = newOutput;
			//setOutput(newOutput);
		}
	}
	
	public void train(double targetValue)
	{
		double error = 0.0;
		do
		{
			double sign = Math.signum(targetValue-getOutput());
			int index = (int)(Math.random()*weights.length);
			if (input[index]!=0.0)
			{
				double delta = sign * ((derivative(_weightedSum) / weights.length) / 2.0);
				_weightedSum -= weights[index]*input[index];
				weights[index] += delta;
				_weightedSum += weights[index]*input[index];
				setOutput(sigmoid(_weightedSum));
			}
			error = Math.abs(targetValue-getOutput());
		} 
		while (error > THRESHOLD);
	}
	
	private double sigmoid(double x)
	{
		return _scale * (1.0 / (1.0 + Math.exp(-x))) + _offset;
	}	
	
	private double derivative(double x)
	{
		double act = sigmoid(x);
		return _scale * act * (1 - act);
	}
}
