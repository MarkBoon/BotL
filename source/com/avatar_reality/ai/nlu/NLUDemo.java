package com.avatar_reality.ai.nlu;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class NLUDemo
{
	private Dictionary dictionary;
	
	private JFrame window;
	private JTextField wordField;
	private JTextArea definitionArea;
	private JComboBox synonymList;
	private JComboBox hypernymList;
	private JComboBox hyponymList;
	
	private DefaultComboBoxModel synonymListModel;
	private DefaultComboBoxModel hypernymListModel;
	private DefaultComboBoxModel hyponymListModel;
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);
	
		NLUDemo demo = new NLUDemo();
		demo.initDictionary();
		demo.initGUIComponents();
	}

	public void initDictionary()
	{
		dictionary = new Dictionary();
		dictionary.getFromDB();
	}

	public void initGUIComponents()
	{	
		window = new JFrame();
		window.setLocation(new Point(200,200));
		window.setSize(800, 400);
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(1, 2));
		window.setContentPane(mainPanel);
		
		JPanel textPanel = new JPanel();
		wordField = new JTextField();
		textPanel.setLayout(new BorderLayout());
		textPanel.add(wordField,BorderLayout.NORTH);
		definitionArea = new JTextArea();
		definitionArea.setLineWrap(true);
		textPanel.add(definitionArea);
		window.add(textPanel);
		
		JPanel selectionPanel = new JPanel();
		selectionPanel.setLayout(new GridLayout(3,1));
		synonymList = new JComboBox();
		hypernymList = new JComboBox();
		hyponymList = new JComboBox();
	
		JPanel synonymPanel = new JPanel();
		synonymPanel.setLayout(new FlowLayout());
		JButton synonymButton = new JButton("Select");
		synonymPanel.add(synonymList);
		synonymPanel.add(synonymButton);

		selectionPanel.add(hypernymList);
		selectionPanel.add(synonymPanel);
		selectionPanel.add(hyponymList);

		synonymButton.addActionListener(new ActionListener()
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					WordDefinition selectedWord = (WordDefinition)synonymListModel.getSelectedItem();
					if (selectedWord!=null)
					{
						wordField.setText(selectedWord.word);
						selectWord(selectedWord.word);
					}
				}
			});
		
		synonymListModel = new DefaultComboBoxModel();
		hypernymListModel = new DefaultComboBoxModel();
		hyponymListModel = new DefaultComboBoxModel();
		
		synonymList.setModel(synonymListModel);
		hypernymList.setModel(hypernymListModel);
		hyponymList.setModel(hyponymListModel);
		
		window.add(selectionPanel);
		
		wordField.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					String selectedWord = wordField.getText();
					selectWord(selectedWord);
				}
			});
		
		synonymList.addActionListener(new ActionListener()
			{
				
				@Override
				public void actionPerformed(ActionEvent event)
				{
					WordDefinition selectedWord = (WordDefinition)synonymListModel.getSelectedItem();
					if (selectedWord!=null)
						selectWordDefinition(selectedWord);
				}
			});		
	}
	
	public void selectWord(String word)
	{
		if (word!=null && !word.isEmpty())
		{
			List<WordDefinition> wordList = dictionary._index.get(word);
			if (wordList!=null)
			{
				boolean found = false;
				synonymListModel.removeAllElements();
				for (int i=0; i<wordList.size(); i++)
				{
					WordDefinition definition = wordList.get(i);
					synonymListModel.addElement(definition);
					if (!found && definition.toString().startsWith(word))
					{
						synonymListModel.setSelectedItem(wordList.get(i));
						found = true;
					}
				}
				if (!found)
					synonymListModel.setSelectedItem(wordList.get(0));
			}
		}
	}
	
	public void selectWordDefinition(WordDefinition definition)
	{
		definitionArea.setText(definition.definition);
		hypernymListModel.removeAllElements();
		hyponymListModel.removeAllElements();
		for (DefinitionLink link : definition.links)
		{
			if (link.getType()==LinkType.HYPERNYM)
			{
				WordDefinition parentDefinition = dictionary._dictionary.get(link.getWordDefinitionId());
				if (parentDefinition!=null)
					hypernymListModel.addElement(parentDefinition);
			}
			if (link.getType()!=LinkType.HYPERNYM)
			{
				WordDefinition reference = dictionary._dictionary.get(link.getWordDefinitionId());
				if (reference!=null)
					hyponymListModel.addElement(reference);
			}
		}
	}
}
