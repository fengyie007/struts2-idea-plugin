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

package com.intellij.struts2.reference;

import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.struts2.reference.jsp.HtmlFormActionReferenceProvider;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.virtualFile;
import static com.intellij.patterns.StandardPatterns.or;
import static com.intellij.patterns.StandardPatterns.string;
import static com.intellij.patterns.XmlPatterns.xmlAttributeValue;
import static com.intellij.patterns.XmlPatterns.xmlTag;

/**
 * Contributes references for HTML form tags with separate namespace and action attributes.
 * This enables navigation from action attributes in HTML forms to Struts action definitions.
 *
 * @author Enhanced for HTML form support
 */
public class HtmlFormReferenceContributor extends PsiReferenceContributor {

  @Override
  public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
    // Pattern for HTML form tags in JSP files
    final XmlAttributeValuePattern formActionPattern =
        xmlAttributeValue()
            .withLocalName("action")
            .withSuperParent(2, xmlTag().withLocalName("form"))
            .inVirtualFile(or(virtualFile().withName(string().endsWith(".jsp")),
                              virtualFile().withName(string().endsWith(".jspx"))));

    // Register the HTML form action reference provider
    registrar.registerReferenceProvider(formActionPattern, new HtmlFormActionReferenceProvider());
  }
}