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

    	String inFileName = args[0], outFilePath = null;

		if (!inFileName.endsWith(".asm"))
   		{
    		System.err.println("Invalid File Type\nUse .asm file containing hack assembly code");
    		return;
    	}

		File fileIn = new File(inFileName);
		if(!fileIn.exists())
		{
			System.err.println(inFileName + " : File Not Found");
			return;
		}

		parser = new Parser(inFileName);
		code = new Code();
		symboltable = new SymbolTable();

    	outFilePath = fileIn.getAbsolutePath().substring(0, fileIn.getAbsolutePath().lastIndexOf('.'))+".hack";
    	File fileOut = new File(outFilePath);
    	
    	FileWriter fw = null;
    	try {
			fw = new FileWriter(fileOut);
		} catch (IOException e) {
			e.printStackTrace();
		}
		bw = new BufferedWriter(fw);

		firstPass();
		parser = new Parser(inFileName);	// re-initialize the file for reading
		secondPass();
    	closeFile();
    	System.out.println("Success : "+outFilePath+" generated");
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