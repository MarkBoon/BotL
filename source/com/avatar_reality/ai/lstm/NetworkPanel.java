package com.avatar_reality.ai.lstm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class NetworkPanel
	extends JComponent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int CELL_SIZE = 50;
	private static final int FONT_SIZE = 12;
	
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

	NeuronController _controller;
	Neuron _selectedNeuron;
	
	public NetworkPanel(NeuronController controller)
	{
		_controller = controller;
		_selectedNeuron = null;
		
		_nrInputNodes = controller.getNetwork().getInputNodes().length;
		_nrHiddenNodes = controller.getNetwork().getHiddenNodes().length;
		_nrOutputNodes = controller.getNetwork().getOutputNodes().length;
		
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
			controller.getNetwork().getInputNodes()[i].setX(x);
			controller.getNetwork().getInputNodes()[i].setY(y);
		}
		y = _height - CELL_SIZE - _margin;
		for (int i=0; i<_nrOutputNodes; i++)
		{
			int span = _width / _nrOutputNodes;
			x = i*span + (span-CELL_SIZE)/2 + _margin;
			controller.getNetwork().getOutputNodes()[i].setX(x);
			controller.getNetwork().getOutputNodes()[i].setY(y);
		}
		for (int i=0; i<_nrHiddenNodes; i++)
		{
			y = CELL_SIZE*2 + _margin + ( (i&1)==0 ? CELL_SIZE : 0 );
			x = i*CELL_SIZE + _margin;
			controller.getNetwork().getHiddenNodes()[i].setX(x);
			controller.getNetwork().getHiddenNodes()[i].setY(y);
		}
		
		addMouseListener(
				new MouseAdapter()
				{
					public void mouseClicked(MouseEvent event)
					{
						handleMouseClick(event.getX(),event.getY());
					}
				}
			);
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
		
		for (Neuron n : _controller.getNetwork().getInputNodes())
			drawConnections(g2d, n, Color.BLACK);
		for (Neuron n : _controller.getNetwork().getHiddenNodes())
			drawConnections(g2d, n, Color.BLACK);
	
		if (_selectedNeuron!=null)
			drawConnections(g2d, _selectedNeuron, Color.BLUE);
		
		paintInput(g2d);
		paintOutput(g2d);
		paintHiddenLayer(g2d);
		
		if (_selectedNeuron!=null)
			paintCell(g2d, _selectedNeuron, Color.RED);
	}

	public void paintInput(Graphics2D g)
	{
		for (Neuron n : _controller.getNetwork().getInputNodes())
			paintCell(g, n, Color.BLACK);
	}

	public void paintOutput(Graphics2D g)
	{
		for (Neuron n : _controller.getNetwork().getOutputNodes())
			paintCell(g, n, Color.BLACK);
	}

	public void paintHiddenLayer(Graphics2D g)
	{
		for (Neuron n : _controller.getNetwork().getHiddenNodes())
			paintCell(g, n, Color.BLACK);
	}
	
	public void paintCell(Graphics2D g, Neuron n, Color color)
	{
		g.setColor(Color.WHITE);
		g.fill(new Ellipse2D.Double(n.getX(), n.getY(), CELL_SIZE, CELL_SIZE));
		g.setColor(color);
		g.draw(new Ellipse2D.Double(n.getX(), n.getY(), CELL_SIZE, CELL_SIZE));
		String value = Double.toString(n.getOutput());
		int xOffset = CELL_SIZE/2 - _fontMetrics.stringWidth(value)/2;
		int yOffset = 1+CELL_SIZE/2 + _fontMetrics.getAscent()/2;
		g.setColor(color);
		g.drawString(value,n.getX()+xOffset,n.getY()+yOffset);
	}
	
	public void drawConnections(Graphics2D g, Neuron n, Color color)
	{
		g.setColor(color);
		int offset = CELL_SIZE/2;
		for (Connection c : n.outputConnections)
			g.drawLine(c._source.getX()+offset, c._source.getY()+offset, c._target.getX()+offset, c._target.getY()+offset);
	}

	public Dimension getMinimumSize()
	{
		return _minimumSize;
	}
	
	public void handleMouseClick(int x, int y)
	{
		Neuron n = null;
		if (y<CELL_SIZE*2)
		{
			int span = _width / _nrInputNodes;
			int index = (x - (span - CELL_SIZE)/2 - _margin) / span;
			if (index>=0 && index < _nrInputNodes)
				n = _controller.getNetwork().getInputNodes()[index];
		}
		else if (y>_height-CELL_SIZE*2)
		{
			int span = _width / _nrOutputNodes;
			int index = (x - (span - CELL_SIZE)/2 - _margin) / span;
			if (index>=0 && index < _nrOutputNodes)
				n = _controller.getNetwork().getOutputNodes()[index];
		}
		else
		{
			int index = (x-_margin) / CELL_SIZE;
			if (index>=0 && index < _nrHiddenNodes)
				n = _controller.getNetwork().getHiddenNodes()[index];
		}
		
		if (n!=null)
		{
			Rectangle r = new Rectangle(n.getX(),n.getY(),CELL_SIZE,CELL_SIZE);
			if (r.contains(x, y))
				_selectedNeuron = n;
			else
				_selectedNeuron = null;
		}
		else
			_selectedNeuron = null;
		_controller.setSelectedNeuron(_selectedNeuron);
		update(getGraphics());
	}

	public static void main(String[] args)
	{
		JPanel panel = new JPanel();
		NeuralNetwork network = new NeuralNetwork(5, 25, 5);
		NeuronController controller = new NeuronController();
		controller.setNetwork(network);
		NetworkPanel networkPanel = new NetworkPanel(controller);
		NeuronCommandPanel commandPanel = new NeuronCommandPanel(controller);
		
		panel.setLayout( new BorderLayout());
		panel.add(networkPanel, BorderLayout.CENTER);
		panel.add(commandPanel, BorderLayout.EAST);
		
		JFrame window = new JFrame();
		window.setLocation(200, 200);
		window.setSize(networkPanel._width+100, networkPanel._height+20);
		window.setContentPane(panel);
		//window.pack();
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
