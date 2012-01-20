package com.avatar_reality.ai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;

import org.basex.core.BaseXException;

public class BaseXExceptionX
	extends Exception
{
	private static final long serialVersionUID = 8659901542517203009L;

	private BaseXException _exception;
	private String _queryString;
	private PrintStream _output = System.err;
	
	public BaseXExceptionX(String queryString, org.basex.core.BaseXException original)
	{
		_queryString = queryString;
		_exception = original;
	}
	
	@Override
	public void printStackTrace()
	{
		printErrorDetail();
		_exception.printStackTrace();
	}
	
	public void printErrorDetail()
	{
		String originalMessage = _exception.getMessage();
		int lineIndex = originalMessage.indexOf("at line ");
		int commaIndex = originalMessage.indexOf(",",lineIndex);
		int columnIndex = originalMessage.indexOf(" column ",commaIndex);
		int colonIndex = originalMessage.indexOf(":",columnIndex);
		String lineNumberString = originalMessage.substring(lineIndex+8,commaIndex);
		int lineNumber = Integer.parseInt(lineNumberString);
		String columnNumberString = originalMessage.substring(columnIndex+8,colonIndex);
		int columnNumber = Integer.parseInt(columnNumberString);
		String message = originalMessage.substring(colonIndex+1);
		
		try
		{
			BufferedReader errorReader = new BufferedReader(new StringReader(_queryString));
			String line = null;
			for (int i=0; i<lineNumber; i++)
			{
				line = errorReader.readLine();
				if (line==null)
				{
					_output.println("Unexpected end of error message...");
					return;
				}
				_output.println(line);
			}
			StringBuilder arrowLine = new StringBuilder(columnNumber);
			for (int i=0; i<columnNumber-1; i++)
			{
				if (line.charAt(i)=='\t')
					arrowLine.append('\t');
				else
					arrowLine.append(' ');
			}
			arrowLine.append('^');
			
			_output.println(arrowLine);
			_output.println(arrowLine);
			_output.println(arrowLine);
			_output.println(message+"\n");
			
			while (errorReader.ready())
			{
				line = errorReader.readLine();
				if (line==null)
					break;
				_output.println(line);
			}
			errorReader.close();
			_output.println();
		}
		catch (IOException e)
		{
			_output.println("Unable to process the error... "+e.getMessage());
		}
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		return _exception.equals(obj);
	}

	/**
	 * @return
	 * @see java.lang.Throwable#getCause()
	 */
	public Throwable getCause()
	{
		return _exception.getCause();
	}

	/**
	 * @return
	 * @see java.lang.Throwable#getLocalizedMessage()
	 */
	public String getLocalizedMessage()
	{
		return _exception.getLocalizedMessage();
	}

	/**
	 * @return
	 * @see java.lang.Throwable#getMessage()
	 */
	public String getMessage()
	{
		return _exception.getMessage();
	}

	/**
	 * @return
	 * @see java.lang.Throwable#getStackTrace()
	 */
	public StackTraceElement[] getStackTrace()
	{
		return _exception.getStackTrace();
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return _exception.hashCode();
	}

	/**
	 * @param cause
	 * @return
	 * @see java.lang.Throwable#initCause(java.lang.Throwable)
	 */
	public Throwable initCause(Throwable cause)
	{
		return _exception.initCause(cause);
	}

	/**
	 * @param s
	 * @see java.lang.Throwable#printStackTrace(java.io.PrintStream)
	 */
	public void printStackTrace(PrintStream s)
	{
		_exception.printStackTrace(s);
	}

	/**
	 * @param s
	 * @see java.lang.Throwable#printStackTrace(java.io.PrintWriter)
	 */
	public void printStackTrace(PrintWriter s)
	{
		_exception.printStackTrace(s);
	}

	/**
	 * @param stackTrace
	 * @see java.lang.Throwable#setStackTrace(java.lang.StackTraceElement[])
	 */
	public void setStackTrace(StackTraceElement[] stackTrace)
	{
		_exception.setStackTrace(stackTrace);
	}

	/**
	 * @return
	 * @see java.lang.Throwable#toString()
	 */
	public String toString()
	{
		return _exception.toString();
	}
}
