package com.avatar_reality.ai.lstm;

import java.beans.PropertyChangeListener;

public class NeuralNetwork
{
	private SigmoidNeuron[] _inputNodes;
	private SigmoidNeuron[] _outputNodes;
	private SigmoidNeuron[] _hiddenNodes;
	
	public NeuralNetwork(int nrInputNodes, int nrHiddenNodes, int nrOutputNodes)
	{
		_inputNodes = new SigmoidNeuron[nrInputNodes];
		_outputNodes = new SigmoidNeuron[nrOutputNodes];
		_hiddenNodes = new SigmoidNeuron[nrHiddenNodes];
		
		for (int i=0; i<nrInputNodes; i++)
			_inputNodes[i] = new SigmoidNeuron("Input-"+i, 2.0, -1);
		for (int i=0; i<nrHiddenNodes; i++)
		{
			_hiddenNodes[i] = new SigmoidNeuron("Hidden-"+i, 2.0, -1);
			for (int j=0; j<nrInputNodes; j++)
			{
				_hiddenNodes[i].createConnection(_inputNodes[j], 0.0);
			}
		}
		
//		for (int i=0; i<nrHiddenNodes; i++)
//		{
//			for (int j=0; j<nrHiddenNodes; j++)
//			{
//				if (i!=j)
//					_hiddenNodes[j].createInternalConnection(_hiddenNodes[i], 0.0);
//					
//			}
//		}

		for (int i=0; i<nrOutputNodes; i++)
		{
			_outputNodes[i] = new SigmoidNeuron("Output-"+i, 2.0, -1);
			for (int j=0; j<nrHiddenNodes; j++)
			{
				_outputNodes[i].createConnection(_hiddenNodes[j], 0.1);
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
	
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		for (Neuron n : _inputNodes)
			n.addPropertyChangeListener(listener);
		for (Neuron n : _hiddenNodes)
			n.addPropertyChangeListener(listener);
		for (Neuron n : _outputNodes)
			n.addPropertyChangeListener(listener);
	}
	
	public void reset()
	{
		for (Neuron n : _inputNodes)
			n.setInhibited(false);
		for (Neuron n : _hiddenNodes)
			n.setInhibited(false);
		for (Neuron n : _outputNodes)
			n.setInhibited(false);		
	}
}
