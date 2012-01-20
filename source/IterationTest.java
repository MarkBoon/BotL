import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.XQuery;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;

public class IterationTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			String testQuery = "doc('test.xml')/test-file/tests/test";
			Context context = new Context();
			QueryProcessor outerProcessor = new QueryProcessor(testQuery, context);
			Iter outerIterator;
			outerIterator = outerProcessor.iter();
			Item outerItem;
			int i=0;
			while ((outerItem=outerIterator.next())!=null)
			{
				System.out.println("outer item: "+outerItem);
				String insertQuery = "insert node <result>"+i+"</result> into doc('test.xml')/test-file/results";
				System.out.println("Insert query: "+insertQuery);
				new XQuery(insertQuery).execute(context, System.out);
				QueryProcessor innerProcessor = new QueryProcessor(testQuery, context);
				Iter innerIterator;
				innerIterator = innerProcessor.iter();
				Item innerItem;
				while ((innerItem=innerIterator.next())!=null)
				{
					System.out.println("inner item: "+innerItem);
				}
				i++;
				String deleteQuery = "delete nodes doc('test.xml')/test-file/results";
				System.out.println("Delete query: "+deleteQuery);
				new XQuery(deleteQuery).execute(context);
			}
		}
		catch (QueryException e)
		{
			e.printStackTrace();
		} 
		catch (BaseXException e)
		{
			e.printStackTrace();
		}
	}

}
