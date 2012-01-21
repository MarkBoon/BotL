package com.avatar_reality.ai;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.jlinkgrammar.Parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.basex.api.dom.BXNList;
import org.basex.api.dom.BXNode;
import org.basex.core.Context;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.Open;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;

/**
 * 
 */
public class BotCommandProcessor
{
	private static final boolean SINGLE_BOT_ONLY = true;
	
	private static LexicalizedParser lp = new LexicalizedParser("grammar/englishPCFG.ser.gz");
    private static TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
	
	public static void main(String[] args)
	{
		if (args.length!=1)
		{
			System.err.println("Usage: java com.avatar_reality.ai.BotCommandProcessor <botl-file>");
			System.exit(-1);
		}

//		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);
//		_nrQueries = 0;
//		long t0 = System.currentTimeMillis();
//		create("TestBot", new BotCommandPrinter());
//		long t1 = System.currentTimeMillis();
//		getSingleton().loadTriggers(args[0]);
//		long t2 = System.currentTimeMillis();
//		getSingleton().receiveSayAction("test", "foo bar");
//		getSingleton().receiveSayAction("test", "Tell me, will I win the lottery?");
//		long t3 = System.currentTimeMillis();
//		_logger.info("Queries: \t"+_nrQueries);
//		_logger.info("Init: \t"+(t1-t0)+" ms.");
//		_logger.info("Load: \t"+(t2-t1)+" ms.");
//		_logger.info("Proc: \t"+(t3-t2)+" ms.");
//		_logger.info("Total: \t"+(t3-t0)+" ms.");

		create("TestBot", new BotCommandPrinter());
		getSingleton().loadTriggers(args[0]);
		StringBuffer buffer = new StringBuffer();
		try
		{
			while (getInput(buffer, System.in, System.out))
			{
				if (buffer.toString().equals("quit"))
					break;
				long t1 = System.currentTimeMillis();
				getSingleton().receiveSayAction("test_bot", buffer.toString());
				long t2 = System.currentTimeMillis();
				System.out.println("Processing time: "+(t2-t1)+" ms.");
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("serial")
	static class MyDateFormat extends SimpleDateFormat
	{
		public MyDateFormat(String template)
		{
			super(template);
		}
		public String getFormat(Date date)
		{
			String dateFormat = format(date)+"$$";
			dateFormat = dateFormat.replace("00$$", ":00");
			return dateFormat;
		}
	}
	private static MyDateFormat DATE_TIME_FORMATTER = new MyDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	private static String CURRENT_TIME() { return DATE_TIME_FORMATTER.getFormat( new Date() ); }
	
	static Logger _logger;
	static
	{
		BasicConfigurator.configure();
		_logger = Logger.getLogger(BotCommandProcessor.class);
//		Logger.getRootLogger().setLevel(Level.TRACE);
	}
	static int _nrQueries;

	private static BotCommandProcessor _singleton;
	private static ThreadLocal<BotCommandProcessor> botCommandProcessorRef = new ThreadLocal<BotCommandProcessor>()
		{
		    protected BotCommandProcessor initialValue()
		    {
		    	return new BotCommandProcessor();
		    }
		};

	static Hashtable<String, String> _transientTable = new Hashtable<String, String>();
	private static Hashtable<String, String> _responses = new Hashtable<String, String>();
	

	private Context _context;
	private String _userID;
	private String _localFunctions;
	private String _currentTrigger;
	private BotCommandListener _botCommandHandler;
	private OntologyManager _ontologyManager;
	
	private List<String> _queryList = new ArrayList<String>();
	
	public static BotCommandProcessor getSingleton()
	{
		if (SINGLE_BOT_ONLY)
		{
			if (_singleton==null)
				_singleton = new BotCommandProcessor();
			return _singleton;
		}
		else
			return botCommandProcessorRef.get();
	}
	
	protected BotCommandProcessor()
	{
	}
	
	public static BotCommandProcessor create(String userID, BotCommandListener commandHandler)
	{
		Parser.InitializeVars(new String[]{});
		getSingleton().setUserID(userID);
		getSingleton().setCommandHandler(commandHandler);
		return getSingleton();
	}
	
	private void setUserID(String userID)
	{
		_userID = userID;
		_context = new Context();
		try
		{
			new Open(userID).execute(_context);
		}
		catch (org.basex.core.BaseXException exception)
		{
			try
			{
				_logger.info("Creating new bot: "+userID);
				new CreateDB(userID).execute(_context);
				String insertQuery = "insert node <BOT-L name='"+userID+"'/> into doc('"+userID+"')";
				_logger.trace("Insert query: "+insertQuery);
				String result = new XQueryX(insertQuery).execute(_context);
				_logger.trace("Result: "+result);
			}
			catch (BaseXExceptionX nestedException)
			{
				nestedException.printStackTrace();
			}
			catch (org.basex.core.BaseXException nestedException)
			{
				nestedException.printStackTrace();
			}
		}
	}
	
	private String getUserID()
	{
		return _userID;
	}
	
	private BotCommandListener getCommandHandler()
	{
		return _botCommandHandler;
	}
	
	private void setCommandHandler(BotCommandListener handler)
	{
		_botCommandHandler = handler;
	}
	
	private Context getContext()
	{
		return _context;
	}
	
	private void setFunctions(String functionDeclarations)
	{
		_localFunctions = functionDeclarations;
	}
	
	public void loadTriggers(String fileName)
	{
		String userID = getUserID();
		Context context = getContext();
		
		try
		{
			String loadFile = XSLTPipeline.processPipeline(fileName);
			
			// Copy triggers
			String deleteQuery = "delete nodes collection('"+userID+"')/BOT-L/trigger_set";
			_logger.trace("Delete query: "+deleteQuery);
			new XQueryX(deleteQuery).execute(context);
			String insertQuery = "for $triggerSet in doc('"+loadFile+"')/BOT-L/trigger_set "+
			"return ( "+
			"insert node $triggerSet "+
			"into doc('"+userID+"')/BOT-L)";
			_logger.trace("Insert query: "+insertQuery);
			new XQueryX(insertQuery).execute(context);
			
			// Copy memory
/*			deleteQuery = "delete nodes collection('"+userID+"')/BOT-L/memory";
			_logger.trace("Delete query: "+deleteQuery);
			new XQueryX(deleteQuery).execute(context);
			insertQuery = "for $memory in doc('"+loadFile+"')/BOT-L/memory "+
			"return ( "+
			"insert node $memory "+
			"into doc('"+userID+"')/BOT-L)";
			_logger.trace("Insert query: "+insertQuery);
			new XQueryX(insertQuery).execute(context);
*/
			
//			// Copy ontology
//			deleteQuery = "delete nodes collection('"+userID+"')/BOT-L/*:Ontology";
//			_logger.trace("Delete query: "+deleteQuery);
//			new XQueryX(deleteQuery).execute(context);
//			insertQuery = "for $ontology in doc('"+loadFile+"')/BOT-L/*:Ontology "+
//			"return ( "+
//			"insert node $ontology "+
//			"into doc('"+userID+"')/BOT-L)";
//			_logger.trace("Insert query: "+insertQuery);
//			new XQueryX(insertQuery).execute(context);
			
			// Copy player-info (since this is dynamically created it's basically equivalent to delting it.)
			deleteQuery = "delete nodes collection('"+getUserID()+"')/BOT-L/player-info";
			_logger.trace("Delete query: "+deleteQuery);
			new XQueryX(deleteQuery).execute(getContext());
			insertQuery = "for $node in doc('"+loadFile+"')/BOT-L/player-info "+
			"return ( "+
			"insert node $node "+
			"into doc('"+getUserID()+"')/BOT-L)";
			_logger.trace("Insert query: "+insertQuery);
			new XQueryX(insertQuery).execute(getContext());
			
			// Copy functions
			deleteQuery = "delete nodes collection('"+userID+"')/BOT-L/declare";
			_logger.trace("Delete query: "+deleteQuery);
			new XQueryX(deleteQuery).execute(context);
			insertQuery = "for $declaration in doc('"+loadFile+"')/BOT-L/declare "+
			"return ( "+
			"insert node $declaration "+
			"into doc('"+userID+"')/BOT-L)";
			_logger.trace("Insert query: "+insertQuery);
			new XQueryX(insertQuery).execute(context);
			
			StringBuilder declarationBuilder = new StringBuilder();

			String namespaceQuery = "collection()/BOT-L/declare/namespace";
			_logger.trace("Get namespaces: "+namespaceQuery);
			QueryProcessor processor = new QueryProcessor(namespaceQuery, context);
			
			Iter iterator = processor.iter();
			Item item;
			while ((item=iterator.next())!=null)
			{
				BXNode node = (BXNode)item.toJava();
				declarationBuilder.append('\n');
				declarationBuilder.append(node.getTextContent());
				declarationBuilder.append(';');
			}
			
			String ontologyQuery = "collection()/BOT-L/*:Ontology";
			_logger.trace("Ontology query: "+ontologyQuery);
			String ontologyXML = new XQueryX(ontologyQuery).execute(context);
			_logger.info("Ontology result: "+ontologyXML);
			_ontologyManager = new OntologyManager(this, ontologyXML);
	
			String functionQuery = "collection()/BOT-L/declare/function[@always='true']";
			_logger.trace("Get functions: "+functionQuery);
			processor = new QueryProcessor(functionQuery, context);
			
			iterator = processor.iter();
			while ((item=iterator.next())!=null)
			{
				BXNode node = (BXNode)item.toJava();
				declarationBuilder.append('\n');
				declarationBuilder.append(node.getTextContent());
				declarationBuilder.append(';');
			}
			
			declarationBuilder.append('\n');
			getSingleton().setFunctions(declarationBuilder.toString());
			String initEvent = "<event type='init' dateTime='"+CURRENT_TIME()+"' persistent='true'/>";
			processEvent(initEvent, "receiveInitEvent");
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}
	
	public void deleteTransientVariables()
	{
		_transientTable.clear();
	}
	
	public static void processEvent(String event, String triggerName)
	{
		getSingleton().deleteEvents();
		getSingleton().deleteTransientVariables();
		getSingleton()._currentTrigger = triggerName;
		getSingleton().processEvent(event);
		getSingleton().processPostQueries();
	}
	
	private void deleteEvents()
	{
		try
		{
			String deleteEventQuery = "delete nodes collection()/BOT-L/event[@persistent!='true' or not(@persistent)]";
			_logger.trace("Delete Event: "+deleteEventQuery);
			new XQueryX(deleteEventQuery).execute(_context); _nrQueries++;
		}
		catch (BaseXExceptionX e)
		{
			e.printStackTrace();
		}
	}
	
	private void processEvent(String event)
	{
		try
		{
			insertEvent(event);

			String fromQuery = "collection()/BOT-L/event[1]/@from/string()";
			String from = new XQueryX(fromQuery).execute(_context); _nrQueries++;
			transientSet("_from_",from);
			transientSet("_botName_",_userID);
			
			processEventTriggers();
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	/**
	 * @param event
	 * @throws BaseXExceptionX
	 */
	private void insertEvent(String event) throws BaseXExceptionX
	{
		String insertEventQuery = "insert node "+event+" as first into collection('"+_userID+"')/BOT-L";
		_logger.trace("Insert Event: "+insertEventQuery);
		new XQueryX(insertEventQuery).execute(_context); _nrQueries++;
	}

	private boolean processEventTriggers()
		throws Exception
	{
		String inQuery = "collection()/BOT-L/event[1]/@in/string()";
		XQueryX query = new XQueryX(inQuery);
		String input = query.execute(_context); _nrQueries++;
		transientSet("_input_",input);
		
		String processQuery = "collection()/BOT-L/trigger_set/trigger[@name='"+_currentTrigger+"']";
		_logger.trace("Process Event: "+processQuery);
		QueryProcessor processor = new QueryProcessor(processQuery, _context); _nrQueries++;
		BXNode node = (BXNode) processor.iter().next().toJava();
		return executeCommands(node, input);
	}

	private boolean executeCommands(BXNode node, String input) throws BaseXExceptionX
	{
		try
		{
			input = input.replace("  "," ");
			BXNList childList = node.getChildNodes();
			for (int i=0; i<childList.getLength(); i++)
			{
				BXNode childNode = childList.item(i);
				String nodeName = childNode.getNodeName();
				if (nodeName.equals("matches"))
				{
					String pattern = childNode.getAttributes().getNamedItem("pattern").getNodeValue();
					String matchQueryString = "matches(\""+input+"\",\""+pattern+"\",'i')";
//					Pattern regexpPattern = Pattern.compile(pattern);
//					Matcher matcher = regexpPattern.matcher(input);
//					_logger.trace("Match RegExp: '"+input+"' with '"+pattern+"'");
//					if (matcher.find())
					_logger.trace("Match Query: "+matchQueryString);
					XQueryX matchQuery = new XQueryX(matchQueryString); _nrQueries++;
					if (matchQuery.execute(_context).equals("true"))
					{
						_logger.debug("Matched Query: "+matchQueryString);
						transientSet("_pattern_", pattern);
						if (executeCommands(childNode, input))
							return true;
					}
					else
						_logger.trace(" = false");
				}
				if (nodeName.equals("return"))
				{
					String text = childNode.getTextContent();
					if (!text.trim().isEmpty())
					{
						_logger.debug("Return Query: "+text);
						XQueryX outQuery = new XQueryX(_localFunctions+text);
						outQuery.execute(_context); _nrQueries++;
					}
					return true;
				}
				if (nodeName.equals("srai"))
				{
					String text = childNode.getTextContent();
					_logger.trace("SRAI Query: "+_localFunctions+text);
					XQueryX sraiQuery = new XQueryX(_localFunctions+text);
					String result = sraiQuery.execute(_context); _nrQueries++;
					result = result.replace("  "," ");

					_logger.debug("\nSRAI: "+result+"\n");
					if (result.equals(input))
					{
						_logger.debug("SRAI Identity!");
						continue;
					}
					
					String event = createSayEvent(transientGet("_from_"),result, false);
					processEvent(event);
					return true;
				}
				if (nodeName.equals("execute"))
				{
					String query = childNode.getTextContent();
					_logger.debug("Execute Query: "+query);
					new XQueryX(_localFunctions+query).execute(_context); _nrQueries++;
				}
				if (nodeName.equals("if"))
				{
					String query = childNode.getFirstChild().getTextContent();
					_logger.debug("Condition Query: "+query);
					String result = new XQueryX(_localFunctions+query).execute(_context); _nrQueries++;
//					_logger.trace("\nFull query: "+_localFunctions+query);
					_logger.trace("\nResult: "+result);
					if (result.equals("true"))
					{
						if (childList.getLength()>i+1)
						{
							BXNode thenNode = childList.item(++i);
							if (thenNode.getNodeName().equals("then"))
							{
								if (executeCommands(thenNode, input))
									return true;								
							}
							// else XXX if must be followed by then.
						}
					}
				}
				if (nodeName.equals("goto"))
				{
					String label = childNode.getAttributes().getNamedItem("label").getNodeValue();
					String gotoQueryString = "collection()/BOT-L//*[@label='"+label+"']";
					_logger.debug("Goto Query: "+gotoQueryString);
					QueryProcessor processor = new QueryProcessor(gotoQueryString, _context); _nrQueries++;
					Iter iterator = processor.iter();
					Item item = iterator.next();
					return executeCommands((BXNode)item.toJava(), input);
				}
				// XXX - unrecognized statement.
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
		return false;
	}
	
	private void processPostQueries()
	{
		try
		{
			for (String query : _queryList)
			{
				_logger.debug("Process query: "+query);
				new XQueryX(query).execute(_context);
			}
		}
		catch (BaseXExceptionX e)
		{
			e.printStackTrace();
		}
		finally
		{
			_queryList.clear();
		}
	}

	public static String createSayEvent(String entityName, String text, boolean persistent)
	{
		long t0 = System.currentTimeMillis();
		String grammar = "";
		try
		{
			String grammarText = parseGrammar(text);
			String grammarText2 = Parser.parse(text).constituentXML;
		    _logger.info(grammarText2);
			 grammar = "<grammar>"+grammarText+"</grammar>";
		}
		catch (Exception exception)
		{
			_logger.error("Unexpected exception while parsing grammar.",exception);
		}
		long t1 = System.currentTimeMillis();
		_logger.debug("Grammar: \t"+(t1-t0)+" ms.");
		String event = "<event type='say' in=\""+text+"\" from='"+entityName+"' dateTime='"+CURRENT_TIME()+"' persistent='"+persistent+"'>" + grammar + "</event>";
		return event;
	}
	
	private static String parseGrammar(String text)
	{
	    List<CoreLabel> rawWords2 = 
	      tokenizerFactory.getTokenizer(new StringReader(text)).tokenize();
	    Tree parse = lp.apply(rawWords2);

	    TreePrint tp = new TreePrint("penn");
	    StringWriter stringWriter = new StringWriter();
	    PrintWriter pw = new PrintWriter(stringWriter);
	    tp.printTree(parse,pw);
	    String output =  stringWriter.toString();
	    output = output.replace("&lt;", "<");
	    output = output.replace("&gt;", ">");
	    output = output.replace("<.> .</.>", "");
	    output = output.replace("<.> ?</.>", "");
	    output = output.replace("<,> ,</,>", "");
	    output = output.replace("PRP$", "PRP-S");
	    output = output.replace("WP$", "WP-S");
//	    output = output.replace(".>", "PUNCTUATION>");
//	    output = output.replace("?>", "QUESTION>");
	    _logger.info(output);
	    return output;
	}
	
	public static void say(String text)
	{
		String userID = getSingleton().getUserID();
		String event = "<event type='say' in=\""+text+"\" from='"+userID+"' dateTime='"+CURRENT_TIME()+"' persistent='true'/>";
		String insertEventQuery = "insert node "+event+" as first into collection('"+userID+"')/BOT-L";
		getSingleton().addPostQuery(insertEventQuery);
		
		getSingleton().getCommandHandler().say(text);
		_responses.put(transientGet("_from_"),text);
	}
	
	public static void walkTo(String location)
	{
		String userID = getSingleton().getUserID();
		String event = "<event type='walk' in=\""+location+"\" from='"+userID+"' dateTime='"+CURRENT_TIME()+"' persistent='true'/>";
		String insertEventQuery = "insert node "+event+" as first into collection('"+userID+"')/BOT-L";
		getSingleton().addPostQuery(insertEventQuery);
		_logger.info("Walk to "+location);
		getSingleton().getCommandHandler().walkTo(location);
	}
	
	public static void runTo(String location)
	{
		String userID = getSingleton().getUserID();
		String event = "<event type='run' in=\""+location+"\" from='"+userID+"' dateTime='"+CURRENT_TIME()+"' persistent='true'/>";
		String insertEventQuery = "insert node "+event+" as first into collection('"+userID+"')/BOT-L";
		getSingleton().addPostQuery(insertEventQuery);
		
		getSingleton().getCommandHandler().walkTo(location);
	}
	
	public static String chooseRandomDestination(String location, double radius)
	{
		return getSingleton().getCommandHandler().chooseRandomDestination(location, radius);
	}
	
	public static OntologyManager getOntologyManager()
	{
		return getSingleton()._ontologyManager;
	}
	
	public static void faceTo(String text)
	{
		getSingleton().getCommandHandler().faceTo(text);
	}
	
	public void addPostQuery(String query)
	{
		_queryList.add(query);
	}
	
	public static void transientSet(String name, String value)
	{
		_transientTable.put(name,value);
	}
	
	public static String transientGet(String name)
	{
		String value =  _transientTable.get(name);
		return value;
	}
	
	public String receiveSayAction(String entityName, String text)
	{
		_responses.put(entityName, "");
		String scrubbedText = text.replace("<", " ");
		scrubbedText = scrubbedText.replace(">", " ");

		processEvent(createSayEvent(entityName, scrubbedText, true), "receiveSay");
		return _responses.get(entityName);
	}
	
	public void receiveTimerEvent(Date dateTime)
	{
		String timerEvent = "<event type='timer' in=\""+DATE_TIME_FORMATTER.getFormat(dateTime)+"\" dateTime='"+CURRENT_TIME()+"' persistent='false'/>";
		processEvent(timerEvent, "receiveTimerEvent");
	}
	
	public void receiveArrival(String location)
	{
		String arrivalEvent = "<event type='arrival' in=\""+location+"\" dateTime='"+CURRENT_TIME()+"' persistent='true'/>";
		_logger.info("Arrive at "+location);

		processEvent(arrivalEvent, "receiveArrival");
	}
	
	public static void print(String text)
	{
		System.out.println(text);
	}
	
	public void updatePlayerInfo(String playerInfo)
	{
		try
		{
			String deleteQuery = "delete nodes collection()/BOT-L/player-info";
			_logger.trace("Delete query: "+deleteQuery);
			new XQueryX(deleteQuery).execute(getContext());
			String insertQuery = "insert node "+playerInfo+" into collection()/BOT-L";
			_logger.trace("Insert query: \n"+insertQuery);
			new XQueryX(insertQuery).execute(getContext());
		}
		catch (BaseXExceptionX e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void updateObjectInfo(String objectInfo)
	{
		try
		{
			String deleteQuery = "delete nodes collection()/BOT-L/object-info";
			_logger.trace("Delete query: "+deleteQuery);
			new XQueryX(deleteQuery).execute(getContext());
			String insertQuery = "insert node "+objectInfo+" into collection()/BOT-L";
			_logger.trace("Insert query: \n"+insertQuery);
			new XQueryX(insertQuery).execute(getContext());
		}
		catch (BaseXExceptionX e)
		{
			e.printStackTrace();
		}
		
	}

    public static boolean getInput(StringBuffer input_string, InputStream in, PrintStream out)
        throws IOException
    {
        int c;
        input_string.setLength(0);
        out.flush();
        c = in.read();

        while (c != '\n')
        {
            if (c < 0)
            {
                return false;
            }
            input_string.append((char)c);
            c = in.read();
        }
        return true;
    }
}
