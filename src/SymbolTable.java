import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * [NOVO] Tabela de Simbolos (modelo do Portugol):
 * Identificador | Escopo | Tipo | Declaracao | Usos (ou fim)
 *
 * - Escopo 0 = nivel global (nome da funcao). Cada "{ }" aberto por funcao/if/else/while/for
 *   sobe um nivel (1, 2, 3...), igual a profundidade do bloco.
 * - Declaracao = linha onde o identificador foi declarado.
 * - Usos = linhas onde o identificador foi lido/escrito (sem repetir a mesma linha).
 *   Para a funcao, a ultima coluna mostra a linha em que ela termina (o "fim" do Portugol).
 */
public class SymbolTable {

	public static class Entry {
		final String name;
		final int scope;
		final String type;
		final int declarationLine;
		final TreeSet<Integer> uses = new TreeSet<>(); // TreeSet: ordenado e sem linhas repetidas
		int endLine = 0; // so usado pela funcao (linha do "}" final)

		Entry(String name, int scope, String type, int declarationLine) {
			this.name = name;
			this.scope = scope;
			this.type = type;
			this.declarationLine = declarationLine;
		}
	}

	private final List<Entry> entries = new ArrayList<>();               // ordem de declaracao (para imprimir)
	private final Deque<Map<String, Entry>> scopes = new ArrayDeque<>(); // pilha de escopos visiveis

	// Abre um escopo novo (entrou em um bloco)
	public void enterScope() {
		scopes.push(new HashMap<>());
	}

	// Fecha o escopo atual (saiu do bloco): os nomes dele deixam de ser visiveis
	public void exitScope() {
		scopes.pop();
	}

	// Escopo atual: 0 = global
	public int currentScope() {
		return scopes.size() - 1;
	}

	// Registra uma declaracao no escopo atual. Erro se o nome ja existe NESTE escopo.
	public Entry declare(String name, String type, int line) {
		Map<String, Entry> current = scopes.peek();
		if (current.containsKey(name)) {
			throw new RuntimeException("Semantic Error: '" + name + "' already declared in this scope (line "
					+ current.get(name).declarationLine + "). Line " + line);
		}
		Entry entry = new Entry(name, currentScope(), type, line);
		current.put(name, entry);
		entries.add(entry);
		return entry;
	}

	// Registra o uso de um identificador: procura do escopo mais interno ate o global.
	public void use(String name, int line) {
		for (Map<String, Entry> scope : scopes) { // ArrayDeque como pilha: percorre do topo para a base
			Entry entry = scope.get(name);
			if (entry != null) {
				entry.uses.add(line);
				return;
			}
		}
		throw new RuntimeException("Semantic Error: '" + name + "' used without being declared. Line " + line);
	}

	public void print() {
		System.out.println("TABELA DE SÍMBOLOS");
		System.out.println();
		String format = "%-14s %-7s %-16s %-11s %s%n";
		System.out.printf(format, "Identificador", "Escopo", "Tipo", "Declaração", "Usos (ou fim)");
		for (Entry e : entries) {
			String usesText;
			if (e.endLine > 0) {
				usesText = String.valueOf(e.endLine); // funcao: mostra onde ela termina
			} else {
				StringBuilder sb = new StringBuilder();
				for (int line : e.uses) {
					if (sb.length() > 0) sb.append(", ");
					sb.append(line);
				}
				usesText = sb.toString();
			}
			System.out.printf(format, e.name, e.scope, e.type, e.declarationLine, usesText);
		}
	}
}
