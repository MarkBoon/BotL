package com.avatar_reality.ai.nlu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.stanford.nlp.trees.TypedDependency;

public class Dictionary
{
	HashMap<String,List<WordDefinition>> _index = new HashMap<String, List<WordDefinition>>();
	HashMap<String,WordDefinition> _dictionary = new HashMap<String, WordDefinition>();
	HashMap<String,String> _irregularVerbs = new HashMap<String, String>();
	HashMap<String,String> _irregularNouns = new HashMap<String, String>();
	HashMap<String,String> _irregularAdjectives = new HashMap<String, String>();
	HashMap<String,String> _irregularAdverbs = new HashMap<String, String>();
	ArrayList<WordDefinition> _wordList = new ArrayList<WordDefinition>();

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		long t = System.currentTimeMillis();
		Dictionary dictionary = new Dictionary();
		dictionary.getExtra();
		long t1 = System.currentTimeMillis() - t;
		System.out.println("Parsing took "+t1+" ms.");
		System.out.println("Parsed "+dictionary._dictionary.size()+" words");
		System.out.println("Indexed "+dictionary._index.size()+" words");
	}
	
	public void getDefault() throws Exception
	{
		parse("data.noun");
		parse("data.verb");
		parse("data.adj");
		parse("data.adv");
		parse("extra.exc");
		parseExceptions("verb.exc",_irregularVerbs);
		parseExceptions("noun.exc",_irregularNouns);
		parseExceptions("adj.exc",_irregularAdjectives);
		parseExceptions("adv.exc",_irregularAdverbs);

		WordDefinition.save(_wordList);
	}
	
	public void getExtra() throws Exception
	{
		parse("extra.exc");

		WordDefinition.saveNew(_wordList);
	}
	
	public void getFromDB()
	{
//		parse("extra.exc");
		parseExceptions("verb.exc",_irregularVerbs);
		parseExceptions("noun.exc",_irregularNouns);
		parseExceptions("adj.exc",_irregularAdjectives);
		parseExceptions("adv.exc",_irregularAdverbs);

		Transaction tx = null;
	    Session session = WordDefinition.getSessionFactory().openSession();

	    int nrDefinitions = 0;
	    
	    try
	    {
	    	tx = session.beginTransaction();
	    	Query query = session.createQuery("select wd from WordDefinition as wd");
	    	@SuppressWarnings("unchecked")
			List<WordDefinition> records = query.list();
	    	for (Iterator<WordDefinition> iter = records.iterator(); iter.hasNext();)
	    	{
	    		WordDefinition wd = iter.next();
	    		wd.parseLine(wd.line);
	    		addDefinition(wd);
	    		nrDefinitions += wd.synonyms.size();
	    	}
	    	tx.commit();
	    }
	    finally
	    {
	    	session.close();
	    }
	    System.out.println("Nr of indexes: "+_index.size());
	    System.out.println("Nr of definitions: "+nrDefinitions);
	}
	
	public void parse(String file)
	{
		try 
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while (reader.ready())
			{
				String line = reader.readLine();
				if (!line.startsWith("  "))
					parseWordLine(line.trim());
			}
			reader.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void parseExceptions(String file, HashMap<String,String> exceptions)
	{
		try 
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while (reader.ready())
			{
				String line = reader.readLine();
				if (!line.startsWith("  "))
				{
					String[] tokens = line.split(" ");
					exceptions.put(tokens[0], tokens[1]);
				}
			}
			reader.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void parseWordLine(String line)
	{
		if (line.length()==0)
			return;

		WordDefinition definition = WordDefinition.parse(line);
		addDefinition(definition);
		
//		try 
//		{
//			definition.save();
//		}
//		catch (Exception e) 
//		{
//			e.printStackTrace();
//		}
//
	}
	public void addDefinition(WordDefinition definition)
	{
		int nrSynonyms = definition.synonyms.size();
		_dictionary.put(definition.id,definition);
		_wordList.add(definition);
		definition.dictionary = this;
		
		for (int i=0; i<nrSynonyms; i++)
		{
			String word = definition.synonyms.get(i);
			List<WordDefinition> list = _index.get(word);
			if (list==null)
			{
				list = new ArrayList<WordDefinition> ();
				_index.put(word, list);
			}
			list.add(definition);
		}			
	}
	
	public WordDefinition getDefinition(String id)
	{
		return _dictionary.get(id);
	}
	
	private void add(String word, ArrayList<String> list)
	{
		if (word!=null && !list.contains(word))
			list.add(word);
	}
	
	public void addVerb(String word, ArrayList<String> list)
	{
		String verb = _irregularVerbs.get(word);
		add(verb,list);
		if (word.endsWith("ing"))
		{
			verb = word.substring(0,word.length()-3);
			if (isVerb(verb))
				add(verb,list);
			verb = word.substring(0,word.length()-3)+"e";
			if (isVerb(verb))
				add(verb,list);
		}
		else if (word.endsWith("ed"))
		{
			verb = word.substring(0,word.length()-2);
			if (isVerb(verb))
				add(verb,list);			
			verb = word.substring(0,word.length()-1);
			if (isVerb(verb))
				add(verb,list);		
		}
		else if (word.endsWith("es"))
		{
			verb = word.substring(0,word.length()-2);
			if (isVerb(verb))
				add(verb,list);
		}
		else if (word.endsWith("s"))
		{
			verb = word.substring(0,word.length()-1);
			if (isVerb(verb))
				add(verb,list);
		}
	}
	
	public boolean isVerb(String word)
	{
		List<WordDefinition> list = _index.get(word);
		if (list==null)
			return false;
		for (WordDefinition wd : list)
			if (wd.type == WordType.VERB)
				return true;
		return false;
	}
	
	public void addNoun(String word, ArrayList<String> list)
	{
		String noun = _irregularNouns.get(word);
		add(noun,list);
		if (word.endsWith("ies"))
		{
			String singular = word.substring(0,word.length()-3)+"y";
			if (isNoun(singular))
				add(singular,list);
		}
		if (word.endsWith("s") && !word.endsWith("ss"))
		{
			String singular = word.substring(0,word.length()-1);
			if (isNoun(singular))
				add(singular,list);
		}
	}
	
	public boolean isNoun(String word)
	{
		List<WordDefinition> list = _index.get(word);
		if (list==null)
			return false;
		for (WordDefinition wd : list)
			if (wd.type == WordType.NOUN)
				return true;
		return false;
	}
	
	public void addAdjective(String word, ArrayList<String> list)
	{
		String adjective = _irregularAdjectives.get(word);
		add(adjective,list);
		if (word.endsWith("ier"))
		{
			adjective = word.substring(0,word.length()-3)+"y";
			if (isAdjective(adjective))
				add(adjective,list);
		}
		else if (word.endsWith("ily"))
		{
			adjective = word.substring(0,word.length()-3)+"y";
			if (isAdjective(adjective))
				add(adjective,list);
		}
		else if (word.endsWith("iest"))
		{
			adjective = word.substring(0,word.length()-4)+"y";
			if (isAdjective(adjective))
				add(adjective,list);
		}
		else if (word.endsWith("er"))
		{
			adjective = word.substring(0,word.length()-2);
			if (isAdjective(adjective))
				add(adjective,list);
			adjective = word.substring(0,word.length()-1);
			if (isAdjective(adjective))
				add(adjective,list);
		}
		else if (word.endsWith("est"))
		{
			adjective = word.substring(0,word.length()-3);
			if (isAdjective(adjective))
				add(adjective,list);			
			adjective = word.substring(0,word.length()-2);
			if (isAdjective(adjective))
				add(adjective,list);			
		}
		else if (word.endsWith("ly"))
		{
			adjective = word.substring(0,word.length()-2);
			if (isAdjective(adjective))
				add(adjective,list);
		}
	}
	
	public boolean isAdjective(String word)
	{
		List<WordDefinition> list = _index.get(word);
		if (list==null)
			return false;
		for (WordDefinition wd : list)
			if (wd.type == WordType.ADJECTIVE)
				return true;
		return false;
	}
	
	public void addConnection(WordConnection connection)
	{
		WordDefinition d1 = getDefinition(connection.id1);
		if (d1!=null)
		{
			d1.connections.add(connection);
		}
		WordDefinition d2 = getDefinition(connection.id2);
		if (d2!=null)
		{
			d2.connections.add(connection);
		}
		if (connection.relation.startsWith("prep_"))
		{
			String preposition = connection.relation.substring(5);
			List<WordDefinition> list = _index.get(preposition);
			if (list==null)
			{
//				System.err.println("Unknown preposition: "+connection.relation);
			}
			else
			{
				for (WordDefinition wd : list)
				{
					if (wd.type==WordType.PREPOSITION)
						wd.connections.add(connection);
				}
			}
		}
	}
	
	public void incrementConnection(WordConnection wordConnection)
	{
		Transaction tx = null;
	    Session session = WordDefinition.getSessionFactory().openSession();

	    try
	    {
	    	tx = session.beginTransaction();
	    	Query query = session.createQuery("select c from WordConnection as c where c.id1='"+wordConnection.id1+"' and c.id2='"+wordConnection.id2+"' and c.relation='"+wordConnection.relation+"'");
			WordConnection connection = (WordConnection)query.uniqueResult();
	    	if (connection!=null)
	    	{
	    		connection.occurences++;
	    		session.update(connection);
	        }
	    	else
	    	{
	    		System.err.println("New connection");
	    		session.save(wordConnection);
	    		addConnection(wordConnection);
	    	}
	    	WordDefinition word1 = _dictionary.get(wordConnection.id1);
	    	if (word1!=null)
	    	{
	    		word1.increaseOccurrence();
	    		session.update(word1);	    		
	    	}
	    	WordDefinition word2 = _dictionary.get(wordConnection.id2);
	    	if (word2!=null)
	    	{
	    		word2.increaseOccurrence();
	    		session.update(word2);	    		
	    	}
            session.flush();	    	    		
	    	tx.commit();
	    }
	    finally
	    {
	    	session.close();
	    }
	}
	
	public String getRandomSentence()
	{
		while (true)
		{
			int index = (int)(Math.random()*_wordList.size());
			WordDefinition definition = _wordList.get(index);
			String explanation = definition.line.substring(definition.line.indexOf("|"));
			String[] sentences = explanation.split("; ");
			if (sentences.length>1)
			{
				index = (int) (Math.random() * (sentences.length-1)) + 1;
				String sentence = sentences[index];
				if (sentence.startsWith("\"") && sentence.endsWith("\""))
					return sentence.substring(1,sentence.length()-1);
			}
		}
	}
	
	public void createRandomConnection(SentenceAnalyzer analyzer)
	{
		WordDefinition definition = null;
		while (true)
		{
//			if (!found && !sentence.isEmpty())
//			{
//				System.err.println("Skip: "+sentence);
//			}
//			found = false;
			
			int index = (int)(Math.random()*_wordList.size());
			definition = _wordList.get(index);
			createConnections(definition, analyzer);	
		}
	}

	public void createAllConnections(SentenceAnalyzer analyzer)
	{
		for (WordDefinition wd : _dictionary.values())
		{
			createConnections(wd,analyzer);
		}
	}
	
	public boolean createConnections(WordDefinition definition, SentenceAnalyzer analyzer)
	{
		WordDefinition altDefinition;
		boolean found = false;
		String explanation = definition.line.substring(definition.line.indexOf("|"));
		String[] sentences = explanation.split("; ");
		for (String sentence : sentences)
		{
			sentence = sentence.replace("n't", " not");
			sentence = sentence.replace(",","");
			sentence = sentence.replace(";","");
			sentence = sentence.replace(".","");
			sentence = sentence.replace("?","");
			if (sentence.contains("'"))
				continue;
			sentence = sentence.trim();
			if (sentence.startsWith("\"") && sentence.endsWith("\""))
			{
				List<TypedDependency> dependencies = analyzer.createConnectionList(sentence);
				for (TypedDependency dependency : dependencies)
				{		
					if (!dependency.reln().getShortName().equals("prep"))
						continue;
			    	String relation = dependency.reln().toString();
			    	String idString1 = dependency.gov().value().toLowerCase();
			    	String idString2 = dependency.dep().value().toLowerCase();
			    	
			    	ArrayList<String> id1List = new ArrayList<String>();
			    	id1List.add(idString1);
			    	addAdjective(idString1, id1List);
			    	addNoun(idString1, id1List);
			    	addVerb(idString1, id1List);
			    	ArrayList<String> id2List = new ArrayList<String>();
			    	id2List.add(idString2);
			    	addAdjective(idString2, id2List);
			    	addNoun(idString2, id2List);
			    	addVerb(idString2, id2List);
			    	
			    	for (String id1 : id1List)
			    	{
			    		for (String id2 : id2List)
			    		{
						    if (dependency.gov().index()>0)
						    {
							    System.out.println(relation+"("+id1+"-"+dependency.gov().index()+","+id2+"-"+dependency.dep().index()+")");
						    	ArrayList<String> wordList = new ArrayList<String>();
						    	wordList.add(id1);
						    	if (definition.isWord(wordList))
						    	{
							    	WordConnection wordConnection = new WordConnection();
							    	wordConnection.relation = relation;
							    	wordConnection.id1 =definition.id;
						    		List<WordDefinition> wd2List = (List<WordDefinition>) _index.get(id2);
						    		if (wd2List!=null && wd2List.size()==1)
								    	wordConnection.id2 = wd2List.get(0).id;
						    		else
								    	wordConnection.id2 = id2;
								    System.out.println("1-Save "+relation+"("+wordConnection.id1+","+wordConnection.id2+")");
							    	incrementConnection(wordConnection);
							    	found = true;
						    	}
						    	
						    	wordList = new ArrayList<String>();
						    	wordList.add(id2);
						    	if (definition.isWord(wordList))
						    	{
							    	WordConnection wordConnection = new WordConnection();
							    	wordConnection.id2 =definition.id;
						    		List<WordDefinition> wd1List = (List<WordDefinition>) _index.get(id1);
						    		if (wd1List!=null && wd1List.size()==1)
								    	wordConnection.id1 = wd1List.get(0).id;
						    		else
								    	wordConnection.id1 = id1;
							    	wordConnection.relation = relation;
								    System.out.println("2-Save "+relation+"("+wordConnection.id1+","+wordConnection.id2+")");
							    	incrementConnection(wordConnection);
							    	found = true;
						    	}
						    	
						    	if ((id1List.size()==1 || (id1List.size()==2 && _index.get(id1List.get(0))==null)) && _index.get(id1)!=null && _index.get(id1).size()==1)
						    	{
						    		altDefinition = _index.get(id1).get(0);
							    	WordConnection wordConnection = new WordConnection();
							    	wordConnection.relation = relation;
							    	wordConnection.id1 =altDefinition.id;
						    		List<WordDefinition> wd2List = (List<WordDefinition>) _index.get(id2);
						    		if (wd2List!=null && wd2List.size()==1)
								    	wordConnection.id2 = wd2List.get(0).id;
						    		else
								    	wordConnection.id2 = id2;
								    System.out.println("3-Save "+relation+"("+wordConnection.id1+","+wordConnection.id2+")");
							    	incrementConnection(wordConnection);
							    	found = true;
						    	}
						    	
						    	if ((id2List.size()==1 || (id2List.size()==2 && _index.get(id2List.get(0))==null)) && _index.get(id2)!=null && _index.get(id2).size()==1)
						    	{
						    		altDefinition = _index.get(id2).get(0);
							    	WordConnection wordConnection = new WordConnection();
							    	wordConnection.id2 =altDefinition.id;
						    		List<WordDefinition> wd1List = (List<WordDefinition>) _index.get(id1);
						    		if (wd1List!=null && wd1List.size()==1)
								    	wordConnection.id1 = wd1List.get(0).id;
						    		else
								    	wordConnection.id1 = id1;
							    	wordConnection.relation = relation;
								    System.out.println("4-Save "+relation+"("+wordConnection.id1+","+wordConnection.id2+")");
							    	incrementConnection(wordConnection);
							    	found = true;
						    	}
						    }
			    		}
			    	}
				}
			}
		}
		return found;
	}
}
