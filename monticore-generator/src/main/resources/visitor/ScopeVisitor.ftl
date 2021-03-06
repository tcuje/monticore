<#-- (c) https://github.com/MontiCore/monticore -->
${tc.signature("astType", "symbolTablePackage", "cd", "existsST")}
<#assign genHelper = glex.getGlobalVar("visitorHelper")>

<#-- Copyright -->
${defineHookPoint("JavaCopyright")}

<#-- set package -->
package ${genHelper.getVisitorPackage()};

<#if existsST>
import ${symbolTablePackage}.*;
</#if>
import de.monticore.symboltable.Symbol;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.MutableScope;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Default scope-visitor for the {@code ${genHelper.getCdName()}} language.<br/>
 * <br/>
 * <b>Running a visitor</b>: Starting a traversal of a scope is as simple as calling {@code handle(scope)}. Note that the visitor only handles nodes of language {@code ${genHelper.getCdName()}}.<br/>
 * <br/>
 * <b>Implementing a visitor:</b><br/>
 * You should never use {@code this}, but always make use of {@link #getRealThis()}. This ensures that the visitor can be reused by composition.<br/>
 * <br/>
 * <ul>
 *   <li><b>Visiting nodes</b>: You may override the {@code visit(node)} and {@code endVisit(node)} methods to do something at specific scope nodes.<br/><br/></li>
 *   <li><b>Traversal</b>: You may override the {@code traverse(node)} methods, if you want to change the climb down strategy for traversing children (e.g. ordering the children). Be aware of the underlying double-dispatch mechanism: probably you want to call {@code childNode.accept(getRealThis())} and <b>not</b> {@code handle(childNode)}<br/><br/></li>
 *   <li><b>Handling of nodes</b>: You may override the {@code handle(node)} methods, if you want to change its default implementation (depth-first iteration): {@code visit(node); traverse(node); endVisit(node);}<br/><br/></li>
 * </ul>
 */
public interface ${genHelper.getScopeVisitorType()} extends ${genHelper.getSymbolVisitorType()} { 

  /**
   * Sets the visitor to use for handling and traversing nodes.
   * This method is not implemented by default and visitors intended for reusage
   * in other languages should override this method together with
   * {@link #getRealThis()} to make a visitor composable.
   * RealThis is used to allow visitor composition, where a delegating visitor
   * utilizes this setter to set another visitor as the handle/traversal
   * controller. If this method is not overridden by the language developer,
   * the visitor still can be reused, by implementing this method in a
   * decorator.
   * @param realThis the real instance to use for handling and traversing nodes.
   * @see ${genHelper.getCdName()}DelegatorVisitor 
   */
  default public void setRealThis(${genHelper.getScopeVisitorType()} realThis) {
    throw new UnsupportedOperationException("0xA7011${genHelper.getGeneratedErrorCode(ast)} The setter for realThis is not implemented. You might want to implement a wrapper class to allow setting/getting realThis.");
  }

  /**
   * By default this method returns {@code this}. Visitors intended for reusage
   * in other languages should override this method together with
   * {@link #setRealThis(${genHelper.getVisitorType()})} to make a visitor
   * composable.
   * See {@link #setRealThis(${genHelper.getVisitorType()})} for more information.
   * @see #setRealThis(${genHelper.getVisitorType()})
   * @see ${genHelper.getCdName()}DelegatorVisitor
   */
  default public ${genHelper.getScopeVisitorType()} getRealThis() {
    return this;
  }
  
  /* ------------------------------------------------------------------------*/
  
  /**
   * By default this method is not called, because the default visitor only
   * visits a scope in its dynamic runtime type. Use an InheritanceVisitor
   * if you want to visit a node in its super types as well.
   *
   * @param scope the scope that is entered 
   */
  default public void visit(Scope scope) {
  }

  /**
   * By default this method is not called, because the default visitor only
   * visits a scope in its dynamic runtime type. Use an InheritanceVisitor
   * if you want to visit a node in its super types as well.
   *
   * @param scope the scope that is left 
   */
  default public void endVisit(Scope scope) {
  }
  
  /* ------------------------------------------------------------------------*/
  
  
  <#if existsST && glex.getGlobalVar("stHelper")?has_content>
    <#assign stHelper = glex.getGlobalVar("stHelper")>
    <#assign scopeType = astType.getName() + "Scope">
  
      default public void visit(${scopeType} scope) {}
      
      default public void endVisit(${scopeType} scope) {}
      
      default public void handle(${scopeType} scope) {
        getRealThis().visit(scope);
        getRealThis().traverse(scope);
        getRealThis().endVisit(scope);
      }
  
    default public void traverse(${scopeType} scope) {
      // traverse symbols within the scope
      Iterator<Entry<String, Collection<Symbol>>> iter_symbols = scope.getLocalSymbols().entrySet().iterator();
      while (iter_symbols.hasNext()) {
        iter_symbols.next().getValue().forEach(s -> ((${"ICommon" + stHelper.getGrammarSymbol().getName() + "Symbol"})s).accept(getRealThis()));
      }
      
      // traverse sub-scopes
      Iterator<MutableScope> iter_scopes = scope.getSubScopes().iterator();
      while (iter_scopes.hasNext()) {
        ((${"I" + stHelper.getGrammarSymbol().getName() + "Scope"})iter_scopes.next()).accept(getRealThis());
      }
    }
  </#if>

}
