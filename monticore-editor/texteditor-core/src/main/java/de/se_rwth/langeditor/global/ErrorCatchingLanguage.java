/*******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2015, 2016, MontiCore, All rights reserved.
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
 *******************************************************************************/
package de.se_rwth.langeditor.global;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.core.resources.IProject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import de.monticore.ast.ASTNode;
import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.SymbolKind;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.langeditor.language.Language;
import de.se_rwth.langeditor.language.OutlineElementSet;
import de.se_rwth.langeditor.language.ParserConfig;
import de.se_rwth.langeditor.modelstates.ModelState;

final class ErrorCatchingLanguage implements Language {
  
  private final Language language;
  
  ErrorCatchingLanguage(Language language) {
    this.language = language;
  }
  
  @Override
  public String getExtension() {
    return language.getExtension();
  }
  
  @Override
  public ParserConfig<?> getParserConfig() {
    return language.getParserConfig();
  }
  
  public void buildProject(IProject project, ImmutableSet<ModelState> modelStates,
      ImmutableList<Path> modelPath) {
    try {
      language.buildProject(project, modelStates, modelPath);
    }
    catch (Exception e) {
      Log.error("0xA1115 Error while building project.", e);
    }
  }
  
  public void buildModel(ModelState modelState) {
    try {
      language.buildModel(modelState);
    }
    catch (Exception e) {
      Log.error("0xA1116 Error while building model.", e);
    }
  }
  
  public ImmutableList<String> getKeywords() {
    try {
      return language.getKeywords();
    }
    catch (Exception e) {
      Log.error("0xA1117 Error while retrieving keywords.", e);
      return ImmutableList.of();
    }
  }
  
  public OutlineElementSet getOutlineElementSet() {
    try {
      return language.getOutlineElementSet();
    }
    catch (Exception e) {
      Log.error("0xA1118 Error determining outline elements.", e);
      return OutlineElementSet.empty();
    }
  }
  
 
  @Override
  public Collection<? extends SymbolKind> getCompletionKinds() {
    try {
      return language.getCompletionKinds();
    }
    catch (Exception e) {
      Log.error("0xA1123 Error determining completion kinds.", e);
      return Sets.newHashSet();
    }
  }

  public Optional<Supplier<Optional<ASTNode>>> createResolver(ASTNode astNode) {
    try {
      return language.createResolver(astNode);
    }
    catch (Exception e) {
      Log.error("0xA1119 Error while creating hyperlink.", e);
      return Optional.empty();
    }
  }

  /**
   * @see de.se_rwth.langeditor.language.Language#getScope()
   */
  @Override
  public Optional<GlobalScope> getScope(ASTNode node) {
    return language.getScope(node);
  }
}
