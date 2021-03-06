/* (c) https://github.com/MontiCore/monticore */

package de.monticore.codegen.symboltable;

import java.io.File;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import de.monticore.generating.GeneratorEngine;
import de.monticore.generating.GeneratorSetup;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.grammar.grammar._ast.ASTGrammarReference;
import de.monticore.grammar.grammar._ast.ASTMCGrammar;
import de.monticore.grammar.symboltable.MCGrammarSymbol;
import de.monticore.grammar.symboltable.MCGrammarSymbolReference;
import de.monticore.grammar.symboltable.MCProdSymbol;
import de.monticore.io.paths.IterablePath;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.logging.Log;

import static com.google.common.collect.Lists.newArrayList;
import static de.monticore.codegen.GeneratorHelper.SCOPE;
import static de.se_rwth.commons.logging.Log.debug;

public class SymbolTableGenerator {

  public static final String PACKAGE = "_symboltable";

  public static final String LOG = SymbolTableGenerator.class.getSimpleName();

  private final ModelingLanguageGenerator modelingLanguageGenerator;

  private final ModelLoaderGenerator modelLoaderGenerator;

  private final ModelNameCalculatorGenerator modelNameCalculatorGenerator;

  private final ResolvingFilterGenerator resolvingFilterGenerator;

  private final SymbolGenerator symbolGenerator;

  private final SymbolKindGenerator symbolKindGenerator;

  private final ScopeSpanningSymbolGenerator scopeSpanningSymbolGenerator;

  private final ScopeGenerator scopeGenerator;

  private final SymbolReferenceGenerator symbolReferenceGenerator;

  private final SymbolTableCreatorGenerator symbolTableCreatorGenerator;

  private final ArtifactScopeSerializerGenerator symbolTableSerializationGenerator;
  
  private final SymbolInterfaceGenerator symbolInterfaceGenerator;

  protected SymbolTableGenerator(
      ModelingLanguageGenerator modelingLanguageGenerator,
      ModelLoaderGenerator modelLoaderGenerator,
      ModelNameCalculatorGenerator modelNameCalculatorGenerator,
      ResolvingFilterGenerator resolvingFilterGenerator,
      SymbolGenerator symbolGenerator,
      SymbolKindGenerator symbolKindGenerator,
      ScopeSpanningSymbolGenerator scopeSpanningSymbolGenerator,
      ScopeGenerator scopeGenerator,
      SymbolReferenceGenerator symbolReferenceGenerator,
      SymbolTableCreatorGenerator symbolTableCreatorGenerator,
      ArtifactScopeSerializerGenerator symbolTableSerializationGenerator,
      SymbolInterfaceGenerator symbolInterfaceGenerator) {
    this.modelingLanguageGenerator = modelingLanguageGenerator;
    this.modelLoaderGenerator = modelLoaderGenerator;
    this.modelNameCalculatorGenerator = modelNameCalculatorGenerator;
    this.resolvingFilterGenerator = resolvingFilterGenerator;
    this.symbolGenerator = symbolGenerator;
    this.symbolKindGenerator = symbolKindGenerator;
    this.scopeGenerator = scopeGenerator;
    this.scopeSpanningSymbolGenerator = scopeSpanningSymbolGenerator;
    this.symbolReferenceGenerator = symbolReferenceGenerator;
    this.symbolTableCreatorGenerator = symbolTableCreatorGenerator;
    this.symbolTableSerializationGenerator = symbolTableSerializationGenerator;
    this.symbolInterfaceGenerator = symbolInterfaceGenerator;
  }

