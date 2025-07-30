/*
 * Copyright 2013 The authors
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

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.javaee.web.CustomServletReferenceAdapter;
import com.intellij.javaee.web.ServletMappingInfo;
import com.intellij.openapi.paths.PathReference;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.html.HtmlTag;
import com.intellij.struts2.Struts2Icons;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.struts2.model.constant.StrutsConstantHelper;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ConstantFunction;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.intellij.struts2.reference.ActionUtils.getActionName;
import static com.intellij.struts2.reference.ActionUtils.getNamespace;

/**
 * Provides links to Action-URLs in all places where Servlet-URLs are processed.
 *
 * @author Yann C&eacute;bron
 */
final class ActionLinkReferenceProvider extends CustomServletReferenceAdapter {
  @Override
  protected PsiReference[] createReferences(@NotNull final PsiElement psiElement,
                                            final int offset,
                                            final String text,
                                            @Nullable final ServletMappingInfo info,
                                            final boolean soft) {
    final StrutsModel strutsModel = StrutsManager.getInstance(psiElement.getProject()).getCombinedModel(psiElement);

    if (strutsModel == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    final List<String> actionExtensions = StrutsConstantHelper.getActionExtensions(psiElement);
    if (actionExtensions.isEmpty()) {
      return PsiReference.EMPTY_ARRAY;
    }

    if (text.indexOf('/') != -1) {
      return new PsiReference[]{
        new ActionLinkPackageReference(psiElement, offset, text, soft, strutsModel),
        new ActionLinkReference(psiElement, offset, text, soft, strutsModel, actionExtensions)
      };
    }
    else {
      return new PsiReference[]{
        new ActionLinkReference(psiElement, offset, text, soft, strutsModel, actionExtensions)
      };
    }
  }

  @Override
  @Nullable
  public PathReference createWebPath(final String path,
                                     @NotNull final PsiElement psiElement,
                                     final ServletMappingInfo servletMappingInfo) {
    final StrutsManager strutsManager = StrutsManager.getInstance(psiElement.getProject());
    if (strutsManager.getCombinedModel(psiElement) == null) {
      return null;
    }

    return new PathReference(path, new ConstantFunction<>(Struts2Icons.Action)); /*{
TODO not needed so far ?!
   public PsiElement resolve() {
        return action.getXmlTag();
      }
    };*/
  }


  private static final class ActionLinkReference extends PsiReferenceBase<PsiElement> implements EmptyResolveMessageProvider {

    private final StrutsModel strutsModel;
    private final List<String> actionExtensions;
    private final String fullActionPath;

    private ActionLinkReference(final PsiElement element,
                                final int offset,
                                final String text,
                                final boolean soft,
                                final StrutsModel strutsModel,
                                final List<String> actionExtensions) {
      super(element, new TextRange(offset, offset + text.length()), soft);
      this.strutsModel = strutsModel;
      this.actionExtensions = actionExtensions;

      fullActionPath = PathReference.trimPath(getValue());
      final int lastSlash = fullActionPath.lastIndexOf("/");

      // adapt TextRange to everything behind /packageName/
      if (lastSlash != -1) {
        setRangeInElement(TextRange.from(offset + lastSlash + 1, fullActionPath.length() - lastSlash - 1));
      }

      // reduce to action-name if full path given
      for (final String actionExtension : actionExtensions) {
        if (StringUtil.endsWith(fullActionPath, actionExtension)) {
          setRangeInElement(TextRange.from(getRangeInElement().getStartOffset(),
                                           getRangeInElement().getLength() - actionExtension.length()));
          break;
        }
      }
    }

    @Override
    public PsiElement resolve() {
      final String ourActionExtension = ContainerUtil.find(actionExtensions, s -> StringUtil.endsWith(fullActionPath, s));
      if (ourActionExtension == null) {
        return null;
      }

      // First try: use original logic
      final String actionName = getActionName(fullActionPath, ourActionExtension);
      final String namespace = getNamespace(fullActionPath);
      List<Action> actions = strutsModel.findActionsByName(actionName, namespace);
      
      // If original logic found results, return them
      if (!actions.isEmpty()) {
        return getFirstActionElement(actions);
      }

      //删除第一级path前缀,再进行查找
      int pos = -1;
      if(fullActionPath.startsWith("/")) {
        pos = StringUtils.indexOf(fullActionPath, "/", 1);
      } else {
        pos = StringUtils.indexOf(fullActionPath, "/");
      }
      if (pos != -1) {
        String fullActionPathNew = fullActionPath.substring(pos);
        final String actionName2 = getActionName(fullActionPathNew, ourActionExtension);
        final String namespace2 = getNamespace(fullActionPathNew);
        List<Action> actions2 = strutsModel.findActionsByName(actionName2, namespace2);

        // If original logic found results, return them
        if (!actions2.isEmpty()) {
          return getFirstActionElement(actions2);
        }
      }

      // If original logic didn't find results, try to resolve action in HTML form tag
      final HtmlTag htmlTag = PsiTreeUtil.getParentOfType(getElement().getParent(), HtmlTag.class);
      if (htmlTag != null && htmlTag.getName().equals("form")) {
        final String xmlNamespace = htmlTag.getAttributeValue("namespace");
        // Try with XML-derived namespace
        actions = strutsModel.findActionsByName(actionName, xmlNamespace);
        if (!actions.isEmpty()) {
          final Action myAction = actions.get(0);
          return myAction.getXmlTag();
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
      final String namespace = getNamespace(fullActionPath);

      final String firstExtension = actionExtensions.get(0);

      final List<Action> actionList = strutsModel.getActionsForNamespace(namespace);
      final List<Object> variants = new ArrayList<>(actionList.size());
      for (final Action action : actionList) {
        final String actionPath = action.getName().getStringValue();
        if (actionPath != null) {
          variants.add(LookupElementBuilder.create(actionPath + firstExtension)
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

  }

  /**
   * Provides reference to S2-package within action-path.
   */
  private static final class ActionLinkPackageReference extends PsiReferenceBase<PsiElement> implements EmptyResolveMessageProvider {

    private final String namespace;
    private final List<StrutsPackage> allStrutsPackages;
    private final String fullActionPath;

    private ActionLinkPackageReference(final PsiElement element,
                                       final int offset,
                                       final String text,
                                       final boolean soft,
                                       final StrutsModel strutsModel) {
      super(element, computeRange(offset, text), soft);

      fullActionPath = PathReference.trimPath(text);
      namespace = getNamespace(fullActionPath);

      allStrutsPackages = strutsModel.getStrutsPackages();
    }

    private static TextRange computeRange(final int offset, final String text) {
      final int lastSlash = text.lastIndexOf('/');
      return new TextRange(offset, offset + (lastSlash == -1 ? text.length() : lastSlash));
    }

    @Override
    public PsiElement resolve() {
      for (final StrutsPackage strutsPackage : allStrutsPackages) {
        if (Objects.equals(namespace, strutsPackage.searchNamespace())) {
          return strutsPackage.getXmlTag();
        }
      }

      return null;
    }

    @Override
    public Object @NotNull [] getVariants() {
      return ContainerUtil.map2Array(allStrutsPackages, Object.class, strutsPackage -> {
        final String packageNamespace = strutsPackage.searchNamespace();
        return LookupElementBuilder.create(packageNamespace.length() != 1 ? packageNamespace + "/" : packageNamespace)
          .withIcon(StrutsIcons.STRUTS_PACKAGE)
          .withTypeText(strutsPackage.getName().getStringValue());
      });
    }

    @Override
    @NotNull
    public String getUnresolvedMessagePattern() {
      return "Cannot resolve Struts 2 package '" + namespace + "'";
    }
  }
}
