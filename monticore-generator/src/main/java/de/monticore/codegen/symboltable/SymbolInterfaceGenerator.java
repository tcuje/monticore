/* (c) https://github.com/MontiCore/monticore */

package de.monticore.codegen.symboltable;

import de.monticore.generating.GeneratorEngine;
import de.monticore.grammar.symboltable.MCGrammarSymbol;
import de.monticore.io.paths.IterablePath;

public interface SymbolInterfaceGenerator {

  void generate(GeneratorEngine genEngine, SymbolTableGeneratorHelper genHelper,
                IterablePath handCodedPath, MCGrammarSymbol grammarSymbol);

}
