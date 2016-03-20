import java.io.*;

public class CodeWriter
{
	private static BufferedWriter bw;
	private static int jumpLabelId = 0;
	private String fileName;
	private String funcName = null;
	private static int retAddressId = 0;

	public CodeWriter(File fileOut)
	{
    	FileWriter fw = null;
    	try {
			fw = new FileWriter(fileOut);
		} catch (IOException e) {
			e.printStackTrace();
		}
		bw = new BufferedWriter(fw);
	}

	public void setFileName(String fn)
	{
		fileName = fn;
		printlnToFile("//Translation of "+fileName);
	}

	private String getFileName()
	{
		return fileName.substring(0,fileName.lastIndexOf('.'));
	}

	public void writeInit()
	{
		String temp = 	"@256\n" +
						"D=A\n" +
						"@SP\n" +
						"M=D";
		printlnToFile(temp);
		writeCall("Sys.init", 0);
	}

	public void writeArithmetic(String command)
	{
		switch(command)
		{
			case "add" :
				printlnToFile(arithmeticBinaryTemplate('+'));
				break;
			case "sub" :
				printlnToFile(arithmeticBinaryTemplate('-'));
				break;
			case "neg" :
				printlnToFile(arithmeticUnaryTemplate('-'));
				break;
			case "eq" :
				printlnToFile(logicalTemplate("JEQ"));
				break;
			case "gt" :
				printlnToFile(logicalTemplate("JGT"));
				break;
			case "lt" :
				printlnToFile(logicalTemplate("JLT"));
				break;
			case "and" :
				printlnToFile(arithmeticBinaryTemplate('&'));
				break;
			case "or" :
				printlnToFile(arithmeticBinaryTemplate('|'));
				break;
			case "not" :
				printlnToFile(arithmeticUnaryTemplate('!'));
				break;
		}
	}

	private String arithmeticBinaryTemplate(char op)
	{	
		return 	writePopD() + "\n"
				+ "A=A-1\n"		//A=SP-1
				+ "M=M"+op+"D";
	}

	private String arithmeticUnaryTemplate(char op)
	{ 	
		return 	"@SP\n"			//A=address of SP
				+ "A=M-1\n"		//A=SP-1
				+ "M="+op+"M";
	}

	private String logicalTemplate(String type)
	{
		String temp;
		temp = 	writePopD() + "\n" +
				"A=A-1\n" +				//A=SP-1
				"D=M-D\n" +				//D=stack[SP-1]-stack[SP]
				"@TRUE_" + jumpLabelId + "\n" +
				"D;" + type + "\n" +
				"@SP\n" +				//false(0x0000 = 0) //A=address of SP
				"A=M-1\n" +				//A=SP-1
				"M=0\n" +				//stack[SP-1] = false
				"@CONTINUE_" + jumpLabelId + "\n" +
				"0;JMP\n" +
				"(TRUE_" + jumpLabelId + ")\n" +
				"@SP\n" +				//true(0xFFFF = -1) //A=address of SP
				"A=M-1\n" +				//A=SP-1
				"M=-1\n" +				//stack[SP-1] = true
				"(CONTINUE_" + jumpLabelId + ")";
		jumpLabelId++;
		return temp;
	}

	public void writePushPop(commandType command, String segment, int index)
	{
		if(command == commandType.C_PUSH)
		{
			switch(segment)
			{
				case "local" :
					printlnToFile(pushTemplate("LCL", index));
					break;
				case "argument" :
					printlnToFile(pushTemplate("ARG", index));
					break;
				case "this" :
					printlnToFile(pushTemplate("THIS", index));
					break;
				case "that" :
					printlnToFile(pushTemplate("THAT", index));
					break;
				case "pointer" :
					printlnToFile(pushTemplate("R3", index));
					break;
				case "temp" :
					printlnToFile(pushTemplate("R5", index));
					break;
				case "constant" :
					printlnToFile(pushConstant(index));
					break;
				case "static" :
					printlnToFile(pushStatic(index));
					break;
			}
		}
		else if(command == commandType.C_POP)
		{
			switch(segment)
			{
				case "local" :
					printlnToFile(popTemplate("LCL", index));
					break;
				case "argument" :
					printlnToFile(popTemplate("ARG", index));
					break;
				case "this" :
					printlnToFile(popTemplate("THIS", index));
					break;
				case "that" :
					printlnToFile(popTemplate("THAT", index));
					break;
				case "pointer" :
					printlnToFile(popTemplate("R3", index));
					break;
				case "temp" :
					printlnToFile(popTemplate("R5", index));
					break;
				case "static" :
					printlnToFile(popStatic(index));
					break;
			}
		}
	}

	private String pushTemplate(String segment, int index)
	{
		String baseAddressCommand;
		if(segment.equals("R3") || segment.equals("R5"))
			baseAddressCommand = "D=A\n";	//D=Base Address
		else
			baseAddressCommand = "D=M\n";	//D=base address
		
		return	"@" + segment + "\n" +	//A=symbol refering to base address
				baseAddressCommand +
				"@" + index + "\n" +	//A=index
				"A=D+A\n" + 			//A=segment+index
				"D=M\n" +				//D=segment[index]
				writePushD();
	}

