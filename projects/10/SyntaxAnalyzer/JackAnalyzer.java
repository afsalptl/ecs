import java.io.*;
import java.util.*;

public class JackAnalyzer
{
	public static void main(String[] args)
    {
		if (args.length != 1) 
   		{
    		System.err.println("Invalid Format\nUse : java JackAnalyzer <filename|directory>");
    		return;
    	}

    	String inFileName = args[0], outFilePath = null, outTokenFilePath = null;
    	File fileIn = new File(inFileName);
    	File fileOut,tokenFileOut;
    	ArrayList<File> fileList = new ArrayList<File>();

    	if(fileIn.isFile())
    	{
    		if (!inFileName.endsWith(".jack"))
	    		throw new IllegalArgumentException("Invalid File Type\nUse .jack file | directory containing jack files");
    		fileList.add(fileIn);
    	}
    	else if(fileIn.isDirectory())
    	{
    		fileList = getFiles(fileIn);
    		if(fileList.size() == 0)
    			throw new IllegalArgumentException("No jack files in this directory");
   		}
    	else
    		throw new IllegalArgumentException("No file|directory with the given name : "+inFileName);

	    for (File f: fileList)
	    {
	        outFilePath = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(".")) + ".xml";
	        outTokenFilePath = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(".")) + "T.xml";
	        fileOut = new File(outFilePath);
	        tokenFileOut = new File(outTokenFilePath);

	        CompilationEngine compilationEngine = new CompilationEngine(f,fileOut,tokenFileOut);
	        compilationEngine.compileClass();

	        System.out.println("File created : " + outFilePath);
	        System.out.println("File created : " + outTokenFilePath);    	
	    }
        System.out.println("Success");
	}

    private static ArrayList<File> getFiles(File dir)
    {
    	ArrayList<File> temp = new ArrayList<File>();
    	File[] files = dir.listFiles();
    	for (File f:files) 
    	{
    		if(f.getName().endsWith(".jack"))
    			temp.add(f);
    	}
    	return temp;
    }
}