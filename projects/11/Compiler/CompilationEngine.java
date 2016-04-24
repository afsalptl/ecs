import java.io.File;

public class CompilationEngine 
{
    private VMWriter vmWriter;
    private JackTokenizer tokenizer;
    private SymbolTable st;
    private String currentClass, currentSubroutine;
    private int labelIndex;

    public CompilationEngine(File fileIn, File fileOut) 
    {
        tokenizer = new JackTokenizer(fileIn);
        vmWriter = new VMWriter(fileOut);
        st = new SymbolTable();

        labelIndex = 0;
    }

    private String currentFunction()
    {
        if(currentClass.length() != 0 && currentSubroutine.length() !=0)
            return currentClass + "." + currentSubroutine;
        return "";
    }

    /**
     * class : 'class' className '{' classVarDec* subroutineDec* '}' 
     */
    public void compileClass()
    {
        tokenizer.advance();
        if(tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.keyWord() != KeyWord.CLASS)
            ReportError("class");

        requireIdentifier("className");
        currentClass = tokenizer.identifier();
        requireSymbol('{');
        compileClassVarDec();
        compileSubroutine();
        requireSymbol('}');

        if (tokenizer.hasMoreTokens())
            throw new IllegalStateException("Unexpected tokens");

        vmWriter.close();
    }

    /** 
     * classVarDec : ('static'|'field') type varName (','varName)* ';' 
     */
    private void compileClassVarDec()
    {
        tokenizer.advance();
        if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '}')
        {
            tokenizer.pointerBack();
            return;
        }
        else if(tokenizer.tokenType() != TokenType.KEYWORD)
            ReportError("keyword");
        else if(tokenizer.keyWord() == KeyWord.CONSTRUCTOR || tokenizer.keyWord() == KeyWord.FUNCTION || tokenizer.keyWord() == KeyWord.METHOD)
        {
            tokenizer.pointerBack();
            return;
        }

        if(tokenizer.keyWord() != KeyWord.STATIC && tokenizer.keyWord() != KeyWord.FIELD)
            ReportError("static | field");

        Symbol.KIND kind = null;
        String type = "";
        String name = "";

        if(tokenizer.keyWord() == KeyWord.STATIC)
            kind = Symbol.KIND.STATIC;
        else if(tokenizer.keyWord() == KeyWord.FIELD)
            kind = Symbol.KIND.FIELD;

        type = compileType();

        do{
            requireIdentifier("varName");
            name = tokenizer.identifier();
            st.define(name, type, kind);

            tokenizer.advance();
            if(tokenizer.tokenType() != TokenType.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ';'))
                ReportError("',' or ';'");