  public void generate(GlobalExtensionManagement glex, ASTMCGrammar astGrammar, SymbolTableGeneratorHelper genHelper,
                       File outputPath, final IterablePath handCodedPath) {
    
    // Always set symbol table helper even if symbol table is not generated to
    // provide corresponding information and prevent wrong symbol assignments.
    glex.setGlobalValue("stHelper", genHelper);
    
    MCGrammarSymbol grammarSymbol = genHelper.getGrammarSymbol();

    // Skip generation if no rules are defined in the grammar, since no top asts
    // will be generated.
    if (!grammarSymbol.getStartProd().isPresent()) {
      return;
    }
    debug("Start symbol table generation for the grammar " + astGrammar.getName(), LOG);

    // TODO PN consider only class rules?
    final Collection<MCProdSymbol> allSymbolDefiningRules = genHelper.getAllSymbolDefiningRules();
    final Collection<MCProdSymbol> allSymbolDefiningRulesWithSuperGrammar = genHelper.getAllSymbolDefiningRulesInSuperGrammar();
    final Collection<MCProdSymbol> allScopeSpanningRules = genHelper.getAllScopeSpanningRules();
    Collection<String> ruleNames = newArrayList();
    Collection<String> ruleNamesWithSuperGrammar = newArrayList();
    for (MCProdSymbol prod : allSymbolDefiningRules) {
      ruleNames.add(prod.getSymbolDefinitionKind().isPresent() ? prod.getSymbolDefinitionKind().get() : prod.getName());
    }

    for (MCProdSymbol prod : genHelper.getAllSymbolDefiningRulesInSuperGrammar()) {
      ruleNamesWithSuperGrammar.add(genHelper.getQualifiedProdName(prod));
    }

    /* If no rules with name are defined, no symbols can be generated. Hence,
     * skip generation of: ModelNameCalculator, SymbolTableCreator, Symbol,
     * SymbolKind, SymbolReference, ScopeSpanningSymbol, (Spanned) Scope and
     * ResolvingFilter */
    final boolean skipSymbolTableGeneration = allSymbolDefiningRules.isEmpty() && allScopeSpanningRules.isEmpty();

    final GeneratorSetup setup = new GeneratorSetup();
    setup.setOutputDirectory(outputPath);
    glex.setGlobalValue("nameHelper", new Names());
    glex.setGlobalValue("skipSTGen", skipSymbolTableGeneration);
    setup.setGlex(glex);

    final GeneratorEngine genEngine = new GeneratorEngine(setup);

    modelingLanguageGenerator.generate(genEngine, genHelper, handCodedPath, grammarSymbol,
        ruleNamesWithSuperGrammar);
    modelLoaderGenerator.generate(genEngine, genHelper, handCodedPath, grammarSymbol);

    if (!skipSymbolTableGeneration) {
      modelNameCalculatorGenerator.generate(genEngine, genHelper, handCodedPath, grammarSymbol,
          ruleNames);
      symbolTableCreatorGenerator.generate(genEngine, genHelper, handCodedPath, grammarSymbol);
      symbolTableSerializationGenerator.generate(genEngine, genHelper, handCodedPath, grammarSymbol);

      for (MCProdSymbol ruleSymbol : allSymbolDefiningRules) {
        generateSymbolOrScopeSpanningSymbol(genEngine, genHelper, ruleSymbol, handCodedPath);
        symbolKindGenerator.generate(genEngine, genHelper, handCodedPath, ruleSymbol);
        symbolReferenceGenerator.generate(genEngine, genHelper, handCodedPath, ruleSymbol,
            genHelper.isScopeSpanningSymbol(ruleSymbol));
        resolvingFilterGenerator.generate(genEngine, genHelper, handCodedPath, ruleSymbol);
      }
    }
    //a symbol interface and scope is generated for all grammars
    symbolInterfaceGenerator.generate(genEngine, genHelper, handCodedPath, grammarSymbol);
    scopeGenerator.generate(genEngine, genHelper, handCodedPath, grammarSymbol.getName() + SCOPE, allSymbolDefiningRules, allSymbolDefiningRulesWithSuperGrammar);


    debug("End symbol table generation for the grammar " + astGrammar.getName(), LOG);
  }

  private void generateSymbolOrScopeSpanningSymbol(GeneratorEngine genEngine,
                                                   SymbolTableGeneratorHelper genHelper,
                                                   MCProdSymbol ruleSymbol, IterablePath handCodedPath) {
    if (ruleSymbol.getAstNode().isPresent()) {
      if (!genHelper.isScopeSpanningSymbol(ruleSymbol)) {
        symbolGenerator.generate(genEngine, genHelper, handCodedPath, ruleSymbol);
      } else {
        scopeSpanningSymbolGenerator.generate(genEngine, genHelper, handCodedPath, ruleSymbol);
      }
    }
  }

}
