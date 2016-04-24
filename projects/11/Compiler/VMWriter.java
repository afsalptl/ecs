import java.io.*;
import java.util.*;

public class VMWriter
{
	public static enum SEGMENT
	{
		CONST, ARG, LOCAL, STATIC, THIS, THAT, POINTER, TEMP, NIL
	}

	public static enum COMMAND
	{
		ADD, SUB, NEG, EQ, GT, LT, AND, OR, NOT, NIL
	}

	private PrintWriter printWriter;
	private static HashMap<SEGMENT, String> segmentMap = new HashMap<SEGMENT, String>();
	private static HashMap<COMMAND, String> commandMap = new HashMap<COMMAND, String>();

	static
	{
		segmentMap.put(SEGMENT.CONST, "constant");
		segmentMap.put(SEGMENT.ARG, "argument");
		segmentMap.put(SEGMENT.LOCAL, "local");
		segmentMap.put(SEGMENT.STATIC, "static");
		segmentMap.put(SEGMENT.THIS, "this");
		segmentMap.put(SEGMENT.THAT, "that");
		segmentMap.put(SEGMENT.POINTER, "pointer");
		segmentMap.put(SEGMENT.TEMP, "temp");

		commandMap.put(COMMAND.ADD, "add");
		commandMap.put(COMMAND.SUB, "sub");
		commandMap.put(COMMAND.NEG, "neg");
		commandMap.put(COMMAND.EQ, "eq");
		commandMap.put(COMMAND.GT, "gt");
		commandMap.put(COMMAND.LT, "lt");
		commandMap.put(COMMAND.AND, "and");
		commandMap.put(COMMAND.OR, "or");
		commandMap.put(COMMAND.NOT, "not");
	}

	public VMWriter(File fileOut)
	{
		try {
            printWriter = new PrintWriter(fileOut);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
	}

	public void writePush(SEGMENT segment, int index)
	{
		printCommand("push "+segmentMap.get(segment)+" "+index);
	}

	public void writePop(SEGMENT segment, int index)
	{
		printCommand("pop "+segmentMap.get(segment)+" "+index);
	}

	public void writeArithmetic(COMMAND command)
	{
		printCommand(commandMap.get(command));
	}

	public void writeLabel(String label)
	{
		printCommand("label "+label);
	}

	public void writeGoto(String label)
	{
		printCommand("goto "+label);
	}

	public void writeIf(String label)
	{
		printCommand("if-goto "+label);
	}

	public void writeCall(String name, int nArgs)
	{
		printCommand("call "+name+" "+nArgs);
	}

	public void writeFunction(String name, int nLocals)
	{
		printCommand("function "+name+" "+nLocals);
	}

	public void writeReturn()
	{
		printCommand("return");
	}

	public void printCommand(String cmd)
	{
		printWriter.println(cmd);
	}

	public void close()
	{
        printWriter.close();
    }
}