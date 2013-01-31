package com.avatar_reality.ai.lstm;

public class SigmoidNeuron
	extends WeightedNeuron
{
	private static final double THRESHOLD = 0.01;

	double _weightedSum;
	double _scale = 1.0;
	double _offset = 0.0;
	
	public SigmoidNeuron(double scale, double offset)
	{
		_scale = scale;
		_offset = offset;
	}
	
	public SigmoidNeuron()
	{
		this(1.0, 0.0);
	}
	
	synchronized public void adjustInput(double value, int index, boolean propagate)
	{
		double oldValue = input[index];
		if (Math.abs(value-oldValue)>THRESHOLD)
		{
			input[index] = value;
			_weightedSum -= weights[index]*oldValue;
			_weightedSum += weights[index]*value;
			double newOutput = sigmoid(_weightedSum);
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
		return _scale * (1.0 / (1.0 + Math.pow(Math.E,x))) - _offset;
	}	
}
