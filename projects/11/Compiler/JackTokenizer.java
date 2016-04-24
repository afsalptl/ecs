import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JackTokenizer 
{
	private Scanner scanner;
	private Boolean isBlockCommentStarted = false;
	private ArrayList<String> tokens;
	private static Pattern tokenPattern;
	private static String keyWordRegex, symbolRegex, intRegex, strRegex, idRegex;
	private String currentToken;
    private TokenType currentTokenType;
    private int pointer = 0;
	private static HashMap<String,KeyWord> keyWordMap = new HashMap<String,KeyWord>();
	private static HashSet<Character> opSet = new HashSet<Character>();

	static
	{
		keyWordMap.put("class",KeyWord.CLASS);
		keyWordMap.put("constructor", KeyWord.CONSTRUCTOR);
		keyWordMap.put("function", KeyWord.FUNCTION);
		keyWordMap.put("method", KeyWord.METHOD);
		keyWordMap.put("field", KeyWord.FIELD);
		keyWordMap.put("static", KeyWord.STATIC);
		keyWordMap.put("var", KeyWord.VAR);
		keyWordMap.put("int", KeyWord.INT);
		keyWordMap.put("char", KeyWord.CHAR);
		keyWordMap.put("boolean", KeyWord.BOOLEAN);
		keyWordMap.put("void", KeyWord.VOID);
		keyWordMap.put("true", KeyWord.TRUE);
		keyWordMap.put("false", KeyWord.FALSE);
		keyWordMap.put("null", KeyWord.NULL);
		keyWordMap.put("this", KeyWord.THIS);
		keyWordMap.put("let", KeyWord.LET);
		keyWordMap.put("do", KeyWord.DO);
		keyWordMap.put("if", KeyWord.IF);
		keyWordMap.put("else", KeyWord.ELSE);
		keyWordMap.put("while", KeyWord.WHILE);
		keyWordMap.put("return", KeyWord.RETURN);

		opSet.add('+');
		opSet.add('-');
		opSet.add('*');
		opSet.add('/');
		opSet.add('&');
		opSet.add('|');
        opSet.add('<');
        opSet.add('>');
        opSet.add('=');
	}

	public JackTokenizer(File inFile)
	{ 
		try {
			scanner = new Scanner(inFile);
		} catch (FileNotFoundException e) {
			System.err.println(inFile + " : File Not Found");
		}

		initializeRegex();
		tokens = new ArrayList<String>();

		String line;
		while(scanner.hasNext())
		{
			line = removeBlockComment(removeLineComment(scanner.nextLine()));
			if(line.length()!=0)
			{
				Matcher m = tokenPattern.matcher(line);
				while(m.find())
				{
					tokens.add(m.group());
				}
			}
        }
	}

	public boolean hasMoreTokens()
	{
		return pointer < tokens.size();
	}

	public void advance()
	{
        if (hasMoreTokens()) 
            currentToken = tokens.get(pointer++);
        else
            throw new IllegalStateException("No more tokens");

        if(currentToken.matches(keyWordRegex))
        	currentTokenType = TokenType.KEYWORD;
        else if(currentToken.matches(symbolRegex))
        	currentTokenType = TokenType.SYMBOL;
        else if(currentToken.matches(idRegex))
        	currentTokenType = TokenType.IDENTIFIER;
        else if(currentToken.matches(intRegex))
        	currentTokenType = TokenType.INT_CONST;
        else if(currentToken.matches(strRegex))
        	currentTokenType = TokenType.STRING_CONST;
        else
        	throw new IllegalArgumentException("Unknown token:" + currentToken);
	}

	public String getCurrentToken()
	{
		return currentToken;
	}

	public TokenType tokenType()
	{
		return currentTokenType;
	}

	public KeyWord keyWord()
	{
		if(currentTokenType == TokenType.KEYWORD)
			return keyWordMap.get(currentToken);
		else
			throw new IllegalStateException("Current token is not a keyword");
	}

	public char symbol()
	{
		if(currentTokenType == TokenType.SYMBOL)
			return currentToken.charAt(0);
		else
			throw new IllegalStateException("Current token is not a symbol");
	}

	public String identifier()
	{
		if(currentTokenType == TokenType.IDENTIFIER)
			return currentToken;
		else
			throw new IllegalStateException("Current token is not an identifier");
	}

	public Integer intVal()
	{
		if(currentTokenType == TokenType.INT_CONST)
		{
			int intval = Integer.parseInt(currentToken);
			if(intval >= 0 && intval <= 32767)
				return intval;
			else
				throw new IllegalStateException("Current token is not an integer");
		}
		else
			throw new IllegalStateException("Current token is not an integer");
	}

	public String stringVal()
	{
		if(currentTokenType == TokenType.STRING_CONST)
			return currentToken.substring(1,currentToken.length() - 1);
		else
			throw new IllegalStateException("Current token is not a string constant");
	}

	public void pointerBack()
	{
		if(pointer > 0)
			pointer--;
	}

    public boolean isOp()
    {
        return opSet.contains(symbol());
    }

	private void initializeRegex()
	{
		keyWordRegex="";
		for (String seg: keyWordMap.keySet())
			keyWordRegex += seg + "|";

		symbolRegex = "[\\{\\}\\(\\)\\[\\]\\.\\,\\;\\+\\-\\*\\/\\&\\|\\<\\>\\=\\~]";
		intRegex = "[0-9]+";
		strRegex = "\"[^\"\\n]*\"";
		idRegex = "[_a-zA-Z][_a-zA-Z0-9]*";

		tokenPattern = Pattern.compile(idRegex + "|" + keyWordRegex + symbolRegex + "|" + intRegex + "|" + strRegex);
	}

	private String removeLineComment(String strIn)
	{
		String temp = strIn.replaceAll("//.*","");
		return temp.replaceAll("(\\s)+"," ").trim();
	}

	private String removeBlockComment(String strIn)
	{
		int startIndex = strIn.indexOf("/*");
		int endIndex = strIn.lastIndexOf("*/");
		String result;
		if(isBlockCommentStarted)
		{
			if(endIndex == -1)
				result = "";
			else
			{
				isBlockCommentStarted = false;
				result = strIn.substring(endIndex+2);
			}
		}
		else
		{
			if(startIndex == -1)
				result = strIn;
			else if(endIndex == -1)
			{
				isBlockCommentStarted = true;
				result = strIn.substring(0,startIndex);
			}
			else
				result = (strIn.substring(0,startIndex)+strIn.substring(endIndex+2));
		}
		return result.replaceAll("(\\s)+"," ").trim();
	}	
}