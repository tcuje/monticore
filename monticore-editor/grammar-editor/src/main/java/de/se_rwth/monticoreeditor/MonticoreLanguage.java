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
package de.se_rwth.monticoreeditor;

import static de.se_rwth.langeditor.util.Misc.loadImage;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.core.resources.IProject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.monticore.ast.ASTNode;
import de.monticore.grammar.cocos.GrammarCoCos;
import de.monticore.grammar.grammar._ast.ASTASTRule;
import de.monticore.grammar.grammar._ast.ASTAbstractProd;
import de.monticore.grammar.grammar._ast.ASTClassProd;
import de.monticore.grammar.grammar._ast.ASTExternalProd;
import de.monticore.grammar.grammar._ast.ASTInterfaceProd;
import de.monticore.grammar.grammar._ast.ASTLexProd;
import de.monticore.grammar.grammar._ast.ASTMCGrammar;
import de.monticore.grammar.grammar_withconcepts._cocos.Grammar_WithConceptsCoCoChecker;
import de.monticore.grammar.grammar_withconcepts._parser.Grammar_WithConceptsAntlrLexer;
import de.monticore.grammar.grammar_withconcepts._parser.Grammar_WithConceptsAntlrParser;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.langeditor.language.Language;
import de.se_rwth.langeditor.language.OutlineElementSet;
import de.se_rwth.langeditor.language.OutlineElementSet.Builder;
import de.se_rwth.langeditor.language.ParserConfig;
import de.se_rwth.langeditor.modelstates.ModelState;

public final class MonticoreLanguage implements Language {
  
  private final Resolving resolving = new Resolving();
  
  @Override
  public String getExtension() {
    return "mc4";
  }
  
  @Override
  public ParserConfig<?> getParserConfig() {
    return new ParserConfig<>(Grammar_WithConceptsAntlrLexer::new,
        Grammar_WithConceptsAntlrParser::new,
        Grammar_WithConceptsAntlrParser::mCGrammar);
  }
  
  @Override
  public void buildProject(IProject project, ImmutableSet<ModelState> modelStates,
      ImmutableList<Path> modelPath) {
    resolving.buildProject(project, modelStates, modelPath);
    modelStates.forEach(MonticoreLanguage::checkContextConditions);
  }
  
  @Override
  public void buildModel(ModelState modelState) {
    resolving.buildModel(modelState);
    checkContextConditions(modelState);
  }
  
  @Override
  public ImmutableList<String> getKeywords() {
    return ImmutableList.of("component", "package", "grammar", "options", "astimplements",
        "astextends",
        "interface", "enum", "implements", "external", "fragment",
        "extends", "returns", "ast", "token", "protected");
  }
  
  @Override
  public OutlineElementSet getOutlineElementSet() {
    Builder builder = OutlineElementSet.builder();
    builder.add(ASTClassProd.class,
        ASTClassProd::getName,
        loadImage("icons/teamstrm_rep.gif"));
    builder.add(ASTInterfaceProd.class,
        ASTInterfaceProd::getName,
        loadImage("icons/intf_obj.gif"));
    builder.add(ASTAbstractProd.class,
        ASTAbstractProd::getName,
        loadImage("icons/class_abs_tsk.gif"));
    builder.add(ASTASTRule.class,
        ASTASTRule::getType,
        loadImage("icons/source_attach_attrib.gif"));
    builder.add(ASTExternalProd.class,
        ASTExternalProd::getName,
        loadImage("icons/element.gif"));
    builder.add(ASTLexProd.class,
        ASTLexProd::getName,
        loadImage("icons/tnames_co.gif"));
    return builder.build();
  }
  
  @Override
  public Optional<Supplier<Optional<ASTNode>>> createResolver(ASTNode astNode) {
    return resolving.createResolver(astNode);
  }
  
  private static void checkContextConditions(ModelState modelState) {
    if (modelState.getRootNode() instanceof ASTMCGrammar) {
      Log.getFindings().clear();
      Grammar_WithConceptsCoCoChecker cocoChecker = new GrammarCoCos().getCoCoChecker();
      cocoChecker.handle((ASTMCGrammar) modelState.getRootNode());
      
      Log.getFindings().stream()
          .forEach(finding -> {
            finding.getSourcePosition().ifPresent(position -> {
              modelState.addAdditionalError(position, finding.getMsg());
            });
          });
    }
  }
}