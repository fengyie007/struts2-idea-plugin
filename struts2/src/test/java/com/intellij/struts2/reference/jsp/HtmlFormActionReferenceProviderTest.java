/*
 * Copyright 2025 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts2.reference.jsp;

import com.intellij.psi.PsiReference;
import com.intellij.struts2.BasicLightHighlightingTestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;

/**
 * Tests for {@link HtmlFormActionReferenceProvider}.
 * 
 * @author Enhanced HTML form support tests
 */
public class HtmlFormActionReferenceProviderTest extends BasicLightHighlightingTestCase {

  @Override
  protected String getTestDataPath() {
    return super.getTestDataPath() + "/reference/jsp/htmlform/";
  }

  /**
   * Test action reference resolution with separate namespace attribute.
   */
  public void testHtmlFormActionWithNamespace() throws Throwable {
    createStrutsFileSet("struts-htmlform.xml");
    
    myFixture.configureByFiles("htmlform-action-reference.jsp", "struts-htmlform.xml");
    
    // Test that action reference can be resolved
    final PsiReference reference = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
    assertNotNull("Action reference should be found", reference);
    
    final String actionName = reference.getCanonicalText();
    assertEquals("processCodeInputContinue.do", actionName);
    
    // Test that reference resolves to action definition
    assertNotNull("Action reference should resolve", reference.resolve());
  }

  /**
   * Test code completion for actions in HTML form with namespace.
   */
  public void testHtmlFormActionCompletion() throws Throwable {
    createStrutsFileSet("struts-htmlform.xml");
    
    myFixture.configureByFiles("htmlform-action-completion.jsp", "struts-htmlform.xml");
    
    CodeInsightTestUtil.doCompletionTest(myFixture, "processCodeInputContinue.do", 1);
  }

  /**
   * Test namespace action separation functionality using misc test files.
   */
  public void testNamespaceActionSeparation() throws Throwable {
    myFixture.copyFileToProject("../../misc/test-struts-config.xml", "struts-config.xml");
    createStrutsFileSet("struts-config.xml");
    
    myFixture.copyFileToProject("../../misc/test-namespace-action.jsp", "test-namespace-action.jsp");
    
    // Test that the file can be configured and processed
    myFixture.configureByFile("test-namespace-action.jsp");
    assertNotNull("Test file should be configured", myFixture.getFile());
  }
}