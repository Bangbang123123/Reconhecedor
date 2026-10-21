import java.util.Queue;

public class Parser {
    private final Queue<Token> tokens;

    // [NOVO] tabelas preenchidas durante a analise sintatica
    private final SymbolTable symbols = new SymbolTable();
    private final CodeTable codes = new CodeTable();

    public Parser(Queue<Token> tokens) {
        this.tokens = tokens;
    }

    // [NOVO] acesso as tabelas depois do parse()
    public SymbolTable getSymbolTable() {
    	return symbols;
    }
    public CodeTable getCodeTable() {
    	return codes;
    }

    private Token peek() {
        return tokens.element();
    }

    private Token consume(TokenType type) {
        Token token = tokens.poll();
        if (token.getType() == type) {
        	System.out.print(token.toString() +" ");
            return token;
        }
        System.out.println();
        throw new RuntimeException("Syntax Error: Expected " + type + " but found " + token.getType() + ". Line " + token.getLine());
    }

    public void parse() {
    	function();
        consume(TokenType.EOF);
    }

    // [NOVO] Bloco "{ programa }": devolve {linha do '{', linha do '}'} para a Tabela de Codigos
    private int[] block() {
    	int open = consume(TokenType.LBRACE).getLine();
    	program();
    	int close = consume(TokenType.RBRACE).getLine();
    	return new int[] {open, close};
    }

    // Function
    private void function() {
    	Token type = consume(TokenType.TYPE);
    	Token name = consume(TokenType.NAME);

    	// [NOVO] escopo 0 (global) guarda o nome da funcao; escopo 1 guarda parametros e corpo
    	symbols.enterScope();
    	SymbolTable.Entry functionSymbol = symbols.declare(name.getValue(), "Função(" + type.getValue() + ")", name.getLine());
    	CodeTable.Entry functionCode = codes.openFunction(name.getValue());
    	functionCode.start1 = type.getLine();
    	symbols.enterScope();

    	consume(TokenType.LPAREN);
    	if (peek().getType() == TokenType.TYPE) {
    		parameter();
    		while (peek().getType() == TokenType.COMMA) {
    			consume(TokenType.COMMA);
    			parameter();
    		}
    	}
    	consume(TokenType.RPAREN);
    	int[] body = block();

    	// [NOVO] fecha as tabelas da funcao (equivale ao "Principal" do Portugol)
    	functionCode.end1 = body[1];
    	functionCode.start2 = body[0];
    	functionCode.end2 = body[1];
    	functionSymbol.endLine = body[1];
    	symbols.exitScope();
    	symbols.exitScope();
    }

    // [NOVO] Parametro "tipo nome": ja entra na Tabela de Simbolos (escopo da funcao)
    private void parameter() {
    	Token type = consume(TokenType.TYPE);
    	Token name = consume(TokenType.NAME);
    	symbols.declare(name.getValue(), type.getValue(), name.getLine());
    }

    // Program
    private void program() {
    	command();
    	while (peek().getType() == TokenType.SEMICOLON) {
    		consume(TokenType.SEMICOLON);
    		command();
    	}
    }
    private void command() {
    	switch (peek().getType()) {
    	case TokenType.IF:
    		if_command();
    		break;
    	case TokenType.WHILE:
    		while_command();
    		break;
    	case TokenType.FOR:
    		for_command();
    		break;
    	case TokenType.TYPE:
    		declaration();
    		break;
    	case TokenType.NAME:
    		Token variable = compound_name();
    		use(variable); // [NOVO] registra o uso na Tabela de Simbolos
    		switch (peek().getType()) {
    		case TokenType.ASSIGN:
    			assignment();
    			break;
    		case TokenType.SEMICOLON:
    			break;
			default:
				throw new RuntimeException("\nSyntax Error: Unexpected token " + peek().getType() +", expected ASSIGN. Line " + peek().getLine());
    		}
    		break;
    	case TokenType.SEMICOLON, TokenType.RBRACE:
    		break;
    	default:
    		throw new RuntimeException("\nSyntax Error: Unexpected token " + peek().getType() +", expected commands or rbrace. Line " + peek().getLine());
    	}
    }

    // [NOVO] Declaracao "tipo nome" ou "tipo nome = valor": registra a variavel na Tabela de Simbolos
    private void declaration() {
    	Token type = consume(TokenType.TYPE);
    	Token name = consume(TokenType.NAME);
    	symbols.declare(name.getValue(), type.getValue(), name.getLine());
    	switch (peek().getType()) {
    	case TokenType.ASSIGN:
    		assignment();
    		break;
    	case TokenType.SEMICOLON:
    		break;
    	default:
    		throw new RuntimeException("\nSyntax Error: Unexpected token " + peek().getType() +", expected ASSIGN. Line " + peek().getLine());
    	}
    }

    // [NOVO] Registra o uso de uma variavel simples (nome sozinho). Chamadas e membros (a.b, f()) nao entram.
    private void use(Token variable) {
    	if (variable != null) {
    		symbols.use(variable.getValue(), variable.getLine());
    	}
    }


