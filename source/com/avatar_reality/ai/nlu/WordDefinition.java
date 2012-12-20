package com.avatar_reality.ai.nlu;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

@Entity
@Table(name="word_definition")
public class WordDefinition
{
    @Id
    @Column(name="id")
	protected String id;
    @Column(name="line")
	protected String line;
    @Column(name="occurrences")
	protected int occurrences;
    @Transient
	protected WordType type;
    @Transient
	protected String word;
    @Transient
	protected List<String> synonyms = new ArrayList<String>();
    @Transient
	protected List<DefinitionLink> links = new ArrayList<DefinitionLink>();
	
    @Transient
	protected String definition;

    @Transient
	protected List<WordConnection> connections = new ArrayList<WordConnection>();
	
    @Transient
	protected Dictionary dictionary;
	
    public WordDefinition()
    {	
    }
    
	public String toString()
	{
		String synonym = "?";
		String typeString = "";
		if (type!=null)
			typeString = " ("+type.toString()+")";
		if (synonyms.size()>1)
		{
			synonym = synonyms.get(1);
		}
		else
		{
			for (DefinitionLink link : links)
			{
				if (link.getType()==LinkType.HYPERNYM)
				{
					WordDefinition parent = dictionary._dictionary.get(link.getWordDefinitionId());
					if (parent!=null)
						synonym = parent.word;
				}
			}
		}
		if (synonym=="?" && links.size()>0)
		{
			WordDefinition link = dictionary._dictionary.get(links.get(0).getWordDefinitionId());
			if (link!=null)
				synonym = link.word;
		}
		return  synonyms.get(0)+typeString+" - ["+synonym+"]";
	}
	
	public boolean isWord(String word)
	{
		for (String w : synonyms)
			if (w.equals(word) || w.endsWith("_"+word))
				return true;
		return false;
	}
	
