import java.io.*;
import java.util.*;

public class VMtranslator
{
	public static void main(String[] args)
    {
		if (args.length != 1) 
   		{
    		System.err.println("Invalid Format\nUse : java VMtranslator <filename|directory>");
    		return;
    	}

    	ArrayList<File> fileList = new ArrayList<File>();
    	String inFileName = args[0], outFilePath = null;
    	File fileIn = new File(inFileName);

    	if(fileIn.isFile())
    	{
    		if (!inFileName.endsWith(".vm"))
   			{
	    		System.err.println("Invalid File Type\nUse .vm file | directory containing vm files");
	    		return;
    		}

    		fileList.add(fileIn);
    		outFilePath = fileIn.getAbsolutePath().substring(0,fileIn.getAbsolutePath().lastIndexOf('.'))+".asm";
    	}
    	else if(fileIn.isDirectory())
    	{
    		fileList = getFiles(fileIn);

    		if(fileList.size() == 0)
    		{	
    			System.err.println("No vm files in this directory");
    			return;
    		}

    		outFilePath = fileIn.getAbsolutePath() + "/" + fileIn.getName() + ".asm";
    	}
    	else
    	{
    		System.err.println("No file|directory with the given name : "+inFileName);
    		return;
    	}

    	File fileOut = new File(outFilePath);
    	CodeWriter codewriter = new CodeWriter(fileOut);

    	commandType cType;
		String arg1;
		int arg2;

    	for(File f : fileList)
    	{
    		Parser parser = new Parser(f.getAbsolutePath());

    		codewriter.setFileName(f.getName());

			while(parser.hasMoreCommands())
			{
				arg1 = null;
				arg2 = -1;
				parser.advance();
				cType = parser.commandType();
				if(cType != commandType.C_RETURN)
					arg1 = parser.arg1();
				if(cType == commandType.C_PUSH || cType == commandType.C_POP || cType == commandType.C_FUNCTION || cType == commandType.C_CALL )
					arg2 = parser.arg2();
				
				if(cType == commandType.C_ARITHMETIC)
					codewriter.writeArithmetic(arg1);
				else if(cType == commandType.C_PUSH || cType == commandType.C_POP)
					codewriter.writePushPop(cType, arg1, arg2);
			}
			System.out.println(f.getName() + " translated");
    	}

    	codewriter.Close();
    	System.out.println("Success : "+outFilePath+" generated");
    }

    private static ArrayList<File> getFiles(File dir)
    {
    	ArrayList<File> temp = new ArrayList<File>();
    	File[] files = dir.listFiles();
    	for (File f:files) 
    	{
    		if(f.getName().endsWith(".vm"))
    			temp.add(f);
    	}
    	return temp;
    }
}