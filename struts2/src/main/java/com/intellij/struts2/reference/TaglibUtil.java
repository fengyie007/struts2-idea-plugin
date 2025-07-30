/*
 * Copyright 2007 The authors
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

import com.intellij.lang.ognl.OgnlLanguage;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Various utilities for taglibs.
 *
 * @author Yann C&eacute;bron
 */
public final class TaglibUtil {

  /**
   * Splits action-name from action-method.
   */
  public static final char BANG_SYMBOL = '!';

  private TaglibUtil() {
  }

  /**
   * Checks whether the given attribute value is a dynamic expression.
   * Currently only checks for OGNL.
   *
   * @param attributeValue The attribute value to check.
   * @return true if yes, false otherwise.
   */
  public static boolean isDynamicExpression(@NotNull @NonNls final String attributeValue) {
    return StringUtil.startsWith(attributeValue, OgnlLanguage.EXPRESSION_PREFIX) ||
        StringUtil.containsChar(attributeValue, '{');
  }

  /**
   * Trims the given value to the real action path.
   *
   * @param attributeValue Custom tag attribute value.
   * @return Action path.
   */
  @NotNull
  @NonNls
  public static String trimActionPath(@NotNull @NonNls final String attributeValue) {
    final int bangIndex = attributeValue.indexOf(BANG_SYMBOL);
    if (bangIndex == -1) {
      return attributeValue;
    }

    return attributeValue.substring(0, bangIndex);
  }

  /**
   * Gets namespace from the XML tag or its parent tags.
   * This method looks for namespace attribute in the current tag and parent hierarchy.
   * It supports both Struts tags and HTML form tags with separate namespace and action attributes.
   *
   * @param xmlTag The XML tag to search from.
   * @return The namespace value, or null if not found.
   */
  @Nullable
  public static String getNamespaceFromTag(@NotNull final XmlTag xmlTag) {
    // Check current tag for namespace attribute
    String namespace = xmlTag.getAttributeValue("namespace");
    if (namespace != null) {
      return namespace;
    }

    // Look up the parent hierarchy for namespace
    XmlTag parentTag = xmlTag.getParentTag();
    while (parentTag != null) {
      namespace = parentTag.getAttributeValue("namespace");
      if (namespace != null) {
        return namespace;
      }
      parentTag = parentTag.getParentTag();
    }

    return null;
  }

  /**
   * Gets namespace from the XML attribute value by looking at its containing tag and parent hierarchy.
   * This method is specifically designed for action attributes that might have separate namespace attributes.
   *
   * @param xmlAttributeValue The XML attribute value to search from.
   * @return The namespace value, or null if not found.
   */
  @Nullable
  public static String getNamespaceFromAttributeValue(@NotNull final XmlAttributeValue xmlAttributeValue) {
    final XmlTag tag = PsiTreeUtil.getParentOfType(xmlAttributeValue, XmlTag.class);
    if (tag == null) {
      return null;
    }

    return getNamespaceFromTag(tag);
  }

}