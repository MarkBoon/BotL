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

public class Dictionary
{
	HashMap<String,List<WordDefinition>> _index = new HashMap<String, List<WordDefinition>>();
	HashMap<String,WordDefinition> _dictionary = new HashMap<String, WordDefinition>();
	HashMap<String,String> _irregularVerbs = new HashMap<String, String>();
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		long t = System.currentTimeMillis();
		Dictionary dictionary = new Dictionary();
		dictionary.getDefault();
		long t1 = System.currentTimeMillis() - t;
		System.out.println("Parsing took "+t1+" ms.");
		System.out.println("Parsed "+dictionary._dictionary.size()+" words");
		System.out.println("Indexed "+dictionary._index.size()+" words");
	}
	
	public void getDefault()
	{
		parse("data.noun");
		parse("data.verb");
		parse("data.adj");
		parse("data.adv");
		parseVerbs("verb.exc");
	}
	
	public void getFromDB()
	{
		parseVerbs("verb.exc"); // TODO

		Transaction tx = null;
	    Session session = WordDefinition.getSessionFactory().openSession();

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
	    	}
	    	tx.commit();
	    }
	    finally
	    {
	    	session.close();
	    }
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
					parseWordLine(line);
			}
			reader.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void parseVerbs(String file)
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
					_irregularVerbs.put(tokens[0], tokens[1]);
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
	
	public String getVerb(String word)
	{
		String verb = _irregularVerbs.get(word);
		if (verb!=null)
			return verb;
		if (word.endsWith("ing"))
		{
			verb = word.substring(0,word.length()-3);
			if (isVerb(verb))
				return verb;
		}
		else if (word.endsWith("ed"))
		{
			verb = word.substring(0,word.length()-2);
			if (isVerb(verb))
				return verb;			
		}
		else if (word.endsWith("s"))
		{
			verb = word.substring(0,word.length()-1);
			if (isVerb(verb))
				return verb;
		}
		return word;
	}
	
	public boolean isVerb(String verb)
	{
		List<WordDefinition> list = _index.get(verb);
		if (list==null)
			return false;
		for (WordDefinition wd : list)
			if (wd.type == WordType.VERB)
				return true;
		return false;
	}
	
	public void addConnection(WordConnection connection)
	{
		if (getDefinition(connection.id1)!=null)
			getDefinition(connection.id1).connections.add(connection);
		if (getDefinition(connection.id2)!=null)
			getDefinition(connection.id2).connections.add(connection);
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
	    		session.save(wordConnection);
	    		addConnection(wordConnection);
	    	}
            session.flush();	    	    		
	    	tx.commit();
	    }
	    finally
	    {
	    	session.close();
	    }
	}
}
