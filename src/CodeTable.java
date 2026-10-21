import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeTable {

	public static class Entry {
		String name;
		int start1, end1;
		int start2, end2; 
	}

	private final List<Entry> entries = new ArrayList<>();
	private final Map<String, Integer> counters = new HashMap<>();

	public Entry open(String kind) {
		int number = counters.merge(kind, 1, Integer::sum);
		Entry entry = new Entry();
		entry.name = kind + number;
		entries.add(entry);
		return entry;
	}

	public Entry openFunction(String functionName) {
		Entry entry = new Entry();
		entry.name = "FUNCTION " + functionName;
		entries.add(entry);
		return entry;
	}

	public void markElse(Entry ifEntry) {
		ifEntry.name = ifEntry.name + " - ELSE" + ifEntry.name.substring(2);
	}

	private static String show(int value) {
		return value == 0 ? "" : String.valueOf(value);
	}

	public void print() {
		System.out.println("TABELA DE CÓDIGOS");
		System.out.println();
		String format = "%-22s %-13s %-11s %-13s %s%n";
		System.out.printf(format, "Comando", "Início Bloco1", "Fim Bloco1", "Início Bloco2", "Fim Bloco2");
		for (Entry e : entries) {
			System.out.printf(format, e.name, show(e.start1), show(e.end1), show(e.start2), show(e.end2));
		}
	}
}
