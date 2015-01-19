/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package resourcetypes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.ade.configuration.CmsConfigurationReader;
import org.opencms.configuration.CmsConfigurationException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;
import projectconstants.ProjectConstants;
import static projectconstants.ProjectConstants.KEY_PREFIX_DESCRIPTION;
import static projectconstants.ProjectConstants.KEY_PREFIX_NAME;
import static projectconstants.ProjectConstants.KEY_PREFIX_TITLE;
import static projectconstants.ProjectConstants.PATH_I18N;
import static projectconstants.ProjectConstants.PROPERTIES_ENCODING;
import static projectconstants.ProjectConstants.PROPERTIES_FILE_NAME;
import static projectconstants.ProjectConstants.SAMPLE_SCHEMA_TYPE_NAME;

/**
 *
 * @author Tom
 */
public class CmsNewResourceTypeProcedure {

    /**
     * Copies the sample formatter JSP, creates the associated formatter and
     * module configuration.<p>
     *
     * @param isSchema
     * @param module
     * @param cmsVersion
     * @param m_cms
     * @param moduleFolder the module folder name
     * @param m_resInfo
     *
     * @throws CmsIllegalArgumentException in case something goes wrong copying
     * the resources
     * @throws CmsException in case something goes wrong copying the resources
     * @throws java.io.UnsupportedEncodingException
     */
    public static void createSampleFiles(boolean isSchema, CmsModule module, String cmsVersion, CmsObject m_cms, String moduleFolder, CmsResourceTypeInfoBean m_resInfo, String ide_project_dir, String iconPath) throws CmsIllegalArgumentException, CmsException, UnsupportedEncodingException, IOException {
        OpenCmsVersion version = OpenCmsVersion.getEnum(cmsVersion);
        switch (version) {
            case V850:
                copyFiles_8x(isSchema, module, m_cms, moduleFolder, m_resInfo, ide_project_dir, iconPath);
                break;
            case V851:
                copyFiles_8x(isSchema, module, m_cms, moduleFolder, m_resInfo, ide_project_dir, iconPath);
                break;
            case V852:
                copyFiles_8x(isSchema, module, m_cms, moduleFolder, m_resInfo, ide_project_dir, iconPath);
                break;
            default:
                break;
        }

    }

    private static void copyFiles_8x(boolean isSchema, CmsModule module, CmsObject m_cms, String moduleFolder, CmsResourceTypeInfoBean m_resInfo, String ide_project_dir, String iconPath) throws CmsIllegalArgumentException, CmsException, UnsupportedEncodingException, IOException {
        if (isSchema) {
            copySampleSchemaFiles_8x(m_cms, module, moduleFolder, m_resInfo, ide_project_dir, iconPath);
        } else {
            createSampleFormatterFiles_8x(m_cms, moduleFolder, m_resInfo);
        }
    }

    public static void createSampleFormatterFiles_8x(CmsObject m_cms, String moduleFolder, CmsResourceTypeInfoBean m_resInfo) throws CmsException {
        String formatterFolder = CmsStringUtil.joinPaths(moduleFolder, ProjectConstants.PATH_FORMATTERS);
        if (!m_cms.existsResource(formatterFolder)) {
            m_cms.createResource(formatterFolder, CmsResourceTypeFolder.getStaticTypeId());
        }

        String formatterJSP = CmsStringUtil.joinPaths(formatterFolder, m_resInfo.getName() + "-formatter.jsp");
        if (!m_cms.existsResource(formatterJSP)) {
            m_cms.createResource(formatterJSP, CmsResourceTypeJsp.getJSPTypeId());
        }
        updateModuleConfig(m_cms, moduleFolder, m_resInfo);
    }

