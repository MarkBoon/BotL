package com.avatar_reality.ai.lstm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class NetworkPanel
	extends JComponent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int CELL_SIZE = 50;
	private static final int FONT_SIZE = 12;
	
	Neuron[] _inputNodes;
	Neuron[] _outputNodes;
	Neuron[] _hiddenNodes;
	int _nrInputNodes;
	int _nrHiddenNodes;
	int _nrOutputNodes;
	
	int _height;
	int _width;
	int _margin;
	int _fontHeight;
	int _fontWidth;
	FontMetrics _fontMetrics;
	Dimension _minimumSize;
	
	public NetworkPanel(Neuron[] inputNodes, Neuron[] outputNodes, Neuron[] hiddenNodes)
	{
		_inputNodes = inputNodes;
		_outputNodes = outputNodes;
		_hiddenNodes = hiddenNodes;
				
		_nrInputNodes = inputNodes.length;
		_nrOutputNodes = outputNodes.length;
		_nrHiddenNodes = hiddenNodes.length;
		
		_margin = 10;
		_height = 6 * CELL_SIZE+_margin*2;
		_width = _nrHiddenNodes * CELL_SIZE + _margin*2;
		_minimumSize = new Dimension(_width,_height);

		int y = _margin;
		int x = 0;
		for (int i=0; i<_nrInputNodes; i++)
		{
			int span = _width / _nrInputNodes;
			x = i*span + (span-CELL_SIZE)/2+_margin;
			_inputNodes[i].setX(x);
			_inputNodes[i].setY(y);
		}
		y = _height - CELL_SIZE - _margin;
		for (int i=0; i<_nrOutputNodes; i++)
		{
			int span = _width / _nrOutputNodes;
			x = i*span + (span-CELL_SIZE)/2 + _margin;
			_outputNodes[i].setX(x);
			_outputNodes[i].setY(y);
		}
		for (int i=0; i<_nrHiddenNodes; i++)
		{
			y = CELL_SIZE*2 + _margin + ( (i&1)==0 ? CELL_SIZE : 0 );
			x = i*CELL_SIZE + _margin;
			_hiddenNodes[i].setX(x);
			_hiddenNodes[i].setY(y);
		}
	}
	
	public void paint(Graphics g)
	{
		Graphics2D g2d = (Graphics2D)g;
		Font f = new Font("Courier", Font.PLAIN, FONT_SIZE);
		
		g2d.setFont(f);	
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		_fontMetrics = g.getFontMetrics(f);
		_fontWidth = g2d.getFontMetrics().getMaxAdvance();
		_fontHeight = g2d.getFontMetrics().getHeight();
		//g.clearRect(0, 0, _width, _height);
		
		for (Neuron n : _inputNodes)
			drawConnections(g2d, n);
		for (Neuron n : _hiddenNodes)
			drawConnections(g2d, n);
		
		paintInput(g2d);
		paintOutput(g2d);
		paintHiddenLayer(g2d);
	}

	public void paintInput(Graphics2D g)
	{
		for (int i=0; i<_nrInputNodes; i++)
		{
			paintCell(g, _inputNodes[i]);
		}
	}

	public void paintOutput(Graphics2D g)
	{
		for (int i=0; i<_nrOutputNodes; i++)
		{
			paintCell(g, _outputNodes[i]);
		}
	}

	public void paintHiddenLayer(Graphics2D g)
	{
		for (int i=0; i<_nrHiddenNodes; i++)
		{
			paintCell(g, _hiddenNodes[i]);
		}
	}
	
	public void paintCell(Graphics2D g, Neuron n)
	{
		g.setColor(Color.BLACK);
		g.draw(new Ellipse2D.Double(n.getX(), n.getY(), CELL_SIZE, CELL_SIZE));
		g.setColor(Color.WHITE);
		g.fill(new Ellipse2D.Double(n.getX()+1, n.getY()+1, CELL_SIZE-2, CELL_SIZE-2));
		String value = Double.toString(n.output);
		int xOffset = CELL_SIZE/2 - _fontMetrics.stringWidth(value)/2;
		int yOffset = 1+CELL_SIZE/2 + _fontMetrics.getAscent()/2;
		g.setColor(Color.BLACK);
		g.drawString(value,n.getX()+xOffset,n.getY()+yOffset);
	}
	
	public void drawConnections(Graphics2D g, Neuron n)
	{
		g.setColor(Color.BLACK);
		int offset = CELL_SIZE/2;
		for (Connection c : n.outputConnections)
			g.drawLine(c._source.getX()+offset, c._source.getY()+offset, c._target.getX()+offset, c._target.getY()+offset);
	}

	public Dimension getMinimumSize()
	{
		return _minimumSize;
	}
	
	public static void main(String[] args)
	{
		Neuron[] input = new SigmoidNeuron[5];
		Neuron[] output = new SigmoidNeuron[5];
		Neuron[] hidden = new SigmoidNeuron[25];
		
		for (int i=0; i<5; i++)
			input[i] = new SigmoidNeuron(2.0, -1);
		for (int i=0; i<25; i++)
		{
			hidden[i] = new SigmoidNeuron(2.0, -1);
			for (int j=0; j<5; j++)
			{
				input[j].addOutputConnection(new Connection(input[j], hidden[i], i));
			}
		}
		for (int i=0; i<5; i++)
		{
			output[i] = new SigmoidNeuron(2.0, -1);
			for (int j=0; j<25; j++)
			{
				hidden[j].addOutputConnection(new Connection(hidden[j], output[i], i));
			}
		}
		
		NetworkPanel panel = new NetworkPanel(input, output, hidden);
		
		JFrame window = new JFrame();
		window.setLocation(200, 200);
		window.setSize(panel._width, panel._height+20);
		window.setContentPane(panel);
		//window.pack();
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
