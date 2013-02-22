package com.avatar_reality.ai.lstm;

public class NeuralNetwork
{
	private Neuron[] _inputNodes;
	private Neuron[] _outputNodes;
	private Neuron[] _hiddenNodes;
	
	public NeuralNetwork(int nrInputNodes, int nrHiddenNodes, int nrOutputNodes)
	{
		_inputNodes = new SigmoidNeuron[nrInputNodes];
		_outputNodes = new SigmoidNeuron[nrOutputNodes];
		_hiddenNodes = new SigmoidNeuron[nrHiddenNodes];
		
		for (int i=0; i<nrInputNodes; i++)
			_inputNodes[i] = new SigmoidNeuron(2.0, -1);
		for (int i=0; i<nrHiddenNodes; i++)
		{
			_hiddenNodes[i] = new SigmoidNeuron(2.0, -1);
			for (int j=0; j<nrInputNodes; j++)
			{
				_inputNodes[j].addOutputConnection(new Connection(_inputNodes[j], _hiddenNodes[i], i));
			}
		}
		for (int i=0; i<nrOutputNodes; i++)
		{
			_outputNodes[i] = new SigmoidNeuron(2.0, -1);
			for (int j=0; j<nrHiddenNodes; j++)
			{
				_hiddenNodes[j].addOutputConnection(new Connection(_hiddenNodes[j], _outputNodes[i], i));
			}
		}		
	}
	
	public Neuron[] getInputNodes()
	{
		return _inputNodes;
	}
	
	public Neuron[] getOutputNodes() 
	{
		return _outputNodes;
	}
	
	public Neuron[] getHiddenNodes() 
	{
		return _hiddenNodes;
	}
}
