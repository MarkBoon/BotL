package com.avatar_reality.ai.lstm;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class NeuronCommandPanel
	extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	NeuronController _controller;
	
	JTextField _inputField;
	JTextField _nrToDoField;
	JButton	_propagateButton;
	JButton _runButton;
	
	public NeuronCommandPanel(NeuronController controller)
	{
		_controller = controller;

		_inputField = new JTextField();
		_inputField.setEditable(false);
		_nrToDoField = new JTextField();
		_nrToDoField.setEditable(false);
		_propagateButton = new JButton("Step");
		_runButton = new JButton("Run");
		
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		setAlignmentX(LEFT_ALIGNMENT);
		
		_inputField.setMaximumSize(new Dimension(Short.MAX_VALUE,25));
		_nrToDoField.setMaximumSize(new Dimension(Short.MAX_VALUE,25));
		
		add(_inputField);
		add(_nrToDoField);
		add(_propagateButton);
		add(_runButton);
		
		_controller.addPropertyChangeListener(new PropertyChangeListener()
			{
				@Override
				public void propertyChange(PropertyChangeEvent event)
				{
					_inputField.setEditable(event.getNewValue()!=null);
				}
			});
		
		_inputField.addActionListener( new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent event)
				{
					Neuron n = _controller.getSelectedNeuron();
					if (_inputField.getText()!=null && _inputField.getText().length()!=0)
					{
						double value = Double.parseDouble(_inputField.getText());
						n.setOutput(value);
//						_controller.addToQueue(n);
						_inputField.setText("");
					}
				}
			});
		
		_propagateButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					_controller.doStep();
				}
			});
		
		_runButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					_controller.doRun();
				}
			});
	}
}
