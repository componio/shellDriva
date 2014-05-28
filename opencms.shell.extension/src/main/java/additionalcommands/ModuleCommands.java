/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package additionalcommands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsConfigurationException;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsExportPoint;
import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsMessages;
import org.opencms.importexport.CmsExportParameters;
import org.opencms.importexport.CmsImportExportException;
import org.opencms.importexport.CmsImportParameters;
import org.opencms.importexport.CmsVfsImportExportHandler;
import org.opencms.lock.CmsLockException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsShell;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.I_CmsShellCommands;
import org.opencms.main.Messages;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleImportExportHandler;
import org.opencms.report.CmsShellReport;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.security.CmsSecurityException;
import org.opencms.synchronize.CmsSynchronize;
import org.opencms.synchronize.CmsSynchronizeException;
import org.opencms.synchronize.CmsSynchronizeSettings;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import static projectconstants.ProjectConstants.PATH_CLASSES;
import static projectconstants.ProjectConstants.PATH_ELEMENTS;
import static projectconstants.ProjectConstants.PATH_FORMATTERS;
import static projectconstants.ProjectConstants.PATH_LIB;
import static projectconstants.ProjectConstants.PATH_RESOURCES;
import static projectconstants.ProjectConstants.PATH_SCHEMAS;
import static projectconstants.ProjectConstants.PATH_TEMPLATES;

/**
 *
 * @author Thomas
 */
public class ModuleCommands implements I_CmsShellCommands {

    private CmsObject m_cms;
    private CmsShell m_shell;
    private static final Log LOG = OpenCms.getLog(ModuleCommands.class);

    public ModuleCommands() {
    }

    /**
     * creates a new module in opencms
     *
     * @param moduleName
     * @param version
     * @throws CmsSecurityException
     * @throws CmsConfigurationException
     * @throws CmsRoleViolationException
     * @throws CmsLockException
     * @throws CmsException
     */
    public void createNewModule(String moduleName, String version, String actionClass) {
        CmsModule newModule = new CmsModule();
        newModule.setName(moduleName);
        newModule.setNiceName(moduleName);
        newModule.getVersion().setVersion(version);
        newModule.setCreateClassesFolder(true);
        newModule.setCreateFormattersFolder(true);
        newModule.setCreateElementsFolder(true);
        newModule.setCreateModuleFolder(true);
        newModule.setCreateLibFolder(true);
        newModule.setCreateModuleFolder(true);
        newModule.setCreateResourcesFolder(true);
        newModule.setCreateSchemasFolder(true);
        newModule.setCreateTemplateFolder(true);
        newModule.setResources(new ArrayList<String>());
        newModule.setExportPoints(new ArrayList<CmsExportPoint>());
        newModule.setResourceTypes(new ArrayList<I_CmsResourceType>());
        newModule.setExplorerTypes(new ArrayList<CmsExplorerTypeSettings>());
        if (actionClass != null && !actionClass.isEmpty()) {
            newModule.setActionClass(actionClass);
        }
        if (!OpenCms.getModuleManager().hasModule(moduleName)) {
            try {
                createModuleFolders(newModule, m_cms);
                OpenCms.getModuleManager().addModule(m_cms, newModule);
            } catch (CmsException ex) {
                LOG.error("Can't create module", ex);
            }
        }
    }

