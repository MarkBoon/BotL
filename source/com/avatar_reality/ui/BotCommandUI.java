package com.avatar_reality.ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.GridLayout;
import javax.swing.JSplitPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JEditorPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import javax.swing.SpringLayout;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JScrollPane;

import com.avatar_reality.ai.BotCommandPrinter;
import com.avatar_reality.ai.BotCommandProcessor;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class BotCommandUI {

	private JFrame frame;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) 
	{
		EventQueue.invokeLater(new Runnable() 
		{
			public void run() 
			{
				try 
				{
					BotCommandUI window = new BotCommandUI();
					window.frame.setVisible(true);

					BotCommandProcessor.create("TestBot", new BotCommandPrinter());
					BotCommandProcessor.getSingleton().loadTriggers(args[0]);
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public BotCommandUI() 
	{
		initialize();

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() 
	{
		frame = new JFrame();
		frame.setBounds(100, 100, 878, 522);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SpringLayout springLayout = new SpringLayout();
		frame.getContentPane().setLayout(springLayout);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		springLayout.putConstraint(SpringLayout.NORTH, splitPane, 0, SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, splitPane, 0, SpringLayout.WEST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, splitPane, 0, SpringLayout.SOUTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, splitPane, 0, SpringLayout.EAST, frame.getContentPane());
		frame.getContentPane().add(splitPane);
		
		JPanel panel = new JPanel();
		splitPane.setLeftComponent(panel);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setResizeWeight(0.3);
		sl_panel.putConstraint(SpringLayout.NORTH, splitPane_1, 0, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, splitPane_1, 0, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.SOUTH, splitPane_1, 0, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, splitPane_1, 0, SpringLayout.EAST, panel);
		splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		panel.add(splitPane_1);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "Chat", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPane_1.setLeftComponent(panel_2);
		SpringLayout sl_panel_2 = new SpringLayout();
		panel_2.setLayout(sl_panel_2);
		
		textField = new JTextField();
		textField.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				handleInput();
			}
		});
		sl_panel_2.putConstraint(SpringLayout.NORTH, textField, 0, SpringLayout.NORTH, panel_2);
		sl_panel_2.putConstraint(SpringLayout.WEST, textField, 0, SpringLayout.WEST, panel_2);
		sl_panel_2.putConstraint(SpringLayout.EAST, textField, 0, SpringLayout.EAST, panel_2);
		panel_2.add(textField);
		textField.setColumns(10);
		
		JTextArea textArea_3 = new JTextArea();
		textArea_3.setEditable(false);
		sl_panel_2.putConstraint(SpringLayout.NORTH, textArea_3, 30, SpringLayout.NORTH, textField);
		sl_panel_2.putConstraint(SpringLayout.WEST, textArea_3, 0, SpringLayout.WEST, panel_2);
		sl_panel_2.putConstraint(SpringLayout.SOUTH, textArea_3, 0, SpringLayout.SOUTH, panel_2);
		sl_panel_2.putConstraint(SpringLayout.EAST, textArea_3, 0, SpringLayout.EAST, panel_2);
		panel_2.add(textArea_3);
		
		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new TitledBorder(null, "Console", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPane_1.setRightComponent(panel_3);
		SpringLayout sl_panel_3 = new SpringLayout();
		panel_3.setLayout(sl_panel_3);
		
		JScrollPane scrollPane = new JScrollPane();
		sl_panel_3.putConstraint(SpringLayout.NORTH, scrollPane, 0, SpringLayout.NORTH, panel_3);
		sl_panel_3.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, panel_3);
		sl_panel_3.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.SOUTH, panel_3);
		sl_panel_3.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, panel_3);
		panel_3.add(scrollPane);
		
		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		
		JPanel panel_1 = new JPanel();
		splitPane.setRightComponent(panel_1);
		SpringLayout sl_panel_1 = new SpringLayout();
		panel_1.setLayout(sl_panel_1);
		
		JSplitPane splitPane_2 = new JSplitPane();
		splitPane_2.setResizeWeight(0.3);
		splitPane_2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		sl_panel_1.putConstraint(SpringLayout.NORTH, splitPane_2, 0, SpringLayout.NORTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.WEST, splitPane_2, 0, SpringLayout.WEST, panel_1);
		sl_panel_1.putConstraint(SpringLayout.SOUTH, splitPane_2, 0, SpringLayout.SOUTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.EAST, splitPane_2, 0, SpringLayout.EAST, panel_1);
		panel_1.add(splitPane_2);
		
		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new TitledBorder(null, "Query", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPane_2.setLeftComponent(panel_4);
		SpringLayout sl_panel_4 = new SpringLayout();
		panel_4.setLayout(sl_panel_4);
		
		JButton btnNewButton = new JButton("Execute");
		sl_panel_4.putConstraint(SpringLayout.WEST, btnNewButton, 0, SpringLayout.WEST, panel_4);
		sl_panel_4.putConstraint(SpringLayout.SOUTH, btnNewButton, 0, SpringLayout.SOUTH, panel_4);
		panel_4.add(btnNewButton);
		
		JTextArea textArea_2 = new JTextArea();
		sl_panel_4.putConstraint(SpringLayout.NORTH, textArea_2, 0, SpringLayout.NORTH, panel_4);
		sl_panel_4.putConstraint(SpringLayout.WEST, textArea_2, 0, SpringLayout.WEST, panel_4);
		sl_panel_4.putConstraint(SpringLayout.SOUTH, textArea_2, -6, SpringLayout.NORTH, btnNewButton);
		sl_panel_4.putConstraint(SpringLayout.EAST, textArea_2, 0, SpringLayout.EAST, panel_4);
		panel_4.add(textArea_2);
		
		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new TitledBorder(null, "Result", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPane_2.setRightComponent(panel_5);
		SpringLayout sl_panel_5 = new SpringLayout();
		panel_5.setLayout(sl_panel_5);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		sl_panel_5.putConstraint(SpringLayout.NORTH, scrollPane_1, 0, SpringLayout.NORTH, panel_5);
		sl_panel_5.putConstraint(SpringLayout.WEST, scrollPane_1, 0, SpringLayout.WEST, panel_5);
		sl_panel_5.putConstraint(SpringLayout.SOUTH, scrollPane_1, 0, SpringLayout.SOUTH, panel_5);
		sl_panel_5.putConstraint(SpringLayout.EAST, scrollPane_1, 0, SpringLayout.EAST, panel_5);
		panel_5.add(scrollPane_1);
		
		JTextArea textArea_1 = new JTextArea();
		textArea_1.setEditable(false);
		scrollPane_1.setViewportView(textArea_1);
	}
	
	private void handleInput()
	{
		String input = textField.getText();
		if (input.equals("quit"))
			System.exit(0);
		long t1 = System.currentTimeMillis();
		String output = BotCommandProcessor.getSingleton().receiveSayAction("test_bot", input);
		long t2 = System.currentTimeMillis();
		System.out.println("Processing time: "+(t2-t1)+" ms.");		
	}
}
