
public enum TokenType {
	// Delimiters
	LPAREN, RPAREN, LBRACE, RBRACE, POINT, COMMA, SEMICOLON, EOF,
	
	// No category
	ASSIGN, TYPE,
	
	// Keywords
	WHILE, IF, ELSE, FOR,
	
	// Numbers and letters
	NUMBER, NAME, LITERAL,
	
	// Operators (Math)
	PLUS, MINUS, MULTIPLY, DIVIDE,
	
	// Operators (Comparison)
	GREATER, LESSER, GREATER_EQUAL, LESSER_EQUAL, EQUAL, DIFFERENT,
	
	// Operators (Binary)
	NOT, OR, AND
}
