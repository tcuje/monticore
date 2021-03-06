/* (c) https://github.com/MontiCore/monticore */

package de.monticore.grammar.symboltable;

import de.monticore.ast.ASTNode;
import de.monticore.grammar.Multiplicity;
import de.monticore.grammar.grammar._ast.*;
import de.monticore.grammar.grammar_withconcepts._visitor.Grammar_WithConceptsVisitor;
import de.monticore.grammar.prettyprint.Grammar_WithConceptsPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.symboltable.*;

import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static de.monticore.codegen.mc2cd.MCGrammarSymbolTableHelper.*;
import static de.monticore.grammar.HelperGrammar.findImplicitTypes;
import static de.monticore.grammar.Multiplicity.*;
import static de.monticore.grammar.grammar._ast.GrammarMill.symbolDefinitionBuilder;
import static de.se_rwth.commons.Names.getQualifiedName;
import static de.se_rwth.commons.StringTransformations.uncapitalize;
import static de.se_rwth.commons.logging.Log.*;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class MontiCoreGrammarSymbolTableCreator extends CommonSymbolTableCreator
        implements Grammar_WithConceptsVisitor {

  private String packageName = "";

  private MCGrammarSymbol grammarSymbol;

  private ASTMCGrammar astGrammar;

  public MontiCoreGrammarSymbolTableCreator(
          ResolvingConfiguration resolvingConfig,
          MutableScope enclosingScope) {
    super(resolvingConfig, enclosingScope);
  }

  /**
   * Creates the symbol table starting from the <code>rootNode</code> and
   * returns the first scope that was created.
   *
   * @param rootNode the root node
   * @return the first scope that was created
   */
  public Scope createFromAST(ASTMCGrammar rootNode) {
    errorIfNull(rootNode);
    handle(rootNode);
    return getFirstCreatedScope();
  }

  // PN is tested
  @Override
  public void visit(ASTMCGrammar astGrammar) {
    debug("Building Symboltable for Grammar: " + astGrammar.getName(),
            MontiCoreGrammarSymbolTableCreator.class.getSimpleName());

    packageName = getQualifiedName(astGrammar.getPackageList());
    this.astGrammar = astGrammar;

    final List<ImportStatement> imports = new ArrayList<>();
    if (astGrammar.getImportStatementList() != null) {
      for (ASTMCImportStatement imp : astGrammar.getImportStatementList()) {
        imports.add(new ImportStatement(getQualifiedName(imp.getImportList()),
                imp.isStar()));
      }
    }

    final ArtifactScope scope = new ArtifactScope(empty(), packageName, imports);
    putOnStack(scope);

    grammarSymbol = new MCGrammarSymbol(astGrammar.getName());
    grammarSymbol.setComponent(astGrammar.isComponent());

    addToScopeAndLinkWithNode(grammarSymbol, astGrammar);

    addSuperGrammars(astGrammar, grammarSymbol);
  }

  @Override
  public void endVisit(ASTMCGrammar astGrammar) {

    setComponentsCardinality();

    setEnclosingScopeOfNodes(astGrammar);

    computeStartParserProd(astGrammar);

    setSymbolRule();

    // remove grammar scope
    removeCurrentScope();

    // remove artifact scope
    removeCurrentScope();
  }

  private void setSymbolRule() {
    for (ASTSymbolRule symbolRule : astGrammar.getSymbolRuleList()) {
      Optional<MCProdSymbol> prod = grammarSymbol.getProd(symbolRule.getType());
      if (prod.isPresent() && !prod.get().isSymbolDefinition()) {
        ASTSymbolDefinition symbolDefinition = symbolDefinitionBuilder().setGenSymbol(true).build();
        setSymbolDefinition(prod.get(), newArrayList(symbolDefinition));
      }
    }
  }

  @Override
  public void visit(ASTInterfaceProd ast) {
    MCProdSymbol prodSymbol = new MCProdSymbol(ast.getName());
    prodSymbol.setInterface(true);

    setSymbolDefinition(prodSymbol, ast.getSymbolDefinitionList());

    setSuperProdsAndTypes(prodSymbol, emptyList(),
            emptyList(), ast.getSuperInterfaceRuleList(), ast.getASTSuperInterfaceList());

    addToScopeAndLinkWithNode(prodSymbol, ast);
  }

  @Override
  public void endVisit(ASTInterfaceProd astInterfaceProd) {
    removeCurrentScope();
  }

  @Override
  public void endVisit(ASTConstantGroup astConstantGroup) {
    removeCurrentScope();
  }

  @Override
  public void visit(ASTLexProd ast) {
    final MCProdSymbol prodSymbol = new MCProdSymbol(ast.getName());
    prodSymbol.setLexerProd(true);

    addToScopeAndLinkWithNode(prodSymbol, ast);
  }

  @Override
  public void endVisit(ASTLexProd astLexProd) {
    removeCurrentScope();
  }

  @Override
  public void visit(ASTClassProd ast) {
    final MCProdSymbol prodSymbol = new MCProdSymbol(ast.getName());

    setSymbolDefinition(prodSymbol, ast.getSymbolDefinitionList());

    setSuperProdsAndTypes(prodSymbol, ast.getSuperRuleList(),
            ast.getASTSuperClassList(), ast.getSuperInterfaceRuleList(), ast.getASTSuperInterfaceList());

    addToScopeAndLinkWithNode(prodSymbol, ast);
  }

  public void endVisit(ASTClassProd astClassProd) {
    removeCurrentScope();
  }

  @Override
  public void visit(ASTAbstractProd ast) {
    MCProdSymbol prodSymbol = new MCProdSymbol(ast.getName());
    prodSymbol.setAbstract(true);

    setSymbolDefinition(prodSymbol, ast.getSymbolDefinitionList());

    setSuperProdsAndTypes(prodSymbol, ast.getSuperRuleList(),
            ast.getASTSuperClassList(), ast.getSuperInterfaceRuleList(), ast.getASTSuperInterfaceList());

    addToScopeAndLinkWithNode(prodSymbol, ast);
  }

  @Override
  public void endVisit(ASTAbstractProd astAbstractProd) {
    removeCurrentScope();
  }

  @Override
  public void visit(ASTExternalProd ast) {
    final MCProdSymbol prodSymbol = new MCProdSymbol(ast.getName());
    prodSymbol.setExternal(true);

    setSymbolDefinition(prodSymbol, ast.getSymbolDefinitionList());


    addToScopeAndLinkWithNode(prodSymbol, ast);
  }

  @Override
  public void endVisit(ASTExternalProd astExternalProd) {
    removeCurrentScope();
  }

  @Override
  public void visit(ASTEnumProd ast) {
    final MCProdSymbol prodSymbol = new MCProdSymbol(ast.getName());
    prodSymbol.setEnum(true);

    addToScopeAndLinkWithNode(prodSymbol, ast);
  }

  @Override
  public void endVisit(ASTEnumProd astEnumProd) {
    removeCurrentScope();
  }

  @Override
  public void visit(ASTTerminal ast) {
    final String usageName = ast.getUsageNameOpt().orElse(null);
    final Symbol currentSymbol = currentSymbol().orElse(null);

    if (currentSymbol != null) {
      final String symbolName = isNullOrEmpty(usageName) ? ast.getName() : usageName;
      MCProdComponentSymbol prodComponent = new MCProdComponentSymbol(symbolName);

      prodComponent.setUsageName(usageName);

      if (currentSymbol instanceof MCProdSymbol) {
        MCProdSymbol surroundingProd = (MCProdSymbol) currentSymbol;
        setComponentMultiplicity(prodComponent, ast);
        prodComponent = surroundingProd.addProdComponent(prodComponent);
      } else {
        addToScope(prodComponent);
      }

      setLinkBetweenSymbolAndNode(prodComponent, ast);
      prodComponent.setTerminal(true);
      setComponentMultiplicity(prodComponent, ast);
    }
  }

  @Override
  public void visit(ASTNonTerminal ast) {
    final String usageName = ast.getUsageNameOpt().orElse(null);
    final Symbol currentSymbol = currentSymbol().orElse(null);

    if (currentSymbol != null) {
      final String symbolName = isNullOrEmpty(usageName) ? ast.getName() : usageName;
      MCProdComponentSymbol prodComponent = new MCProdComponentSymbol(symbolName);

      prodComponent.setUsageName(usageName);
      MCProdSymbolReference symRef = new MCProdSymbolReference(ast.getName(),
              currentScope().orElse(null));
      prodComponent.setReferencedProd(symRef);

      if (currentSymbol instanceof MCProdSymbol) {
        MCProdSymbol surroundingProd = (MCProdSymbol) currentSymbol;

        MCProdComponentSymbol prevProdComp = surroundingProd
                .getProdComponent(prodComponent.getName()).orElse(null);

        Optional<MCProdSymbol> byReference = resolveRule(astGrammar, ast.getName());
        if (!byReference.isPresent() || !byReference.get().isLexerProd()) {

          if (prevProdComp != null && prevProdComp.getReferencedProd().isPresent()) {
            boolean sameType = prevProdComp.getReferencedProd().get().getName()
                    .equals(ast.getName());
            if (!sameType) {
              boolean subType = isSubType(prevProdComp.getReferencedProd().get(),
                      symRef)
                      || isSubType(symRef, prevProdComp.getReferencedProd().get());
              if (!subType) {
                error("0xA4077 The production " + surroundingProd.getName()
                        + " must not use the attribute name " + symbolName +
                        " for different nonterminals.");
              }
            }
          }
        }
        prodComponent = surroundingProd.addProdComponent(prodComponent);
      } else {
        addToScope(prodComponent);
      }

      setLinkBetweenSymbolAndNode(prodComponent, ast);
      prodComponent.setNonterminal(true);
      prodComponent.setReferencedSymbolName(ast.getReferencedSymbolOpt().orElse(""));
    }
  }

  @Override
  public void visit(ASTASTRule ast) {
    final Optional<MCProdSymbol> prodSymbol = grammarSymbol.getProdWithInherited(ast.getType());
    if (!prodSymbol.isPresent()) {
      error(
              "0xA4076 There must not exist an AST rule for the nonterminal " + ast.getType()
                      + " because there exists no production defining " + ast.getType(),
              ast.get_SourcePositionStart());
    }
    ast.getAdditionalAttributeList().forEach(a -> addAttributeInAST(prodSymbol.get(), a));
  }

  void setComponentMultiplicity(MCProdComponentSymbol prod, ASTNode ast) {
    Multiplicity multiplicity = determineMultiplicity(astGrammar, ast);
    if (multiplicity == LIST) {
      prod.setList(true);
    } else if (multiplicity == OPTIONAL) {
      prod.setOptional(true);
    }
  }

  @Override
  public void visit(ASTLexNonTerminal astNode) {
    final Optional<MCProdComponentSymbol> sym = addRuleComponent(nullToEmpty(astNode.getName()),
            astNode, "");

    if (sym.isPresent()) {
      sym.get().setLexerNonterminal(true);
    }
  }

  @Override
  public void visit(ASTConstantGroup astNode) {
    Optional<String> attrName = getConstantName(astNode,
            currentSymbol());

    final String usageName = astNode.getUsageNameOpt().orElse(null);
    final Symbol currentSymbol = currentSymbol().orElse(null);

    if (currentSymbol != null && attrName.isPresent()) {
      MCProdComponentSymbol prodComponent = new MCProdComponentSymbol(attrName.get());
      prodComponent.setConstantGroup(true);
      prodComponent.setUsageName(usageName);

      if (currentSymbol instanceof MCProdSymbol) {
        MCProdSymbol surroundingProd = (MCProdSymbol) currentSymbol;
        final String symbolName = isNullOrEmpty(usageName)
                ? attrName.get()
                : usageName;
        Optional<MCProdComponentSymbol> prevProdComp = surroundingProd
                .getProdComponent(symbolName);

        if (prevProdComp.isPresent() && !prevProdComp.get().isConstantGroup()) {
          error("0xA4006 The production " + surroundingProd.getName()
                  + " must not use the attribute name " + attrName.get() +
                  " for constant group and nonterminals.");
        }
        if (prevProdComp.isPresent()) {
          prodComponent = prevProdComp.get();
          prodComponent.setList(true);
          setLinkBetweenSymbolAndNode(prodComponent, astNode);
          putSpannedScopeOnStack(prodComponent);
        } else {
          final Optional<MCProdComponentSymbol> sym = addRuleComponent(attrName.orElse(""),
                  astNode, astNode.getUsageNameOpt().orElse(null));
          if (sym.isPresent()) {
            sym.get().setConstantGroup(true);
            addToScopeAndLinkWithNode(sym.get(), astNode);
          }
        }
      }
    }
  }

  @Override
  public void visit(ASTConstant astNode) {
    final Symbol currentSymbol = currentSymbol().orElse(null);
    if (currentSymbol != null) {
      final String symbolName = astNode.isPresentHumanName()
              ? astNode.getHumanName()
              : astNode.getName();
      MCProdComponentSymbol prodComponent = new MCProdComponentSymbol(symbolName);
      prodComponent.setConstant(true);
      prodComponent.setUsageName(astNode.getHumanNameOpt().orElse(null));

      if (currentSymbol instanceof MCProdSymbol) {
        MCProdSymbol surroundingProd = (MCProdSymbol) currentSymbol;
        prodComponent = surroundingProd.addProdComponent(prodComponent);
      } else if (currentSymbol instanceof MCProdComponentSymbol) {
        MCProdComponentSymbol surroundingProd = (MCProdComponentSymbol) currentSymbol;
        surroundingProd.addSubProdComponent(prodComponent);
      } else {
        addToScope(prodComponent);
      }
      setLinkBetweenSymbolAndNode(prodComponent, astNode);
    }
  }

  /**
   * Create entry for an implicit rule defined in another lexrule by using an
   * action and changing the type of the token
   */
  @Override
  public void visit(ASTLexActionOrPredicate action) {
    Grammar_WithConceptsPrettyPrinter prettyPrinter = new Grammar_WithConceptsPrettyPrinter(new IndentPrinter());
    for (String typeName : findImplicitTypes(action, prettyPrinter)) {
      // Create rule if needed
      Optional<MCProdSymbol> rule = grammarSymbol.getProd(typeName);
      if (!rule.isPresent()) {
        // Create entry for an implicit rule
        final MCProdSymbol prodSymbol = new MCProdSymbol(typeName);
        prodSymbol.setLexerProd(true);
      }
    }
  }

  private Optional<MCProdComponentSymbol> addRuleComponent(String name, ASTNode node,
                                                           String usageName) {
    final Symbol currentSymbol = currentSymbol().orElse(null);

    if (currentSymbol != null) {
      final String symbolName = isNullOrEmpty(usageName) ? name : usageName;
      MCProdComponentSymbol prodComponent = new MCProdComponentSymbol(symbolName);

      prodComponent.setUsageName(usageName);

      if (currentSymbol instanceof MCProdSymbol) {
        MCProdSymbol surroundingProd = (MCProdSymbol) currentSymbol;
        prodComponent = surroundingProd.addProdComponent(prodComponent);
      } else {
        addToScope(prodComponent);
      }
      setLinkBetweenSymbolAndNode(prodComponent, node);
      return of(prodComponent);
    }
    return empty();

  }

  private void addSuperGrammars(ASTMCGrammar astGrammar, MCGrammarSymbol grammarSymbol) {
    for (ASTGrammarReference ref : astGrammar.getSupergrammarList()) {
      final String superGrammarName = getQualifiedName(ref.getNameList());

      final MCGrammarSymbolReference superGrammar = new MCGrammarSymbolReference(
              superGrammarName, currentScope().orElse(null));

      grammarSymbol.addSuperGrammar(superGrammar);
    }
  }

  private void setSuperProdsAndTypes(MCProdSymbol prodSymbol, List<ASTRuleReference> superProds,
                                     List<ASTGenericType> astSuperClasses, List<ASTRuleReference> superInterfaceProds,
                                     List<ASTGenericType> astSuperInterfaces) {
    final Scope enclosingScope = currentScope().get();

    // A extends B
    for (ASTRuleReference astSuperProd : superProds) {
      MCProdSymbolReference superProd = new MCProdSymbolReference(astSuperProd.getTypeName(),
              enclosingScope);
      prodSymbol.addSuperProd(superProd);
    }

    // A astextends B
    for (ASTGenericType astSuperClass : astSuperClasses) {
      MCProdOrTypeReference superClass = new MCProdOrTypeReference(astSuperClass.getTypeName(),
              enclosingScope);
      prodSymbol.addAstSuperClass(superClass);
    }

    // A implements B
    for (ASTRuleReference astInterface : superInterfaceProds) {
      MCProdSymbolReference superProd = new MCProdSymbolReference(astInterface.getTypeName(),
              enclosingScope);
      prodSymbol.addSuperInterfaceProd(superProd);
    }

    // A astimplements B
    for (ASTGenericType astInterface : astSuperInterfaces) {
      MCProdOrTypeReference superClass = new MCProdOrTypeReference(astInterface.getTypeName(),
              enclosingScope);
      prodSymbol.addAstSuperInterface(superClass);
    }
  }

  /**
   * Set cardinality of all grammar's nonterminals
   */
  private void setComponentsCardinality() {
    for (MCProdSymbol prodSymbol : grammarSymbol.getProdsWithInherited().values()) {
      Collection<MCProdAttributeSymbol> astAttributes = prodSymbol.getProdAttributes();
      for (MCProdComponentSymbol component : prodSymbol.getProdComponents()) {
        if (component.isNonterminal()) {
          setComponentMultiplicity(component, component.getAstNode().get());
          Optional<MCProdAttributeSymbol> attribute = astAttributes.stream()
                  .filter(a -> a.getName().equals(component.getName())).findAny();
          if (attribute.isPresent()) {
            Multiplicity multiplicity = multiplicityOfAttributeInAST(
                    (ASTAdditionalAttribute) attribute.get().getAstNode().get());
            component.setList(multiplicity == LIST);
            component.setOptional(multiplicity == OPTIONAL);
          }
        }
      }
    }
  }

  private void setSymbolDefinition(MCProdSymbol prodSymbol,
                                   List<ASTSymbolDefinition> listOfDefs) {
    for (ASTSymbolDefinition symbolDefinition : listOfDefs) {
      if (symbolDefinition.isGenSymbol()) {
        String symbolKindName = prodSymbol.getName();

        if (symbolDefinition.isPresentSymbolName()
                && !symbolDefinition.getSymbolName().isEmpty()) {
          symbolKindName = symbolDefinition.getSymbolName();
        }
        prodSymbol.setProdDefiningSymbolKind(symbolKindName);
      }
      if (symbolDefinition.isGenScope()) {
        prodSymbol.setScopeDefinition(symbolDefinition.isGenScope());
      }
    }
  }

  private void computeStartParserProd(ASTMCGrammar astGrammar) {
    if (astGrammar.getStartRulesOpt().isPresent()) {
      String name = astGrammar.getStartRules().getName();
      Optional<MCProdSymbol> prod = grammarSymbol.getProdWithInherited(name);
      if (!prod.isPresent()) {
        error("0xA0243 Rule " + name + " couldn't be found!");
      } else {
        prod.get().setStartProd(true);
        grammarSymbol.setStartProd(prod.get());
      }
    } else {
      final Set<ASTProd> firstProductions = newLinkedHashSet();
      // The start rule for parsing is the first occurring Interface-, Abstract-
      // or Class-Production in this grammar
      if (astGrammar.getClassProdList().size() != 0) {
        firstProductions.add(astGrammar.getClassProdList().get(0));
      }
      if (astGrammar.getInterfaceProdList().size() != 0) {
        firstProductions.add(astGrammar.getInterfaceProdList().get(0));
      }
      if (astGrammar.getAbstractProdList().size() != 0) {
        firstProductions.add(astGrammar.getAbstractProdList().get(0));
      }
      setStartProd(firstProductions);
    }
  }

  /**
   * Set start parser production
   */
  private void setStartProd(Set<ASTProd> firstProductions) {
    // Set start parser rule
    ASTProd firstProduction = null;
    for (ASTProd prod : firstProductions) {
      // TODO: add a common interface to the MC grammar for all these
      // productions and remove this hack
      if ((firstProduction == null)
              || (firstProduction.get_SourcePositionStart()
              .compareTo(prod.get_SourcePositionStart()) > 0)) {
        firstProduction = prod;
      }
    }

    if (firstProduction != null) {
      Optional<MCProdSymbol> prod = grammarSymbol.getProdWithInherited(firstProduction.getName());
      if (!prod.isPresent()) {
        error("0xA2174 Prod " + firstProduction.getName() + " couldn't be found! Pos: "
                + firstProduction.get_SourcePositionStart());
      } else {
        prod.get().setStartProd(true);
        grammarSymbol.setStartProd(prod.get());
      }
    }
  }


  /**
   * @param mcProdSymbol
   * @param astAttribute
   */
  private void addAttributeInAST(MCProdSymbol mcProdSymbol, ASTAdditionalAttribute astAttribute) {
    String attributeName = astAttribute.getNameOpt()
            .orElse(uncapitalize(astAttribute.getGenericType().getTypeName()));

    MCProdAttributeSymbol astAttributeSymbol = new MCProdAttributeSymbol(attributeName);
    MCProdOrTypeReference attributeType = new MCProdOrTypeReference(
            astAttribute.getGenericType().getTypeName(), mcProdSymbol.getSpannedScope());
    astAttributeSymbol.setTypeReference(attributeType);

    mcProdSymbol.addProdAttribute(astAttributeSymbol);
    //
    // Optional<MCProdComponentSymbol> mcComponent =
    // mcProdSymbol.getProdComponent(attributeName);
    // astAttributeSymbol.setReferencedComponent(mcComponent);

    setLinkBetweenSymbolAndNode(astAttributeSymbol, astAttribute);

  }

}