    private static void updateModuleConfig(CmsObject m_cms, String moduleFolder, CmsResourceTypeInfoBean m_resInfo) throws CmsException {
        String moduleConfig = CmsStringUtil.joinPaths(moduleFolder, ".config");
        if (!m_cms.existsResource(moduleConfig)) {
            m_cms.createResource(
                    moduleConfig,
                    OpenCms.getResourceManager().getResourceType(CmsADEManager.MODULE_CONFIG_TYPE).getTypeId());
        }
        CmsFile moduleConfigFile = m_cms.readFile(moduleConfig);
        lockTemporary(m_cms, moduleConfigFile);
        CmsXmlContent moduleConfigContent = CmsXmlContentFactory.unmarshal(m_cms, moduleConfigFile);
        I_CmsXmlContentValue resourceTypeValue = moduleConfigContent.addValue(
                m_cms,
                CmsConfigurationReader.N_RESOURCE_TYPE,
                CmsConfigurationReader.DEFAULT_LOCALE,
                0);
        I_CmsXmlContentValue typeValue = moduleConfigContent.getValue(resourceTypeValue.getPath()
                + "/"
                + CmsConfigurationReader.N_TYPE_NAME, CmsConfigurationReader.DEFAULT_LOCALE);
        typeValue.setStringValue(m_cms, m_resInfo.getName());
        moduleConfigFile.setContents(moduleConfigContent.marshal());
        m_cms.writeFile(moduleConfigFile);
    }

    private static boolean createImage(String vfs_path, String iconPath, int width, int height, String img_type, CmsObject m_cms) throws IOException, CmsException {
        String resized_img_path = IconRenderer.renderIcon(iconPath, width, height, img_type);
        if (resized_img_path != null) {
            File res_file = new File(resized_img_path);
            if (res_file.exists()) {
                byte[] content = Files.readAllBytes(res_file.toPath());
                m_cms.createResource(vfs_path, CmsResourceTypeImage.getStaticTypeId());
                CmsFile resized = m_cms.readFile(vfs_path);
                resized.setContents(content);
                m_cms.writeFile(resized);
                res_file.delete();
                return true;
            }
        }
        return false;
    }

    /**
     * Adds the given messages to the workplace properties file.<p>
     *
     * @param messages the messages
     * @param propertiesFile the properties file
     *
     * @throws CmsException if writing the properties fails
     * @throws UnsupportedEncodingException in case of encoding issues
     */
    private static void addMessagesToPropertiesFile(CmsObject m_cms, Map<String, String> messages, CmsFile propertiesFile)
            throws CmsException, UnsupportedEncodingException {

        lockTemporary(m_cms, propertiesFile);
        StringBuilder contentBuffer = new StringBuilder();
        contentBuffer.append(new String(propertiesFile.getContents(), PROPERTIES_ENCODING));
        for (Map.Entry<String, String> entry : messages.entrySet()) {
            contentBuffer.append("\n");
            contentBuffer.append(entry.getKey());
            contentBuffer.append("=");
            contentBuffer.append(entry.getValue());
        }
        contentBuffer.append("\n");
        propertiesFile.setContents(contentBuffer.toString().getBytes(PROPERTIES_ENCODING));
        m_cms.writeFile(propertiesFile);
    }

