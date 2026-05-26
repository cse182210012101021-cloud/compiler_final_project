import java.util.*;

public class Lexer {
    private String input;
    private int pos = 0;

    public Lexer(String input) { this.input = input; }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        int length = input.length();

        while (pos < length) {
            char current = input.charAt(pos);

            if (Character.isWhitespace(current)) {
                pos++;
                continue;
            }

          
            if (Character.isDigit(current)) {
                StringBuilder number = new StringBuilder();
                while (pos < length && Character.isDigit(input.charAt(pos))) {
                    number.append(input.charAt(pos));
                    pos++;
                }
                tokens.add(new Token(TokenType.NUMBER, number.toString()));
                continue;
            }

    
            if (current == '"') {
                StringBuilder str = new StringBuilder();
                pos++; 
                while (pos < length && input.charAt(pos) != '"') {
                    str.append(input.charAt(pos));
                    pos++;
                }
                pos++; 
                tokens.add(new Token(TokenType.STRING_LITERAL, str.toString()));
                continue;
            }


            if (!Character.isWhitespace(current) && !Character.isDigit(current) && 
                "=+;-()/*;\"".indexOf(current) == -1) {
                StringBuilder word = new StringBuilder();
                while (pos < length && !Character.isWhitespace(input.charAt(pos)) && 
                      "=+;-()/*;\"".indexOf(input.charAt(pos)) == -1) {
                    word.append(input.charAt(pos));
                    pos++;
                }
                
                String result = word.toString();
                if (result.equals("সংখ্যা") || result.equals("লেখা")) {
                    tokens.add(new Token(TokenType.DATA_TYPE, result));
                } 
                 // ================= IF-ELSE KEYWORDS (🔥 NEW ADD) =================
                else if (result.equals("যদি")) {   // IF keyword
                    tokens.add(new Token(TokenType.IF, result));
                }
                else if (result.equals("না")) {    // ELSE keyword
                    tokens.add(new Token(TokenType.ELSE, result));
                }

                else if (result.equals("এবং")) {
    tokens.add(new Token(TokenType.AND, "&&"));
}
                
                
                else {
                    tokens.add(new Token(TokenType.IDENTIFIER, result));
                }
                continue;
            }

            

            if (current == '=') tokens.add(new Token(TokenType.ASSIGN, "="));
            else if (current == '+') tokens.add(new Token(TokenType.PLUS, "+"));
            else if (current == '-') tokens.add(new Token(TokenType.MINUS, "-"));
            else if (current == '*') tokens.add(new Token(TokenType.MULTIPLY, "*"));
            else if (current == '/') tokens.add(new Token(TokenType.DIVIDE, "/"));
            else if (current == ';') tokens.add(new Token(TokenType.SEMICOLON, ";"));



             // ================= COMPARISON OPERATORS (🔥 NEW ADD) =================
            else if (current == '>') tokens.add(new Token(TokenType.GREATER, ">"));
            else if (current == '<') tokens.add(new Token(TokenType.LESS, "<"));

            // ================= BRACKETS (🔥 NEW ADD FOR IF CONDITION) =================
            else if (current == '(') tokens.add(new Token(TokenType.LPAREN, "("));
            else if (current == ')') tokens.add(new Token(TokenType.RPAREN, ")"));
            else {
    System.out.println("Warning: Unknown character -> " + current);
}

            pos++;
        }
        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }
}