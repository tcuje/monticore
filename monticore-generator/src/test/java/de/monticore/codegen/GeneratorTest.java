/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2015, MontiCore, All rights reserved.
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

package de.monticore.codegen;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

import de.monticore.MontiCoreConfiguration;
import de.se_rwth.commons.logging.Log;

/**
 * TODO: Write me!
 *
 * @author (last commit) $Author$
 */
public abstract class GeneratorTest {
  
  /**
   * Parent folder for the generated code
   */
  protected static final String OUTPUT_FOLDER = "target/generated-sources/monticore/codetocompile";
  
  protected static final String GRAMMAR_EXTENSION = ".mc4";
  
  /**
   * Base generator arguments
   */
  private List<String> generatorArguments = Lists
      .newArrayList(
          getConfigProperty(MontiCoreConfiguration.Options.MODELPATH.toString()), "src/test/resources",
          getConfigProperty(MontiCoreConfiguration.Options.OUT.toString()), OUTPUT_FOLDER,
          getConfigProperty(MontiCoreConfiguration.Options.HANDCODEDPATH.toString()),
          "src/test/resources");
  
  protected static final String LOG = "GeneratorTest";
  
  /**
   * Generated source code using the generator which has to be tested
   * 
   * @param model
   */
  protected abstract void doGenerate(String model);
  
  /**
   * Gets path to the generated code for the given model
   * 
   * @param model
   * @return path to the generated code
   */
  protected abstract Path getPathToGeneratedCode(String model);
  
  /**
   * Test generation of code for a correct model 
   * 
   * @param model
   */
  public abstract void testCorrect(String model);
  
  public void testCorrect(String model, boolean compileGeneratedCode) {
    if (compileGeneratedCode) {
      testCorrect(model);
    }
    else {
      doGenerate(model);
    }
  }
  
  public void testCorrect(String model, Path pathToCompile) {
    doGenerate(model);
    doCompile(model, pathToCompile); // TODO: fix compile path of visitor
  }
  
  /**
   * TODO: extend path to compile
   * 
   * @param model
   * @param pathToCompile
   */
  protected void doCompile(String model, Path pathToCompile) {
    boolean compilationSuccess = compile(pathToCompile);
    assertTrue("There are compile errors in generated code for the model: " +
        model, compilationSuccess);
  }
  
  /**
   * Test generation of code for a false model TODO: Write me!
   * 
   * @param model
   * @param errorsNumber expected errors
   */
  protected void testFalse(String model, int errorsNumber) {
    // implement
  }
  
  /**
   * Compiles the files in the given directory, printing errors to the console
   * if any occur.
   * 
   * @param sourceCodePath the source directory to be compiled
   * @return true if the compilation succeeded
   */
  protected boolean compile(Path sourceCodePath) {
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
    boolean compilationSuccess = false;
    Optional<CompilationTask> task = setupCompilation(sourceCodePath, diagnostics);
    if (!task.isPresent()) {
      return compilationSuccess;
    }
    compilationSuccess = task.get().call();
    if (!compilationSuccess) {
      for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
        Log.error(diagnostic.toString());
      }
    }
    return compilationSuccess;
  }
  
  /**
   * Instantiates all the parameters required for a CompilationTask and returns
   * the finished task.
   * 
   * @param sourceCodePath the source directory to be compiled
   * @param diagnostics a bin for any error messages that may be generated by
   * the compiler
   * @return the compilationtask that will compile any entries in the given
   * directory
   */
  protected Optional<CompilationTask> setupCompilation(Path sourceCodePath,
      DiagnosticCollector<JavaFileObject> diagnostics) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler != null) {
      StandardJavaFileManager fileManager = compiler
          .getStandardFileManager(diagnostics, null, null);
      // set compiler's classpath to be same as the runtime's
      String sPath = Paths.get("target/generated-sources/monticore/codetocompile")
          .toAbsolutePath().toString();
      List<String> optionList = Lists.newArrayList("-classpath",
          System.getProperty("java.class.path") + File.pathSeparator + sPath, "-sourcepath", sPath);
      // System.out.println("Options" + optionList);
      Iterable<? extends JavaFileObject> javaFileObjects = getJavaFileObjects(sourceCodePath,
          fileManager);
      // System.out.println("Java files" + javaFileObjects);
      return Optional.of(compiler.getTask(null, fileManager, diagnostics, optionList, null,
          javaFileObjects));
    }
    return Optional.empty();
  }
  
  /**
   * Creates an Iterable over all the JavaFileObjects contained in a given
   * directory.
   * 
   * @param sourceCodePath the directory from which JavaFileObjects are to be
   * retrieved
   * @param fileManager the StandardJavaFileManager to be used for the
   * JavaFileObject creation
   * @return the JavaFileObjects contained in the given directory
   */
  protected Iterable<? extends JavaFileObject> getJavaFileObjects(Path sourceCodePath,
      StandardJavaFileManager fileManager) {
    Collection<File> files = FileUtils.listFiles(sourceCodePath.toFile(), new String[] { "java" },
        true);
    return fileManager.getJavaFileObjects(files.toArray(new File[files.size()]));
  }
  
  protected void dependencies(String... dependencies) {
    for (String dependency : dependencies) {
      if (!Files.exists(getPathToGeneratedCode(dependency))) {
        doGenerate(dependency);
        compile(getPathToGeneratedCode(dependency));
      }
    }
  }
  
  /**
   * @return generatorArguments
   */
  public List<String> getGeneratorArguments() {
    return this.generatorArguments;
  }
  
  /**
   * @param generatorArguments the generatorArguments to set
   */
  public void setGeneratorArguments(List<String> generatorArguments) {
    this.generatorArguments = generatorArguments;
  }
  
  public static String getConfigProperty(String property) {
    return new StringBuilder("-").append(property).toString();
  }
  
}
