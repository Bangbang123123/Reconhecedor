import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Queue;

public class Main {

	// Programa de exemplo na SUA linguagem (mesma logica do exemplo Portugol: pares e impares entre A e B).
	// Para testar outro programa: java Main caminho/do/arquivo.txt
	private static final String EXAMPLE =
			  "void exemplo() {\n"
			+ "    int a;\n"
			+ "    int b;\n"
			+ "    int c;\n"
			+ "    float d;\n"
			+ "    leia(a);\n"
			+ "    leia(b);\n"
			+ "    if (a < b) {\n"
			+ "        while (a < b) {\n"
			+ "            d = a / 2;\n"
			+ "            if (d != a) {\n"
			+ "                escreva('valor de A e Impar: ', a);\n"
			+ "            } else {\n"
			+ "                escreva('valor de A e Par: ', a);\n"
			+ "            };\n"
			+ "            a = a + 1;\n"
			+ "        };\n"
			+ "    } else {\n"
			+ "        if (a == b) {\n"
			+ "            escreva('A e Igual a B ', a, b);\n"
			+ "        } else {\n"
			+ "            escreva('A e Menor que B: ', a, b);\n"
			+ "        };\n"
			+ "    };\n"
			+ "}\n";

	public static void main(String[] args) throws Exception {
		String source = (args.length > 0) ? Files.readString(Path.of(args[0])) : EXAMPLE;

		// Mostra o codigo numerado (as tabelas se referem a essas linhas)
		String[] lines = source.split("\n", -1);
		for (int i = 0; i < lines.length; i++) {
			System.out.printf("%3d. %s%n", i + 1, lines[i].replace("\r", ""));
		}
		System.out.println();

    	Lexer lexer = new Lexer(source);
    	Queue<Token> list = lexer.tokenize();
    	for (Token token : list) {
    		System.out.print(token.toString() +" ");
    	}
    	System.out.println();

    	Parser parser = new Parser(list);
    	parser.parse();
    	System.out.println("\nApproved!\n");

    	parser.getSymbolTable().print();
    	System.out.println();
    	parser.getCodeTable().print();
	}

}
