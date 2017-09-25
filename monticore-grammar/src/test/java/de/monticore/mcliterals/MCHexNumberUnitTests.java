/*
 * ******************************************************************************
 * MontiCore Language Workbench, www.monticore.de
 * Copyright (c) 2017, MontiCore, All rights reserved.
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
 */
package de.monticore.mcliterals;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Optional;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.monticore.mchexnumbers._ast.ASTHexInteger;
import de.monticore.mchexnumbers._ast.ASTHexadecimal;
import de.monticore.mcnumbers._ast.ASTDecimal;
import de.monticore.mcnumbers._ast.ASTInteger;
import de.monticore.mcnumbers._ast.ASTNumber;
import de.monticore.testmcliteralsv3._ast.ASTAnyToken;
import de.monticore.testmcliteralsv3._ast.ASTAnyTokenList;
import de.monticore.testmcliteralsv3._ast.ASTIntegerList;
import de.monticore.testmcliteralsv3._ast.ASTNumberList;
import de.monticore.testmcliteralsv3._parser.TestMCLiteralsV3Parser;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.LogStub;


public class MCHexNumberUnitTests {
    
  // setup the language infrastructure
  TestMCLiteralsV3Parser parser = new TestMCLiteralsV3Parser() ;
  
  @BeforeClass
  public static void init() {
    // replace log by a sideffect free variant
    LogStub.init();
  }
  

  @Before
  public void setUp() { 
    Log.getFindings().clear();
  }
  
  // --------------------------------------------------------------------
  // Numbers: Hexadecimal Encodings
  // --------------------------------------------------------------------

  // --------------------------------------------------------------------
  @Test
  public void testHex1() throws IOException {
    Optional<ASTNumber> os = parser.parseString_Number( " 0x34 " );
    assertEquals(true, os.isPresent());
    assertEquals(0x34, os.get().getValue());
    assertEquals("0x34", os.get().getSource());
  }

  // --------------------------------------------------------------------
  @Test
  public void testNumbers() throws IOException {
    ASTNumberList ast = parser.parseString_NumberList(
    	"[463 0x23 - 0x0045  -0X3AFFA-27 0x3affa-0xFEDBCAabcdef]" ).get();
    assertEquals(7, ast.getNumbers().size());

    ASTNumber n = ast.getNumbers().get(0);
    assertEquals(ASTDecimal.class, n.getClass());
    assertEquals(463, n.getValue());

    n = ast.getNumbers().get(1);
    assertEquals(ASTHexadecimal.class, n.getClass());
    assertEquals(0x23, n.getValue());

    n = ast.getNumbers().get(2);
    assertEquals(ASTHexInteger.class, n.getClass());
    assertEquals(-0x45, n.getValue());
    assertEquals("-0x0045", n.getSource());

    n = ast.getNumbers().get(3);
    assertEquals(ASTHexInteger.class, n.getClass());
    assertEquals(-0x3AFFA, n.getValue());

    n = ast.getNumbers().get(4);
    assertEquals(ASTInteger.class, n.getClass());
    assertEquals(-27, n.getValue());

    n = ast.getNumbers().get(5);
    assertEquals(ASTHexadecimal.class, n.getClass());
    assertEquals(0x3affa, n.getValue());

    n = ast.getNumbers().get(6);
    assertEquals(ASTHexInteger.class, n.getClass());
    assertEquals(-0xFEDBCAabcdefl, n.getValue());

  }

  // --------------------------------------------------------------------
  // Numbers: Nat
  // --------------------------------------------------------------------

