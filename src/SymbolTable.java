import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class SymbolTable {

	public static class Entry {
		final String name;
		final int scope;
		final String type;
		final int declarationLine;
		final TreeSet<Integer> uses = new TreeSet<>();
		int endLine = 0;

		Entry(String name, int scope, String type, int declarationLine) {
			this.name = name;
			this.scope = scope;
			this.type = type;
			this.declarationLine = declarationLine;
		}
	}

	private final List<Entry> entries = new ArrayList<>();          
	private final Deque<Map<String, Entry>> scopes = new ArrayDeque<>(); 

	public void enterScope() {
		scopes.push(new HashMap<>());
	}

	public void exitScope() {
		scopes.pop();
	}

	public int currentScope() {
		return scopes.size() - 1;
	}

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

	public void use(String name, int line) {
		for (Map<String, Entry> scope : scopes) {
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
				usesText = String.valueOf(e.endLine); 
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
