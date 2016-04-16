import java.io.*;
import java.util.*;

public class CompilationEngine 
{
    private PrintWriter printWriter, tokenPrintWriter;
    private JackTokenizer tokenizer;

    public CompilationEngine(File fileIn, File fileOut, File tokenFileOut) 
    {
        try {
            tokenizer = new JackTokenizer(fileIn);
            printWriter = new PrintWriter(fileOut);
            tokenPrintWriter = new PrintWriter(tokenFileOut);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /** 
     * class : 'class' className '{' classVarDec* subroutineDec* '}' 
     */
    public void compileClass()
    {
        tokenizer.advance();
        if(tokenizer.tokenType() != TokenType.KEYWORD || tokenizer.keyWord() != KeyWord.CLASS)
            ReportError("class");
        else
        {
            tokenPrintWriter.print("<tokens>\n");
            printWriter.print("<class>\n");
            PrintOut("keyword","class");
        }

        requireIdentifier("className");
        requireSymbol('{');
        compileClassVarDec();
        compileSubroutine();
        requireSymbol('}');

        if (tokenizer.hasMoreTokens())
            throw new IllegalStateException("Unexpected tokens");

        tokenPrintWriter.print("</tokens>\n");
        printWriter.print("</class>\n");

        printWriter.close();
        tokenPrintWriter.close();
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

        printWriter.print("<classVarDec>\n");

        if(tokenizer.keyWord() != KeyWord.STATIC && tokenizer.keyWord() != KeyWord.FIELD)
            ReportError("static | field");

        PrintOut("keyword", tokenizer.getCurrentToken());

        compileType();

        do{
            requireIdentifier("varName");

            tokenizer.advance();
            if(tokenizer.tokenType() != TokenType.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ';'))
                ReportError("',' or ';'");

            if(tokenizer.symbol() == ',')
                PrintOut("symbol",",");
            else
            {
                PrintOut("symbol",";");
                break;
            }
        }while(true);

        printWriter.print("</classVarDec>\n");
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

        printWriter.print("<subroutineDec>\n");
        PrintOut("keyword", tokenizer.getCurrentToken());

        tokenizer.advance();
        if(tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyWord() == KeyWord.VOID)
            PrintOut("keyword", "void");
        else
        {
            tokenizer.pointerBack();
            compileType();
        }

        requireIdentifier("subroutineName");
        requireSymbol('(');
        printWriter.print("<parameterList>\n");
        compileParameterList();
        printWriter.print("</parameterList>\n");
        requireSymbol(')');
        compileSubroutineBody();

        printWriter.print("</subroutineDec>\n");
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

        tokenizer.pointerBack();
        do{
            compileType();

            requireIdentifier("varName");

            tokenizer.advance();
            if(tokenizer.tokenType() != TokenType.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ')'))
                ReportError("',' or ')'");

            if(tokenizer.symbol() == ',')
                PrintOut("symbol", ",");
            else
            {
                tokenizer.pointerBack();
                break;
            }
        }while(true);
    }

    /**
     * subroutineBody : '{' varDec* statements '}' 
     */
    private void compileSubroutineBody()
    {
        printWriter.print("<subroutineBody>\n");
        requireSymbol('{');
        compileVarDec();
        printWriter.print("<statements>\n");
        compileStatements();
        printWriter.print("</statements>\n");
        requireSymbol('}');
        printWriter.print("</subroutineBody>\n");
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

        printWriter.print("<varDec>\n");
        PrintOut("keyword", "var");

        compileType();

        do{
            requireIdentifier("varName");

            tokenizer.advance();
            if(tokenizer.tokenType() != TokenType.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ';'))
                ReportError("',' or ';'");

            if(tokenizer.symbol() == ',')
                PrintOut("symbol",",");
            else
            {
                PrintOut("symbol",";");
                break;
            }
        }while(true);

        printWriter.print("</varDec>\n");
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
        printWriter.print("<letStatement>\n");
        PrintOut("keyword", "let");

        requireIdentifier("varName");

        tokenizer.advance();
        if(tokenizer.tokenType() != TokenType.SYMBOL || (tokenizer.symbol() != '[' && tokenizer.symbol() != '='))
            ReportError("'[' | '='");

