package com.avatar_reality.ai.lstm;

public class LSTMBlock
	extends Neuron
{
	IdentityNeuron[] inputNeurons;
	SigmoidNeuron[] sigmoidNeurons;
	MultiplicationNeuron[] inhibitNeurons;
	SigmaNeuron memoryNeuron;
	MultiplicationNeuron outputNeuron;
	
	public LSTMBlock()
	{
		sigmoidNeurons = new SigmoidNeuron[4];
		for (int i=0; i<4; i++)
			sigmoidNeurons[i] = new SigmoidNeuron();
		inhibitNeurons = new MultiplicationNeuron[2];
		for (int i=0; i<2; i++)
			inhibitNeurons[i] = new MultiplicationNeuron();
		memoryNeuron = new SigmaNeuron();

		outputNeuron = new MultiplicationNeuron();
		outputNeuron.createConnection(memoryNeuron);
		outputNeuron.createConnection(sigmoidNeurons[3]);
		
		memoryNeuron.createConnection(inhibitNeurons[0]);
		memoryNeuron.createConnection(inhibitNeurons[1]);
		
		inhibitNeurons[0].createConnection(sigmoidNeurons[0]);
		inhibitNeurons[0].createConnection(sigmoidNeurons[1]);
		inhibitNeurons[1].createConnection(sigmoidNeurons[2]);
		inhibitNeurons[1].createConnection(memoryNeuron);
	}
	
	@Override
	synchronized public void adjustInput(double value, int index)
	{
		inputNeurons[index].adjustInput(value, index);
	}

	@Override
	void createConnection(Neuron source) 
	{
		IdentityNeuron[] newInput = new IdentityNeuron[inputNeurons.length+1];
		newInput[inputNeurons.length] = new IdentityNeuron();
		
		if (inputNeurons.length>0)
			System.arraycopy(inputNeurons, 0, newInput, 0, inputNeurons.length);
		inputNeurons = newInput;
		
		source.addOutputConnection(new Connection(source,inputNeurons[inputNeurons.length-1],inputNeurons.length-1));
		for (Neuron n : sigmoidNeurons)
			n.createConnection(inputNeurons[inputNeurons.length-1]);
	}
}
