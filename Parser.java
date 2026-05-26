import java.util.*;

public class Parser {

    private List<Token> tokens;
    private int pos = 0;
    private SymbolTable symbolTable;
    private CodeGenerator codeGenerator;
    private boolean hasError = false;

    private static final Set<TokenType> COMP_OPS = new HashSet<>(Arrays.asList(
        TokenType.GREATER, TokenType.LESS,
        TokenType.EQUALS, TokenType.NOT_EQUALS,
        TokenType.LESS_EQ, TokenType.GREATER_EQ
    ));

    public Parser(List<Token> tokens, SymbolTable symbolTable, CodeGenerator codeGenerator) {
        this.tokens        = tokens;
        this.symbolTable   = symbolTable;
        this.codeGenerator = codeGenerator;
    }

    private Token currentToken() {
        if (pos >= tokens.size()) return tokens.get(tokens.size() - 1);
        return tokens.get(pos);
    }
    private void consume(TokenType type) {
        if (currentToken().getType() == type) pos++;
        else throw new RuntimeException(
            "Syntax Error: Expected " + type + " but found '" + currentToken().getValue() + "'");
    }
    private boolean check(TokenType type) { return currentToken().getType() == type; }

    private void syncToSemicolon() {
        while (!check(TokenType.SEMICOLON) && !check(TokenType.EOF)) pos++;
        if (check(TokenType.SEMICOLON)) pos++;
        hasError = true;
    }

    public CodeGenerator getCodeGenerator() { return codeGenerator; }
    public boolean hadError() { return hasError; }

    // ── Main parse loop ───────────────────────────────────────────────────────