        boolean expExist = false;

        if(tokenizer.symbol() == '[')
        {
            expExist = true;
            PrintOut("symbol", "[");
            compileExpression();
            requireSymbol(']');
        }

        if (expExist)
            tokenizer.advance();

        PrintOut("symbol", "=");
        compileExpression();
        requireSymbol(';');

        printWriter.print("</letStatement>\n");
    }

    /** 
     * ifStatement : 'if' '(' expression ')' '{' statements '}' ('else' '{' statements '}')?
     */
    private void compileIf()
    {
        printWriter.print("<ifStatement>\n");
        PrintOut("keyword","if");

        requireSymbol('(');
        compileExpression();
        requireSymbol(')');
        requireSymbol('{');
        printWriter.print("<statements>\n");
        compileStatements();
        printWriter.print("</statements>\n");
        requireSymbol('}');

        tokenizer.advance();
        if(tokenizer.tokenType() == TokenType.KEYWORD && tokenizer.keyWord() == KeyWord.ELSE)
        {
            PrintOut("keyword","else");
            requireSymbol('{');
            printWriter.print("<statements>\n");
            compileStatements();
            printWriter.print("<statements>\n");
            requireSymbol('}');
        }
        else
            tokenizer.pointerBack();

        printWriter.print("</ifStatement>\n");   
    }

    /**
     * whileStatement : 'while' '(' expression ')' '{' statements '}'
     */
    private void compileWhile()
    {
        printWriter.print("<whileStatement>\n");
        PrintOut("keyword","while");
        requireSymbol('(');
        compileExpression();
        requireSymbol(')');
        requireSymbol('{');
        printWriter.print("<statements>\n");
        compileStatements();
        printWriter.print("</statements>\n");
        requireSymbol('}');
        printWriter.print("</whileStatement>\n");
    }

    /**
     * doStatement : 'do' subroutineCall ';'
     */
    private void compileDo()
    {
        printWriter.print("<doStatement>\n");
        PrintOut("keyword","do");
        compileSubroutineCall();
        requireSymbol(';');
        printWriter.print("</doStatement>\n");
    }

    /**
     * ReturnStatement : 'return' expression? ';'
     */
    private void compileReturn()
    {
        printWriter.print("<returnStatement>\n");
        PrintOut("keyword","return");

        tokenizer.advance();
        if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ';')
        {
            PrintOut("symbol",";");
            printWriter.print("</returnStatement>\n");
            return;
        }

        tokenizer.pointerBack();
        compileExpression();
        requireSymbol(';');
        printWriter.print("</returnStatement>\n");
    }

    /**
     * expression : term (op term)*
     */
    private void compileExpression()
    {
        printWriter.print("<expression>\n");
        compileTerm();

        do{
            tokenizer.advance();
            if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.isOp())
            {
                if(tokenizer.symbol() == '>')
                    PrintOut("symbol", "&gt;");
                else if(tokenizer.symbol() == '<')
                    PrintOut("symbol", "&lt;");
                else if(tokenizer.symbol() == '&')
                    PrintOut("symbol", "&amp;");
                else if(tokenizer.symbol() == '"')
                    PrintOut("symbol", "&quot;");
                else
                    PrintOut("symbol",tokenizer.symbol()+"");

                compileTerm();
            }
            else
            {
                tokenizer.pointerBack();
                break;
            }
        }while(true);

        printWriter.print("</expression>\n");
    }

    /**
     * term : integerConstant|stringConstant|keywordConstant|varName|varName'['expression']'|subroutineCall|'('expression')'|unaryOp term
     */
    private void compileTerm()
    {
        printWriter.print("<term>\n");

        tokenizer.advance();
        if(tokenizer.tokenType() == TokenType.IDENTIFIER)
        {
            String tempId = tokenizer.identifier();

            tokenizer.advance();
            if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '[')
            {
                PrintOut("identifier", tempId);
                PrintOut("symbol", "[");
                compileExpression();
                requireSymbol(']');
            }
            else if(tokenizer.tokenType() == TokenType.SYMBOL && (tokenizer.symbol() == '(' || tokenizer.symbol() == '.'))
            {
                tokenizer.pointerBack();
                tokenizer.pointerBack();
                compileSubroutineCall();
            }
            else
            {
                PrintOut("identifier", tempId);
                tokenizer.pointerBack();
            }
        }
        else
        {
            if(tokenizer.tokenType() == TokenType.INT_CONST)
                PrintOut("integerConstant", tokenizer.intVal().toString());
            else if(tokenizer.tokenType() == TokenType.STRING_CONST)
                PrintOut("stringConstant", tokenizer.stringVal());
            else if(tokenizer.tokenType() == TokenType.KEYWORD && (tokenizer.keyWord() == KeyWord.TRUE || tokenizer.keyWord() == KeyWord.FALSE || tokenizer.keyWord() == KeyWord.NULL || tokenizer.keyWord() == KeyWord.THIS))
                PrintOut("keyword", tokenizer.getCurrentToken());
            else if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '(')
            {    
                PrintOut("symbol", "(");
                compileExpression();
                requireSymbol(')');
            }
            else if(tokenizer.tokenType() == TokenType.SYMBOL && (tokenizer.symbol() == '-' || tokenizer.symbol() == '~'))
            {    
                PrintOut("symbol", tokenizer.symbol()+"");
                compileTerm();
            }
            else
                ReportError("integerConstant | stringConstant | keywordConstant | '(' expression ')' | unaryOp term");
        }

        printWriter.print("</term>\n");
    }

    /**
     * subroutineCall : subroutineName '(' expressionList ')' | (className|varName)'.'subroutineName'('expressionList')'
     */
    private void compileSubroutineCall()
    {
        requireIdentifier("identifier");

        tokenizer.advance();
        if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '(')
        {
            PrintOut("symbol","(");
            printWriter.print("<expressionList>\n");
            compileExpressionList();
            printWriter.print("</expressionList>\n");
            requireSymbol(')');
        }
        else if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == '.')
        {
            PrintOut("symbol",".");
            requireIdentifier("subroutineName");
            requireSymbol('(');
            printWriter.print("<expressionList>\n");
            compileExpressionList();
            printWriter.print("</expressionList>\n");
            requireSymbol(')');
        }
        else
            ReportError("'(' | '.'");
    }

    /**
     * expressionList : (expression (','expression)*)?
     */
    private void compileExpressionList()
    {
        tokenizer.advance();
        if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ')')
            tokenizer.pointerBack();
        else
        {
            tokenizer.pointerBack();
            compileExpression();

            do{
                tokenizer.advance();
                if(tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == ',')
                {
                    PrintOut("symbol",",");
                    compileExpression();
                }
                else
                {
                    tokenizer.pointerBack();
                    break;
                }
            }while(true);
        }
    }

    /**
     * type : 'int'|'char'|'boolean'|className 
     */
    private void compileType()
    {
        tokenizer.advance();
        if(tokenizer.tokenType() == TokenType.KEYWORD && (tokenizer.keyWord() == KeyWord.INT || tokenizer.keyWord() == KeyWord.CHAR || tokenizer.keyWord() == KeyWord.BOOLEAN))
            PrintOut("keyword", tokenizer.getCurrentToken());
        else if(tokenizer.tokenType() == TokenType.IDENTIFIER)
            PrintOut("identifier", tokenizer.identifier());
        else
            ReportError("int | char | boolean | className");
    }

    private void requireSymbol(char symbol)
    {
        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.SYMBOL && tokenizer.symbol() == symbol)
            PrintOut("symbol",symbol+"");
        else
            ReportError("'"+symbol+"'");
    }

    private void requireIdentifier(String idType)
    {
        tokenizer.advance();
        if (tokenizer.tokenType() == TokenType.IDENTIFIER)
            PrintOut("identifier", tokenizer.identifier());
        else
            ReportError(idType);
    }

    private void PrintOut(String tag, String value)
    {
        printWriter.print("<"+tag+"> "+value+" </"+tag+">\n");
        tokenPrintWriter.print("<"+tag+"> "+value+" </"+tag+">\n");
    }

    private void ReportError(String val)
    {
        throw new IllegalStateException("\nExpected token missing : " + val + "\nCurrent token:" + tokenizer.getCurrentToken());
    }
}