    /**
     * Adds the explorer type messages to the modules workplace bundle.<p>
     *
     * @param m_cms
     * @param setting the explorer type settings
     * @param moduleFolder the module folder name
     * @param m_resInfo
     *
     * @throws CmsException if writing the bundle fails
     * @throws UnsupportedEncodingException in case of encoding issues
     */
    public static void addTypeMessages(CmsObject m_cms, CmsExplorerTypeSettings setting, String moduleFolder, CmsResourceTypeInfoBean m_resInfo)
            throws CmsException, UnsupportedEncodingException {

        Map<String, String> messages = new HashMap<String, String>();
        // check if any messages to set
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_resInfo.getNiceName())) {
            String key = KEY_PREFIX_NAME + m_resInfo.getName();
            messages.put(key, m_resInfo.getNiceName());
            setting.setKey(key);
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_resInfo.getDescription())) {
            String key = KEY_PREFIX_DESCRIPTION + m_resInfo.getName();
            messages.put(key, m_resInfo.getDescription());
            setting.setInfo(key);
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_resInfo.getTitle())) {
            String key = KEY_PREFIX_TITLE + m_resInfo.getName();
            messages.put(key, m_resInfo.getTitle());
            setting.setTitleKey(key);
        }
        if (!messages.isEmpty()) {
            String workplacePropertiesFile = CmsStringUtil.joinPaths(
                    moduleFolder,
                    ProjectConstants.PATH_CLASSES,
                    m_resInfo.getModuleName().replace(".", "/"),
                    PROPERTIES_FILE_NAME);
            if (m_cms.existsResource(workplacePropertiesFile)) {
                addMessagesToPropertiesFile(m_cms, messages, m_cms.readFile(workplacePropertiesFile));
            }
        }
    }

    /**
     * Locks the given resource temporarily.<p>
     *
     * @param resource the resource to lock
     *
     * @throws CmsException if locking fails
     */
    private static void lockTemporary(CmsObject m_cms, CmsResource resource) throws CmsException {
        CmsUser user = m_cms.getRequestContext().getCurrentUser();
        CmsLock lock = m_cms.getLock(resource);
        if (!lock.isOwnedBy(user)) {
            m_cms.lockResourceTemporary(resource);
        } else if (!lock.isOwnedInProjectBy(user, m_cms.getRequestContext().getCurrentProject())) {
            m_cms.changeLock(resource);
        }
    }

    public static CmsResourceTypeXmlContent getSchemaResourceXmlContent(CmsResourceTypeInfoBean m_resInfo) throws CmsConfigurationException {
        CmsResourceTypeXmlContent type = new CmsResourceTypeXmlContent();
        type.addConfigurationParameter(CmsResourceTypeXmlContent.CONFIGURATION_SCHEMA, m_resInfo.getSchema());
        type.setAdditionalModuleResourceType(true);
        type.setModuleName(m_resInfo.getModuleName());
        type.initConfiguration(
                m_resInfo.getName(),
                String.valueOf(m_resInfo.getId()),
                CmsResourceTypeXmlContent.class.getName());
        return type;
    }

    public static CmsExplorerTypeSettings getResourceExplorerTypeSettings(CmsResourceTypeInfoBean m_resInfo) {
        CmsExplorerTypeSettings setting = new CmsExplorerTypeSettings();
        setting.setTypeAttributes(
                m_resInfo.getName(),
                m_resInfo.getNiceName(),
                m_resInfo.getSmallIcon(),
                m_resInfo.getBigIcon(),
                "xmlcontent");
        setting.setNewResourceUri("newresource_xmlcontent.jsp?newresourcetype=" + m_resInfo.getName());
        setting.setNewResourcePage("structurecontent");
        setting.setAutoSetNavigation("false");
        setting.setAutoSetTitle("false");
        setting.setNewResourceOrder("10");
        setting.setAddititionalModuleExplorerType(true);
        return setting;
    }

    /**
     * Copies sample schema and resource type icons and adds the resources to
     * the module.<p>
     *
     * @param m_cms
     * @param module the module
     * @param moduleFolder the module folder name
     * @param m_resInfo
     * @param ide_project_dir
     *
     * @throws CmsIllegalArgumentException in case something goes wrong copying
     * the resources
     * @throws CmsException in case something goes wrong copying the resources
     * @throws java.io.UnsupportedEncodingException
     */
    public static void copySampleSchemaFiles_8x(CmsObject m_cms, CmsModule module, String moduleFolder, CmsResourceTypeInfoBean m_resInfo, String ide_project_dir, String iconPath)
            throws CmsIllegalArgumentException, CmsException, UnsupportedEncodingException, IOException {
        List<String> moduleResource = new ArrayList<String>(module.getResources());
        if (!m_cms.existsResource(moduleFolder)) {
            m_cms.createResource(moduleFolder, CmsResourceTypeFolder.getStaticTypeId());
            moduleResource.add(moduleFolder);
        }
        String schemaFolder = CmsStringUtil.joinPaths(moduleFolder, "schemas");
        if (!m_cms.existsResource(schemaFolder)) {
            m_cms.createResource(schemaFolder, CmsResourceTypeFolder.getStaticTypeId());
        }
        String schemaFile = CmsStringUtil.joinPaths(schemaFolder, m_resInfo.getName() + ".xsd");

        if (!m_cms.existsResource(schemaFile)) {
            m_cms.createResource(schemaFile, CmsResourceTypePlain.getStaticTypeId());
            String sample_rfs = ide_project_dir + "/" + "samples" + "/" + "sample-schema" + ".xsd";
            File sample_schema = new File(sample_rfs);
            if (sample_schema.exists()) {
                byte[] bytes = Files.readAllBytes(sample_schema.toPath());
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_resInfo.getSchemaTypeName())) {
                    // replace the sample schema type name with the provided name
                    CmsFile schema = m_cms.readFile(schemaFile);
                    String schemaContent = new String(bytes);
                    schemaContent = schemaContent.replaceAll(SAMPLE_SCHEMA_TYPE_NAME, m_resInfo.getSchemaTypeName());
                    schema.setContents(schemaContent.getBytes());
                    m_cms.writeFile(schema);
                }
            }
        }
        m_resInfo.setSchema(schemaFile);
        String filetypesFolder = "/system/workplace/resources/filetypes/";
        String smallIcon = CmsStringUtil.joinPaths(filetypesFolder, m_resInfo.getName() + ".png");
        if (!m_cms.existsResource(smallIcon)) {
            if (iconPath != null && !iconPath.isEmpty()) {
                boolean addIcon = createImage(smallIcon, iconPath, 16, 16, "png", m_cms);
                if (addIcon) {
                    moduleResource.add(smallIcon);
                    m_resInfo.setSmallIcon(m_resInfo.getName() + ".png");
                }
            }
        }
        String bigIcon = CmsStringUtil.joinPaths(filetypesFolder, m_resInfo.getName() + "_big.png");
        if (!m_cms.existsResource(bigIcon)) {
            if (iconPath != null || iconPath.isEmpty()) {
                boolean addIcon = createImage(bigIcon, iconPath, 24, 24, "png", m_cms);
                if (addIcon) {
                    moduleResource.add(bigIcon);
                    m_resInfo.setBigIcon(m_resInfo.getName() + "_big.png");
                }
            }
        }
        module.setResources(moduleResource);
    }

    private static void copyFileToIde(CmsObject m_cms, String vfs_path, String rfs_path, String filename) throws CmsException, IOException {
        BufferedOutputStream writer = null;
        if (m_cms.existsResource(vfs_path)) {
            CmsFile cms_file = m_cms.readFile(vfs_path);
            File rfs_file = new File(rfs_path + "/" + filename);
            File tmp = new File(rfs_path + "/" + "tmp_" + filename);
            if (tmp.exists()) {
                tmp.delete();
            }
            //temp
            tmp.createNewFile();
            writer = new BufferedOutputStream(new FileOutputStream(tmp));
            writer.write(cms_file.getContents());
            writer.close();

            //overwrite file
            writer = new BufferedOutputStream(new FileOutputStream(rfs_file, false));
            writer.write(cms_file.getContents());
            writer.close();

            tmp.delete();
        }
    }

    public static void copyFilesToIde(
            String ide_project_path,
            CmsObject m_cms,
            String moduleFolder,
            CmsResourceTypeInfoBean m_resInfo) throws CmsException, IOException {

        String formatterFolder = CmsStringUtil.joinPaths(moduleFolder, ProjectConstants.PATH_FORMATTERS);
        if (m_cms.existsResource(formatterFolder)) {
            String ide_formatters = ide_project_path + "/" + "Web" + "/" + "formatters";

            String formatterJSP = CmsStringUtil.joinPaths(formatterFolder, m_resInfo.getName() + "-formatter.jsp");
            copyFileToIde(m_cms, formatterJSP, ide_formatters, m_resInfo.getName() + "-formatter.jsp");

            String formatterConfig = CmsStringUtil.joinPaths(formatterFolder, m_resInfo.getName() + "-formatter-config.xml");
            copyFileToIde(m_cms, formatterConfig, ide_formatters, m_resInfo.getName() + "-formatter-config.xml");
        }

        String schemaFolder = CmsStringUtil.joinPaths(moduleFolder, "schemas");
        if (m_cms.existsResource(schemaFolder)) {
            String ide_schemas = ide_project_path + "/" + "Web" + "/" + "schemas";
            String schemaFile = CmsStringUtil.joinPaths(schemaFolder, m_resInfo.getName() + ".xsd");
            copyFileToIde(m_cms, schemaFile, ide_schemas, m_resInfo.getName() + ".xsd");
        }

        String vfsBundleFileName = CmsStringUtil.joinPaths(moduleFolder, PATH_I18N, m_resInfo.getModuleName() + ProjectConstants.SUFFIX_BUNDLE_FILE);
        String ide_i18n = ide_project_path + "/" + "Web" + "/" + "i18n";
        copyFileToIde(m_cms, vfsBundleFileName, ide_i18n, m_resInfo.getModuleName() + ProjectConstants.SUFFIX_BUNDLE_FILE);

        String moduleConfig = CmsStringUtil.joinPaths(moduleFolder, ".config");
        String ide_web = ide_project_path + "/" + "Web";
        copyFileToIde(m_cms, moduleConfig, ide_web, "config");
    }
}
