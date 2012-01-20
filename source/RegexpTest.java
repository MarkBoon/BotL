import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegexpTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		long t0 = System.currentTimeMillis();
		String in = "doo ";
		String pattern = "do";
		Pattern regexp = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		Matcher regexpMatcher = regexp.matcher(in);
		System.out.print("'"+in+"' matches '"+pattern+"'");
		if (regexpMatcher.find())
			System.out.println(" = true");
		else
			System.out.println(" = false");
		long t1 = System.currentTimeMillis();
		System.out.println("Regexp too "+(t1-t0)+"ms.");
	}

}
