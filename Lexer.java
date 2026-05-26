import java.util.*;

public class Lexer {
    private String input;
    private int pos = 0;

    // Bangla digits → ASCII digits mapping
    private static final Map<Character, Character> BANGLA_DIGITS = new HashMap<>();
    static {
        BANGLA_DIGITS.put('০', '0'); BANGLA_DIGITS.put('১', '1');
        BANGLA_DIGITS.put('২', '2'); BANGLA_DIGITS.put('৩', '3');
        BANGLA_DIGITS.put('৪', '4'); BANGLA_DIGITS.put('৫', '5');
        BANGLA_DIGITS.put('৬', '6'); BANGLA_DIGITS.put('৭', '7');
        BANGLA_DIGITS.put('৮', '8'); BANGLA_DIGITS.put('৯', '9');
    }

    private static final String DELIMITERS = "=+;-()/*\"<> \t\n\r";

    public Lexer(String input) { this.input = input; }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        int length = input.length();

        while (pos < length) {
            char current = input.charAt(pos);

            // Whitespace
            if (Character.isWhitespace(current)) { pos++; continue; }

            // ASCII digit
            if (Character.isDigit(current)) {
                tokens.add(readNumber());
                continue;
            }

            // Bangla digit
            if (BANGLA_DIGITS.containsKey(current)) {
                tokens.add(readBanglaNumber());
                continue;
            }

            // String literal
            if (current == '"') {
                StringBuilder str = new StringBuilder();
                pos++;
                while (pos < length && input.charAt(pos) != '"') {
                    str.append(input.charAt(pos++));
                }
                pos++;
                tokens.add(new Token(TokenType.STRING_LITERAL, str.toString()));
                continue;
            }

            // Two-char operators
            if (pos + 1 < length) {
                String two = "" + current + input.charAt(pos + 1);
                if (two.equals("==")) { tokens.add(new Token(TokenType.EQUALS, "==")); pos += 2; continue; }
                if (two.equals("!=")) { tokens.add(new Token(TokenType.NOT_EQUALS, "!=")); pos += 2; continue; }
                if (two.equals("<=")) { tokens.add(new Token(TokenType.LESS_EQ, "<=")); pos += 2; continue; }
                if (two.equals(">=")) { tokens.add(new Token(TokenType.GREATER_EQ, ">=")); pos += 2; continue; }
            }

            // Single-char operators
            switch (current) {
                case '=': tokens.add(new Token(TokenType.ASSIGN,    "=")); pos++; continue;
                case '+': tokens.add(new Token(TokenType.PLUS,      "+")); pos++; continue;
                case '-': tokens.add(new Token(TokenType.MINUS,     "-")); pos++; continue;
                case '*': tokens.add(new Token(TokenType.MULTIPLY,  "*")); pos++; continue;
                case '/': tokens.add(new Token(TokenType.DIVIDE,    "/")); pos++; continue;
                case ';': tokens.add(new Token(TokenType.SEMICOLON, ";")); pos++; continue;
                case '(': tokens.add(new Token(TokenType.LPAREN,    "(")); pos++; continue;
                case ')': tokens.add(new Token(TokenType.RPAREN,    ")")); pos++; continue;
                case '>': tokens.add(new Token(TokenType.GREATER,   ">")); pos++; continue;
                case '<': tokens.add(new Token(TokenType.LESS,      "<")); pos++; continue;
            }

            // Word / keyword / identifier (handles Unicode/Bangla)
            if (!isDelimiter(current)) {
                StringBuilder word = new StringBuilder();
                while (pos < length && !isDelimiter(input.charAt(pos))) {
                    word.append(input.charAt(pos++));
                }
                String result = word.toString();
                switch (result) {
                    case "সংখ্যা": tokens.add(new Token(TokenType.DATA_TYPE, result)); break;
                    case "লেখা":   tokens.add(new Token(TokenType.DATA_TYPE, result)); break;
                    case "যদি":    tokens.add(new Token(TokenType.IF,        result)); break;
                    case "না":     tokens.add(new Token(TokenType.ELSE,      result)); break;
                    case "এবং":   tokens.add(new Token(TokenType.AND,       "&&"));   break;
                    case "অথবা":  tokens.add(new Token(TokenType.OR,        "||"));   break;
                    default:       tokens.add(new Token(TokenType.IDENTIFIER, result)); break;
                }
                continue;
            }

            System.out.println("Warning: Unknown character -> " + current);
            pos++;
        }

        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }

    private Token readNumber() {
        StringBuilder number = new StringBuilder();
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            number.append(input.charAt(pos++));
        }
        return new Token(TokenType.NUMBER, number.toString());
    }

    private Token readBanglaNumber() {
        StringBuilder number = new StringBuilder();
        while (pos < input.length() && BANGLA_DIGITS.containsKey(input.charAt(pos))) {
            number.append(BANGLA_DIGITS.get(input.charAt(pos++)));
        }
        return new Token(TokenType.NUMBER, number.toString());
    }

    private boolean isDelimiter(char c) {
        return DELIMITERS.indexOf(c) != -1;
    }
}
