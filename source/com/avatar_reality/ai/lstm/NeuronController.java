package com.avatar_reality.ai.lstm;

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
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		_propertyChangeSupport.addPropertyChangeListener(listener);
	}
}
