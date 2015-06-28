/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reverseSyncProcessor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import projectconstants.ProjectConstants;

/**
 *
 * @author Tom
 */
public class ReverseSyncHelper {

    public static void reverseSync(CmsObject m_cms, String rfsResource, String modulename) throws CmsException, IOException {
        File rfsPath = new File(rfsResource);
        String pathPrefix = modulename + "/" + "Web";
        String subPath = rfsResource.substring(rfsResource.indexOf(pathPrefix));
        subPath = subPath.replace(pathPrefix, "");
        String vfsPath = ProjectConstants.PATH_MODULES + "/" + modulename + subPath;
        if (rfsPath.exists() && m_cms.existsResource(vfsPath)) {
            copyVfsFilesToRfsFiles(m_cms, vfsPath, modulename, rfsResource);
        } else {
            System.err.println("!!! Reverse Sync Failed ");
            System.err.println("Path: " + vfsPath + "or ");
            System.err.println("Path: " + rfsResource + "don't exist !!!");
            System.exit(1);
        }
    }

    /**
     * Return a mapping between the resources in the VFS and the RFS with a
     * given prefix. Key : vfsPath
     *
     * @param resources
     * @param modulepath
     * @param modulename
     * @param rfsPrefix
     * @return
     */
    private static LinkedHashMap<String, String> mapVFStoRFS(
            final List<CmsResource> resources,
            final String modulename,
            final String rfsPrefix) {
        String rootPath;
        String vfsPrefix = ProjectConstants.PATH_MODULES + modulename;
        LinkedHashMap<String, String> mapping = new LinkedHashMap<String, String>();
        if (resources == null || resources.isEmpty()) {
            return mapping;
        }
        for (CmsResource resource : resources) {
            rootPath = resource.getRootPath();
            String subPath = CmsStringUtil.getRelativeSubPath(vfsPrefix, rootPath);
            String rfsPath = rfsPrefix + subPath;
            mapping.put(rootPath, rfsPath);
        }
        return mapping;
    }

    private static void copyVfsFilesToRfsFiles(CmsObject m_cms, String vfsPath, String modulename, String rfsPrefix) throws CmsException, IOException {
        List<CmsResource> resources = m_cms.readResources(vfsPath, CmsResourceFilter.ALL);
        LinkedHashMap<String, String> mapping = mapVFStoRFS(resources, modulename, rfsPrefix);

        for (String key : mapping.keySet()) {
            CmsResource resource = m_cms.readResource(key);
            if (resource.isFolder()) {
                File rfsDir = new File(mapping.get(key));
                if (!rfsDir.exists()) {
                    rfsDir.mkdir();
                }
            } else {
                copyVfsFileToRfsFile(m_cms, mapping.get(key), vfsPath);
            }
        }
    }

    private static void copyVfsFileToRfsFile(CmsObject m_cms, String rfsPath, String vfsPath) throws CmsException, IOException {
        File rfsFile = new File(rfsPath);
        if (!rfsFile.exists()) {
            rfsFile.createNewFile();
        }
        if (m_cms.existsResource(vfsPath)) {
            CmsResource resource = m_cms.readResource(vfsPath);
            CmsFile file = m_cms.readFile(resource);
            File parentDir = rfsFile.getParentFile();
            File tmpFile = new File(parentDir.getAbsolutePath() + "/" + "reverseSync.tmp");
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            tmpFile.createNewFile();
            BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(tmpFile));
            writer.write(file.getContents());
            writer.flush();
            writer.close();
            if (tmpFile.exists()) {
                Files.move(tmpFile.toPath(), rfsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
