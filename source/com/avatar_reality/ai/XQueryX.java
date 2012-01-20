package com.avatar_reality.ai;

import java.io.OutputStream;

import org.basex.core.CommandBuilder;
import org.basex.core.Context;
import org.basex.core.Progress;
import org.basex.core.cmd.XQuery;
import org.basex.data.Result;

public class XQueryX
{
	private XQuery _query;
	private String _queryString;
	
	public XQueryX(String queryString)
	{
		_query = new org.basex.core.cmd.XQuery(queryString);
		_queryString = queryString;
	}
	
	public String execute(Context context)
		throws BaseXExceptionX
	{
		try
		{
			return _query.execute(context);
		}
		catch (org.basex.core.BaseXException e)
		{
			throw new BaseXExceptionX(_queryString, e);
		}
	}
	
	public void execute(Context context, OutputStream outputStream)
		throws BaseXExceptionX
	{
		try
		{
			_query.execute(context, outputStream);
		}
		catch (org.basex.core.BaseXException e)
		{
			throw new BaseXExceptionX(_queryString, e);
		}
	}

	/**
	 * 
	 * @see org.basex.core.Progress#abort()
	 */
	public void abort()
	{
		_query.abort();
	}

	/**
	 * @param cb
	 * @see org.basex.core.cmd.AQuery#build(org.basex.core.CommandBuilder)
	 */
	public void build(CommandBuilder cb)
	{
		_query.build(cb);
	}

	/**
	 * 
	 * @see org.basex.core.Progress#checkStop()
	 */
	public final void checkStop()
	{
		_query.checkStop();
	}

	/**
	 * @return
	 * @see org.basex.core.Progress#detail()
	 */
	public final String detail()
	{
		return _query.detail();
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		return _query.equals(obj);
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return _query.hashCode();
	}

	/**
	 * @return
	 * @see org.basex.core.Command#info()
	 */
	public final String info()
	{
		return _query.info();
	}

	/**
	 * @return
	 * @see org.basex.core.Progress#progress()
	 */
	public final double progress()
	{
		return _query.progress();
	}

	/**
	 * @param prog
	 * @see org.basex.core.Progress#progress(org.basex.core.Progress)
	 */
	public final void progress(Progress prog)
	{
		_query.progress(prog);
	}

	/**
	 * @return
	 * @see org.basex.core.cmd.AQuery#result()
	 */
	public final Result result()
	{
		return _query.result();
	}

	/**
	 * @param ctx
	 * @return
	 * @see org.basex.core.Command#run(org.basex.core.Context)
	 */
	public final boolean run(Context ctx)
	{
		return _query.run(ctx);
	}

	/**
	 * @param sec
	 * @see org.basex.core.Progress#startTimeout(long)
	 */
	public final void startTimeout(long sec)
	{
		_query.startTimeout(sec);
	}

	/**
	 * 
	 * @see org.basex.core.Progress#stop()
	 */
	public final void stop()
	{
		_query.stop();
	}

	/**
	 * 
	 * @see org.basex.core.Progress#stopTimeout()
	 */
	public final void stopTimeout()
	{
		_query.stopTimeout();
	}

	/**
	 * @return
	 * @see org.basex.core.Progress#title()
	 */
	public final String title()
	{
		return _query.title();
	}

	/**
	 * @return
	 * @see org.basex.core.Command#toString()
	 */
	public final String toString()
	{
		return _query.toString();
	}
}