    // If
    private void if_command() {
    	consume(TokenType.IF);
    	CodeTable.Entry code = codes.open("IF"); // [NOVO]
    	consume(TokenType.LPAREN);
    	logic_expression();
    	consume(TokenType.RPAREN);

    	symbols.enterScope();
    	int[] block1 = block();
    	symbols.exitScope();
    	code.start1 = block1[0];
    	code.end1 = block1[1];

    	// [NOVO] else opcional: if (...) { ... } else { ... }
    	if (peek().getType() == TokenType.ELSE) {
    		consume(TokenType.ELSE);
    		codes.markElse(code);
    		symbols.enterScope();
    		int[] block2 = block();
    		symbols.exitScope();
    		code.start2 = block2[0];
    		code.end2 = block2[1];
    	}
    }

    // While
    private void while_command() {
    	consume(TokenType.WHILE);
    	CodeTable.Entry code = codes.open("WHILE"); // [NOVO]
    	consume(TokenType.LPAREN);
    	logic_expression();
    	consume(TokenType.RPAREN);

    	symbols.enterScope();
    	int[] block1 = block();
    	symbols.exitScope();
    	code.start1 = block1[0];
    	code.end1 = block1[1];
    }

    // For
    private void for_command() {
    	consume(TokenType.FOR);
    	CodeTable.Entry code = codes.open("FOR"); // [NOVO]
    	consume(TokenType.LPAREN);

    	symbols.enterScope(); // [NOVO] a variavel do for ("int i = 0") vale so dentro do for
    	// [CORRIGIDO] antes chamava assignment() direto e nunca aceitava "i = 0"
    	if (peek().getType() == TokenType.TYPE) {
    		declaration();
    	} else {
    		assignment_statement();
    	}
    	consume(TokenType.SEMICOLON);
    	logic_expression();
    	consume(TokenType.SEMICOLON);
    	assignment_statement();

    	consume(TokenType.RPAREN);
    	int[] block1 = block();
    	symbols.exitScope();
    	code.start1 = block1[0];
    	code.end1 = block1[1];
    }

    // [NOVO] "nome = expressao" (usado no inicio e no passo do for)
    private void assignment_statement() {
    	Token variable = compound_name();
    	use(variable);
    	assignment();
    }


    // Assignment
    private void assignment() {
    	consume(TokenType.ASSIGN);
    	switch (peek().getType()) {
    	case TokenType.LITERAL:
    		consume(TokenType.LITERAL);
    		break;
    	default:
    		math_expression();
    		break;
    	}
    }

    // Variables and functions connected with points
    // [ALTERADO] agora devolve o token do nome quando ele e uma variavel simples (sem '.' e sem chamada);
    // devolve null quando e chamada de funcao ou acesso com ponto (a.b, f(x)), que nao sao variaveis.
    private Token compound_name() {
    	Token first = consume(TokenType.NAME);
    	boolean simple = true;
    	if (peek().getType() == TokenType.LPAREN) {
    		simple = false;
    		consume(TokenType.LPAREN);
    		list_of_arguments();
    		consume(TokenType.RPAREN);
    	}
    	while (peek().getType() == TokenType.POINT) {
    		simple = false;
    		consume(TokenType.POINT);
    		consume(TokenType.NAME);
    		if (peek().getType() == TokenType.LPAREN) {
    			consume(TokenType.LPAREN);
    			list_of_arguments();
    			consume(TokenType.RPAREN);
    		}
        }
    	return simple ? first : null;
    }

    // List of arguments
    private void list_of_arguments() {
    	if (peek().getType() != TokenType.RPAREN) {
    		argument();
	    	while (peek().getType() == TokenType.COMMA) {
	    		consume(TokenType.COMMA);
	    		argument();
	    	}
    	}

    }

    // [NOVO] argumento de chamada: literal ('texto') ou expressao. Permite escreva('texto', a).
    private void argument() {
    	if (peek().getType() == TokenType.LITERAL) {
    		consume(TokenType.LITERAL);
    	} else {
    		math_expression();
    	}
    }


    // Logic expression
    private void logic_expression() {
    	if (peek().getType() == TokenType.NOT) {
    		consume(TokenType.NOT);
    	}
    	boolean_factor();
    }

    private void boolean_factor() {
    	comparison();
        while (peek().getType() == TokenType.AND || peek().getType() == TokenType.OR) {
            consume(peek().getType());
            logic_expression();
        }
    }

    private void comparison() {
    	math_expression();
    	switch (peek().getType()) {
    	case TokenType.GREATER, TokenType.LESSER, TokenType.GREATER_EQUAL, TokenType.LESSER_EQUAL,TokenType.EQUAL,TokenType.DIFFERENT:
    		consume(peek().getType());
    		math_expression();
    		break;
    	default: break;
    	}
    }


    // Math expression
    private void math_expression() {
        term();
        while (peek().getType() == TokenType.PLUS || peek().getType() == TokenType.MINUS) {
            consume(peek().getType());
            term();
        }
    }
    private void term() {
        factor();
        while (peek().getType() == TokenType.MULTIPLY || peek().getType() == TokenType.DIVIDE) {
            consume(peek().getType());
            factor();
        }
    }
    private void factor() {
        switch (peek().getType()) {
        case TokenType.NAME:
        	use(compound_name()); // [NOVO] registra o uso na Tabela de Simbolos
        	break;
		case TokenType.NUMBER:
		    consume(TokenType.NUMBER);
		    break;
		case TokenType.LPAREN:
			consume(TokenType.LPAREN);
            math_expression();
            consume(TokenType.RPAREN);
			break;
		default:
			throw new RuntimeException("\nSyntax Error: Unexpected token " + peek().getType() +", expected NUMBER, NAME or LPAREN. Line " + peek().getLine());
        }
    }
}
