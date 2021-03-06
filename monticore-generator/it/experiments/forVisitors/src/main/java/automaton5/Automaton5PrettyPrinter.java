/* (c) Monticore license: https://github.com/MontiCore/monticore */
package automaton5;

import automaton5._ast.*;
import automaton5._visitor.*;

/**
 * Pretty prints automatons. Use {@link #print(ASTAutomaton5)} to start a pretty
 * print and get the result by using {@link #getResult()}.
 *

 */
public class Automaton5PrettyPrinter implements Automaton5Visitor {

  protected String result = "";
  
  protected int indention = 0;
  
  protected String indent = "";
  
  /**
   * Prints the automaton
   * 
   * @param automaton
   */
  public void print(ASTAutomaton automaton) {
    handle(automaton);
  }
  
  /**
   * Gets the printed result.
   * 
   * @return the result of the pretty print.
   */
  public String getResult() {
    return this.result;
  }
  
  @Override
  public void visit(ASTAutomaton node) {
    println("automaton " + node.getName() + " {");
    indent();
  }
  
  @Override
  public void endVisit(ASTAutomaton node) {
    unindent();
    println("}");
  }
  
  @Override
  public void visit(ASTState node) {
    println("state " + node.getName() +";");
  }
  
  @Override
  public void visit(ASTTransition node) {
    print(node.getFrom());
    print(" - " + node.getInput() + " > ");
    print(node.getTo());
    // println(";");
    println(";    /*PP5*/");
  }
  

  // the following part manages indentation -----------------
  
  protected void print(String s) {
    result += (indent + s);
    indent = "";
  }
  
  protected void println(String s) {
    result += (indent + s + "\n");
    indent = "";
    calcIndention();
  }
  
  protected void calcIndention() {
    indent = "";
    for (int i = 0; i < indention; i++) {
      indent += "  ";
    }
  }
  
  protected void indent() {
    indention++;
    calcIndention();
  }
  
  protected void unindent() {
    indention--;
    calcIndention();
  }
}
