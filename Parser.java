
import java.util.List;

public class Parser {

    private List<Token> tokens;
    private int pos = 0;
    private SymbolTable symbolTable;

private CodeGenerator codeGenerator;
    private boolean hasError = false;

  public Parser(
        List<Token> tokens,
        SymbolTable symbolTable,
        CodeGenerator codeGenerator
) {

    this.tokens = tokens;
    this.symbolTable = symbolTable;
    this.codeGenerator = codeGenerator;
}

    private Token currentToken() {

        if (pos >= tokens.size())
            return tokens.get(tokens.size() - 1);

        return tokens.get(pos);
    }

    private void consume(TokenType type) {

        if (currentToken().getType() == type) {
            pos++;
        } else {

            throw new RuntimeException(
                "Syntax Error: Expected "
                + type +
                " but found "
                + currentToken().getType()
            );
        }
    }

    private void syncToSemicolon() {

        while (currentToken().getType() != TokenType.SEMICOLON &&
               currentToken().getType() != TokenType.EOF) {

            pos++;
        }

        if (currentToken().getType() == TokenType.SEMICOLON)
            pos++;
    }

    public CodeGenerator getCodeGenerator() {
    return codeGenerator;
}

    public void parse() {
        

        while (currentToken().getType() != TokenType.EOF) {

            try {

                parseStatement();

            } catch (RuntimeException e) {

                System.out.println("Error: " + e.getMessage());

                hasError = true;

                syncToSemicolon();
            }
        }

        if (!hasError) {

            codeGenerator.printCode();

        } else {

            System.out.println(
                "\n--- Code Generation Skipped Errors ---"
            );
        }
    }

    private void parseStatement() {


        if (currentToken().getType() == TokenType.IF) {

            parseIfStatement();

            return;
        }

        if (currentToken().getType() == TokenType.IDENTIFIER) {

            String varName = currentToken().getValue();

            if (!symbolTable.exists(varName)) {

                throw new RuntimeException(
                    "Semantic Error: Variable '"
                    + varName +
                    "' not declared."
                );
            }

            consume(TokenType.IDENTIFIER);

            consume(TokenType.ASSIGN);

            String type = symbolTable.getType(varName);

        
            if (type.equals("সংখ্যা")) {

                int result = parseExpression();

                symbolTable.setValue(varName, result);

                codeGenerator.generateAssignment(
                    varName,
                    String.valueOf(result)
                );

                System.out.println(
                    "Success: '" + varName +
                    "' updated value: " + result
                );
            }


            else if (type.equals("লেখা")) {

                if (currentToken().getType()
                        != TokenType.STRING_LITERAL) {

                    throw new RuntimeException(
                        "Type Error: String expected."
                    );
                }

                String value = currentToken().getValue();

                consume(TokenType.STRING_LITERAL);

                symbolTable.setValue(varName, value);

                codeGenerator.generateStringDeclaration(
                    varName,
                    value
                );

                System.out.println(
                    "Success: '" + varName +
                    "' updated string: \"" + value + "\""
                );
            }

            consume(TokenType.SEMICOLON);

            return;
        }

        if (currentToken().getType() == TokenType.DATA_TYPE) {

            String type = currentToken().getValue();

            consume(TokenType.DATA_TYPE);

            String varName = currentToken().getValue();

            consume(TokenType.IDENTIFIER);

            if (symbolTable.exists(varName)) {

                hasError = true;

                throw new RuntimeException(
                    "Semantic Error: Variable '"
                    + varName +
                    "' already declared."
                );
            }

            symbolTable.add(varName, type);

            if (currentToken().getType() == TokenType.ASSIGN) {

                consume(TokenType.ASSIGN);

                if (type.equals("সংখ্যা")) {

                    handleNumericExpression(varName);

                } else if (type.equals("লেখা")) {

                    handleStringExpression(varName);
                }
            }

            consume(TokenType.SEMICOLON);

            return;
        }

        throw new RuntimeException(
            "Syntax Error: Unexpected token "
            + currentToken().getValue()
        );
    }

    private void parseIfStatement() {

    consume(TokenType.IF);
    consume(TokenType.LPAREN);

    boolean conditionResult = evaluateCondition(); 
    consume(TokenType.RPAREN);

    if (conditionResult) {

        while (currentToken().getType() != TokenType.ELSE &&
               currentToken().getType() != TokenType.EOF) {

            parseStatement();
        }

        if (currentToken().getType() == TokenType.ELSE) {
            consume(TokenType.ELSE);

            while (currentToken().getType() != TokenType.EOF &&
                   currentToken().getType() != TokenType.SEMICOLON) {

                parseStatement();
            }
        }

    } else {

        while (currentToken().getType() != TokenType.ELSE &&
               currentToken().getType() != TokenType.EOF) {

            pos++;
        }

        if (currentToken().getType() == TokenType.ELSE) {

            consume(TokenType.ELSE);

            while (currentToken().getType() != TokenType.EOF &&
                   currentToken().getType() != TokenType.SEMICOLON) {

                parseStatement();
            }
        }
    }
}


