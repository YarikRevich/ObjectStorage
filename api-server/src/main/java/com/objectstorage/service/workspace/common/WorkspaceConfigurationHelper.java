package com.objectstorage.service.workspace.common;

/**
 * Contains helpful tools used for workspace configuration.
 */
public class WorkspaceConfigurationHelper {

    /**
     * Creates folder definition with the help of the given folder name for ZIP achieve.
     *
     * @param name given folder name value.
     * @return wrapped token.
     */
    public static String getZipFolderDefinition(String name) {
        return String.format("%s/", name);
    }

    /**
     * Creates zip file definition with the help of the given file name.
     *
     * @param name given zip file name.
     * @return wrapped zip file.
     */
    public static String getZipFile(String name) {
        return String.format("%s.zip", name);
    }
}