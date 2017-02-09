<#--
***************************************************************************************
Copyright (c) 2016, MontiCore
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its contributors
may be used to endorse or promote products derived from this software
without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.
***************************************************************************************
-->
${signature("ruleSymbol")}

<#assign ruleName = ruleSymbol.getName()>
<#assign genHelper = glex.getGlobalVar("stHelper")>
<#assign fqn = genHelper.getQualifiedGrammarName()?lower_case>
<#assign astPrefix = fqn + "._ast.AST">

  @Override
  public void visit(${astPrefix}${ruleName} ast) {
    MutableScope scope = create_${ruleName}(ast);
    initialize_${ruleName}(scope, ast);
    putOnStack(scope);
    setLinkBetweenSpannedScopeAndNode(scope, ast);
  }

  protected MutableScope create_${ruleName}(${astPrefix}${ruleName} ast) {
  <#if !genHelper.isNamed(ruleSymbol)>
    // creates new visibility scope
    return new de.monticore.symboltable.CommonScope(false);
  <#else>
    // creates new shadowing scope
    return new de.monticore.symboltable.CommonScope(true);
  </#if>
  }

  protected void initialize_${ruleName}(MutableScope scope, ${astPrefix}${ruleName} ast) {
  <#if !genHelper.isNamed(ruleSymbol)>
    // e.g., scope.setName(ast.getName())
  <#else>
    scope.setName(ast.getName());
  </#if>
  }

  ${includeArgs("symboltable.symboltablecreators.EndVisitMethod", ruleSymbol)}