    private String expressionToString() {

    StringBuilder expr = new StringBuilder();

    while (currentToken().getType() != TokenType.SEMICOLON &&
           currentToken().getType() != TokenType.RPAREN &&
           currentToken().getType() != TokenType.EOF) {

        expr.append(currentToken().getValue()).append(" ");

        pos++;
    }

    return expr.toString().trim();
}

    private void handleNumericExpression(String varName) {

        if (currentToken().getType()
                == TokenType.STRING_LITERAL) {

            hasError = true;

            throw new RuntimeException(
                "Type Error: 'সংখ্যা' টাইপ ভেরিয়েবলে টেক্সট রাখা সম্ভব নয়।"
            );
        }

        int finalResult = parseExpression();

        symbolTable.setValue(varName, finalResult);

        codeGenerator.generateNumberDeclaration(
            varName,
            String.valueOf(finalResult)
        );

        System.out.println(
            "Success: '" + varName +
            "' calculated result: " + finalResult
        );
    }

    private int parseExpression() {

        int result = parseTerm();

        while (currentToken().getType() == TokenType.PLUS ||
               currentToken().getType() == TokenType.MINUS) {

            TokenType op = currentToken().getType();

            consume(op);

            int nextVal = parseTerm();

            if (op == TokenType.PLUS)
                result += nextVal;
            else
                result -= nextVal;
        }

        return result;
    }

    private int parseTerm() {

        int result = parseFactor();

        while (currentToken().getType()
                    == TokenType.MULTIPLY ||

               currentToken().getType()
                    == TokenType.DIVIDE) {

            TokenType op = currentToken().getType();

            consume(op);

            int nextVal = parseFactor();

            if (op == TokenType.MULTIPLY) {

                result *= nextVal;

            } else {

                if (nextVal == 0)

                    throw new RuntimeException(
                        "Runtime Error: Division by zero!"
                    );

                result /= nextVal;
            }
        }

        return result;
    }

    
    private boolean evaluateCondition() {

    int left = parseExpression();

    TokenType op = currentToken().getType();

    if (op == TokenType.GREATER) {
        consume(TokenType.GREATER);
        int right = parseExpression();
        return left > right;
    }

    if (op == TokenType.LESS) {
        consume(TokenType.LESS);
        int right = parseExpression();
        return left < right;
    }

    throw new RuntimeException("Invalid condition operator");
}
   
    private boolean evaluateSingleCondition() {

        int left = parseExpression();

        TokenType op = currentToken().getType();

        if (op == TokenType.GREATER) {

            consume(TokenType.GREATER);

            int right = parseExpression();

            return left > right;
        }

        if (op == TokenType.LESS) {

            consume(TokenType.LESS);

            int right = parseExpression();

            return left < right;
        }

        throw new RuntimeException(
            "Invalid condition operator"
        );
    }

    private int parseFactor() {

        if (currentToken().getType()
                == TokenType.NUMBER) {

            int val = Integer.parseInt(
                currentToken().getValue()
            );

            consume(TokenType.NUMBER);

            return val;
        }

        if (currentToken().getType()
                == TokenType.IDENTIFIER) {

            String name = currentToken().getValue();

            consume(TokenType.IDENTIFIER);

            if (!symbolTable.exists(name)) {

                throw new RuntimeException(
                    "Undefined variable: " + name
                );
            }

            return symbolTable.getIntValue(name);
        }


        if (currentToken().getType()
                == TokenType.LPAREN) {

            consume(TokenType.LPAREN);

            int value = parseExpression();

            consume(TokenType.RPAREN);

            return value;
        }

        throw new RuntimeException(
            "Type Error: সংখ্যা প্রত্যাশিত।"
        );
    }

    private void handleStringExpression(String varName) {

        if (currentToken().getType()
                == TokenType.NUMBER) {

            hasError = true;

            throw new RuntimeException(
                "Type Error: 'লেখা' টাইপ ভেরিয়েবলে সরাসরি নম্বর রাখা সম্ভব নয়।"
            );
        }

        if (currentToken().getType()
                == TokenType.STRING_LITERAL) {

            String value = currentToken().getValue();

            consume(TokenType.STRING_LITERAL);


            symbolTable.setValue(varName, value);

            codeGenerator.generateStringDeclaration(
                varName,
                value
            );

            System.out.println(
                "Success: '" + varName +
                "' assigned string: \"" +
                value + "\""
            );

        } else {

            throw new RuntimeException(
                "Type Error: 'লেখা' টাইপে উদ্ধৃতি চিহ্নসহ (\" \") টেক্সট দিতে হবে।"
            );
        }
    }
}

