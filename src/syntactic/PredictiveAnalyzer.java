package syntactic;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import syntactic.grammar.Derivation;
import syntactic.grammar.Grammar;
import syntactic.grammar.NonTerminal;
import syntactic.grammar.NonTerminalName;
import syntactic.grammar.OperatorsGrammar;
import syntactic.grammar.Symbol;
import syntactic.grammar.Terminal;
import lexical.LexicalAnalyzer;
import lexical.Token;
import lexical.TokenCategory;

public class PredictiveAnalyzer {

	private Grammar grammar;
	private PredictiveTable predictiveTable;
	private LexicalAnalyzer lexicalAnalyzer;
	private PrecedenceAnalyzer precedenceAnalyzer;

	private Stack<Symbol> stack;
	private Derivation derivation;

	// TODO
	public PredictiveAnalyzer(Grammar grammar, PredictiveTable predictiveTable,
			LexicalAnalyzer lexicalAnalyzer) {

		this.grammar = grammar;
		this.predictiveTable = predictiveTable;
		this.lexicalAnalyzer = lexicalAnalyzer;
		precedenceAnalyzer = new PrecedenceAnalyzer(lexicalAnalyzer);

		stack = new Stack<Symbol>();
		derivation = new Derivation();
	}

	public void predictiveAnalyze() {

		Symbol topSymbol;
		Token token = new Token();
		Terminal terminal;
		NonTerminal topNonTerminal;
		Integer derivationNumber;

		if (lexicalAnalyzer.hasMoreTokens()) {

			token = lexicalAnalyzer.nextToken();

			terminal = new Terminal(token);
			stack.push(new NonTerminal(NonTerminalName.PROGRAM));
			int counter = 1;

			while (!stack.isEmpty()) {

				topSymbol = stack.peek();

				if (topSymbol.isTerminal()) {

					if (topSymbol.getValue() == terminal.getValue()) {
						stack.pop();
						if (lexicalAnalyzer.hasMoreTokens()) {
							token = lexicalAnalyzer.nextToken();
							terminal = new Terminal(token);
						}

					} else {
						SyntaticAnalyzer.printError(token);
						System.exit(1);
					}

				} else {

					topNonTerminal = (NonTerminal) topSymbol;

					if (topNonTerminal.getName() == NonTerminalName.EXPRESSION) {
						if (!OperatorsGrammar.getInstance()
								.getOperatorsGrammarSymbols()
								.contains(terminal.getCategory())) {
							SyntaticAnalyzer.printError(token);
							System.exit(1);

						} else {
							if (precedenceAnalyzer.precedenceAnalysis(token)) {

								stack.pop();
								topSymbol = stack.peek();

								terminal = new Terminal(
										precedenceAnalyzer.getEndOfSentence());

							} else {
								System.out.println("EXPRESSION ERROR!");
								break;
							}
						}
					} else {

						derivationNumber = null;

						if (topNonTerminal.getName() == NonTerminalName.VALUE
								&& terminal.getCategory() != TokenCategory.ARRAYBEGIN) {

							derivationNumber = Grammar.EXPRESSION;

						} else {
							derivationNumber = predictiveTable
									.getDerivationNumber(
											topNonTerminal.getName(),
											terminal.getCategory());
						}

						if (derivationNumber != null) {
							derivation = grammar.getGrammarMap().get(
									derivationNumber);

							if (derivation != null) {
								System.out.print(topNonTerminal.getName() + "("
										+ counter++ + ")" + " = ");
								stack.pop();
								// TO REMOVE
								Symbol symb;
								Terminal term;
								NonTerminal nonTerm;

								for (int i = derivation.getSymbolsList().size() - 1; i >= 0; i--) {

									symb = derivation.getSymbolsList().get(i);
									if (symb.isTerminal()) {
										term = (Terminal) symb;
									} else {
										nonTerm = (NonTerminal) symb;
									}

									stack.push(symb);
								}

								for (int i = 0; i < derivation.getSymbolsList()
										.size(); i++) {
									symb = derivation.getSymbolsList().get(i);
									if (symb.isTerminal()) {
										term = (Terminal) symb;
										System.out.print("'"
												+ term.getCategory().toString()
														.toLowerCase() + "'"
												+ " ");
									} else {
										nonTerm = (NonTerminal) symb;
										System.out.print(nonTerm.getName()
												+ "(" + counter++ + ")" + " ");
									}
								}
								System.out.println();
							} else {
								System.out.println(topNonTerminal.getName()
										+ "(" + counter++ + ")" + " = epsilon");
								stack.pop();
							}

						} else {
							SyntaticAnalyzer.printError(terminal
									.getTerminalToken());
							System.exit(1);
						}
					}
				}
			}
		}
	}
}