    /**
     * Creates all module folders that are selected in the input form.<p>
     *
     * taken from org.opencms.workplace.tools.modules.CmsModulesEditBase check
     * http://www.opencms.org/javadoc/modules-workplace/org/opencms/workplace/tools/modules/CmsModulesEditBase.html
     *
     * @param module the module
     *
     * @return the updated module
     *
     * @throws CmsException if somehting goes wrong
     */
    private void createModuleFolders(CmsModule module, CmsObject cms) {
        String modulePath = CmsWorkplace.VFS_PATH_MODULES + module.getName() + "/";
        List<CmsExportPoint> exportPoints = module.getExportPoints();
        //unmodifiable
        List<String> resources = module.getResources();

        // set the createModuleFolder flag if any other flag is set
        if (module.isCreateClassesFolder()
                || module.isCreateElementsFolder()
                || module.isCreateLibFolder()
                || module.isCreateResourcesFolder()
                || module.isCreateSchemasFolder()
                || module.isCreateTemplateFolder()
                || module.isCreateFormattersFolder()) {
            module.setCreateModuleFolder(true);
        }

        // check if we have to create the module folder
        int folderId = CmsResourceTypeFolder.getStaticTypeId();
        int moduleConfigId = 28;
        try {
            LOG.info("!Start create module folders !");
            if (module.isCreateModuleFolder()) {
                /**
                 * if (cms.existsResource(modulePath)) {
                 * cms.lockResource(cms.readResource(modulePath));
                 * cms.deleteResource(modulePath,
                 * CmsResource.DELETE_REMOVE_SIBLINGS); }*
                 */
                cms.createResource(modulePath, folderId);
                // add the module folder to the resource list
                resources.add(modulePath);
                module.setResources(resources);
            }

            // check if we have to create the template folder
            if (module.isCreateTemplateFolder()) {
                String path = modulePath + PATH_TEMPLATES;
                cms.createResource(path, folderId);
            }

            // check if we have to create the elements folder
            if (module.isCreateElementsFolder()) {
                String path = modulePath + PATH_ELEMENTS;
                cms.createResource(path, folderId);
            }

            if (module.isCreateFormattersFolder()) {
                String path = modulePath + PATH_FORMATTERS;
                cms.createResource(path, folderId);
            }

            // check if we have to create the schemas folder
            if (module.isCreateSchemasFolder()) {
                String path = modulePath + PATH_SCHEMAS;
                cms.createResource(path, folderId);
            }

            // check if we have to create the resources folder
            if (module.isCreateTemplateFolder()) {
                String path = modulePath + PATH_RESOURCES;
                cms.createResource(path, folderId);
            }

            // check if we have to create the lib folder
            if (module.isCreateLibFolder()) {
                String path = modulePath + PATH_LIB;
                cms.createResource(path, folderId);
                CmsExportPoint exp = new CmsExportPoint(path, "WEB-INF/lib/");
                exportPoints.add(exp);
                module.setExportPoints(exportPoints);
            }

            // check if we have to create the classes folder
            if (module.isCreateClassesFolder()) {
                String path = modulePath + PATH_CLASSES;
                cms.createResource(path, folderId);
                CmsExportPoint exp = new CmsExportPoint(path, "WEB-INF/classes/");
                exportPoints.add(exp);
                module.setExportPoints(exportPoints);

                // now create all subfolders for the package structure
                StringTokenizer tok = new StringTokenizer(module.getName(), ".");
                while (tok.hasMoreTokens()) {
                    String folder = tok.nextToken();
                    path += folder + "/";
                    cms.createResource(path, folderId);
                }
            }
            //create .config from type module_config
            String configFile = modulePath + ".config";
            cms.createResource(configFile, moduleConfigId);
            LOG.info("!End create module folders !");
        } catch (CmsException ex) {
            LOG.error("Error durinbg creation of Resources: ", ex);
        }
    }

