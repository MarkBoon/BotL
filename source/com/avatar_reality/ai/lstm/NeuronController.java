package com.avatar_reality.ai.lstm;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayDeque;
import java.util.Queue;

public class NeuronController
{
	public static final String SELECTED_NEURON_PROPERTY = "selectedNeuron";
	private Neuron _selectedNeuron;
	private NeuralNetwork _network;
	private PropertyChangeSupport _propertyChangeSupport;
	private Queue<Neuron> _actionQueue;
	
	public NeuronController()
	{
		_propertyChangeSupport = new PropertyChangeSupport(this);
		_actionQueue = new ArrayDeque<Neuron>();
		
	}
	
	public Neuron getSelectedNeuron() 
	{
		return _selectedNeuron;
	}
	
	public void setSelectedNeuron(Neuron selectedNeuron) 
	{
		Neuron previous = _selectedNeuron;
		_selectedNeuron = selectedNeuron;
		_propertyChangeSupport.firePropertyChange(SELECTED_NEURON_PROPERTY,previous,_selectedNeuron);
	}
	
	public NeuralNetwork getNetwork() 
	{
		return _network;
	}
	
	public void setNetwork(NeuralNetwork network) 
	{
		_network = network;
		_network.addPropertyChangeListener(new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				Neuron n = (Neuron)evt.getSource();
				if (n.hasOutputConnections())
					addToQueue(n);
			}
		});
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		_propertyChangeSupport.addPropertyChangeListener(listener);
	}
	
	public void addToQueue(Neuron n)
	{
		_actionQueue.add(n);
	}
	
	public void doStep()
	{
		Neuron n = _actionQueue.poll();
		setSelectedNeuron(n);
		if (n!=null)
		{
			n.fireChange();
		}
//		else
//			_network.reset();
	}
	
	public void doRun()
	{
		while (!_actionQueue.isEmpty())
			doStep();
	}
}
