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
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.sf.jlinkgrammar.Linkage;
import net.sf.jlinkgrammar.Parser;

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
	
	private static final String NOUN_LINKAGE = ".n";
	private static final String VERB_LINKAGE = ".v";
	private static final String ADJECTIVE_LINKAGE = ".a";
//	private static final String DETERMINATOR_LINKAGE = ".d";
	
	private JFrame window;
	private JTextField sentenceField;
	private JPanel analyzePanel;
	private JPanel buttonPanel;
	private Dictionary dictionary;

	private LexicalizedParser lp = new LexicalizedParser("grammar/englishPCFG.ser.gz");
    private TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
    private TreebankLanguagePack tlp = new PennTreebankLanguagePack();
    private GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
	
	private String originalSentence;
	private String[] originalTokens = new String[0];
	private String[] tokens = new String[0];
	private ArrayList<String>[] tokenLists = new ArrayList[0];
	private String[] ids;

	private HashMap<String,Integer> candidateMap = new HashMap<String,Integer>();
	private ArrayList<String> candidateList = new ArrayList<String>();
	private ArrayList<DefaultComboBoxModel> listList = new ArrayList<DefaultComboBoxModel>();
	private List<TypedDependency> connectionList;
	private Linkage linkage;

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
		Parser.InitializeVars(new String[]{});
	}

	public void initGUIComponents()
	{
		window = new JFrame();
		window.setLocation(new Point(200,600));
		window.setSize(800, 150);
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(3, 1));
		window.setContentPane(mainPanel);

		sentenceField = new JTextField();
		mainPanel.add(sentenceField);
		
		analyzePanel = new JPanel();
		analyzePanel.setLayout(new FlowLayout());
		mainPanel.add(analyzePanel);

		buttonPanel = new JPanel();
		JButton rememberButton = new JButton("Remember");
		buttonPanel.add(rememberButton);

		rememberButton.addActionListener(new ActionListener()
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					remember();
				}
			});

		JButton randomizeButton = new JButton("Randomize");
		buttonPanel.add(randomizeButton);
		randomizeButton.addActionListener(new ActionListener()
		{	
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				String sentence = dictionary.getRandomSentence();
				sentenceField.setText(sentence);
				setSentence(sentence);
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
				SwingUtilities.invokeLater(new Runnable()
					{	
						@Override
						public void run() 
						{
							analyzePanel.invalidate();
							window.validate();
						}
					});
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

	private String[] tokenize(String sentence)
	{
		originalTokens = sentence.split(" ");
		ArrayList<String> list = new ArrayList<String>(originalTokens.length);
		for (int i=0; i<originalTokens.length; i++)
		{
			/*if (i<originalTokens.length-1)
			{
				String doubleToken = originalTokens[i]+"_"+originalTokens[i+1];
				if (dictionary._index.get(doubleToken)!=null)
				{
					linkage.word.remove(i+1);
					list.add(doubleToken);
					i++;
					continue;
				}
			}*/
			list.add(originalTokens[i]);
		}
		String[] result = new String[list.size()];
		list.toArray(result);
		return result;
	}
	
	public void setSentence(String sentence)
	{
		originalSentence = sentence;
		linkage = Parser.parse(sentence);
		
		listList.clear();
		ArrayList<JComponent> componentList = new ArrayList<JComponent>();
		analyzePanel.removeAll();
		tokens = tokenize(sentence);
		tokenLists = new ArrayList[tokens.length];
		
		for (int i=0; i<tokens.length; i++)
		{
			ArrayList<String> tokenList = new ArrayList<String>();
			tokenLists[i] = tokenList;
			String token = tokens[i];
			tokenList.add(token);
			if (!tokenList.contains(token.toLowerCase()))
				tokenList.add(token.toLowerCase());
			dictionary.addVerb(token,tokenList);
			dictionary.addNoun(token,tokenList);
			dictionary.addAdjective(token,tokenList);
		}
		/*
		for (int i=0; i<tokens.length; i++)
		{
			String token = tokens[i];
			String verb = dictionary.getVerb(token);
			String noun = dictionary.makeSingular(token);
			String adjective = dictionary.getAdjective(token);
			if (noun!=null && verb!=null && !noun.equals(verb))
			{
				String linkageWord = linkage.word.get(i+1);
				if (linkageWord.endsWith(NOUN_LINKAGE))
					token = noun;
				else
					token = verb;
			}
			else if (verb!=null)
				token = verb;
			else if (noun!=null)
				token = noun;
			else if (adjective!=null)
				token = adjective;
			tokens[i] = token;
		}
		*/
			
		createConnectionList(sentence);
		initializeCandidates();
		
	    List<CoreLabel> rawWords = tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
	  	Tree parse = lp.apply(rawWords);
	    //GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		parse.pennPrint();
		
		ids = new String[tokens.length];
		int index = 0;
		for (ArrayList<String> tokenList: tokenLists)
		{
			final DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
			final int listIndex = index;
			final JComboBox comboBox = new JComboBox(comboBoxModel);

			for (String token : tokenList)
			{
				List<WordDefinition> list = dictionary._index.get(token);
				if (list==null)
				{
//					JLabel label = new JLabel("["+token+"]");
//					componentList.add(label);
//					analyzePanel.add(label);
//					ids[index] = token;
				}
				else if (list.size()<2)
				{
					comboBoxModel.addElement(list.get(0));
//					JLabel label = new JLabel(token);
//					componentList.add(label);
//					analyzePanel.add(label);
//					ids[index] = list.get(0).id;
//					label.setToolTipText(list.get(0).definition);
				}
				else
				{
					boolean isNoun = false;
					boolean isVerb = false;
					boolean isAdjective = false;
					
					String linkageWord = linkage.word.get(index+1);
					if (linkageWord.endsWith(NOUN_LINKAGE))
						isNoun = true;
					else if (linkageWord.endsWith(VERB_LINKAGE))
						isVerb = true;
					else if (linkageWord.endsWith(ADJECTIVE_LINKAGE))
						isAdjective = true;
					
					for (WordDefinition d : list)
					{
						if ((isNoun && d.type==WordType.NOUN)
						||  (isVerb && d.type==WordType.VERB)
						||  (isAdjective && d.type==WordType.ADJECTIVE))
							increaseRating(d.id, 1);
						
						comboBoxModel.addElement(d);
					}
				}
			}
			
			if (comboBoxModel.getSize()==0)
			{
				String token = tokenList.get(0);
				JLabel label = new JLabel("["+token+"]");
				componentList.add(label);
				analyzePanel.add(label);
				ids[index] = token;				
			}
			else if (comboBoxModel.getSize()==1)
			{
				WordDefinition wd = (WordDefinition)comboBoxModel.getElementAt(0);
				JLabel label = new JLabel(wd.word);
				componentList.add(label);
				analyzePanel.add(label);
				ids[index] = wd.id;
				label.setToolTipText(wd.definition);
			}
			else
			{
				componentList.add(comboBox);
				analyzePanel.add(comboBox);
				comboBox.addActionListener(new ActionListener()
					{	
						@Override
						public void actionPerformed(ActionEvent arg0)
						{
							WordDefinition wd = (WordDefinition) comboBoxModel.getSelectedItem();
							ids[listIndex] = wd.id;
							comboBox.setToolTipText(wd.definition);
						}
					});
				ids[index] = ((WordDefinition)comboBoxModel.getElementAt(0)).id;
				listList.add(comboBoxModel);
				comboBox.setSelectedIndex(0);
			}
			
			index++;
		}
		/*
		for (String token: tokens)
		{
			List<WordDefinition> list = dictionary._index.get(token);
			if (list==null)
			{
				JLabel label = new JLabel("["+token+"]");
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
				label.setToolTipText(list.get(0).definition);
			}
			else
			{
				boolean isNoun = false;
				boolean isVerb = false;
				boolean isAdjective = false;
				
				String linkageWord = linkage.word.get(index+1);
				if (linkageWord.endsWith(NOUN_LINKAGE))
					isNoun = true;
				else if (linkageWord.endsWith(VERB_LINKAGE))
					isVerb = true;
				else if (linkageWord.endsWith(ADJECTIVE_LINKAGE))
					isAdjective = true;
				
				final DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
				final int listIndex = index;
				final JComboBox comboBox = new JComboBox(comboBoxModel);
				for (WordDefinition d : list)
				{
					if ((isNoun && d.type==WordType.NOUN)
					||  (isVerb && d.type==WordType.VERB)
					||  (isAdjective && d.type==WordType.ADJECTIVE))
						increaseRating(d.id, 1);
					
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
							ids[listIndex] = wd.id;
							comboBox.setToolTipText(wd.definition);
						}
					});
				ids[index] = ((WordDefinition)comboBoxModel.getElementAt(0)).id;
				listList.add(comboBoxModel);
				comboBox.setSelectedIndex(0);
			}
			index++;
		}
		*/
		analyzePanel.invalidate();
		window.pack();
		for (String w : linkage.word)
			System.out.print(w+" ");
		System.out.println();
		
		rateCandidates();
		selectBestCandidates();
	}
	
	private void initializeCandidates()
	{
		candidateMap.clear();
		candidateList.clear();
		for (ArrayList<String> tokenList : tokenLists)
		{
			for (String token : tokenList)
			{
				List<WordDefinition> list = dictionary._index.get(token);
				if (list!=null)
				{
					for (WordDefinition definition : list)
					{
						Integer v = candidateMap.put(definition.id, definition.occurrences);
						if (v==null)
							candidateList.add(definition.id);
					}
				}
				/*else
				{
					Integer v = candidateMap.put(token, 0);
					if (v==null)
						candidateList.add(token);
				}*/
			}
		}
	}
	
	private void rateCandidates()
	{
		for (String candidateID : candidateList)
		{
			WordDefinition wd = dictionary.getDefinition(candidateID);
			int rating = analyze(candidateID,candidateID,1,false);
			rating += analyze(candidateID,candidateID,1,true);
			if (rating!=0)
				System.out.println("Rating for "+dictionary.getDefinition(candidateID)+" increased by "+rating);
			increaseRating(candidateID, rating);
		}		
	}
	
	private int analyze(String startId, String id, int level, boolean up)
	{
		int logLevel = (int)(Math.log(level)/Math.log(2));
		
		if (level>MAX_DEPTH)
			return 0;

		int rating = 0;
		WordDefinition wd = dictionary.getDefinition(id);
		if (wd==null)
			return 0;

		if (wd.line.toLowerCase().contains(originalSentence.toLowerCase()))
			rating += 1000 / level;

		if (up)
		{
			String via = "";
			if (!id.equals(startId))
				via = " via "+dictionary.getDefinition(startId);

			for (WordConnection connection : wd.connections)
			{
				// Potential self-referencing problem, giving undesired boost.
				if (connection.id1.equals(id) && hasConnection(startId, connection.id2, connection.relation))
				{
					System.out.println("Found "+logLevel+" level ["+connection.relation+"] connection between "+dictionary.getDefinition(id)+" and "+dictionary.getDefinition(connection.id2)+via);
					rating += BASE_RATING/level;
					increaseRating(connection.id2, BASE_RATING/level);
				}
				if (connection.id2.equals(id) &&  hasConnection(startId, connection.id1, connection.relation))
				{
					System.out.println("Found "+logLevel+" level ["+connection.relation+"] connection between "+dictionary.getDefinition(id)+" and "+dictionary.getDefinition(connection.id1)+via);
					rating += BASE_RATING/level;
					increaseRating(connection.id1, BASE_RATING/level);
				}
			}
		}
		
		for (DefinitionLink link : wd.links)
		{
			if (link.getType()==LinkType.HYPERNYM)
			{
				if (up)
					rating += analyze(startId, link.getWordDefinitionId(), level*2,true);
			}
			else
				if (!up)
					rating += analyze(startId, link.getWordDefinitionId(), level*2,false);		
		}
		
		return rating;
	}
	
	private void increaseRating(String id, int amount)
	{
		Integer storedRating = candidateMap.get(id);
		if (storedRating==null)
			return;
		storedRating += amount;
		candidateMap.put(id, storedRating);
	}
	
	private boolean hasConnection(String id1, String id2, String relation)
	{
    	WordDefinition definition1 = dictionary.getDefinition(id1);
    	WordDefinition definition2 = dictionary.getDefinition(id2);
    	if (definition1==null && definition2==null)
    	{
    		return false;
    	}
    	
	    for (TypedDependency dependency : connectionList)
	    {
	    	String compareRelation = dependency.reln().getShortName();
	    	if (relation.equals(compareRelation))
	    	{
	    		ArrayList<String> compareList1 = tokenLists[dependency.gov().index()-1];
	    		ArrayList<String> compareList2 = tokenLists[dependency.dep().index()-1];
		    	//String compareId1 = dependency.gov().value();
		    	//String compareId2 = dependency.dep().value();
//		    	String verb = dictionary.getVerb(compareId1);
//		    	if (verb!=null)
//		    		compareId1 = verb;
//		    	verb = dictionary.getVerb(compareId2);
//		    	if (verb!=null)
//		    		compareId2 = verb;
		    	
		    	if (definition1==null)
		    	{
		    		if (compareList1.contains(id1) &&  definition2.isWord(compareList2))
		    			return true;
		    		if (compareList2.contains(id1) &&  definition2.isWord(compareList1))
		    			return true;
		    	}
		    	else if (definition2==null)
		    	{
			    	if (definition1.isWord(compareList1) && compareList2.contains(id2))
			    		return true;
			    	if (definition1.isWord(compareList2) && compareList1.contains(id2))
			    		return true;
		    	}
		    	else
		    	{
			    	if (definition1.isWord(compareList1) && definition2.isWord(compareList2))
			    		return true;
			    	if (definition1.isWord(compareList2) && definition2.isWord(compareList1))
			    		return true;
		    	}
	    	}
	    }
		return false;
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

		createConnectionList(sentenceField.getText());
	    System.out.println(connectionList);
	    System.out.println();
	    for (TypedDependency dependency : connectionList)
	    {
	    	String relation = dependency.reln().getShortName();
	    	String id1 = dependency.gov().value();
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
	
	private void createConnectionList(String line)
	{
	    List<CoreLabel> rawWords = tokenizerFactory.getTokenizer(new StringReader(line)).tokenize();
	  	Tree parse = lp.apply(rawWords);
	    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
	    connectionList = gs.typedDependenciesCCprocessed(true);		
	}
}
