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

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.struts2.reference.js.JavaScriptActionLinkReferenceProvider;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.virtualFile;
import static com.intellij.patterns.StandardPatterns.or;
import static com.intellij.patterns.StandardPatterns.string;

/**
 * Registers reference providers for JavaScript code embedded in JSP files to support Struts action link navigation.
 * This addresses the issue where JavaScript action URLs in JSP files cannot be resolved while standalone JS files work fine.
 * 
 * @author Enhanced for JSP embedded JavaScript action link support
 */
public class JspJavaScriptReferenceContributor extends PsiReferenceContributor {

  @Override
  public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
    // Register for JavaScript string literals within JSP files
    registrar.registerReferenceProvider(
        PlatformPatterns.psiElement(JSLiteralExpression.class)
            .inVirtualFile(or(virtualFile().withName(string().endsWith(".jsp")),
                              virtualFile().withName(string().endsWith(".jspx")))),
        new JavaScriptActionLinkReferenceProvider()
    );
  }
}