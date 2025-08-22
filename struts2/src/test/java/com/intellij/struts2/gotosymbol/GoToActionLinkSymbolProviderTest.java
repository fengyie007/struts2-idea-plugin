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

package com.intellij.struts2.gotosymbol;

import com.intellij.openapi.module.Module;
import com.intellij.struts2.BasicLightHighlightingTestCase;
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Test for {@link GoToActionLinkSymbolProvider}.
 */
public class GoToActionLinkSymbolProviderTest extends BasicLightHighlightingTestCase {

  @Override
  protected String getTestDataPath() {
    return super.getTestDataPath() + "/gotosymbol/actionLink/";
  }

  @Override
  protected String getTestDataLocation() {
    return getTestDataPath();
  }

  public void testActionLinkNames() throws Throwable {
    createStrutsFileSet("struts-actionLink.xml");

    final GoToActionLinkSymbolProvider provider = new GoToActionLinkSymbolProvider();
    final Module module = myFixture.getModule();
    
    final Set<String> names = new HashSet<>();
    provider.addNames(module, names);
    
    // Should find action links with .do extension
    assertContainsElements(names, "/testNamespace/testAction.do");
    assertContainsElements(names, "rootAction.do");
  }

  @NotNull
  protected String getTestDataBasePath() {
    return "/contrib/struts2/plugin/src/test/testData";
  }

}