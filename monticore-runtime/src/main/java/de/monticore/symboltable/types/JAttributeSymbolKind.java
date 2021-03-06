/* (c) https://github.com/MontiCore/monticore */

package de.monticore.symboltable.types;

import de.monticore.symboltable.SymbolKind;

public class JAttributeSymbolKind implements SymbolKind {

  private static final String NAME = "de.monticore.symboltable.types.JAttributeSymbolKind";

  protected JAttributeSymbolKind() {
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean isKindOf(SymbolKind kind) {
    return NAME.equals(kind.getName()) || SymbolKind.super.isKindOf(kind);
  }

}