  // --------------------------------------------------------------------
  @Test
  public void testNat1() throws IOException {
    ASTDecimal ast = parser.parseString_Decimal( " 9" ).get();
    assertEquals("9", ast.getSource());
    assertEquals(9, ast.getValue());
    assertEquals(9, ast.getValueInt());
  }
  @Test
  public void testNat2() throws IOException {
    ASTDecimal ast = parser.parseString_Decimal( " 0" ).get();
    assertEquals("0", ast.getSource());
    assertEquals(0, ast.getValue());
  }
  @Test
  public void testNat3() throws IOException {
    Optional<ASTDecimal> os = parser.parseString_Decimal( " 00 0 " );
    assertEquals(false, os.isPresent());
  }
  @Test
  public void testNat4() throws IOException {
    ASTDecimal ast = parser.parseString_Decimal( " 23 " ).get();
    assertEquals("23", ast.getSource());
    assertEquals(23, ast.getValue());
    assertEquals(23, ast.getValueInt());
  }
  @Test
  public void testNat5() throws IOException {
    ASTDecimal ast = parser.parseString_Decimal( " 463 " ).get();
    assertEquals(463, ast.getValue());
  }

  // --------------------------------------------------------------------
  @Test
  public void testNat6() throws IOException {
    Optional<ASTDecimal> os = parser.parseString_Decimal( " 0x23 " );
    assertEquals(false, os.isPresent());
  }

  // --------------------------------------------------------------------
  @Test
  public void testTokens() throws IOException {
    ASTAnyTokenList ast = parser.parseString_AnyTokenList( "[463 23]" ).get();
    assertEquals(2, ast.getAnyTokens().size());
    ASTAnyToken a0 = ast.getAnyTokens().get(0);
    assertTrue(a0.getDecimalToken().isPresent());
    assertEquals("463", a0.getDecimalToken().get());
    ASTAnyToken a1 = ast.getAnyTokens().get(1);
    assertTrue(a1.getDecimalToken().isPresent());
    assertEquals("23", a1.getDecimalToken().get());
  }

  // --------------------------------------------------------------------
  @Test
  public void testTokens2() throws IOException {
    ASTAnyTokenList ast = parser.parseString_AnyTokenList(
    	"[9 'a' 45 00 47]" ).get();
    assertEquals(6, ast.getAnyTokens().size());
    assertEquals("9", ast.getAnyTokens().get(0).getDecimalToken().get());
    assertEquals("a", ast.getAnyTokens().get(1).getCharToken().get());
    assertEquals("45", ast.getAnyTokens().get(2).getDecimalToken().get());
    // Observe the separated '0's!
    assertEquals("0", ast.getAnyTokens().get(3).getDecimalToken().get());
    assertEquals("0", ast.getAnyTokens().get(4).getDecimalToken().get());
    assertEquals("47", ast.getAnyTokens().get(5).getDecimalToken().get());
  }

  // --------------------------------------------------------------------
  @Test
  public void testAbstractInterfaceFunctions() throws IOException {
    ASTNumber ast = parser.parseString_Decimal( " 234 " ).get();
    assertEquals(234, ast.getValue());
    assertEquals(234, ast.getValueInt());
    assertEquals("234", ast.getSource());
  }

  // --------------------------------------------------------------------
  // Numbers: Integer
  // --------------------------------------------------------------------

  // --------------------------------------------------------------------
  @Test
  public void testInt() throws IOException {
    ASTInteger ast = parser.parseString_Integer( " -463 " ).get();
    assertEquals(-463, ast.getValue());
    assertEquals(-463, ast.getValueInt());
    assertEquals("-463", ast.getSource());
  }

  // --------------------------------------------------------------------
  @Test
  public void testIntTokens2() throws IOException {
    ASTIntegerList ast = parser.parseString_IntegerList(
        "[9, -45, -0, - 47]" ).get();
    assertEquals(4, ast.getIntegers().size());
    assertEquals(9, ast.getIntegers().get(0).getValue());
    assertEquals("9", ast.getIntegers().get(0).getSource());
    assertEquals(-45, ast.getIntegers().get(1).getValue());
    assertEquals("-45", ast.getIntegers().get(1).getSource());
    assertEquals(0, ast.getIntegers().get(2).getValue());
    // "-" is still present
    assertEquals("-0", ast.getIntegers().get(2).getSource());
    assertEquals(-47, ast.getIntegers().get(3).getValue());
    // space between the two token is missing 
    assertEquals("-47", ast.getIntegers().get(3).getSource());
  }

}
