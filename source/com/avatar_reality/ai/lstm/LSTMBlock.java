package com.avatar_reality.ai.lstm;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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
		memoryNeuron.weights[1] = 1.0;
		
		inhibitNeurons[0].createConnection(sigmoidNeurons[0]);
		inhibitNeurons[0].createConnection(sigmoidNeurons[1]);
		inhibitNeurons[1].createConnection(sigmoidNeurons[2]);
		inhibitNeurons[1].createConnection(memoryNeuron,false);
	}
	
	@Override
	synchronized public void adjustInput(double value, int index, boolean propagate)
	{
		inputNeurons[index].adjustInput(value, index, propagate);
	}

	@Override
	void createConnection(Neuron source) 
	{
		int length = 1;
		if (inputNeurons!=null)
			length = inputNeurons.length+1;
		IdentityNeuron[] newInput = new IdentityNeuron[length];
		newInput[length-1] = new IdentityNeuron();
		
		if (inputNeurons!=null && inputNeurons.length>0)
			System.arraycopy(inputNeurons, 0, newInput, 0, inputNeurons.length);
		inputNeurons = newInput;
		
		if (source!=null)
			source.addOutputConnection(new Connection(source,inputNeurons[inputNeurons.length-1],inputNeurons.length-1));
		for (Neuron n : sigmoidNeurons)
			n.createConnection(inputNeurons[inputNeurons.length-1]);
	}
	
	public void print(PrintStream s) throws Exception
	{
		s.println("======================================================");
		double[][] array = new double[7][4*(inputNeurons.length+1)];
		for (int i=0; i<7; i++)
			for (int j=0; j<array[0].length; j++)
				array[i][j] = Double.NaN;

		for (int i=0; i<4; i++)
		{
			for (int j=0; j<sigmoidNeurons[i].input.length; j++)
			{
				array[0][i*(inputNeurons.length+1)+j] = sigmoidNeurons[i].input[j];
				array[1][i*(inputNeurons.length+1)+j] = sigmoidNeurons[i].weights[j];
			}
		}
		array[2][0] = sigmoidNeurons[0].output;
		array[2][1] = sigmoidNeurons[1].output;
		array[2][8] = sigmoidNeurons[2].output;
		array[2][9] = memoryNeuron.output;
		
		array[3][0] = inhibitNeurons[0].output;
		array[3][8] = inhibitNeurons[1].output;
		array[4][0] = memoryNeuron.weights[0];
		array[4][1] = memoryNeuron.weights[1];
		array[5][0] = memoryNeuron.output;
		array[5][1] = sigmoidNeurons[3].output;
		array[6][0] = outputNeuron.output;
		
		for (int j=0; j<array[0].length-1; j++)
		{
			for (int i=0; i<7; i++)
			{
				double n = Math.rint(10000.0*array[i][j])/10000.0;
				if (Double.isNaN(array[i][j]))
					s.append("\t");
				else
					s.print(n+"\t");
			}
			s.println();
		}
		s.println("======================================================");
	}
	
	public String toString()
	{
		PrintStream s = new PrintStream(new ByteArrayOutputStream(), true);
		try 
		{
			print(s);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return s.toString();
	}
	
	public static void main(String[] args) throws Exception
	{
		LSTMBlock neuron = new LSTMBlock();
		neuron.createConnection(null);
		neuron.createConnection(null);
		neuron.createConnection(null);
		neuron.inputNeurons[0].output = 1.0;
		neuron.inputNeurons[0].fireChange();
		neuron.inputNeurons[2].output = 1.0;
		neuron.inputNeurons[2].fireChange();
		neuron.print(System.out);
	}
}
