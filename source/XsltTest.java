import java.io.BufferedReader;
import java.io.FileReader;

import com.avatar_reality.ai.XSLTPipeline;

/**
 * 
 */
public class XsltTest
{
	public static void main(String[] args)
	{
		if (args.length!=1)
		{
			System.err.println("Need to pass a file-name as argument.");
			return;
		}
		
		try
		{
			String processedFile = XSLTPipeline.processPipeline(args[0]);
			BufferedReader reader = new BufferedReader(new FileReader(processedFile));
			while (reader.ready())
				System.out.println(reader.readLine());
			reader.close();
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}
}
