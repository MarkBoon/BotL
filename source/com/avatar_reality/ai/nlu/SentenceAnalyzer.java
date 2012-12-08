package com.avatar_reality.ai.nlu;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

public class SentenceAnalyzer
{
	private static final int MAX_DEPTH = 32;
	private static final int BASE_RATING = MAX_DEPTH*2;
	
	private JFrame window;
	private JTextField sentenceField;
	private JTextArea definitionField;
	private JPanel analyzePanel;
	private JPanel buttonPanel;
	private Dictionary dictionary;

	private LexicalizedParser lp = new LexicalizedParser("grammar/englishPCFG.ser.gz");
    private TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
    private TreebankLanguagePack tlp = new PennTreebankLanguagePack();
    private GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
	
	private String[] tokens = new String[0];
	private String[] ids;

	private HashMap<String,Integer> candidateMap = new HashMap<String,Integer>();
	private ArrayList<String> candidateList = new ArrayList<String>();
	private ArrayList<DefaultComboBoxModel> listList = new ArrayList<DefaultComboBoxModel>();
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		SentenceAnalyzer analyzer = new SentenceAnalyzer();
		analyzer.initDictionary();
		analyzer.initConnections();
		analyzer.initGUIComponents();
	}

	public void initDictionary()
	{
		dictionary = new Dictionary();
		dictionary.getFromDB();
	}

	public void initGUIComponents()
	{
		window = new JFrame();
		window.setLocation(new Point(200,600));
		window.setSize(800, 150);
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(4, 1));
		window.setContentPane(mainPanel);

		sentenceField = new JTextField();
		mainPanel.add(sentenceField);
		
		analyzePanel = new JPanel();
		analyzePanel.setLayout(new FlowLayout());
		mainPanel.add(analyzePanel);

		definitionField = new JTextArea();
		mainPanel.add(definitionField);

		buttonPanel = new JPanel();
		JButton analyzeButton = new JButton("Analayze");
		JButton rememberButton = new JButton("Remember");
		buttonPanel.add(analyzeButton);
		buttonPanel.add(rememberButton);
	
		analyzeButton.addActionListener(new ActionListener()
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					analyze();
				}
			});
		rememberButton.addActionListener(new ActionListener()
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					remember();
				}
			});
		
		mainPanel.add(buttonPanel);

		window.validate();
		
		sentenceField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String sentence = sentenceField.getText();
				setSentence(sentence);
				window.validate();
			}
		});
	}
	
	private void initConnections()
	{
		Transaction tx = null;
	    Session session = WordDefinition.getSessionFactory().openSession();

	    try
	    {
	    	tx = session.beginTransaction();
	    	Query query = session.createQuery("select c from WordConnection as c");
	    	@SuppressWarnings("unchecked")
	    	List<WordConnection> connections = (List<WordConnection>)query.list();
	    	for (WordConnection connection : connections)
	    	{
	    		dictionary.addConnection(connection);
	    	}
	    	tx.commit();
	    }
	    finally
	    {
	    	session.close();
	    }
		
	}

	public void setSentence(String sentence)
	{
		listList.clear();
		ArrayList<JComponent> componentList = new ArrayList<JComponent>();
		analyzePanel.removeAll();
		tokens = sentence.split(" ");
		ids = new String[tokens.length];
		int index = 0;
		for (String token: tokens)
		{
			String verb = dictionary.getVerb(token);
			if (verb!=null)
				token = verb;
			List<WordDefinition> list = dictionary._index.get(token);
			if (list==null)
			{
				JLabel label = new JLabel(token);
				componentList.add(label);
				analyzePanel.add(label);
				ids[index] = token;
			}
			else if (list.size()<2)
			{
				JLabel label = new JLabel(token);
				componentList.add(label);
				analyzePanel.add(label);
				ids[index] = list.get(0).id;
			}
			else
			{
				final DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
				final int listIndex = index;
				JComboBox comboBox = new JComboBox(comboBoxModel);
				for (WordDefinition d : list)
				{
					if (verb==null || d.type==WordType.VERB)
						comboBoxModel.addElement(d);
				}
				componentList.add(comboBox);
				analyzePanel.add(comboBox);
				comboBox.addActionListener(new ActionListener()
					{	
						@Override
						public void actionPerformed(ActionEvent arg0)
						{
							WordDefinition wd = (WordDefinition) comboBoxModel.getSelectedItem();
							definitionField.setText(wd.definition);
							ids[listIndex] = wd.id;
						}
					});
				ids[index] = ((WordDefinition)comboBoxModel.getElementAt(0)).id;
				listList.add(comboBoxModel);
			}
			index++;
		}
	}
	
	private void analyze()
	{
		initializeCandidates();
		rateCandidates();
		selectBestCandidates();
	}
	
	private void initializeCandidates()
	{
		candidateMap.clear();
		candidateList.clear();
		for (String token : tokens)
		{
			String verb = dictionary.getVerb(token);
			if (verb!=null)
				token = verb;

			List<WordDefinition> list = dictionary._index.get(token);
			if (list!=null)
			{
				for (WordDefinition definition : list)
				{
					Integer v = candidateMap.put(definition.id, 0);
					if (v==null)
						candidateList.add(definition.id);
				}
			}
			else
			{
				Integer v = candidateMap.put(token, 0);
				if (v==null)
					candidateList.add(token);
			}
		}
	}
	
	private void rateCandidates()
	{
		for (String candidateID : candidateList)
		{
			int rating = analyze(candidateID,1,false);
			rating += analyze(candidateID,1,true);
			int storedRating = candidateMap.get(candidateID);
			rating += storedRating;
			candidateMap.put(candidateID, new Integer(rating));
		}		
	}
	
	private int analyze(String id, int level, boolean up)
	{
		if (level>MAX_DEPTH)
			return 0;

		int rating = 0;
		WordDefinition wd = dictionary.getDefinition(id);
		if (wd==null)
			return 0;

		if (up)
		{
			for (WordConnection connection : wd.connections)
			{
				// Potential self-referencing problem, giving undesired boost.
				if (connection.id1.equals(id) && candidateMap.get(connection.id2)!=null)
				{
					rating += BASE_RATING/level;
					int storedRating = candidateMap.get(connection.id2);
					storedRating += BASE_RATING/level;
					candidateMap.put(connection.id2, new Integer(storedRating));
				}
				if (connection.id2.equals(id) && candidateMap.get(connection.id1)!=null)
				{
					rating += BASE_RATING/level;
					int storedRating = candidateMap.get(connection.id1);
					storedRating += BASE_RATING/level;
					candidateMap.put(connection.id1, new Integer(storedRating));
				}
			}
		}
		
		for (DefinitionLink link : wd.links)
		{
			if (link.getType()==LinkType.HYPERNYM)
			{
				if (up)
					rating += analyze(link.getWordDefinitionId(), level*2,true);
			}
			else
				if (!up)
					rating += analyze(link.getWordDefinitionId(), level*2,false);		
		}
		
		return rating;
	}
	
	private void selectBestCandidates()
	{
		for (DefaultComboBoxModel model : listList)
		{
			if (model.getSize()>0)
			{
				WordDefinition selectedWord = (WordDefinition)model.getElementAt(0);
				int bestRating = candidateMap.get(selectedWord.id);
				for (int i=1; i<model.getSize(); i++)
				{
					WordDefinition word = (WordDefinition)model.getElementAt(i);
					int rating = candidateMap.get(word.id);
					if (rating>bestRating)
					{
						bestRating = rating;
						selectedWord = word;
					}
				}
				model.setSelectedItem(selectedWord);
			}
		}
	}

	private void remember()
	{
		for (int i=0; i<ids.length; i++)
			System.out.println(ids[i]+" ");

	    List<CoreLabel> rawWords = tokenizerFactory.getTokenizer(new StringReader(sentenceField.getText())).tokenize();
	  	Tree parse = lp.apply(rawWords);
	    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
	    List<TypedDependency> tdl = gs.typedDependenciesCCprocessed(true);
	    System.out.println(tdl);
	    System.out.println();
	    for (TypedDependency dependency : tdl)
	    {
	    	String relation = dependency.reln().getShortName();
	    	String id1 = dependency.gov().value();
	    	dependency.gov().index();
	    	String id2 = dependency.dep().value();
		    System.out.println(relation+"("+id1+"-"+dependency.gov().index()+","+id2+"-"+dependency.dep().index()+")");
		    if (dependency.gov().index()>0)
		    {
		    	WordConnection wordConnection = new WordConnection();
		    	wordConnection.id1 = ids[dependency.gov().index()-1];
		    	wordConnection.id2 = ids[dependency.dep().index()-1];
		    	wordConnection.relation = relation;
			    System.out.println(relation+"("+wordConnection.id1+","+wordConnection.id2+")");
		    	dictionary.incrementConnection(wordConnection);
		    }
	    }
	}
}