	public String getId()
	{
		return id;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public int getOccurrences()
	{
		return occurrences;
	}
	
	public void setOccurrences(int occurrences)
	{
		this.occurrences = occurrences;
	}
	
	public void increaseOccurrence()
	{
		occurrences++;
	}

	public String getLine()
	{
		return line;
	}

	public void setLine(String line)
	{
		this.line = line;
		if (line.length()>10000)
			System.err.println("Line too long");
		parseLine(line);
	}

	private static ServiceRegistry serviceRegistry;
	private static SessionFactory sessionFactory;

	public static SessionFactory getSessionFactory()
	{
		if (sessionFactory==null)
		{
			try
			{
				Configuration configuration = new Configuration();
				configuration.configure();
				serviceRegistry = new ServiceRegistryBuilder().applySettings(
						configuration.getProperties()).buildServiceRegistry();
				sessionFactory = configuration.buildSessionFactory(serviceRegistry);
			}
			catch (Throwable ex)
			{
				System.err.println("Failed to create sessionFactory object." + ex);
				throw new ExceptionInInitializerError(ex);
			}
		}
		return sessionFactory;
	}
	
	public void save() throws Exception
	{
        boolean existingTrans = false;
        Session session = null;

        try
        {
            session = getSessionFactory().openSession();

            existingTrans = session.getTransaction().isActive();
    
            if (!existingTrans)
            	session.beginTransaction();

            session.save(this);
            session.flush();
  
            if (!existingTrans)
            	session.getTransaction().commit();
            session.close();
        }
        catch(Exception e1)
        {
            if (!existingTrans)
            {
                try
                {
                    session.getTransaction().rollback();
                }
                catch(Exception e2)
                {
                    //log.error("Rollback failed closing session ", e2);
                    session.close();
                }
            }

            throw e1;
        }
		
	}

	public static void save(List<WordDefinition> list) throws Exception
	{
	       boolean existingTrans = false;
	       Session session = null;

	       try
	       {
	            session = getSessionFactory().openSession();

	            existingTrans = session.getTransaction().isActive();
	    
	            if (!existingTrans)
	            	session.beginTransaction();

	            for (WordDefinition definition : list)	
	            	session.save(definition);
	            
	            session.flush();
	  
	            if (!existingTrans)
	            	session.getTransaction().commit();
	            session.close();
	        }
	        catch(Exception e1)
	        {
	            if (!existingTrans)
	            {
	                try
	                {
	                    session.getTransaction().rollback();
	                }
	                catch(Exception e2)
	                {
	                    //log.error("Rollback failed closing session ", e2);
	                    session.close();
	                }
	            }

	            throw e1;
	        }
			
		
	}

	public void update() throws Exception
	{
        boolean existingTrans = false;
        Session session = null;

        try
        {
            session = getSessionFactory().openSession();

            existingTrans = session.getTransaction().isActive();
    
            if (!existingTrans)
            	session.beginTransaction();

            session.update(this);
            session.flush();
  
            if (!existingTrans)
            	session.getTransaction().commit();
            session.close();
        }
        catch(Exception e1)
        {
            if (!existingTrans)
            {
                try
                {
                    session.getTransaction().rollback();
                }
                catch(Exception e2)
                {
                    //log.error("Rollback failed closing session ", e2);
                    session.close();
                }
            }

            throw e1;
        }
		
	}

	protected void parseLine(String line)
	{
		this.line = line;
		String[] tokens = line.split(" ");
		String location = tokens[0];
//		String file = tokens[1];
		String typeString = tokens[2];
		int nrSynonyms = Integer.parseInt(tokens[3],16);
		
		id = location+"-"+typeString;
		
		if (typeString.equals("n"))
			type = WordType.NOUN;
		else if (typeString.equals("v"))
			type = WordType.VERB;
		else if (typeString.equals("a") || typeString.equals("s"))
			type = WordType.ADJECTIVE;
		else if (typeString.equals("r"))
			type = WordType.ADVERB;
		else if (typeString.equals("p"))
			type = WordType.PRONOUN;
		else if (typeString.equals("d"))
			type = WordType.ARTICLE;
		else if (typeString.equals("c"))
			type = WordType.CONJUNCTION;
		else if (typeString.equals("q"))
			type = WordType.PREPOSITION;
		else if (typeString.equals("i"))
			type = WordType.INFINITIVE;
		else if (typeString.equals("x"))
			type = WordType.AUXILIARY_VERB;
		
		for (int i=0; i<nrSynonyms; i++)
		{
			String wordString = tokens[4+i*2];
			if (type==WordType.ADJECTIVE && (wordString.endsWith("(a)") || wordString.endsWith("(p)")))
				wordString = wordString.substring(0,wordString.length()-3);
			if (word==null)
				word = wordString;
			synonyms.add(wordString);
		}
		
		int nrPointers = Integer.parseInt(tokens[4+2*nrSynonyms]);
		
		for (int i=0; i<nrPointers; i++)
		{
			DefinitionLink link = new DefinitionLink();
			String pointerType = tokens[4+2*nrSynonyms+1+i*4];
			String id = tokens[4+2*nrSynonyms+2+i*4];
			String refType = tokens[4+2*nrSynonyms+3+i*4];
			String reference = id+"-"+refType;
			link.setWordDefinitionId(reference);
			if (pointerType.equals("@"))
				link.setType(LinkType.HYPERNYM);
			if (pointerType.equals("~"))
				link.setType(LinkType.HYPONYM);
			if (pointerType.equals("!"))
				link.setType(LinkType.ANTONYM);
			if (pointerType.equals("="))
				link.setType(LinkType.ATTRIBUTE);
			if (pointerType.equals("\\"))
				link.setType(LinkType.PERTAINYM);
			if (pointerType.equals("@i"))
				link.setType(LinkType.INSTANCE_HYPERNYM);
			if (pointerType.equals("~i"))
				link.setType(LinkType.INSTANCE_HYPONYM);
			if (pointerType.equals("#m"))
				link.setType(LinkType.MEMBER_HOLONYM);
			if (pointerType.equals("#s"))
				link.setType(LinkType.SUBSTANCE_HOLONYM);
			if (pointerType.equals("#p"))
				link.setType(LinkType.PART_HOLONYM);
			if (pointerType.equals("%m"))
				link.setType(LinkType.MEMBER_MERONYM);
			if (pointerType.equals("%s"))
				link.setType(LinkType.SUBSTANCE_MERONYM);
			if (pointerType.equals("%p"))
				link.setType(LinkType.PART_MERONYM);
			links.add(link);
		}
		
		int separatorIndex = 4+2*nrSynonyms+1+nrPointers*4;
		if (type == WordType.VERB)
		{
			int nrFrames = Integer.parseInt(tokens[separatorIndex]);
			separatorIndex++;
			for (int i=0; i<nrFrames; i++)
			{
				// TODO - parse frames.
				separatorIndex +=3;
			}
		}
		String separator = tokens[separatorIndex];
		
		definition = line.substring(line.indexOf(separator)+2);			
	}

	public static WordDefinition parse(String line)
	{
		WordDefinition definition = new WordDefinition();
		definition.setLine(line);
		return definition;
	}
}
