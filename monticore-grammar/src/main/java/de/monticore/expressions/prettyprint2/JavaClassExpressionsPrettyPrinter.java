/* (c) https://github.com/MontiCore/monticore */
package de.monticore.expressions.prettyprint2;

import de.monticore.expressions.commonexpressions._ast.ASTArguments;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.javaclassexpressions._ast.*;
import de.monticore.expressions.javaclassexpressions._visitor.JavaClassExpressionsVisitor;
import de.monticore.prettyprint.CommentPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;

public class JavaClassExpressionsPrettyPrinter implements JavaClassExpressionsVisitor {
  
 protected JavaClassExpressionsVisitor realThis;
  
  protected IndentPrinter printer;
  
  public JavaClassExpressionsPrettyPrinter(IndentPrinter printer) {
    this.printer=printer;
    realThis = this;

  }

  @Override
  public void visit(ASTPrimarySuperExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("super");
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }
  
  @Override
  public void handle(ASTSuperSuffix node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    if (node.isPresentName()) {
      getPrinter().print(".");
      if (node.isPresentExtTypeArguments()) {
        node.getExtTypeArguments().accept(getRealThis());
      }
      getPrinter().print(node.getName());
      if (node.isPresentArguments()) {
        node.getArguments().accept(getRealThis());
      }
    }
    else {
      node.getArguments().accept(getRealThis());
    }
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }
  
  @Override
  public void handle(ASTSuperExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getExpression().accept(getRealThis());
    getPrinter().print(".super");
    node.getSuperSuffix().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }
  
  @Override
  public void handle(ASTClassExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getExtReturnType().accept(getRealThis());
    getPrinter().print(".class");
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }
  
  @Override
  public void handle(ASTTypeCastExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("(");
    node.getExtType().accept(getRealThis());
    getPrinter().print(")");
    node.getExpression().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }
  
  @Override
  public void handle(ASTGenericInvocationSuffix node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    if(node.isPresentSuperSuffix()) {
      if (node.isSuper()) {
        getPrinter().print("super");
      }
      node.getSuperSuffix().accept(getRealThis());
    }else if(node.isPresentName()) {
      getPrinter().print(node.getName());
      node.getArguments().accept(getRealThis());
    }else {
      if (node.isThis()) {
        getPrinter().print("this");
      }
      node.getArguments().accept(getRealThis());
      CommentPrettyPrinter.printPostComments(node, getPrinter());
    }
    
  }
  
  @Override
  public void handle(ASTGenericInvocationExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getExpression().accept(getRealThis());
    getPrinter().print(".");
    handle(node.getPrimaryGenericInvocationExpression());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }
  
  @Override
  public void handle(ASTNameExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
   getPrinter().print(node.getName());
   CommentPrettyPrinter.printPostComments(node, getPrinter());
  }
  
  @Override
  public void handle(ASTInstanceofExpression node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    node.getExpression().accept(getRealThis());
    getPrinter().print(" instanceof ");
    node.getExtType().accept(getRealThis());
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }
  
  @Override
  public void handle(ASTArguments node) {
    CommentPrettyPrinter.printPreComments(node, getPrinter());
    getPrinter().print("(");
    int count = 0;
    if (!node.isEmptyExpressions()) {
      for (ASTExpression ast : node.getExpressionList()) {
        if (count > 0) {
          getPrinter().print(",");
        }
        ast.accept(getRealThis());
        count++;
      }
    }
    getPrinter().print(")");
    CommentPrettyPrinter.printPostComments(node, getPrinter());
  }
  
  public IndentPrinter getPrinter() {
    return this.printer;
  }
  
  public String prettyprint(ASTExpression node) {
    getPrinter().clearBuffer();
    node.accept(getRealThis());
    return getPrinter().getContent();
  }
  
  public String prettyprint(ASTGenericInvocationSuffix node) {
    getPrinter().clearBuffer();
    node.accept(getRealThis());
    return getPrinter().getContent();
  }
  
  public String prettyprint(ASTSuperSuffix node) {
    getPrinter().clearBuffer();
    node.accept(getRealThis());
    return getPrinter().getContent();
  }
  
  @Override
  public void setRealThis(JavaClassExpressionsVisitor realThis) {
    this.realThis = realThis;
  }
  
  @Override
  public JavaClassExpressionsVisitor getRealThis() {
    return realThis;
  }
  
}
