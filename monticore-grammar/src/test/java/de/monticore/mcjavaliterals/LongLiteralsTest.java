/* (c) https://github.com/MontiCore/monticore */

package de.monticore.mcjavaliterals;

import de.monticore.mcbasicliterals._ast.ASTLiteral;
import de.monticore.mcjavaliterals._ast.ASTLongLiteral;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.LogStub;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class LongLiteralsTest {


  @BeforeClass
  public static void init() {
    LogStub.init();
    Log.enableFailQuick(false);
  }

  private void checkLongLiteral(long l, String s) throws IOException {
    ASTLiteral lit = MCLiteralsTestHelper.getInstance().parseLiteral(s);
    assertTrue(lit instanceof ASTLongLiteral);
    assertEquals(l, ((ASTLongLiteral) lit).getValue());
    
  }
  
  @Test
  public void testLongLiterals() {
    try {
      // decimal number
      checkLongLiteral(0L, "0L");
      checkLongLiteral(123L, "123L");
      checkLongLiteral(10L, "10L");
      checkLongLiteral(5L, "5L");
      
      // hexadezimal number
      checkLongLiteral(0x12L, "0x12L");
      checkLongLiteral(0XeffL, "0XeffL");
      checkLongLiteral(0x1234567890L, "0x1234567890L");
      checkLongLiteral(0xabcdefL, "0xabcdefL");
      checkLongLiteral(0x0L, "0x0L");
      checkLongLiteral(0xaL, "0xaL");
      checkLongLiteral(0xC0FFEEL, "0xC0FFEEL");
      checkLongLiteral(0x005fL, "0x005fL");
      
      // octal number
      checkLongLiteral(02L, "02L");
      checkLongLiteral(07L, "07L");
      checkLongLiteral(00L, "00L");
      checkLongLiteral(076543210L, "076543210L");
      checkLongLiteral(00017L, "00017L");
    }
    catch (IOException e) {
      fail(e.getMessage());
    }
  }
}