    /**
     * synchronizes the remote files system with the virtual file system of
     * opencms
     *
     * @param rfsFolder
     * @param vfsFolder
     * @throws CmsSynchronizeException
     * @throws CmsException
     */
    public void syncRFSandVFS(String rfsFolder, String vfsFolder) throws CmsSynchronizeException, CmsException {
        File rfsDir = new File(rfsFolder);
        //OpenCms.getWorkplaceManager().addSynchronizeExcludePattern(excludePattern);
        if (rfsDir.exists() && rfsDir.isDirectory() && m_cms.existsResource(vfsFolder)) {
            // save what gets synchronized
            CmsSynchronizeSettings syncSettings = new CmsSynchronizeSettings();
            syncSettings.setDestinationPathInRfs(rfsFolder);
            ArrayList sourceList = new ArrayList();
            sourceList.add(vfsFolder);
            syncSettings.setSourceListInVfs(sourceList);
            syncSettings.setEnabled(true);
            new CmsSynchronize(m_cms, syncSettings, new CmsShellReport(m_cms.getRequestContext().getLocale()));
        } else {
            if (!rfsDir.exists()) {
                System.out.println("Folder " + rfsFolder + "in Remote File System doesn't exist");
            }
            if (!rfsDir.isDirectory()) {
                System.out.println("Folder " + rfsFolder + "in Remote File System is not a directory");
            }
            if (!m_cms.existsResource(vfsFolder)) {
                System.out.println("Folder " + vfsFolder + "in Virtual File System doesn't exist");
            }
        }
    }

    private void syncRFSandVFSWithSettings(CmsSynchronizeSettings settings) throws CmsSynchronizeException, CmsException {
        new CmsSynchronize(m_cms, settings, new CmsShellReport(m_cms.getRequestContext().getLocale()));
    }

    /**
     * synchronizes the remote files system with the virtual file system folders
     * of opencms vfsFolders are separated with ","
     *
     * @param rfsFolder
     * @param vfsFolders
     * @throws CmsSynchronizeException
     * @throws CmsException
     */
    public void syncRFSandVFSMultipleFolders(String rfsFolder, String vfsFolders) throws CmsSynchronizeException, CmsException, Exception {
        String[] folders = vfsFolders.split(",");
        ArrayList sourceList = new ArrayList();
        for (String vfsFolder : folders) {
            if (!m_cms.existsResource(vfsFolder)) {
                System.err.println("Sync aborted cause folder: " + vfsFolder + "don't exist");
                System.err.println("NOTE: Folders should be separated with a \",\"");
                System.exit(1);
            }
            sourceList.add(vfsFolder);
        }
        CmsSynchronizeSettings syncSettings = new CmsSynchronizeSettings();
        syncSettings.setDestinationPathInRfs(rfsFolder);
        syncSettings.setSourceListInVfs(sourceList);
        syncSettings.setEnabled(true);
        syncRFSandVFSWithSettings(syncSettings);
        publishResources(vfsFolders, true);
    }