	private String pushConstant(int index)
	{
		return  "@" + index + "\n" +	//A=index
				"D=A\n" +				//D=index
				writePushD();
	}

	private String pushStatic(int index)
	{
		return 	"@" + getFileName() + "." + index + "\n" +
				"D=M\n" +
				writePushD();
	}

	private String popTemplate(String segment, int index)
	{
		String baseAddressCommand;
		if(segment.equals("R3") || segment.equals("R5"))
			baseAddressCommand = "D=A\n";	//D=Base Address
		else
			baseAddressCommand = "D=M\n";	//D=base address
				
		return  "@" + segment + "\n" +	//A=symbol refering to base address/b.a
				baseAddressCommand +
				"@" + index + "\n" +	//A=index
				"D=D+A\n" + 			//D=segment+index
				"@R13\n" +				//A=R13
				"M=D\n" +				//MEM[R13]=segment+index
				writePopD() + "\n" +
				"@R13\n" +				//A=R13
				"A=M\n" +				//A=segment+index
				"M=D";					//segment[index]=D
	}

	private String popStatic(int index)
	{
		return	writePopD() + "\n" +
				"@" + getFileName() + "." + index + "\n" +
				"M=D";
	}

	public void writeLabel(String label)
	{
		printlnToFile("(" + modifyLabel(label) + ")");
	}

	public void writeGoto(String label)
	{
		String temp = 	"@" + modifyLabel(label) + "\n" +
						"0;JMP";
		printlnToFile(temp);	
	}

	public void writeIf(String label)
	{
		String temp = 	writePopD() + "\n" +
						"@" + modifyLabel(label) + "\n" +
						"D;JNE";
		printlnToFile(temp);
	}

	private String modifyLabel(String label)
	{
		if(funcName != null)
			label = funcName + "$" + label;
		return label;
	}

	public void writeCall(String functionName,int numArgs)
	{
		String retAddressLabel = "RETURN_ADDRESS_" + retAddressId++;

		String temp = 	"@" + retAddressLabel + "\n" +
						"D=A\n" +
						writePushD() + "\n" +
						saveRegister("LCL") +
						saveRegister("ARG") + 
						saveRegister("THIS") +
						saveRegister("THAT") +
						"@SP\n" +
						"D=M\n" +
						"@" + numArgs + "\n" +
						"D=D-A\n" +
						"@5\n" +
						"D=D-A\n" +
						"@ARG\n" +
						"M=D\n" +
						"@SP\n" +
						"D=M\n" +
						"@LCL\n" +
						"M=D\n" +
						"@" + functionName + "\n" +
						"0;JMP\n" +
						"(" + retAddressLabel + ")";
		printlnToFile(temp);
	}

	private String saveRegister(String regName)
	{
		return 	"@" + regName + "\n" +
				"D=M\n" +
				writePushD() + "\n";
	}

	public void writeReturn()
	{
		String temp = 	"@LCL\n" +
						"D=M\n" +
						"@R13\n" +		//R13 is temp.varible for FRAME
						"M=D\n" +
						"@5\n" +
						"A=D-A\n" +
						"D=M\n" +
						"@R14\n" +		//R14 is temp.varible for RET
						"M=D\n" +
						writePopD() + "\n" +
						"@ARG\n" +
						"A=M\n" +
						"M=D\n" +
						"@ARG\n" +
						"D=M\n" +
						"@SP\n" +
						"M=D+1\n" +
						restoreRegister("THAT",1) +
						restoreRegister("THIS",2) +
						restoreRegister("ARG",3) +
						restoreRegister("LCL",4) +
						"@R14\n" +
						"A=M\n" +
						"0;JMP";
		printlnToFile(temp);
	}

	private String restoreRegister(String regName, int index)
	{
		return 	"@R13\n" +
				"D=M\n" +
				"@" + index + "\n" +
				"A=D-A\n" +
				"D=M\n" +
				"@"+ regName + "\n" +
				"M=D\n"; 
	}

	public void writeFunction(String functionName, int numLocals)
	{
		funcName = functionName;
		String temp = "(" + functionName + ")";

		for (int i = 0; i < numLocals ; i++)
			temp = temp + "\n" + pushConstant(0);
		printlnToFile(temp);
	}

	private String writePushD()
	{
		return  "@SP\n" +		//A=address of SP
				"A=M\n" +		//A=SP
				"M=D\n" +		//stack[SP]=D
				"@SP\n" +		//A=address of SP
				"M=M+1";		//SP=SP+1
	}

	private String writePopD()
	{
		return  "@SP\n" +		//A=address of SP
				"AM=M-1\n" +	//A=SP-1, SP=SP-1
				"D=M";			//D=stack[SP]
	}

	public void Close()
	{
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printlnToFile(String line)
	{
		try {
			bw.write(line + "\n\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}