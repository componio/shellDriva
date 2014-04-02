/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cmis;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.client.util.FileUtils;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.opencms.workplace.CmsWorkplace;

/**
 *
 * @author Thomas
 */
public class CmisSyncThread extends Thread {

    private final String rfsFolder;
    private final String vfsFolder;
    private final Session session;

    CmisSyncThread(String rfsFolder, String vfsFolder, Session session) {
        this.rfsFolder = rfsFolder;
        this.vfsFolder = vfsFolder;
        this.session = session;
    }

    public static boolean isFolder(String pathOrIdOfObject, Session session) {
        try {
            FileUtils.getFolder(pathOrIdOfObject, session);
        } catch (Throwable t) {
            return false;
        }
        return true;
    }

    public synchronized static boolean isObject(String pathOrIdOfObject, Session session) {
        try {
            FileUtils.getObject(pathOrIdOfObject, session);
        } catch (Throwable t) {
            return false;
        }
        return true;
    }

    private synchronized static String getVfsParentPathFromFile(File file) {
        String parentPath = file.getParent().replace("\\", "/");
        try {
            parentPath = parentPath.substring(parentPath.indexOf(CmsWorkplace.VFS_PATH_MODULES));
        } catch (Exception e) {
            return null;
        }
        return parentPath;
    }

    private synchronized static LinkedHashMap<String, Long> getLastModifiedTimeInMilliSecFromFileSystem(String rfsPath) {
        LinkedHashMap<String, Long> result = new LinkedHashMap<String, Long>();
        File root = new File(rfsPath);
        String path = "";
        if (root.exists() && root.isDirectory()) {
            path = root.getAbsolutePath().replace("\\", "/");
            String vfsPath = path.substring(path.indexOf(CmsWorkplace.VFS_PATH_MODULES));
            result.put(vfsPath, root.lastModified());

            for (File subfile : root.listFiles()) {
                if (subfile.isDirectory()) {
                    path = subfile.getAbsolutePath().replace("\\", "/");
                    LinkedHashMap<String, Long> subresult = getLastModifiedTimeInMilliSecFromFileSystem(path);
                    if (!subresult.isEmpty()) {
                        result.putAll(subresult);
                    }
                } else {
                    path = subfile.getAbsolutePath().replace("\\", "/");
                    vfsPath = path.substring(path.indexOf(CmsWorkplace.VFS_PATH_MODULES));
                    result.put(vfsPath, subfile.lastModified());
                }
            }
        }
        return result;
    }

    private synchronized static LinkedHashMap<String, Long> modifyFilesFromCMISRepository(String vfsPath, String rfsPrefixPath, LinkedHashMap<String, Long> rfsEntries, Session session) {
        StringBuffer buffer = null;
        if (isFolder(vfsPath, session)) {
            File update = null;
            String updatedFilePath = "";
            String folderPath = "";
            Folder vfs = FileUtils.getFolder(vfsPath, session);
            Folder folder = null;
            InputStream stream = null;
            ContentStream contentStream = null;
            OperationContext opcontext = new OperationContextImpl();
            String filterString = new StringBuffer()
                    .append(PropertyIds.BASE_TYPE_ID).append(",")
                    .append(PropertyIds.LAST_MODIFICATION_DATE).append(",")
                    .append(PropertyIds.NAME).append(",").toString();
            opcontext.setFilterString(filterString);
            opcontext.setIncludeAllowableActions(false);
            opcontext.setIncludePathSegments(false);
            opcontext.setIncludePolicies(false);
            opcontext.setIncludeRelationships(IncludeRelationships.NONE);
            opcontext.setCacheEnabled(true);

            for (CmisObject subfile : vfs.getChildren(opcontext)) {
                if (subfile.getBaseType().getBaseTypeId().value().equals(BaseTypeId.CMIS_FOLDER.value())) {
                    folder = (Folder) subfile;
                    folderPath = folder.getPath();
                    //last "/" cut of folderPath for better comparison between RFS and VFS in HashMap
                    folderPath = (folderPath.lastIndexOf("/") == (folderPath.length() - 1))
                            ? folderPath.substring(0, folderPath.length() - 1) : folderPath;
                    if (!rfsEntries.containsKey(folderPath)) {
                        //folder.deleteTree(true, UnfileObject.DELETE, true);
                        FileUtils.delete(folderPath, session);
                    }
                    rfsEntries = modifyFilesFromCMISRepository(folderPath, rfsPrefixPath, rfsEntries, session);
                    rfsEntries.remove(folderPath);
                } else if (subfile.getBaseType().getBaseTypeId().value().equals(BaseTypeId.CMIS_DOCUMENT.value())) {
                    buffer = new StringBuffer(vfs.getPath()).append(subfile.getName());
                    //delete file from VFS if not in RFS
                    if (!rfsEntries.containsKey(buffer.toString())) {
                        FileUtils.delete(buffer.toString(), session);
                        rfsEntries.remove(buffer.toString());
                        System.out.println("Deleted: " + buffer.toString());
                    } else {
                        //update file in VFS if needed
                        if (rfsEntries.get(buffer.toString()) > subfile.getLastModificationDate().getTimeInMillis()) {
                            updatedFilePath = new StringBuffer(rfsPrefixPath).append(buffer.toString()).toString();
                            update = new File(updatedFilePath);
                            try {
                                // set new content of File
                                stream = new BufferedInputStream(new FileInputStream(update));
                                contentStream = new ContentStreamImpl(buffer.toString(), BigInteger.valueOf(update.length()), MimeTypes.getMIMEType(update), stream);
                                ((Document) subfile).setContentStream(contentStream, true);

                            } catch (FileNotFoundException ex) {
                                Logger.getLogger(CmisSyncThread.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            rfsEntries.remove(buffer.toString());
                            System.out.println("Updated: " + buffer.toString());
                        }
                        rfsEntries.remove(buffer.toString());
                    }
                }
            }
            rfsEntries.remove(vfsPath);
        }
        return rfsEntries;
    }

    public synchronized static void syncFolders(String rfsFolder, String vfsFolder, Session session) {
        String prefixPath = rfsFolder.replace("\\", "/");
        prefixPath = prefixPath.substring(0, prefixPath.indexOf(CmsWorkplace.VFS_PATH_MODULES));
        LinkedHashMap<String, Long> rest = modifyFilesFromCMISRepository(vfsFolder, prefixPath, getLastModifiedTimeInMilliSecFromFileSystem(rfsFolder), session);

        //Add the rest of the RFS to the VFS
        for (String key : rest.keySet()) {
            String addedFilePath = new StringBuffer(prefixPath).append(key).toString();
            File added = new File(addedFilePath.toString());
            String parentPath = getVfsParentPathFromFile(added);
            if (parentPath != null && added.isFile()) {
                try {
                    FileUtils.createDocumentFromFile(parentPath, added,
                            BaseTypeId.CMIS_DOCUMENT.value(), VersioningState.NONE,
                            session);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(CmisSyncModule.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("Added: " + key);
            } else if (added.isDirectory()) {
                FileUtils.createFolder(parentPath, added.getName(),
                        BaseTypeId.CMIS_FOLDER.value(), session);
                System.out.println("Added: " + key);
            }
        }
    }

    @Override
    public void run() {
        syncFolders(rfsFolder, vfsFolder, session);
    }
}
