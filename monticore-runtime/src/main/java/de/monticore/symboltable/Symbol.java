 /*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2015, MontiCore, All rights reserved.
 *
 * This project is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * ******************************************************************************
 */ package de.monticore.symboltable;

 import java.util.Optional;

 import de.monticore.ast.ASTNode;
 import de.monticore.symboltable.modifiers.AccessModifier;
 import de.monticore.symboltable.modifiers.BasicAccessModifier;
 import de.se_rwth.commons.SourcePosition;
 import de.se_rwth.commons.logging.Log;

/**
 * Super type for all symbols of the symbol table.
 *
 * @author  Pedram Mir Seyed Nazari
 *
 */
// TODO PN create an technical interface analogous to Scope and ScopeManipulationApi?
public interface Symbol {

  SymbolKind KIND = new SymbolKind();


  /**
   * @return the symbol name
   */
  String getName();

  /**
   * @return the package of this symbol. Note that this is not the full name {@link #getFullName()} of the
   * symbol. For example, the full name of a state symbol <code>s</code> in a state chart <code>p.q.SC</code> is
   * <code>p.q.SC.s</code>, whereas the package name is <code>p.q</code>.
   *
   * @see #getFullName()
   */
  String getPackageName();

  /**
   * @return the full name of a symbol. For example, the full name of a state symbol <code>s</code>
   * in a state chart <code>p.q.SC</code> is <code>p.q.SC.s</code>.
   *
   * @see #getPackageName()
   */
  String getFullName();


  // TODO getPackageName() and getQualifiedName() => getName() should return simple name

  /**
   * @return the symbol kind
   */
  SymbolKind getKind();

  /**
   * @return true, if this symbol is of the <code>kind</code>.
   * @see SymbolKind#isKindOf(SymbolKind)
   */
  default boolean isKindOf(SymbolKind kind) {
    return getKind().isKindOf(Log.errorIfNull(kind));
  }

  /**
   * @return the access modifier, such as public or protected in Java. By default, the {@link
   * de.monticore.symboltable.modifiers.BasicAccessModifier#ABSENT} is returned, which indicates that
   * the symbol does not have any access modifier. Note that this is not the same as the (implicit) access
   * modifier {@link de.monticore.symboltable.modifiers.BasicAccessModifier#PACKAGE_LOCAL} of Java.
   */
  // TODO PN remove ABSENT Modifier and use Optional instead
  default AccessModifier getAccessModifier() {
    return BasicAccessModifier.ABSENT;
  }

  /**
   * Sets the access modifier, such as public or protected in Java.
   * @param accessModifier the access modifier
   */
  void setAccessModifier(AccessModifier accessModifier);

  /**
   * @param node the corresponding ast node
   */
  void setAstNode(ASTNode node);

  /**
   * @return the corresponding ast node
   */
  // TODO PN doc we don't use <? extends ASTNode> since this could lead to problems with symbol composition
  Optional<ASTNode> getAstNode();

  /**
   * @return the position of this symbol in the source model. By default, it is the source position
   * of the ast node.
   *
   * @see #getAstNode()
   */
  // TODO PN make source position optional
  default SourcePosition getSourcePosition() {
    if (getAstNode().isPresent()) {
      return getAstNode().get().get_SourcePositionStart();
    }
    else {
      return SourcePosition.getDefaultSourcePosition();
    }
  }

  /**
   * @return the enclosing scope of this symbol, i.e., the scope that defines this symbol.
   */
  Scope getEnclosingScope();

  /**
   * @param scope the enclosing scope of this symbol, i.e., the scope that defines this symbol.
   */
  void setEnclosingScope(MutableScope scope);
  
}