    /**
     * synchronizes the remote files system with the virtual file system folders
     * of opencms adds exclusionPatterns to the sync, so if a resource matches
     * with an exclusion pattern this file won't be synced vfsFolders are
     * separated with a ","
     *
     * @param rfsFolder
     * @param vfsFolder
     * @param exclusionFile
     * @throws CmsSynchronizeException
     * @throws CmsException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void syncRFSandVFSExclusionFile(String rfsFolder, String vfsFolders, String exclusionFile) throws CmsSynchronizeException, CmsException,
            FileNotFoundException, IOException, Exception {
        File rfsDir = new File(rfsFolder);
        if (new File(exclusionFile).exists()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(exclusionFile))));
            String exclusionPattern = "";
            while ((exclusionPattern = reader.readLine()) != null) {
                OpenCms.getWorkplaceManager().addSynchronizeExcludePattern(exclusionPattern);
            }
        }
        syncRFSandVFSMultipleFolders(rfsFolder, vfsFolders);
    }

    /**
     * publishes a resource
     *
     * @param resourcename
     * @param siblings
     * @throws Exception
     */
    public void publishResource(String resourcename, boolean siblings) throws Exception {
        OpenCms.getPublishManager().publishResource(m_cms, resourcename, siblings, new CmsShellReport(m_cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * publishes resources, resourcenames are separated with a ","
     *
     * @param resourcename
     * @param siblings
     * @throws Exception
     */
    public void publishResources(String resourcenames, boolean siblings) throws Exception {
        String[] resources = resourcenames.split(",");
        for (String resourcename : resources) {
            if (!m_cms.existsResource(resourcename)) {
                System.err.println("Publish aborted cause resource: " + resourcename + "don't exist");
                System.err.println("NOTE: Resources have to be separated with a \",\"");
                System.exit(1);
            }
        }
        for (String resourcename : resources) {
            publishResource(resourcename, siblings);
        }
    }

    /**
     * Imports a module.
     *
     * Parameters: importFile the absolute path of the import module file
     * Throws: java.lang.Exception if something goes wrong See also:
     * org.opencms.importexport.CmsImportExportManager.importData(org.opencms.file.CmsObject,org.opencms.report.I_CmsReport,org.opencms.importexport.CmsImportParameters)
     * https://github.com/alkacon/opencms-core/blob/build_8_5_2/src/org/opencms/main/CmsShellCommands.java
     */
    private void importModule(String importFile) throws Exception {
        CmsImportParameters params = new CmsImportParameters(importFile, "/", true);
        OpenCms.getImportExportManager().importData(
                m_cms,
                new CmsShellReport(m_cms.getRequestContext().getLocale()),
                params);
    }

    /**
     * Imports multiple modules NOTE: The paths have to be delimited with a ","
     * and the paths to the importFiles should be absolute
     *
     * @param delimitedImportFiles
     * @throws Exception
     */
    public void importModules(String delimitedImportFiles) throws Exception {
        String[] filePaths = delimitedImportFiles.split(",");
        boolean existAllFiles = true;
        String noFileFound = "";
        for (String filePath : filePaths) {
            File importFile = new File(filePath);
            if (!importFile.exists()) {
                noFileFound = filePath;
                existAllFiles = false;
                break;
            }
        }
        if (!existAllFiles) {
            System.err.println("Import Modules aborted!");
            System.err.println("Can't find: " + noFileFound);
            System.err.println("NOTE: Import modules have to be separated with a \",\"");
        } else {
            for (String filePath : filePaths) {
                importModule(filePath);
            }
        }
    }

    /**
     * Exports the module with the given name to the default location and
     * modifies the version number of the module
     *
     * @param moduleName the name of the module to export
     * @throws Exception if something goes wrong * copied from
     * https://github.com/alkacon/opencms-core/blob/build_8_5_2/src/org/opencms/main/CmsShellCommands.java
     */
    public void exportModuleWithVersion(String moduleName, String version) throws Exception {
        CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
        module.getVersion().setVersion(version);
        OpenCms.getModuleManager().updateModule(m_cms, module);

        if (module == null) {
            throw new CmsDbEntryNotFoundException(Messages.get().container(Messages.ERR_UNKNOWN_MODULE_1, moduleName));
        }

        String filename = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
                OpenCms.getSystemInfo().getPackagesRfsPath()
                + CmsSystemInfo.FOLDER_MODULES
                + moduleName
                + "_"
                + OpenCms.getModuleManager().getModule(moduleName).getVersion().toString());

        String[] resources = new String[module.getResources().size()];
        System.arraycopy(module.getResources().toArray(), 0, resources, 0, resources.length);

        // generate a module export handler
        CmsModuleImportExportHandler moduleExportHandler = new CmsModuleImportExportHandler();
        moduleExportHandler.setFileName(filename);
        moduleExportHandler.setAdditionalResources(resources);
        moduleExportHandler.setModuleName(module.getName().replace('\\', '/'));
        moduleExportHandler.setDescription(getMessages().key(
                Messages.GUI_SHELL_IMPORTEXPORT_MODULE_HANDLER_NAME_1,
                new Object[]{moduleExportHandler.getModuleName()}));

        // export the module
        OpenCms.getImportExportManager().exportData(
                m_cms,
                moduleExportHandler,
                new CmsShellReport(m_cms.getRequestContext().getLocale()));
    }

    /**
     * Exports a Resource from the Virtual File System
     *
     * @param vfsResource
     * @param targetPath
     * @param contentAge export only resources after that age(lower bound)
     * @param exportAccountData
     * @param exportAsFiles exports resource as ZIP if set to false
     * @param exportProjectData
     * @param exportResourceData
     * @param exportOnlyFilesForCurrentProject
     * @param includeSystemFolder
     * @param includeUnchangedResources
     * @param recursive
     * @param xmlValidation
     * @throws CmsConfigurationException
     * @throws CmsImportExportException
     * @throws CmsRoleViolationException
     */
    public void exportVFSResource(String vfsResource, String targetPath,
            boolean exportAccountData, boolean exportAsFiles, boolean exportProjectData,
            boolean exportResourceData, boolean exportOnlyFilesForCurrentProject,
            boolean includeSystemFolder, boolean includeUnchangedResources,
            boolean recursive, boolean xmlValidation)
            throws CmsConfigurationException,
            CmsImportExportException,
            CmsRoleViolationException,
            CmsException {

        CmsVfsImportExportHandler exportHandler = new CmsVfsImportExportHandler();

        CmsExportParameters exportParameters = new CmsExportParameters();
        exportParameters.setExportAccountData(exportAccountData);
        exportParameters.setExportAsFiles(exportAsFiles);
        exportParameters.setExportProjectData(exportProjectData);
        exportParameters.setExportResourceData(exportResourceData);
        exportParameters.setInProject(exportOnlyFilesForCurrentProject);
        exportParameters.setIncludeSystemFolder(includeSystemFolder);
        exportParameters.setIncludeUnchangedResources(includeUnchangedResources);
        exportParameters.setPath(targetPath);
        exportParameters.setRecursive(recursive);
        exportParameters.setXmlValidation(xmlValidation);

        //Get resources in a specific time range (last modified)
        List<String> resources = new ArrayList<String>();
        exportParameters.setResources(resources);
        exportHandler.setExportParams(exportParameters);
        OpenCms.getImportExportManager().exportData(m_cms, exportHandler,
                new CmsShellReport(m_cms.getRequestContext().getLocale()));
    }

    /**
     *
     * @param cms
     * @param shell
     */
    @Override
    public void initShellCmsObject(CmsObject cms, CmsShell shell) {
        m_cms = cms;
        m_shell = shell;
    }

    /**
     *
     */
    @Override
    public void shellExit() {
        System.out.println();
        System.out.println(getMessages().key(Messages.GUI_SHELL_GOODBYE_0));
    }

    /**
     *
     */
    @Override
    public void shellStart() {
        System.out.println();
        System.out.println(getMessages().key(Messages.GUI_SHELL_WELCOME_0));
        System.out.println();

        // print the version information
        version();
        // print the copyright message
        copyright();
        // print the help information
        help();
    }

    /**
     * Returns the version information for this OpenCms instance.<p>
     */
    public void version() {
        System.out.println();
        System.out.println(getMessages().key(Messages.GUI_SHELL_VERSION_1, OpenCms.getSystemInfo().getVersionNumber()));
    }

    /**
     * Prints the OpenCms copyright information.<p>
     */
    public void copyright() {
        String[] copy = Messages.COPYRIGHT_BY_ALKACON;
        for (int i = 0; i < copy.length; i++) {
            System.out.println(copy[i]);
        }
    }

    /**
     * Provides help information for the CmsShell.<p>
     */
    public void help() {
        System.out.println();
        System.out.println(getMessages().key(Messages.GUI_SHELL_HELP1_0));
        System.out.println(getMessages().key(Messages.GUI_SHELL_HELP2_0));
        System.out.println(getMessages().key(Messages.GUI_SHELL_HELP3_0));
        System.out.println(getMessages().key(Messages.GUI_SHELL_HELP4_0));
        System.out.println();
    }

    protected CmsMessages getMessages() {
        return m_shell.getMessages();
    }
}
