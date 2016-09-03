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
import java.util.Locale;
import java.util.Map;
import org.dom4j.Element;
import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.ade.configuration.CmsConfigurationReader;
import org.opencms.ade.configuration.formatters.CmsFormatterBeanParser;
import org.opencms.ade.configuration.formatters.CmsFormatterConfigurationCache;
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
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsVfsBundleManager;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.xml.content.CmsVfsBundleLoaderXml;
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
import static projectconstants.ProjectConstants.SAMPLE_FORMATTER;
import static projectconstants.ProjectConstants.SAMPLE_ICON_BIG;
import static projectconstants.ProjectConstants.SAMPLE_ICON_SMALL;
import static projectconstants.ProjectConstants.SAMPLE_SCHEMA;
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
    public static void createSampleFiles(boolean isSchema, CmsModule module, String cmsVersion, CmsObject m_cms, String moduleFolder, CmsResourceTypeInfoBean m_resInfo, String iconPath) throws CmsIllegalArgumentException, CmsException, UnsupportedEncodingException, IOException {
        OpenCmsVersion version = OpenCmsVersion.getEnum(cmsVersion);
        switch (version) {
            case V850:
                copyFiles_8x(isSchema, module, m_cms, moduleFolder, m_resInfo, iconPath);
                break;
            case V851:
                copyFiles_8x(isSchema, module, m_cms, moduleFolder, m_resInfo, iconPath);
                break;
            case V852:
                copyFiles_8x(isSchema, module, m_cms, moduleFolder, m_resInfo, iconPath);
                break;
            case V901:
                copyFiles_9x(isSchema, module, m_cms, moduleFolder, m_resInfo, iconPath);
                break;
            case V950:
                copyFiles_9x(isSchema, module, m_cms, moduleFolder, m_resInfo, iconPath);
                break;
            case V951:
                copyFiles_9x(isSchema, module, m_cms, moduleFolder, m_resInfo, iconPath);
                break;
            case V952:
                copyFiles_9x(isSchema, module, m_cms, moduleFolder, m_resInfo, iconPath);
                break;
            default:
                break;
        }
    }

    private static void copyFiles_8x(boolean isSchema, CmsModule module, CmsObject m_cms, String moduleFolder, CmsResourceTypeInfoBean m_resInfo, String iconPath) throws CmsIllegalArgumentException, CmsException, UnsupportedEncodingException, IOException {
        if (isSchema) {
            copySampleSchemaFiles_8x(m_cms, module, moduleFolder, m_resInfo, iconPath);
        } else {
            createSampleFormatterFiles_8x(m_cms, moduleFolder, m_resInfo);
        }
    }

    private static void copyFiles_9x(boolean isSchema, CmsModule module, CmsObject m_cms, String moduleFolder, CmsResourceTypeInfoBean m_resInfo, String iconPath) throws CmsIllegalArgumentException, CmsException, UnsupportedEncodingException, IOException {
        if (isSchema) {
            copySampleSchemaFiles_9x(m_cms, module, moduleFolder, m_resInfo, iconPath);
        } else {
            createSampleFormatter_9x(m_cms, moduleFolder, m_resInfo);
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

    public static void createSampleFormatter_9x(CmsObject m_cms, String moduleFolder, CmsResourceTypeInfoBean m_resInfo) throws CmsIllegalArgumentException, CmsException {
        String formatterFolder = CmsStringUtil.joinPaths(moduleFolder, ProjectConstants.PATH_FORMATTERS);
        if (!m_cms.existsResource(formatterFolder)) {
            m_cms.createResource(formatterFolder, CmsResourceTypeFolder.getStaticTypeId());
        }
        String formatterJSP = CmsStringUtil.joinPaths(formatterFolder, m_resInfo.getName() + "-formatter.jsp");
        if (!m_cms.existsResource(formatterJSP)) {
            m_cms.copyResource(SAMPLE_FORMATTER, formatterJSP, CmsResource.COPY_AS_NEW);
        }
        String formatterConfig = CmsStringUtil.joinPaths(formatterFolder, m_resInfo.getName() + "-formatter-config.xml");
        if (!m_cms.existsResource(formatterConfig)) {

            m_cms.createResource(
                    formatterConfig,
                    OpenCms.getResourceManager().getResourceType(CmsFormatterConfigurationCache.TYPE_FORMATTER_CONFIG).getTypeId());
            CmsFile configFile = m_cms.readFile(formatterConfig);
            CmsXmlContent configContent = CmsXmlContentFactory.unmarshal(m_cms, configFile);
            if (!configContent.hasLocale(CmsConfigurationReader.DEFAULT_LOCALE)) {
                configContent.addLocale(m_cms, CmsConfigurationReader.DEFAULT_LOCALE);
            }
            I_CmsXmlContentValue typeValue = configContent.getValue(
                    CmsFormatterBeanParser.N_TYPE,
                    CmsConfigurationReader.DEFAULT_LOCALE);
            typeValue.setStringValue(m_cms, m_resInfo.getName());
            I_CmsXmlContentValue formatterValue = configContent.getValue(
                    CmsFormatterBeanParser.N_JSP,
                    CmsConfigurationReader.DEFAULT_LOCALE);
            formatterValue.setStringValue(m_cms, formatterJSP);
            I_CmsXmlContentValue formatterNameValue = configContent.getValue(
                    CmsFormatterBeanParser.N_NICE_NAME,
                    CmsConfigurationReader.DEFAULT_LOCALE);
            formatterNameValue.setStringValue(
                    m_cms,
                    "Sample formatter for "
                    + (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_resInfo.getNiceName())
                            ? m_resInfo.getNiceName()
                            : m_resInfo.getName()));
            // set matching container width to '-1' to fit everywhere
            configContent.addValue(m_cms, CmsFormatterBeanParser.N_MATCH, CmsConfigurationReader.DEFAULT_LOCALE, 0);
            configContent.addValue(
                    m_cms,
                    CmsFormatterBeanParser.N_MATCH + "/" + CmsFormatterBeanParser.N_WIDTH,
                    CmsConfigurationReader.DEFAULT_LOCALE,
                    0);
            I_CmsXmlContentValue widthValue = configContent.getValue(CmsFormatterBeanParser.N_MATCH
                    + "/"
                    + CmsFormatterBeanParser.N_WIDTH
                    + "/"
                    + CmsFormatterBeanParser.N_WIDTH, CmsConfigurationReader.DEFAULT_LOCALE);
            widthValue.setStringValue(m_cms, "-1");

            // enable the formatter
            I_CmsXmlContentValue enabledValue = configContent.getValue(
                    CmsFormatterBeanParser.N_AUTO_ENABLED,
                    CmsConfigurationReader.DEFAULT_LOCALE);
            enabledValue.setStringValue(m_cms, Boolean.TRUE.toString());
            configFile.setContents(configContent.marshal());
            m_cms.writeFile(configFile);
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
            } else {
                String vfsBundleFileName = CmsStringUtil.joinPaths(moduleFolder, PATH_I18N, m_resInfo.getModuleName()
                        + ProjectConstants.SUFFIX_BUNDLE_FILE);
                CmsFile vfsBundle;
                if (m_cms.existsResource(vfsBundleFileName)) {
                    vfsBundle = m_cms.readFile(vfsBundleFileName);
                } else {
                    String bundleFolder = CmsStringUtil.joinPaths(moduleFolder, PATH_I18N);
                    if (!m_cms.existsResource(bundleFolder)) {
                        m_cms.createResource(bundleFolder, CmsResourceTypeFolder.getStaticTypeId());
                    }
                    CmsResource res = m_cms.createResource(
                            vfsBundleFileName,
                            OpenCms.getResourceManager().getResourceType(CmsVfsBundleManager.TYPE_XML_BUNDLE).getTypeId(),
                            null,
                            null);
                    m_cms.writeResource(res);
                    vfsBundle = m_cms.readFile(res);
                }
                addMessagesToVfsBundle(m_cms, messages, vfsBundle);
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

    /**
     * Adds the given messages to the vfs message bundle.<p>
     *
     * @param messages the messages
     * @param vfsBundleFile the bundle file
     *
     * @throws CmsException if something goes wrong writing the file
     */
    private static void addMessagesToVfsBundle(CmsObject m_cms, Map<String, String> messages, CmsFile vfsBundleFile) throws CmsException {
        lockTemporary(m_cms, vfsBundleFile);
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, vfsBundleFile);
        Locale locale = CmsLocaleManager.getDefaultLocale();
        if (!content.hasLocale(locale)) {
            content.addLocale(m_cms, locale);
        }
        Element root = content.getLocaleNode(locale);
        for (Map.Entry<String, String> entry : messages.entrySet()) {
            Element message = root.addElement(CmsVfsBundleLoaderXml.N_MESSAGE);
            Element key = message.addElement(CmsVfsBundleLoaderXml.N_KEY);
            key.setText(entry.getKey());
            Element value = message.addElement(CmsVfsBundleLoaderXml.N_VALUE);
            value.setText(entry.getValue());
        }
        content.initDocument();
        vfsBundleFile.setContents(content.marshal());
        m_cms.writeFile(vfsBundleFile);
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
        setting.setTypeAttributes(m_resInfo.getName(), m_resInfo.getNiceName(), m_resInfo.getSmallIcon());
        /*setting.setTypeAttributes(
                m_resInfo.getName(),
                m_resInfo.getNiceName(),
                m_resInfo.getSmallIcon(),
                m_resInfo.getBigIcon(),
                "xmlcontent");*/
        setting.setNewResourceUri("newresource_xmlcontent.jsp?newresourcetype=" + m_resInfo.getName());
        setting.setNewResourcePage("structurecontent");
        setting.setAutoSetNavigation("false");
        setting.setAutoSetTitle("false");
        setting.setNewResourceOrder("10");
        setting.setAddititionalModuleExplorerType(true);
        return setting;
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
     * Copies sample schema and resource type icons and adds the resources to
     * the module.<p>
     *
     * @param m_cms
     * @param module the module
     * @param moduleFolder the module folder name
     * @param m_resInfo
     *
     * @throws CmsIllegalArgumentException in case something goes wrong copying
     * the resources
     * @throws CmsException in case something goes wrong copying the resources
     * @throws java.io.UnsupportedEncodingException
     */
    public static void copySampleSchemaFiles_9x(CmsObject m_cms, CmsModule module, String moduleFolder, CmsResourceTypeInfoBean m_resInfo, String iconPath)
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
            m_cms.copyResource(SAMPLE_SCHEMA, schemaFile, CmsResource.COPY_AS_NEW);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_resInfo.getSchemaTypeName())) {
                // replace the sample schema type name with the provided name
                CmsFile schema = m_cms.readFile(schemaFile);
                OpenCms.getLocaleManager();
                String schemaContent = new String(schema.getContents(), CmsLocaleManager.getResourceEncoding(
                        m_cms,
                        schema));
                schemaContent = schemaContent.replaceAll(SAMPLE_SCHEMA_TYPE_NAME, m_resInfo.getSchemaTypeName());
                schema.setContents(schemaContent.getBytes());
                m_cms.writeFile(schema);
            }
        }
        m_resInfo.setSchema(schemaFile);
        String filetypesFolder = "/system/workplace/resources/filetypes/";
        String smallIcon = CmsStringUtil.joinPaths(filetypesFolder, m_resInfo.getName() + ".png");
        boolean addIcon = false;
        if (!m_cms.existsResource(smallIcon)) {
            if (iconPath == null || iconPath.isEmpty()) {
                m_cms.copyResource(SAMPLE_ICON_SMALL, smallIcon, CmsResource.COPY_AS_NEW);
                addIcon = true;
            } else {
                addIcon = createImage(smallIcon, iconPath, 16, 16, "png", m_cms);
            }
            if (addIcon) {
                moduleResource.add(smallIcon);

            }
        }

        m_resInfo.setSmallIcon(m_resInfo.getName() + ".png");

        String bigIcon = CmsStringUtil.joinPaths(filetypesFolder, m_resInfo.getName() + "_big.png");
        addIcon = false;
        if (!m_cms.existsResource(bigIcon)) {
            if (iconPath == null || iconPath.isEmpty()) {
                m_cms.copyResource(SAMPLE_ICON_BIG, bigIcon, CmsResource.COPY_AS_NEW);
                addIcon = true;
            } else {
                addIcon = createImage(bigIcon, iconPath, 24, 24, "png", m_cms);
            }
            if (addIcon) {
                moduleResource.add(bigIcon);
            }
        }
        m_resInfo.setBigIcon(m_resInfo.getName() + "_big.png");
        module.setResources(moduleResource);
    }

    /**
     * Copies sample schema and resource type icons and adds the resources to
     * the module.<p>
     *
     * @param m_cms
     * @param module the module
     * @param moduleFolder the module folder name
     * @param m_resInfo
     *
     * @throws CmsIllegalArgumentException in case something goes wrong copying
     * the resources
     * @throws CmsException in case something goes wrong copying the resources
     * @throws java.io.UnsupportedEncodingException
     */
    public static void copySampleSchemaFiles_8x(CmsObject m_cms, CmsModule module, String moduleFolder, CmsResourceTypeInfoBean m_resInfo, String iconPath)
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
