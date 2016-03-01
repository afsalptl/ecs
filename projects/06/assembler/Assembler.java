import java.io.*;

public class Assembler
{
	private static Parser parser;
	private static Code code;
	private static SymbolTable symboltable;
	private static BufferedWriter bw;

	public static void main(String[] args) throws IOException
    {
    	if (args.length != 1) 
   		{
    		System.err.println("Invalid Format\nUse : java Assembler <filename>");
    		return;
    	}

    	String infilename,outfilename,name;
    	infilename = args[0];

		if (!infilename.endsWith(".asm"))
   		{
    		System.err.println("Invalid File Type\nUse .asm file containing hack assembly code");
    		System.exit(-1);
    	}

		File f = new File(infilename);
		if(!f.exists())
		{
			System.err.println(infilename + " : File Not Found");
			System.exit(-1);
		}

    	name = infilename.substring(0, infilename.lastIndexOf('.'));
    	outfilename = name+".hack";

		parser = new Parser(infilename);
		code = new Code();
		symboltable = new SymbolTable();

    	File outfile = new File(outfilename);
    	FileWriter fw = null;
    	try {
			fw = new FileWriter(outfile.getAbsoluteFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
		bw = new BufferedWriter(fw);

		firstPass();
		parser = new Parser(infilename);	// re-initialize the file for reading
		secondPass();
    	closeFile();
    	System.out.println("Success : "+outfilename+" generated");
    }

	private static void firstPass()
	{
		//add symbols encountered into symboltable 
		int commandCounter = 0;
		while(parser.hasMoreCommands())
		{
			parser.advance();
			if(parser.commandType() == parser.L_COMMAND)
				symboltable.addEntry(parser.symbol(),commandCounter);
			else
				commandCounter++;
		}
	}

	private static void secondPass()
	{
		int RAMaddress = 16;
		String hackCommand = null;

		while(parser.hasMoreCommands())
		{
			parser.advance();
			if(parser.commandType() == parser.C_COMMAND)
			{
				String dest_code = code.dest(parser.dest());		
				String comp_code = code.comp(parser.comp());
				String jump_code = code.jump(parser.jump());

				hackCommand = "111" + comp_code + dest_code + jump_code;
				printlnToFile(hackCommand);
			}
			else if(parser.commandType() == parser.A_COMMAND)
			{
				String symbol = parser.symbol();
				if(isNum(symbol))
					hackCommand = decToBin(Integer.parseInt(symbol));
				else
				{
					if(!symboltable.contains(symbol))
					{
						symboltable.addEntry(symbol, RAMaddress);
						RAMaddress++;
					}
					if(symboltable.contains(symbol))
						hackCommand = decToBin(symboltable.getAddress(symbol));
				}
				printlnToFile(hackCommand);
			}
		}
	}

	public static boolean isNum(String symbol)
    {
    	return Character.isDigit(symbol.charAt(0));
    }

    public static String decToBin(int decimal) 
    {
		String binary = Integer.toBinaryString(0x10000 | decimal).substring(1);
		return binary;	
	}

	public static void printlnToFile(String line)
	{
		try {
			bw.write(line + '\n');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void closeFile()
	{
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}