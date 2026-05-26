enum TokenType {

    DATA_TYPE,
    IDENTIFIER,
    ASSIGN,
    NUMBER,
    STRING_LITERAL,

    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,

    SEMICOLON,

    IF,
    ELSE,

    LPAREN,
    RPAREN,

    GREATER,
    LESS,

    AND,

    EOF
}

public class Token {

    private TokenType type;
    private String value;

    public Token(TokenType type, String value) {

        this.type = type;
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {

        return "Token("
                + type +
                ", '"
                + value +
                "')";
    }
}