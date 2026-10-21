import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;


public class Lexer {
	// [CORRIGIDO] o automato de numeros usava a letra 'n' como simbolo de "digito", entao a variavel n virava NUMBER.
	// Agora o simbolo e um caractere de controle que nao aparece em codigo-fonte.
	private static final char DIGIT = '\0';

	private String input;
    private int pos = 0;

    // [NOVO] linha atual do codigo-fonte (comeca em 1) e linha onde o numero em leitura comecou
    private int line = 1;
    private int numberLine = 1;

    public Lexer(String input) {
        this.input = input;
    }


    public Automaton numbers = new Automaton(
    	'0',
		Map.of(
			'0',
			Map.of(
					DIGIT,'1'
			),
			'1',
			Map.of(
					DIGIT,'1',
					'.','2'
			),
			'2',
			Map.of(
					DIGIT,'3'
			),
			'3',
			Map.of(
					DIGIT,'3'
			)
		),
		Set.of('1','3')
	);

    // [NOVO] devolve o caractere 'offset' posicoes a frente sem estourar o texto
    private char lookahead(int offset) {
    	return (pos + offset < input.length()) ? input.charAt(pos + offset) : '\0';
    }

    // [NOVO] fecha o numero que estava sendo lido (se houver) e gera o token NUMBER
    private void flushNumber(Queue<Token> tokens) {
    	if (numbers.inFinalState()) {
			tokens.add(new Token(TokenType.NUMBER, numbers.returnTokenAndReset(), numberLine));
		} else {
			numbers.returnTokenAndReset();
		}
    }

