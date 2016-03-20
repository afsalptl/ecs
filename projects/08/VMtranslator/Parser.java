import java.io.*;
import java.util.*;

public class Parser
{
	private Scanner scanner;
	private String currentCommand=null,tempCommand=null;
	private String[] segments;
	private static final String[] arithmeticCommands = {"add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"};

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

	public commandType commandType()
	{
		commandType cType = null;
		segments = currentCommand.split(" ");

		if(segments.length == 1)
		{
			if(Arrays.asList(arithmeticCommands).contains(segments[0]))
				cType = commandType.C_ARITHMETIC;
			else if(segments[0].equals("return"))
				cType = commandType.C_RETURN;
		}
		else if(segments.length == 2)
		{
			if(segments[0].equals("label"))
				cType = commandType.C_LABEL;
			else if(segments[0].equals("goto"))
				cType = commandType.C_GOTO;
			else if(segments[0].equals("if-goto"))
				cType = commandType.C_IF;
		}
		else if(segments.length == 3)
		{
			if(segments[0].equals("push"))
				cType = commandType.C_PUSH;
			else if(segments[0].equals("pop"))
				cType = commandType.C_POP;
			else if(segments[0].equals("function"))
				cType = commandType.C_FUNCTION;
			else if(segments[0].equals("call"))
				cType = commandType.C_CALL;
		}
		return cType;
	}

	public String arg1()
	{
		String arg1 = null;
		if(commandType() == commandType.C_ARITHMETIC)
			arg1 = segments[0];
		else if(segments.length > 1)
			arg1 = segments[1];
		return arg1;
	}

	public int arg2()
	{
		int arg2 = -1;
		if(segments.length == 3)
			arg2 = Integer.parseInt(segments[2]);
		return arg2;
	}

	private String removeComments(String in)
	{
		String temp = in.replaceAll("//.*","").trim();
		return temp.replaceAll("(\\s)+"," ");
	}
}