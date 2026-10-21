import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [NOVO] Tabela de Codigos (modelo do Portugol):
 * Comando | Inicio Bloco1 | Fim Bloco1 | Inicio Bloco2 | Fim Bloco2
 *
 * - Bloco = do "{" ate o "}" correspondente (linhas onde eles aparecem).
 * - if/else: Bloco1 = bloco do if, Bloco2 = bloco do else (os dois ficam na mesma linha da tabela, "IF1 - ELSE1").
 * - while/for: so Bloco1.
 * - Funcao (equivale ao "Principal" do Portugol): primeiro par = da linha da assinatura ate o "}" final;
 *   segundo par = do "{" ate o "}" do corpo.
 * - Os comandos recebem numero na ordem em que aparecem (IF1, IF2, WHILE1...), como SE1, SE2, ENQUANTO1.
 */
public class CodeTable {

	public static class Entry {
		String name;
		int start1, end1; // Bloco1
		int start2, end2; // Bloco2 (0 = nao existe)
	}

	private final List<Entry> entries = new ArrayList<>();
	private final Map<String, Integer> counters = new HashMap<>();

	// Cria a linha de um comando (IF, WHILE, FOR) no momento em que a palavra-chave e lida,
	// assim as linhas da tabela ficam na ordem em que os comandos aparecem no codigo.
	public Entry open(String kind) {
		int number = counters.merge(kind, 1, Integer::sum);
		Entry entry = new Entry();
		entry.name = kind + number;
		entries.add(entry);
		return entry;
	}

	// Linha da funcao (Principal)
	public Entry openFunction(String functionName) {
		Entry entry = new Entry();
		entry.name = "FUNCTION " + functionName;
		entries.add(entry);
		return entry;
	}

	// Chamado quando um if tem else: o nome vira "IF1 - ELSE1"
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
