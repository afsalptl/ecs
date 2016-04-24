import java.util.*;

public class SymbolTable
{
	private HashMap<String, Symbol> classScopeTable;
	private HashMap<String, Symbol> subroutineScopeTable;
	private HashMap<Symbol.KIND, Integer> indexTable;

	public SymbolTable()
	{
		classScopeTable = new HashMap<String, Symbol>();
		subroutineScopeTable = new HashMap<String, Symbol>();

		indexTable = new HashMap<Symbol.KIND, Integer>();
		indexTable.put(Symbol.KIND.STATIC, 0);
		indexTable.put(Symbol.KIND.FIELD, 0);
		indexTable.put(Symbol.KIND.ARG, 0);
		indexTable.put(Symbol.KIND.VAR, 0);
	}

	public void startSubroutine()
	{
		subroutineScopeTable.clear();
		indexTable.put(Symbol.KIND.ARG, 0);
		indexTable.put(Symbol.KIND.VAR, 0);
	}

	public void define(String name, String type, Symbol.KIND kind)
	{
		int index = indexTable.get(kind);
		Symbol symbol = new Symbol(type, kind, index);
		indexTable.put(kind, index+1);

		if(kind == Symbol.KIND.ARG || kind == Symbol.KIND.VAR)
			subroutineScopeTable.put(name, symbol);
		else if(kind == Symbol.KIND.STATIC || kind == Symbol.KIND.FIELD)
			classScopeTable.put(name, symbol);
	}

	public int varCount(Symbol.KIND kind)
	{
		return indexTable.get(kind);
	}

	public Symbol.KIND kindOf(String name)
	{
		Symbol symbol = lookUp(name);
		if(symbol != null)
			return symbol.getKind();
		else
			return Symbol.KIND.NONE;
	}

	public String typeOf(String name)
	{
		Symbol symbol = lookUp(name);
		if(symbol != null)
			return symbol.getType();
		else
			return "";
	}

	public int indexOf(String name)
	{
		Symbol symbol = lookUp(name);
		if(symbol != null)
			return symbol.getIndex();
		else
			return -1;		
	}

	private Symbol lookUp(String name)
	{
		if(classScopeTable.get(name) != null)
			return classScopeTable.get(name);
		else if(subroutineScopeTable.get(name) != null)
			return subroutineScopeTable.get(name);
		else
			return null;
	}
}