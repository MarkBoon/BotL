package com.avatar_reality.ai;

import java.io.File;
import java.net.URL;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.apache.log4j.Logger;
import org.basex.api.dom.BXNode;
import org.basex.core.Context;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;

public class XSLTPipeline
{
	static Logger _logger;
	static
	{
		_logger = Logger.getLogger(XSLTPipeline.class);
	}
	
	private static XSLTPipeline _singleton;
	
	public static XSLTPipeline getSingleton()
	{
		if (_singleton==null)
			_singleton = new XSLTPipeline();
		return _singleton;
	}
	
	private XSLTPipeline() {}
	
	/**
	 * Process the header of an XML file and extract the processing-instruction
	 * nodes that contain references to a style-sheet. Apply the stylesheets
	 * to the input file, where each consecutive style-sheet is applied to the 
	 * result of the preceding one.
	 * 
	 * @param inputFile
	 * @return file-name of the file containing the results of the style-sheets.
	 */
	public static String processPipeline(String inputFile)
	{
		try
		{
			String piQuery = "doc('"+inputFile+"')/processing-instruction()";
			QueryProcessor processor = new QueryProcessor(piQuery, new Context());
			Iter iterator = processor.iter();
			Item item;
			while ((item=iterator.next())!=null)
			{
				BXNode node = (BXNode)item.toJava();
				String xsltString = node.getTextContent();
				int startIndex = xsltString.indexOf("href");
				if (startIndex>=0)
				{
					startIndex = xsltString.indexOf('=',startIndex);
					startIndex = xsltString.indexOf('"',startIndex);
					char quoteChar = '"';
					if (startIndex < 0)
					{
						startIndex = xsltString.indexOf('\'',startIndex);
						quoteChar='\'';
					}
					int endIndex = xsltString.indexOf(quoteChar,startIndex+1);
					String href = xsltString.substring(startIndex+1,endIndex);
					_logger.debug("HREF="+href);
	
		            Processor proc = new Processor(false);
		            XsltCompiler comp = proc.newXsltCompiler();
		            XsltExecutable exp = null;
		            if (href.startsWith("file:"))
		            	exp = comp.compile(new StreamSource(getSingleton().getClass().getResourceAsStream(href.substring(5))));
		            else
		            	exp = comp.compile(new StreamSource(new URL(href).getFile()));
		            XdmNode source = proc.newDocumentBuilder().build(new StreamSource(new File(inputFile)));
	
		            // send the result to a file
			        File resultFile = File.createTempFile("XSLTResult", ".xml");
	
			        Serializer out = proc.newSerializer(resultFile);
		            out.setOutputProperty(Serializer.Property.METHOD, "xml");
		            out.setOutputProperty(Serializer.Property.INDENT, "yes");
		            XsltTransformer trans = exp.load();
		            trans.setInitialContextNode(source);
		            trans.setDestination(out);
		            trans.transform();
									
			        _logger.debug("Results will go to: "
			                + resultFile.getAbsolutePath(  ));
			        inputFile = resultFile.getAbsolutePath(  );		        
				}
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();			
		}
		return inputFile;
	}


}