            if(tokenizer.symbol() == ';')
                break;
        }while(true);

        compileClassVarDec();
    }

    /**
     * subroutineDec : ('constructor'|'function'|'method') ('void'|type) subroutineName '(' parameterList ')' subroutineBody 
     */
    private void compileSubroutine()
    {
        tokenizer.advance();
        if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '}')
        {
            tokenizer.pointerBack();
            return;
        }
        else if(tokenizer.tokenType() != TokenType.KEYWORD || (tokenizer.keyWord() != KeyWord.CONSTRUCTOR && tokenizer.keyWord() != KeyWord.FUNCTION && tokenizer.keyWord() != KeyWord.METHOD))
            ReportError("constructor | function | method");

        KeyWord kw = tokenizer.keyWord();
        st.startSubroutine();

        if(kw == KeyWord.METHOD)
        {
            st.define("this",currentClass, Symbol.KIND.ARG);
        }

        String type = "";

        tokenizer.advance();
        if(tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyWord() == KeyWord.VOID)
            type = "void";
        else
        {
            tokenizer.pointerBack();
            type = compileType();
        }

        requireIdentifier("subroutineName");
        currentSubroutine = tokenizer.identifier();
        requireSymbol('(');
        compileParameterList();
        requireSymbol(')');
        compileSubroutineBody(kw);

        compileSubroutine();
    }

    /**
     * parameterList : ((type varName)(','type varName)*)? 
     */
    private void compileParameterList()
    {
        tokenizer.advance();
        if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ')')
        {
            tokenizer.pointerBack();
            return;
        }

        String type = "";

        tokenizer.pointerBack();
        do{
            type = compileType();

            requireIdentifier("varName");
            st.define(tokenizer.identifier(), type, Symbol.KIND.ARG);

            tokenizer.advance();
            if(tokenizer.tokenType() != TokenType.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ')'))
                ReportError("',' or ')'");

            if(tokenizer.symbol() == ')')
            {
                tokenizer.pointerBack();
                break;
            }
        }while(true);
    }

    /**
     * subroutineBody : '{' varDec* statements '}' 
     */
    private void compileSubroutineBody(KeyWord kw)
    {
        requireSymbol('{');
        compileVarDec();
        vmWriter.writeFunction(currentFunction(), st.varCount(Symbol.KIND.VAR));

        if(kw == KeyWord.METHOD)
        {
            vmWriter.writePush(VMWriter.SEGMENT.ARG, 0);
            vmWriter.writePop(VMWriter.SEGMENT.POINTER, 0);
        }
        else if(kw == KeyWord.CONSTRUCTOR)
        {
            vmWriter.writePush(VMWriter.SEGMENT.CONST, st.varCount(Symbol.KIND.FIELD));
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(VMWriter.SEGMENT.POINTER, 0);
        }
        compileStatements();
        requireSymbol('}');
    }

    /** 
     * 'var' type varName(','varName)* ';'
     */
    private void compileVarDec()
    {
        tokenizer.advance();
        if(tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.keyWord() != KeyWord.VAR)
        {
            tokenizer.pointerBack();
            return;
        }

        String type = compileType();

        do{
            requireIdentifier("varName");
            st.define(tokenizer.identifier(), type, Symbol.KIND.VAR);

            tokenizer.advance();
            if(tokenizer.tokenType() != TokenType.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ';'))
                ReportError("',' or ';'");

            if(tokenizer.symbol() == ';')
                break;
        }while(true);

        compileVarDec();
    }

    /**
     * statements : statement* 
     */
    private void compileStatements()
    {
        tokenizer.advance();
        if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '}')
        {
            tokenizer.pointerBack();
            return;
        }

        if(tokenizer.tokenType() != TokenType.KEYWORD)
            ReportError("keyword");
        else
        {
            if(tokenizer.keyWord() == KeyWord.LET)
                    compileLet();
            else if(tokenizer.keyWord() == KeyWord.IF)
                    compileIf();
            else if(tokenizer.keyWord() == KeyWord.WHILE)
                    compileWhile();
            else if(tokenizer.keyWord() == KeyWord.DO)
                    compileDo();
            else if(tokenizer.keyWord() == KeyWord.RETURN)
                    compileReturn();
            else
                ReportError("let | if | while | do | return");
        }

        compileStatements();
    }

    /**
     * letStatement : 'let' varName ('[' expression ']')? '=' expression ';'
     */
    private void compileLet()
    {
        requireIdentifier("varName");
        String varName = tokenizer.identifier();

        tokenizer.advance();
        if(tokenizer.tokenType() != TokenType.SYMBOL || (tokenizer.symbol() != '[' && tokenizer.symbol() != '='))
            ReportError("'[' | '='");

        boolean expExist = false;

        if(tokenizer.symbol() == '[')
        {
            expExist = true;
            vmWriter.writePush(getSegment(st.kindOf(varName)), st.indexOf(varName));
            compileExpression();
            requireSymbol(']');
            vmWriter.writeArithmetic(VMWriter.COMMAND.ADD);
        }

        if (expExist)
            tokenizer.advance();

        compileExpression();
        requireSymbol(';');

        if(expExist)
        {
            vmWriter.writePop(VMWriter.SEGMENT.TEMP,0);
            vmWriter.writePop(VMWriter.SEGMENT.POINTER,1);
            vmWriter.writePush(VMWriter.SEGMENT.TEMP,0);
            vmWriter.writePop(VMWriter.SEGMENT.THAT,0);
        }
        else
            vmWriter.writePop(getSegment(st.kindOf(varName)), st.indexOf(varName));
    }

    private VMWriter.SEGMENT getSegment(Symbol.KIND kind)
    {
        switch (kind)
        {
            case FIELD:
                return VMWriter.SEGMENT.THIS;
            case STATIC:
                return VMWriter.SEGMENT.STATIC;
            case VAR:
                return VMWriter.SEGMENT.LOCAL;
            case ARG:
                return VMWriter.SEGMENT.ARG;
            default:
                return VMWriter.SEGMENT.NIL;
        }
    }

    /** 
     * ifStatement : 'if' '(' expression ')' '{' statements '}' ('else' '{' statements '}')?
     */
    private void compileIf()
    {
        String elseLabel = newLabel("ELSE");
        String endLabel = newLabel("END");

        requireSymbol('(');
        compileExpression();
        requireSymbol(')');
        vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);
        vmWriter.writeIf(elseLabel);
        requireSymbol('{');
        compileStatements();
        requireSymbol('}');
        vmWriter.writeGoto(endLabel);
        vmWriter.writeLabel(elseLabel);

        tokenizer.advance();
        if(tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyWord() == KeyWord.ELSE)
        {
            requireSymbol('{');
            compileStatements();
            requireSymbol('}');
        }
        else
            tokenizer.pointerBack();

        vmWriter.writeLabel(endLabel);  
    }

    private String newLabel(String prefix)
    {
        return prefix + "_LABEL_" + (labelIndex++);
    }

    /**
     * whileStatement : 'while' '(' expression ')' '{' statements '}'
     */
    private void compileWhile()
    {
        String continueLabel = newLabel("CONTINUE");
        String topLabel = newLabel("TOP");

        vmWriter.writeLabel(topLabel);
        requireSymbol('(');
        compileExpression();
        requireSymbol(')');
        vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);
        vmWriter.writeIf(continueLabel);
        requireSymbol('{');
        compileStatements();
        requireSymbol('}');
        vmWriter.writeGoto(topLabel);
        vmWriter.writeLabel(continueLabel);
    }

    /**
     * doStatement : 'do' subroutineCall ';'
     */
    private void compileDo()
    {
        compileSubroutineCall();
        requireSymbol(';');
        vmWriter.writePop(VMWriter.SEGMENT.TEMP,0);
    }

    /**
     * ReturnStatement : 'return' expression? ';'
     */
    private void compileReturn()
    {
        tokenizer.advance();
        if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ';')
            vmWriter.writePush(VMWriter.SEGMENT.CONST,0);
        else
        {
            tokenizer.pointerBack();
            compileExpression();
            requireSymbol(';');
        }

        vmWriter.writeReturn();
    }

    /**
     * expression : term (op term)*
     */
    private void compileExpression()
    {
        compileTerm();

        do{
            tokenizer.advance();
            if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.isOp())
            {
                String Op = "";

                switch(tokenizer.symbol())
                {
                    case '+' : Op = "add"; break;
                    case '-' : Op = "sub"; break;
                    case '*' : Op = "call Math.multiply 2"; break;
                    case '/' : Op = "call Math.divide 2"; break;
                    case '<' : Op = "lt"; break;
                    case '>' : Op = "gt"; break;
                    case '=' : Op = "eq"; break;
                    case '&' : Op = "and"; break;
                    case '|' : Op = "or"; break;
                    default : ReportError("Unknown op");
                }

                compileTerm();
                vmWriter.printCommand(Op);
            }
            else
            {
                tokenizer.pointerBack();
                break;
            }
        }while(true);
    }

    /**
     * term : integerConstant|stringConstant|keywordConstant|varName|varName'['expression']'|subroutineCall|'('expression')'|unaryOp term
     */
    private void compileTerm()
    {
        tokenizer.advance();
        if(tokenizer.tokenType() == TokenType.IDENTIFIER)
        {
            String tempId = tokenizer.identifier();

            tokenizer.advance();
            if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '[')
            {
                vmWriter.writePush(getSegment(st.kindOf(tempId)), st.indexOf(tempId));
                compileExpression();
                requireSymbol(']');
                vmWriter.writeArithmetic(VMWriter.COMMAND.ADD);
                vmWriter.writePop(VMWriter.SEGMENT.POINTER,1);
                vmWriter.writePush(VMWriter.SEGMENT.THAT,0);
            }
            else if(tokenizer.tokenType() == TokenType.SYMBOL && (tokenizer.symbol() == '(' || tokenizer.symbol() == '.'))
            {
                tokenizer.pointerBack();
                tokenizer.pointerBack();
                compileSubroutineCall();
            }
            else
            {
                tokenizer.pointerBack();
                vmWriter.writePush(getSegment(st.kindOf(tempId)), st.indexOf(tempId));
            }
        }
        else
        {
            if(tokenizer.tokenType() == TokenType.INT_CONST)
                vmWriter.writePush(VMWriter.SEGMENT.CONST,tokenizer.intVal());
            else if(tokenizer.tokenType() == TokenType.STRING_CONST)
            {
                String str = tokenizer.stringVal();

                vmWriter.writePush(VMWriter.SEGMENT.CONST,str.length());
                vmWriter.writeCall("String.new",1);

                for (int i = 0; i < str.length(); i++){
                    vmWriter.writePush(VMWriter.SEGMENT.CONST,(int)str.charAt(i));
                    vmWriter.writeCall("String.appendChar",2);
                }
            }
            else if(tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyWord() == KeyWord.TRUE)
            {
                vmWriter.writePush(VMWriter.SEGMENT.CONST,0);
                vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);
            }
            else if(tokenizer.tokenType() == TokenType.KEYWORD && (tokenizer.keyWord() == KeyWord.FALSE || tokenizer.keyWord() == KeyWord.NULL))
                vmWriter.writePush(VMWriter.SEGMENT.CONST,0);
            else if(tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyWord() == KeyWord.THIS)
                vmWriter.writePush(VMWriter.SEGMENT.POINTER,0);
            else if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '(')
            {    
                compileExpression();
                requireSymbol(')');
            }
            else if(tokenizer.tokenType() == TokenType.SYMBOL && (tokenizer.symbol() == '-' || tokenizer.symbol() == '~'))
            {
                char s = tokenizer.symbol();
                compileTerm();

                if (s == '-')
                    vmWriter.writeArithmetic(VMWriter.COMMAND.NEG);
                else
                    vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);
            }
            else
                ReportError("integerConstant | stringConstant | keywordConstant | '(' expression ')' | unaryOp term");
        }
    }

    /**
     * subroutineCall : subroutineName '(' expressionList ')' | (className|varName)'.'subroutineName'('expressionList')'
     */
    private void compileSubroutineCall()
    {
        requireIdentifier("identifier");
        String name = tokenizer.identifier();
        int nArgs = 0;

        tokenizer.advance();
        if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '(')
        {
            vmWriter.writePush(VMWriter.SEGMENT.POINTER, 0);
            nArgs = compileExpressionList() + 1;
            requireSymbol(')');
            vmWriter.writeCall(currentClass + '.' + name, nArgs);
        }
        else if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '.')
        {
            String objName = name;
            requireIdentifier("subroutineName");
            name = tokenizer.identifier();
            String type = st.typeOf(objName);

            if (type.equals("int")||type.equals("boolean")||type.equals("char")||type.equals("void"))
                ReportError("no built-in type");
            else if (type.equals(""))
                name = objName + "." + name;
            else 
            {
                nArgs = 1;
                vmWriter.writePush(getSegment(st.kindOf(objName)), st.indexOf(objName));
                name = st.typeOf(objName) + "." + name;
            }

            requireSymbol('(');
            nArgs += compileExpressionList();
            requireSymbol(')');
            vmWriter.writeCall(name,nArgs);
        }
        else
            ReportError("'(' | '.'");
    }

    /**
     * expressionList : (expression (','expression)*)?
     */
    private int compileExpressionList()
    {
        int nArgs = 0;
        tokenizer.advance();
        if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ')')
            tokenizer.pointerBack();
        else
        {
            nArgs = 1;
            tokenizer.pointerBack();
            compileExpression();

            do{
                tokenizer.advance();
                if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ',')
                {
                    compileExpression();
                    nArgs++;
                }
                else
                {
                    tokenizer.pointerBack();
                    break;
                }
            }while(true);
        }
        return nArgs;
    }

    /**
     * type : 'int'|'char'|'boolean'|className 
     */
    private String compileType()
    {
        tokenizer.advance();
        if(tokenizer.tokenType() == TokenType.KEYWORD && (tokenizer.keyWord() == KeyWord.INT || tokenizer.keyWord() == KeyWord.CHAR || tokenizer.keyWord() == KeyWord.BOOLEAN))
            return tokenizer.getCurrentToken();
        else if(tokenizer.tokenType() == TokenType.IDENTIFIER)
            return tokenizer.identifier();
        else
            ReportError("int | char | boolean | className");
        return "";
    }

    private void requireSymbol(char symbol)
    {
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.SYMBOL || tokenizer.symbol() != symbol)
            ReportError("'"+symbol+"'");
    }

    private void requireIdentifier(String idType)
    {
        tokenizer.advance();
        if (tokenizer.tokenType() != TokenType.IDENTIFIER)
            ReportError(idType);
    }

    private void ReportError(String val)
    {
        throw new IllegalStateException("\nExpected token missing : " + val + "\nCurrent token : " + tokenizer.getCurrentToken());
    }
}