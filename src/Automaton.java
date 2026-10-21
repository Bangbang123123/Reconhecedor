import java.util.Map;
import java.util.Set;

public class Automaton {
	private final char initial_state;
	private char current_state;
	private Map<Character, Map<Character, Character>> transitions;
	private Set<Character> final_states;
	private StringBuilder possible_token = new StringBuilder();
	
	public Automaton(char initial_state, Map<Character, Map<Character, Character>> transitions, Set<Character> final_states) {
		this.initial_state = initial_state;
		this.current_state = initial_state;
		this.transitions = transitions;
		this.final_states = final_states;
	}
	
	public String returnTokenAndReset() {
		String content = possible_token.toString();
		possible_token.setLength(0);
		current_state = initial_state;
		return content;
	}
	
	public boolean nextInput(char input, char token) {
		if (transitions.containsKey(current_state) && transitions.get(current_state).containsKey(input)) {
			current_state = transitions.get(current_state).get(input);
			possible_token.append(token);
			return true;
		}
		return false;
	}
	
	public boolean isEmpty() {
		return possible_token.length() == 0;
	}

	public boolean inFinalState() {
		return final_states.contains(current_state);
	}
	
}
