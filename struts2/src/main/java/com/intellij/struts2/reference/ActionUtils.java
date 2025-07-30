package com.intellij.struts2.reference;

import org.jetbrains.annotations.NotNull;

public class ActionUtils {

    @NotNull
    public static String getActionName(final String fullActionPath,
                                        final String ourActionExtension) {
        final int slashIndex = fullActionPath.lastIndexOf("/");
        final int extensionIndex = fullActionPath.lastIndexOf(ourActionExtension);
        return fullActionPath.substring(slashIndex + 1, extensionIndex);
    }

    /**
     * Extracts the namespace from the given action path.
     *
     * @param fullActionPath Full path.
     * @return Namespace.
     */
    public static String getNamespace(final String fullActionPath) {

        final int lastSlash = fullActionPath.lastIndexOf('/');
        // no slash, use fake "root" for resolving "myAction.action"
        if (lastSlash == -1) {
            return "/";
        }

        // root-package
        if (lastSlash == 0) {
            return "/";
        }

        final int firstSlash = fullActionPath.indexOf('/');
        return fullActionPath.substring(firstSlash, lastSlash);
    }
}
