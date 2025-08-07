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

import com.intellij.facet.ProjectFacetManager;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiMethod;
import com.intellij.struts2.Struts2Icons;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.struts2.model.constant.StrutsConstantHelper;
import com.intellij.struts2.reference.ActionUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.model.gotosymbol.GoToSymbolProvider;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Go to Action by .do URL (CTRL+ALT+SHIFT+N).
 * Allows searching by action URLs like "/namespace/actionName.do" and navigating directly to the action method.
 *
 * @author Claude
 */
public class GoToActionLinkSymbolProvider extends GoToSymbolProvider {

  @Override
  protected boolean acceptModule(final Module module) {
    return StrutsFacet.getInstance(module) != null;
  }

  @NotNull
  @Override
  protected Collection<Module> calcAcceptableModules(@NotNull Project project) {
    return ProjectFacetManager.getInstance(project).getModulesWithFacet(StrutsFacet.FACET_TYPE_ID);
  }

  @Override
  protected void addNames(@NotNull final Module module, final Set<String> result) {
    final StrutsModel strutsModel = StrutsManager.getInstance(module.getProject()).getCombinedModel(module);
    if (strutsModel == null) {
      return;
    }

    final Set<String> actionUrls = new HashSet<>();

    strutsModel.processActions(action -> {
      final String actionName = action.getName().getStringValue();
      final String namespace = action.getNamespace();
      
      if (actionName != null) {
        // Get action extensions from struts configuration
        final List<String> actionExtensions = StrutsConstantHelper.getActionExtensions(action.getXmlTag());
        
        for (String extension : actionExtensions) {
          // Add full URL: /namespace/actionName.do
          final String fullUrl = namespace + "/" + actionName + extension;
          actionUrls.add(fullUrl);
          
          // Also add without leading slash for root namespace
          if ("/".equals(namespace)) {
            actionUrls.add(actionName + extension);
          }
        }
      }
      
      return true;
    });
    
    result.addAll(actionUrls);
  }

  @Override
  protected void addItems(@NotNull final Module module, final String name, final List<NavigationItem> result) {
    final StrutsModel strutsModel = StrutsManager.getInstance(module.getProject()).getCombinedModel(module);
    if (strutsModel == null) {
      return;
    }

    // Get action extensions from struts configuration using any action as context
    final List<String> actionExtensions = new ArrayList<>();
    strutsModel.processActions(action -> {
      if (actionExtensions.isEmpty()) {
        actionExtensions.addAll(StrutsConstantHelper.getActionExtensions(action.getXmlTag()));
      }
      return actionExtensions.isEmpty(); // Continue until we get extensions
    });

    // Find which extension matches
    final String matchingExtension = ContainerUtil.find(actionExtensions, ext -> StringUtil.endsWith(name, ext));
    if (matchingExtension == null) {
      return;
    }

    // Try original logic first
    Action foundAction = findActionByUrl(name, matchingExtension, strutsModel);
    
    // If not found, try without first path segment
    if (foundAction == null) {
      foundAction = findActionByUrlWithoutFirstPath(name, matchingExtension, strutsModel);
    }
    
    if (foundAction != null) {
      addNavigationItemsForAction(foundAction, name, result);
    }
  }

  private Action findActionByUrl(String fullUrl, String extension, StrutsModel strutsModel) {
    final String actionName = ActionUtils.getActionName(fullUrl, extension);
    final String namespace = ActionUtils.getNamespace(fullUrl);
    final List<Action> actions = strutsModel.findActionsByName(actionName, namespace);
    return actions.isEmpty() ? null : actions.get(0);
  }

  private Action findActionByUrlWithoutFirstPath(String fullUrl, String extension, StrutsModel strutsModel) {
    int pos = -1;
    if (fullUrl.startsWith("/")) {
      pos = StringUtils.indexOf(fullUrl, "/", 1);
    } else {
      pos = StringUtils.indexOf(fullUrl, "/");
    }
    
    if (pos != -1) {
      String urlWithoutFirstPath = fullUrl.substring(pos);
      return findActionByUrl(urlWithoutFirstPath, extension, strutsModel);
    }
    
    return null;
  }

  private void addNavigationItemsForAction(Action action, String searchUrl, List<NavigationItem> result) {
    final String actionName = action.getName().getStringValue();
    final String namespace = action.getNamespace();
    
    // Navigate to action XML configuration
    final NavigationItem xmlItem = createNavigationItem(action.getXmlTag(),
                                                       searchUrl + " → " + actionName + " [" + namespace + "]",
                                                       Struts2Icons.Action);
    result.add(xmlItem);
    
    // Navigate to action method if available
    final PsiMethod actionMethod = action.searchActionMethod();
    if (actionMethod != null) {
      final String methodName = actionMethod.getName();
      final String className = actionMethod.getContainingClass() != null ? 
          actionMethod.getContainingClass().getName() : "Unknown";
      final NavigationItem methodItem = createNavigationItem(actionMethod,
                                                           searchUrl + " → " + className + "." + methodName + "()",
                                                           Struts2Icons.Method);
      result.add(methodItem);
    }
  }
}