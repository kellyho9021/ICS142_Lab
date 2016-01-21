package crux;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

public class Scanner implements Iterable<Token> {
	private static final int EOF = -1;
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
			//if reader is at EOF, close the file.
			if(kyTu == EOF)
			{
				input.close();
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
		//Remove white spaces
		while(nextChar == ' ' || nextChar == '\r')
		{
			nextChar = readChar();
		}
		//Reader is at EOF
		if (nextChar == EOF)
		{
			t = Token.EOF(lineNum, charPos);
		}
		//Reader is at the end of line
		else if(nextChar == '\n')
		{	
			lineNum++;
			charPos = 0;
			nextChar = readChar();
			t = next();
		}
		else
		{
			//capture the position of token
			int tokPos = charPos;
			//Check tokens that contain letters and digits
			if(Character.isLetterOrDigit(nextChar) || nextChar == '_')
			{
				String str = "";
				if(Character.isLetter(nextChar) || nextChar == '_')
				{
					do{
						str += (char)nextChar;
						nextChar = readChar();
					}while(Character.isLetterOrDigit(nextChar) || nextChar == '_');
					
					if(foundToken(str))
					{
						t = new Token(str, lineNum, tokPos);
					}
					else
						t = Token.Identifier(str, lineNum, tokPos);
				}
				else
				{
					while(Character.isDigit(nextChar))
					{
						str += (char) nextChar;
						nextChar = readChar();
					}
					if(nextChar == '.')
					{
						str += (char) nextChar;
						nextChar = readChar();
						while(Character.isDigit(nextChar))
						{
							str += (char) nextChar;
							nextChar = readChar();
						}
						t = Token.Float(str, lineNum, tokPos);
					}
					else
						t = Token.Integer(str, lineNum, tokPos);
				}
			}
			else if(nextChar == '/')
			{
				nextChar = readChar();
				if(nextChar == '/')				
				{
					do{
						nextChar =readChar();
					}while(nextChar != '\n' && nextChar != EOF);
					t = next();
				}
				else
					t = new Token("/", lineNum, tokPos);
			}
			else if(nextChar == '!')
			{
				nextChar = readChar();
				if(nextChar == '=')
				{
					t = new Token("!=", lineNum, tokPos);
					nextChar = readChar();
				}
				else
					t = Token.Error("Unexpected character: " + '!', lineNum, tokPos);
			}
			else
			{
				String lex = Character.toString((char) nextChar);
				if(nextChar == ':' || nextChar == '=' || nextChar == '<' || nextChar == '>')
				{
					nextChar = readChar();
					if(nextChar == '=' || nextChar == ':')
					{
						String temp = lex + (char) nextChar;
						if(foundToken(temp))
						{
							t = new Token(temp, lineNum, tokPos);
						}
					}
					else
						return new Token(lex, lineNum, tokPos);
				}
				else
				{
					if(foundToken(lex))
					{
						t = new Token(lex, lineNum, tokPos);
					}
					else
						t = Token.Error("Unexpected character: " + lex, lineNum, tokPos);
				}
				nextChar = readChar();	
			}
		}
		return t;
	}

	@Override
	public Iterator<Token> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	// OPTIONAL: any other methods that you find convenient for implementation or testing
	public boolean foundToken(String lex)
	{
		for(Token.Kind iter : Token.Kind.values())
			if(iter.matches(lex))
				return true;
		return false;
	}
}
