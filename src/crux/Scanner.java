package crux;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

public class Scanner implements Iterable<Token> {
	private static final int EOF = -1;
	private static final int BOF = -2;
	public static String studentName = "Kelly Ho";
	public static String studentID = "81482302";
	public static String uciNetID = "doankhah";
	
	private int lineNum;  // current line count
	private int charPos;  // character offset for current line
	private int nextChar; // contains the next char (-1 == EOF)
	private Reader input;
	
	Scanner(Reader reader)
	{
		// TODO: initialize the Scanner
		input = reader;
		lineNum = 1;
		charPos = 0;
		nextChar = readChar();
		
	}	
	
	// OPTIONAL: helper function for reading a single char from input
	//           can be used to catch and handle any IOExceptions,
	//           advance the charPos or lineNum, etc.
	
	private int readChar()
	{
		int kyTu = 0;
		try
		{
			kyTu = input.read();
			charPos++;
			if(kyTu == EOF)
				input.close();
			else if(kyTu == '\n')
			{
				lineNum++;
			}
		} catch (IOException e){
			e.printStackTrace();
		}
		return kyTu;
	}

		

	/* Invariants:
	 *  1. call assumes that nextChar is already holding an unread character
	 *  2. return leaves nextChar containing an untokenized character
	 */
	public Token next()
	{
		// TODO: implement this
		Token t = null;
		if (nextChar == EOF)
			t = Token.EOF(lineNum, charPos);
		return t;
	}

	@Override
	public Iterator<Token> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	// OPTIONAL: any other methods that you find convenient for implementation or testing
}
