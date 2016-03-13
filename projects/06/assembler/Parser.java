import java.io.*;
import java.util.*;

public class Parser
{
	private Scanner scanner;
	private String currentCommand=null,tempCommand=null;
	public static final char A_COMMAND = 'a';
	public static final char C_COMMAND = 'c';
	public static final char L_COMMAND = 'l';

	public Parser(String infile)
	{ 
		try {
			scanner = new Scanner(new FileInputStream(infile));
		} catch (FileNotFoundException e) {
			System.err.println(infile + " : File Not Found");
		}
	}

	public boolean hasMoreCommands()
	{
		while(scanner!=null && scanner.hasNextLine())
		{
			tempCommand = removeComments(scanner.nextLine());
			if(tempCommand.length() != 0)
				return true;
		}
		return false;
	}

	public void advance()
	{
		currentCommand = tempCommand;
	}

	public char commandType()
	{
		if(currentCommand.startsWith("@"))
			return A_COMMAND;
		else if(currentCommand.startsWith("("))
			return L_COMMAND;
		else
			return C_COMMAND;
	}

	public String symbol()
	{
		String symbol = null;
		if(commandType() == A_COMMAND)
			symbol = currentCommand.substring(1);
		else if(commandType() == L_COMMAND)
			symbol = currentCommand.substring(1,currentCommand.indexOf(')'));
		return symbol;
	}

	public String dest()
	{
		String dest = "null";		// is not a null string
		if(currentCommand.contains("="))
			dest = currentCommand.substring(0, currentCommand.indexOf('='));
		return dest;
	}

	public String comp()
	{
		String comp = null;
		if(currentCommand.contains("="))
			comp = currentCommand.substring(currentCommand.indexOf('=')+1);
		else if(currentCommand.contains(";"))
			comp = currentCommand.substring(0, currentCommand.indexOf(';'));
		return comp;
	}

	public String jump()
	{
		String jump = "null";		// is not a null string
		if(currentCommand.contains(";"))
			jump = currentCommand.substring(currentCommand.indexOf(';')+1);
		return jump;
	}

	private String removeComments(String in)
	{
		return in.replaceAll("//.*|(\\s)+","");
	}
}