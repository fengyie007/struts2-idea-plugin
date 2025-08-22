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

package com.intellij.struts2.reference.js;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.struts2.Struts2Icons;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.model.constant.StrutsConstantHelper;
import com.intellij.struts2.reference.ActionUtils;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.struts2.reference.ActionUtils.getActionName;

/**
 * Provides reference resolution for Struts action URLs in JavaScript string literals.
 * Supports URLs like: '/prpins/policyImport/preEndorseChangePlans.do?certiNo=' + applyNo
 *
 * @author Enhanced for JavaScript action link support
 */
public class JavaScriptActionLinkReferenceProvider extends PsiReferenceProvider {

  // No longer using a static pattern - we'll dynamically check for configured action extensions

  @Override
  public PsiReference @NotNull [] getReferencesByElement(@NotNull final PsiElement psiElement,
                                                         @NotNull final ProcessingContext context) {
    if (!(psiElement instanceof JSLiteralExpression)) {
      return PsiReference.EMPTY_ARRAY;
    }

    final JSLiteralExpression jsLiteral = (JSLiteralExpression) psiElement;
    if (!jsLiteral.isQuotedLiteral()) {
      return PsiReference.EMPTY_ARRAY;
    }

    final StrutsManager strutsManager = StrutsManager.getInstance(psiElement.getProject());
    final StrutsModel strutsModel = strutsManager.getCombinedModel(psiElement);
    if (strutsModel == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    final List<String> actionExtensions = StrutsConstantHelper.getActionExtensions(psiElement);
    final String literalValue = jsLiteral.getStringValue();
    if (literalValue == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    // Find action URL with any configured extension in the string
    final String actionUrl = findActionUrlInString(literalValue, actionExtensions);
    if (actionUrl == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    final String ourActionExtension = ContainerUtil.find(actionExtensions, s -> StringUtil.endsWith(actionUrl, s));
    if (ourActionExtension == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    // Calculate the text range within the string literal
    // Look for the action URL pattern in the original literal value
    int startOffset = -1;
    for (final String extension : actionExtensions) {
      final String escapedExtension = Pattern.quote(extension);
      // First try to find JSP expression pattern
      final Pattern jspPattern = Pattern.compile("(\\$\\{[^}]+\\})?(/[^'\"]*" + escapedExtension + ")");
      final Matcher jspMatcher = jspPattern.matcher(literalValue);
      if (jspMatcher.find() && jspMatcher.group(2).equals(actionUrl)) {
        // Found JSP pattern, use the start of the path part (group 2)
        startOffset = jspMatcher.start(2);
        break;
      }
      
      // Fallback to original pattern
      final Pattern fallbackPattern = Pattern.compile("([^'\"]*" + escapedExtension + ")");
      final Matcher fallbackMatcher = fallbackPattern.matcher(literalValue);
      if (fallbackMatcher.find() && fallbackMatcher.group(1).equals(actionUrl)) {
        startOffset = fallbackMatcher.start(1);
        break;
      }
    }
    
    if (startOffset == -1) {
      return PsiReference.EMPTY_ARRAY;
    }

    // Adjust for quote character offset
    final int quoteOffset = 1; // Account for opening quote
    final TextRange textRange = TextRange.from(startOffset + quoteOffset, actionUrl.length());

    return new PsiReference[]{new JavaScriptActionReference(jsLiteral, textRange, actionUrl, ourActionExtension, strutsModel)};
  }

  /**
   * Finds action URL with any configured extension in the given string.
   * Handles JSP expressions like ${ctx}/path/action.do by extracting the path part.
   * @param literalValue the string literal value to search in
   * @param actionExtensions list of configured action extensions
   * @return the action URL if found, null otherwise
   */
  @Nullable
  private static String findActionUrlInString(@NotNull final String literalValue, @NotNull final List<String> actionExtensions) {
    for (final String extension : actionExtensions) {
      // Create a pattern for this specific extension that handles JSP expressions
      final String escapedExtension = Pattern.quote(extension);
      // Pattern to match URLs like ${ctx}/path/action.do or /path/action.do
      final Pattern pattern = Pattern.compile("(?:\\$\\{[^}]+\\})?(/[^'\"]*" + escapedExtension + ")(?:\\\\?.*)?");
      final Matcher matcher = pattern.matcher(literalValue);
      if (matcher.find()) {
        return matcher.group(1); // Return the /path/action.do part without JSP expression prefix
      }
      
      // Fallback to original pattern for URLs without JSP expressions
      final Pattern fallbackPattern = Pattern.compile("([^'\"]*" + escapedExtension + ")(?:\\\\?.*)?(?:['\"].*)?$");
      final Matcher fallbackMatcher = fallbackPattern.matcher(literalValue);
      if (fallbackMatcher.find()) {
        return fallbackMatcher.group(1);
      }
    }
    return null;
  }

  private static final class JavaScriptActionReference extends PsiReferenceBase<JSLiteralExpression> implements EmptyResolveMessageProvider {

    private final String actionUrl;
    private final String actionExtension;
    private final StrutsModel strutsModel;

    private JavaScriptActionReference(final JSLiteralExpression jsLiteral,
                                      final TextRange textRange,
                                      final String actionUrl,
                                      final String actionExtension,
                                      final StrutsModel strutsModel) {
      super(jsLiteral, textRange);
      this.actionUrl = actionUrl;
      this.actionExtension = actionExtension;
      this.strutsModel = strutsModel;
    }

    @Override
    public PsiElement resolve() {
      List<String> list = Arrays.asList(actionExtension);
      final String ourActionExtension = ContainerUtil.find(list, s -> StringUtil.endsWith(actionUrl, s));
      if (ourActionExtension == null) {
        return null;
      }
      // First try: use original logic
      final String actionName = ActionUtils.getActionName(actionUrl, ourActionExtension);
      final String namespace = ActionUtils.getNamespace(actionUrl);
      List<Action> actions = strutsModel.findActionsByName(actionName, namespace);

      // If original logic found results, return them
      if (!actions.isEmpty()) {
        return getFirstActionElement(actions);
      }

      //删除第一级path前缀,再进行查找
      int pos = -1;
      if(actionUrl.startsWith("/")) {
        pos = StringUtils.indexOf(actionUrl, "/", 1);
      } else {
        pos = StringUtils.indexOf(actionUrl, "/");
      }
      if (pos != -1) {
        String actionUrlNew = actionUrl.substring(pos);
        final String actionName2 = getActionName(actionUrlNew, ourActionExtension);
        final String namespace2 = getNamespace(actionUrlNew);
        List<Action> actions2 = strutsModel.findActionsByName(actionName2, namespace2);

        // If original logic found results, return them
        if (!actions2.isEmpty()) {
          return getFirstActionElement(actions2);
        }
      }
      return null;
    }

    private PsiElement getFirstActionElement(List<Action> actions) {
      if (actions.isEmpty()) {
        return null;
      }
      return actions.get(0).getXmlTag();
    }

    @Override
    public Object @NotNull [] getVariants() {
      final String namespace = getNamespace(actionUrl);
      final List<Action> actionList = strutsModel.getActionsForNamespace(namespace);

      final List<Object> variants = new ArrayList<>(actionList.size());
      for (final Action action : actionList) {
        final String actionPath = action.getName().getStringValue();
        if (actionPath != null) {
          final String fullPath = namespace + "/" + actionPath + actionExtension;
          variants.add(LookupElementBuilder.create(fullPath)
                         .withIcon(Struts2Icons.Action)
                         .withTypeText(action.getNamespace()));
        }
      }
      return ArrayUtil.toObjectArray(variants);
    }

    @Override
    @NotNull
    public String getUnresolvedMessagePattern() {
      return "Cannot resolve action '" + getValue() + "'";
    }

    /**
     * Extracts namespace from action URL.
     * For URL like "/prpins/policyImport/action.do", returns "/prpins/policyImport"
     */
    @Nullable
    @NonNls
    private String getNamespace(@NotNull final String fullActionPath) {
      final int lastSlashIndex = fullActionPath.lastIndexOf('/');
      if (lastSlashIndex == -1) {
        return "/"; // Default namespace
      }
      
      final String namespace = fullActionPath.substring(0, lastSlashIndex);
      return namespace.isEmpty() ? "/" : namespace;
    }
  }
}