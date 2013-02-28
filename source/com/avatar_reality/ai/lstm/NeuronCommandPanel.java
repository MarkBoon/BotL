package com.avatar_reality.ai.lstm;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
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
	JPanel _commandPanel;
	JPanel _weightPanel;
	JLabel _weightedSumLabel;
	JLabel _outputLabel;
	
	private boolean running;
	
	public NeuronCommandPanel(NeuronController controller)
	{
		_controller = controller;

		_inputField = new JTextField();
		_inputField.setEditable(false);
		_nrToDoField = new JTextField();
		_nrToDoField.setEditable(false);
		_propagateButton = new JButton("Step");
		_runButton = new JButton("Run");
		_weightedSumLabel = new JLabel();
		_outputLabel = new JLabel();
		
		_commandPanel = new JPanel();
		_weightPanel = new JPanel();

		_commandPanel.setLayout(new BoxLayout(_commandPanel,BoxLayout.Y_AXIS));
		_commandPanel.setAlignmentX(LEFT_ALIGNMENT);
		
		_inputField.setMaximumSize(new Dimension(Short.MAX_VALUE,25));
		_nrToDoField.setMaximumSize(new Dimension(Short.MAX_VALUE,25));
		
		_commandPanel.add(_inputField);
		_commandPanel.add(_nrToDoField);
		_commandPanel.add(_propagateButton);
		_commandPanel.add(_runButton);
		_commandPanel.add(_weightedSumLabel);
		_commandPanel.add(_outputLabel);
		
		setLayout(new GridLayout(1,2));
		add(_commandPanel);
		add(_weightPanel);
		
		_controller.addPropertyChangeListener(new PropertyChangeListener()
			{
				@Override
				public void propertyChange(PropertyChangeEvent event)
				{
					if (running)
						return;

					_inputField.setEditable(event.getNewValue()!=null);
					if (_controller.getSelectedNeuron()!=null)
					{
						_weightedSumLabel.setText("Weighted sum: "+((SigmoidNeuron)_controller.getSelectedNeuron())._weightedSum);
						_outputLabel.setText("Output: "+((SigmoidNeuron)_controller.getSelectedNeuron()).getOutput());
					}
					else 
					{
						_weightedSumLabel.setText("");
						_outputLabel.setText("");
					}
					updateWeightPanel();
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
						n.setOutput(value,true);
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
					running = true;
					_controller.doRun();
					running = false;
				}
			});
	}
	
	private void updateWeightPanel()
	{
		WeightedNeuron n = (WeightedNeuron)_controller.getSelectedNeuron();
		_weightPanel.removeAll();
		if (n!=null && n.weights!=null)
		{
			_weightPanel.setLayout(new GridLayout(n.weights.length,1));
			int i=0;
			for (double w : n.weights)
			{
				String label = n.inputConnections.get(i).toString()+": "+w;
				JLabel weightValue = new JLabel(label);
				_weightPanel.add(weightValue);
				i++;
			}
		}
		_weightPanel.invalidate();
		validate();
	}
}
