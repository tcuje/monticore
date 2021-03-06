/* (c) https://github.com/MontiCore/monticore */

package de.monticore.symboltable.resolving;

import de.monticore.symboltable.Symbol;
import de.monticore.symboltable.SymbolKind;

public interface AdaptedResolvingFilter<S extends Symbol> extends ResolvingFilter<S>{

  SymbolKind getSourceKind();

  Symbol translate(Symbol s);
}
