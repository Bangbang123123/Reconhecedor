
public class Token {
	private TokenType type;
    private String value;
    private int line; // linha do codigo-fonte onde o token apareceu (usada nas tabelas)

    Token(TokenType type, String value, int line) {
        this.type = type;
        this.value = value;
        this.line = line;
    }

    // Getters
    public TokenType getType() {
		return type;
	}
	public String getValue() {
		return value;
	}
	public int getLine() {
		return line;
	}

	// Setters
	public void setType(TokenType type) {
		this.type = type;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
    public String toString() {
        return type + "[" + value + "]";
    }
}
