import java.awt.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Class which models e-NKA. 
 * 
 * @author Ivan Paljak 
 */
public class EpsilonNKA {

	private class State {
		
		public Production p;
		public int dotIndex;
		public Set<String> starts; 
		
		public State() {}
		
		public State(Production p, int dotIndex) {
			this.p = p;
			dotIndex = 0;
		}
		
		public boolean isAlive() {
			return dotIndex < p.right.size();
		}
		
		public String afterDot() {
			return p.right.get(dotIndex);
		}
		
		public void fromState(State s) {
			
			this.p = new Production(); 
			this.p.fromProduction(s.p);
			
			this.dotIndex = s.dotIndex + 1;
			
			this.starts = new HashSet<String>();
			for (String str : s.starts) this.starts.add(new String(str));
			
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(p.left);
			sb.append("->");

			for (int i = 0; i <= p.right.size(); i++) {
				if (i == dotIndex)
					sb.append("*");
				if (i != p.right.size())
					sb.append(p.right.get(i));
			}
			sb.append(", {");

			for (String s : starts) {
				if (s.equals(""))
					sb.append("$,");
				else
					sb.append(s + ",");
			}
			sb.append("}");
			return sb.toString();
		}

		
	}
	
	private class Transition {
		
		public State from, to;
		public String edge;
		
		public Transition(State from, State to, String edge) {
			this.from = from; 
			this.to = to;
			this.edge = edge;
		}
		
		@Override
		public String toString() {
			return from.toString() + " --> ( " + edge + " ) " + to.toString();
		}
		
	}	
	
	private String startingState;
	
	private static HashMap<String, ArrayList<Production>> grammar = new HashMap<>();
	private static BeginsWithTable beginsWith; 
	
	private ArrayList<String> nonterminal;
	private ArrayList<String> terminal;
	
	private ArrayList<State> states; 
	private ArrayList<Transition> transitions; 
	
	public EpsilonNKA(String startingState, HashMap<String, ArrayList<Production>> grammar, 
			BeginsWithTable beginsWith, ArrayList<String> nonterminal, 
			ArrayList<String> terminal) {
		
		this.startingState = startingState;
		this.grammar = grammar;
		this.beginsWith = beginsWith;
		this.nonterminal = nonterminal;
		this.terminal = terminal;
	
		states = new ArrayList<EpsilonNKA.State>();
		transitions = new ArrayList<EpsilonNKA.Transition>();
	
		for (Production p : grammar.get(startingState)) {
			State s = new State(p, 0);
			
			Set<String> starts = new HashSet<String>();
			starts.add("$"); s.starts = starts;
			
			states.add(s);
		}
		
	}
	
	/** Getters for states and transitions **/
	public ArrayList<State> getStates() {
		return states;
	}
	
	public ArrayList<Transition> getTransitions() {
		return transitions;
	}
	
	public void generateEpsilonNKA() {
		
		int stateIndex = 0;
		
		while (stateIndex < states.size()) {
		
			State currentState = states.get(stateIndex);
			if (!currentState.isAlive()) continue;
	
			// b) prijelaz
			State nextState = new State();
			nextState.fromState(currentState);
			
			states.add(nextState);
			transitions.add(new Transition(currentState, nextState, currentState.afterDot()));
			
			// c) prijelaz
			if (terminal.contains(currentState.afterDot())) continue;
			
			Set<String> starting = new HashSet<String>();
			for (String str : beginsWith.getBeginsWith(currentState.afterDot())) {
				starting.add (new String(str));
			}
			
			boolean propagate = false;
			if (currentState.dotIndex + 1 >= currentState.p.right.size()) propagate = true;
			
			for (int i = currentState.dotIndex + 1; i < currentState.p.right.size(); ++i) {
				String currString = currentState.p.right.get(i);
				if (nonterminal.contains(currString)) continue;
				if (beginsWith.generatesEpsilon(currString) || currString.equals("$")) propagate = true;
			}
			
			if (propagate) {
				for (String str : currentState.starts) starting.add(new String(str));
			}
			
			for (Production p : grammar.get(currentState.afterDot())) {
				
				nextState = new State(p, 0);
				nextState.starts = starting;
				
				transitions.add(new Transition(currentState, nextState, "$"));
				
			}
			
		}
		
	}
	
	public void outputStates() {
		for (State s : states) System.out.println(s);
	}
	
	public void outputTransitions() {
		for (Transition t : transitions) System.out.println(t);
	}
	
} 