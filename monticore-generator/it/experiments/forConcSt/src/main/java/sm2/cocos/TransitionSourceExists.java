/* (c) Monticore license: https://github.com/MontiCore/monticore */
package sm2.cocos;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Optional;

import sm2._ast.ASTTransition;
import sm2._cocos.SM2ASTTransitionCoCo;
import sm2._symboltable.StateSymbol;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.logging.Log;

public class TransitionSourceExists implements SM2ASTTransitionCoCo {
  
  @Override
  public void check(ASTTransition node) {
    checkArgument(node.isPresentEnclosingScope());
    
    Scope enclosingScope = node.getEnclosingScope();
    Optional<StateSymbol> sourceState = enclosingScope.resolve(node.getFrom(), StateSymbol.KIND);
    
    if (!sourceState.isPresent()) {
      // Issue error...
      Log.error("0xAUT03 The source state of the transition does not exist.",
          node.get_SourcePositionStart());
    }
  }
}