    public void parse() {
        while (!check(TokenType.EOF)) {
            try { parseStatement(); }
            catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
                syncToSemicolon();
            }
        }
    }

    private void parseStatement() {
        if (check(TokenType.IF))         { parseIfStatement(); return; }
        if (check(TokenType.DATA_TYPE))  { parseVarDecl();     return; }
        if (check(TokenType.IDENTIFIER)) { parseAssignment();  return; }
        throw new RuntimeException("Syntax Error: Unexpected token '" + currentToken().getValue() + "'");
    }

    // ── Variable declaration ──────────────────────────────────────────────────

    private void parseVarDecl() {
        String type    = currentToken().getValue(); consume(TokenType.DATA_TYPE);
        String varName = currentToken().getValue(); consume(TokenType.IDENTIFIER);

        if (symbolTable.exists(varName))
            throw new RuntimeException("Semantic Error: Variable '" + varName + "' already declared.");
        symbolTable.add(varName, type);

        if (check(TokenType.ASSIGN)) {
            consume(TokenType.ASSIGN);
            if (type.equals("সংখ্যা")) handleNumericExpression(varName);
            else                       handleStringExpression(varName);
        }
        consume(TokenType.SEMICOLON);
    }

    // ── Assignment ────────────────────────────────────────────────────────────

    private void parseAssignment() {
        String varName = currentToken().getValue();
        if (!symbolTable.exists(varName))
            throw new RuntimeException("Semantic Error: Variable '" + varName + "' not declared.");
        consume(TokenType.IDENTIFIER);
        consume(TokenType.ASSIGN);
        String type = symbolTable.getType(varName);
        if (type.equals("সংখ্যা")) {
            int result = parseExpression();
            symbolTable.setValue(varName, result);
            codeGenerator.generateAssignment(varName, String.valueOf(result));
            System.out.println("Success: '" + varName + "' updated value: " + result);
        } else {
            if (!check(TokenType.STRING_LITERAL))
                throw new RuntimeException("Type Error: String expected for '" + varName + "'.");
            String value = currentToken().getValue(); consume(TokenType.STRING_LITERAL);
            symbolTable.setValue(varName, value);
            codeGenerator.generateAssignment(varName, "\"" + value + "\"");
            System.out.println("Success: '" + varName + "' updated string: \"" + value + "\"");
        }
        consume(TokenType.SEMICOLON);
    }

    // ── Numeric / String handlers ─────────────────────────────────────────────

    private void handleNumericExpression(String varName) {
        if (check(TokenType.STRING_LITERAL))
            throw new RuntimeException("Type Error: 'সংখ্যা' টাইপ ভেরিয়েবলে টেক্সট রাখা সম্ভব নয়।");
        int result = parseExpression();
        symbolTable.setValue(varName, result);
        codeGenerator.generateNumberDeclaration(varName, String.valueOf(result));
        System.out.println("Success: '" + varName + "' calculated result: " + result);
    }

    private void handleStringExpression(String varName) {
        if (check(TokenType.NUMBER))
            throw new RuntimeException("Type Error: 'লেখা' টাইপ ভেরিয়েবলে সরাসরি নম্বর রাখা সম্ভব নয়।");
        if (!check(TokenType.STRING_LITERAL))
            throw new RuntimeException("Type Error: 'লেখা' টাইপে উদ্ধৃতি চিহ্নসহ টেক্সট দিতে হবে।");
        String value = currentToken().getValue(); consume(TokenType.STRING_LITERAL);
        symbolTable.setValue(varName, value);
        codeGenerator.generateStringDeclaration(varName, value);
        System.out.println("Success: '" + varName + "' assigned string: \"" + value + "\"");
    }

    // ── Arithmetic ────────────────────────────────────────────────────────────

    private int parseExpression() {
        int result = parseTerm();
        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            TokenType op = currentToken().getType(); consume(op);
            int next = parseTerm();
            result = (op == TokenType.PLUS) ? result + next : result - next;
        }
        return result;
    }
    private int parseTerm() {
        int result = parseFactor();
        while (check(TokenType.MULTIPLY) || check(TokenType.DIVIDE)) {
            TokenType op = currentToken().getType(); consume(op);
            int next = parseFactor();
            if (op == TokenType.MULTIPLY) result *= next;
            else {
                if (next == 0) throw new RuntimeException("Runtime Error: Division by zero!");
                result /= next;
            }
        }
        return result;
    }
    private int parseFactor() {
        if (check(TokenType.NUMBER)) {
            int val = Integer.parseInt(currentToken().getValue()); consume(TokenType.NUMBER);
            return val;
        }
        if (check(TokenType.IDENTIFIER)) {
            String name = currentToken().getValue(); consume(TokenType.IDENTIFIER);
            if (!symbolTable.exists(name)) throw new RuntimeException("Undefined variable: " + name);
            return symbolTable.getIntValue(name);
        }
        if (check(TokenType.LPAREN)) {
            consume(TokenType.LPAREN); int val = parseExpression(); consume(TokenType.RPAREN);
            return val;
        }
        throw new RuntimeException("Type Error: সংখ্যা প্রত্যাশিত, পাওয়া গেছে '" + currentToken().getValue() + "'");
    }

    // ── Condition ─────────────────────────────────────────────────────────────

    private boolean evaluateCondition() {
        boolean result = evaluateSingleCondition();
        while (check(TokenType.AND) || check(TokenType.OR)) {
            TokenType logic = currentToken().getType(); consume(logic);
            boolean next = evaluateSingleCondition();
            result = (logic == TokenType.AND) ? (result && next) : (result || next);
        }
        return result;
    }
    private boolean evaluateSingleCondition() {
        int left = parseExpression();
        if (!COMP_OPS.contains(currentToken().getType()))
            throw new RuntimeException("Invalid condition operator: '" + currentToken().getValue() + "'");
        TokenType op = currentToken().getType(); consume(op);
        int right = parseExpression();
        switch (op) {
            case GREATER:    return left > right;
            case LESS:       return left < right;
            case EQUALS:     return left == right;
            case NOT_EQUALS: return left != right;
            case LESS_EQ:    return left <= right;
            case GREATER_EQ: return left >= right;
            default: throw new RuntimeException("Unknown operator");
        }
    }

    // ── If-Else ───────────────────────────────────────────────────────────────
    // Grammar: যদি ( condition ) statement [ না statement ]
    // Approach: save position, parse condition, then handle both branches carefully.

    private void parseIfStatement() {
        consume(TokenType.IF);
        consume(TokenType.LPAREN);
        boolean cond = evaluateCondition();
        consume(TokenType.RPAREN);

        // Save position before then-branch
        int posBefore = pos;

        // Skip then-branch to find 'না' (ELSE)
        skipOneStatement();
        int posAfterThen = pos;
        boolean hasElse  = check(TokenType.ELSE);
        int posAfterElse = posAfterThen;
        if (hasElse) {
            pos++; // consume ELSE
            skipOneStatement();
            posAfterElse = pos;
        }

        if (cond) {
            // Execute then-branch
            pos = posBefore;
            codeGenerator.openIf("true");
            try { parseStatement(); } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage()); syncToSemicolon();
            }
            if (hasElse) {
                codeGenerator.transitionToElse();
                codeGenerator.closeBlock();
            } else {
                codeGenerator.closeBlock();
            }
            pos = posAfterElse;
        } else {
            // Skip then-branch, execute else-branch if present
            codeGenerator.openIf("false");
            if (hasElse) {
                codeGenerator.transitionToElse();
                pos = posAfterThen + 1; // skip ELSE token
                try { parseStatement(); } catch (RuntimeException e) {
                    System.out.println("Error: " + e.getMessage()); syncToSemicolon();
                }
                codeGenerator.closeBlock();
            } else {
                codeGenerator.closeBlock();
            }
            pos = posAfterElse;
        }
    }

    private void skipOneStatement() {
        // Skip tokens until we consume one semicolon (one statement)
        while (!check(TokenType.SEMICOLON) && !check(TokenType.ELSE) && !check(TokenType.EOF)) pos++;
        if (check(TokenType.SEMICOLON)) pos++;
    }
}