    public Queue<Token> tokenize() {
        Queue<Token> tokens = new LinkedList<Token>();

        while (pos < input.length()) {

            // Skip blank space
            if (Character.isWhitespace(input.charAt(pos))) {
            	// [NOVO] espaco/quebra de linha encerra o numero (evita "1 2" virar 12 e erro na linha)
            	flushNumber(tokens);
            	if (input.charAt(pos) == '\n') line++; // [NOVO] contagem de linhas
                pos++;
                continue;
            }

            // Literal detector
            if (input.charAt(pos) == '\'') {
            	int literalLine = line;
            	StringBuilder sb = new StringBuilder();
            	sb.append(input.charAt(pos++));
            	while (pos < input.length() && input.charAt(pos) != '\'') {
            		if (input.charAt(pos) == '\n') line++;
            		sb.append(input.charAt(pos++));
            	}
            	if (pos >= input.length()) {
            		throw new RuntimeException("Lexer Error: Unterminated literal starting at line "+ literalLine);
            	}
            	sb.append(input.charAt(pos++));

            	tokens.add(new Token(TokenType.LITERAL, sb.toString(), literalLine));
            	continue;
            }

            // Number Detector
            char temp = input.charAt(pos);
            if (Character.isDigit(input.charAt(pos))) temp = DIGIT;
            else if (temp == DIGIT) temp = '\1'; // nunca confundir um caractere comum com o simbolo de digito
            boolean numberWasEmpty = !numbers.inFinalState() && numbers.isEmpty();
            if (!numbers.nextInput(temp,input.charAt(pos))) {
            	if (numbers.inFinalState()) {
            		if (pos < input.length() && Character.isJavaIdentifierPart(input.charAt(pos))) {
            			throw new RuntimeException("Lexer Error: Numbers cannot be immediately followed by letters. Line "+ line +", position: "+ pos);
                    }
        			tokens.add(new Token(TokenType.NUMBER,numbers.returnTokenAndReset(), numberLine));
        		} else {
        			numbers.returnTokenAndReset();
        		}
            } else {
            	if (numberWasEmpty) numberLine = line; // [NOVO] guarda a linha do primeiro digito
            	pos++;
            	continue;
            }

            // Reserved keywords detector
            if (Character.isJavaIdentifierStart(input.charAt(pos))) {
            	StringBuilder sb = new StringBuilder();
            	sb.append(input.charAt(pos++));
            	while (pos < input.length() && Character.isJavaIdentifierPart(input.charAt(pos))) {
            		sb.append(input.charAt(pos++));
            	}

            	String unidentified_token = sb.toString();
            	switch (unidentified_token) {
            		case "while":
            			tokens.add(new Token(TokenType.WHILE,"while", line));
            			continue;
            		case "if":
            		    tokens.add(new Token(TokenType.IF, "if", line));
            		    continue;
            		case "else":
            		    tokens.add(new Token(TokenType.ELSE, "else", line));
            		    continue;
            		case "for":
            		    tokens.add(new Token(TokenType.FOR, "for", line));
            		    continue;
            		case "int","float","char","string","boolean","void": // [NOVO] "float" (o Portugol tem tipo real)
            		    tokens.add(new Token(TokenType.TYPE, unidentified_token, line));
            			continue;
            		default:
            			tokens.add(new Token(TokenType.NAME, unidentified_token, line));
            			continue;
            	}
            }


            // Special operation characters
            switch (input.charAt(pos)) {
	            case '+':
	            	tokens.add(new Token(TokenType.PLUS, "+", line));
	            	break;
	            case '-':
	            	tokens.add(new Token(TokenType.MINUS, "-", line));
	            	break;
	            case '*':
	            	tokens.add(new Token(TokenType.MULTIPLY, "*", line));
	            	break;
	            case '/':
	            	tokens.add(new Token(TokenType.DIVIDE, "/", line));
	            	break;
	            case '(':
	            	tokens.add(new Token(TokenType.LPAREN, "(", line));
	            	break;
	            case ')':
	            	tokens.add(new Token(TokenType.RPAREN, ")", line));
	            	break;
	            case '{':
	            	tokens.add(new Token(TokenType.LBRACE, "{", line));
	            	break;
	            case '}':
	            	tokens.add(new Token(TokenType.RBRACE, "}", line));
	            	break;
	            case '.':
	            	tokens.add(new Token(TokenType.POINT, ".", line));
	            	break;
	            case ',':
	            	tokens.add(new Token(TokenType.COMMA, ",", line));
	            	break;
	            case ';':
	            	tokens.add(new Token(TokenType.SEMICOLON, ";", line));
	            	break;
	            // [CORRIGIDO] os operadores de 1 ou 2 caracteres usavam ++pos e depois pos++ de novo,
	            // o que "engolia" o caractere seguinte (ex.: "a<b" perdia o b). Agora usa lookahead().
	            case '!':
	            	if (lookahead(1) == '=') {
	            		tokens.add(new Token(TokenType.DIFFERENT,"!=", line));
	            		pos += 2;
	            		continue;
	            	}
	            	tokens.add(new Token(TokenType.NOT,"!", line));
            		break;
	            case '=':
	            	if (lookahead(1) == '=') {
	            		tokens.add(new Token(TokenType.EQUAL,"==", line));
	            		pos += 2;
	            		continue;
	            	}
	            	tokens.add(new Token(TokenType.ASSIGN,"=", line));
	            	break;
	            case '<':
	            	if (lookahead(1) == '=') {
	            		tokens.add(new Token(TokenType.LESSER_EQUAL,"<=", line));
	            		pos += 2;
	            		continue;
	            	}
	            	tokens.add(new Token(TokenType.LESSER,"<", line));
	            	break;
	            case '>':
	            	if (lookahead(1) == '=') {
	            		tokens.add(new Token(TokenType.GREATER_EQUAL,">=", line));
	            		pos += 2;
	            		continue;
	            	}
	            	tokens.add(new Token(TokenType.GREATER,">", line)); // [CORRIGIDO] o valor estava "<"
	            	break;
	            case '|':
	            	if (lookahead(1) == '|') {
	            		tokens.add(new Token(TokenType.OR,"||", line));
	            		pos += 2;
	            		continue;
	            	} else {
	            		throw new RuntimeException("Lexing Error: Expected character |. Line "+ line +", position: "+ pos);
	            	}
	            case '&':
	            	if (lookahead(1) == '&') {
	            		tokens.add(new Token(TokenType.AND,"&&", line));
	            		pos += 2;
	            		continue;
	            	} else {
	            		throw new RuntimeException("Lexing Error: Expected character &. Line "+ line +", position: "+ pos);
	            	}
	            default:
	            	throw new RuntimeException("Lexing Error: Unexpected character "+ input.charAt(pos) +". Line "+ line +", position: "+ pos);
	        }
            pos++;
        }
        flushNumber(tokens);
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }
}
