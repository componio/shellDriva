/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmis;

import java.util.HashMap;
import java.util.Map;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import java.io.File;
import org.opencms.workplace.CmsWorkplace;

/**
 *
 * @author Thomas
 */
public class CmisSyncModule {

    public static Session getATOMPubSession(String url, String user, String password, String repositoryId) {
        // default factory implementation
        SessionFactory factory = SessionFactoryImpl.newInstance();
        Map<String, String> parameter = new HashMap<String, String>();

        // user credentials
        parameter.put(SessionParameter.USER, user);
        parameter.put(SessionParameter.PASSWORD, password);

        // connection settings
        parameter.put(SessionParameter.ATOMPUB_URL, url);
        parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
        parameter.put(SessionParameter.REPOSITORY_ID, repositoryId);

        // create session
        return factory.createSession(parameter);
    }

    public static void help() {
        System.out.println("Please use for example following Syntax:");
        System.out.println("CmsSyncModule -user \"username\" -password \"password\" -repoUrl \"http://localhost:8080/opencms/cmisatom\" -repoId \"cmis-offline\" "
                + "-rfs \"./cmsSync/system/modules/newModule\" - vfs \"/system/modules/newModule\"");
        System.out.println("Following options are possible");
        System.out.println("-user \"username\" (mandatory)");
        System.out.println("-password \"password\" (mandatory)");
        System.out.println("-repoUrl \"URL to CMIS repository\" (mandatory)");
        System.out.println("-repoId \"Id of CMIS repository\" (mandatory)");
        System.out.println("-rfs \"Sync Folder to RFS system\" (mandatory)");
        System.out.println("-vfs \"Sync Folder to VFS system\" (mandatory)");
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("No arguments assigned");
            help();
            System.exit(1);
        }

        String user = "";
        String password = "";
        String repositoryId = "";
        String repositoryUrl = "";
        String rfsFolder = "";
        String vfsFolder = "";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-user")) {
                user = args[i + 1];
            }
            if (args[i].equalsIgnoreCase("-password")) {
                password = args[i + 1];
            }
            if (args[i].equalsIgnoreCase("-repoUrl")) {
                repositoryUrl = args[i + 1];
            }
            if (args[i].equalsIgnoreCase("-repoId")) {
                repositoryId = args[i + 1];
            }
            if (args[i].equalsIgnoreCase("-rfs")) {
                rfsFolder = args[i + 1];
            }
            if (args[i].equalsIgnoreCase("-vfs")) {
                vfsFolder = args[i + 1];
            }
        }

        if (user.isEmpty()) {
            System.out.println("Please declare a user");
            System.exit(1);
        }
        if (password.isEmpty()) {
            System.out.println("Please declare a password");
            System.exit(1);
        }
        if (repositoryUrl.isEmpty()) {
            System.out.println("Please declare an existing URL for the CMIS repository");
            System.exit(1);
        }
        if (repositoryId.isEmpty()) {
            System.out.println("Please declare an existing Id for the CMIS repository");
            System.exit(1);
        }
        if (rfsFolder.isEmpty()) {
            System.out.println("Please declare a sync folder for the RFS");
            System.exit(1);
        }
        if (vfsFolder.isEmpty()) {
            System.out.println("Please declare a sync folder for the VFS(Virtual File System)");
            System.exit(1);
        }

        Session atomSession = getATOMPubSession(repositoryUrl, user,
                password, repositoryId);

        File rfsDir = new File(rfsFolder);
        for (File f : rfsDir.listFiles()) {
            if (f.isDirectory()) {
                String rfsPath = f.getAbsolutePath().replace("\\", "/");
                String vfsPath = rfsPath.substring(rfsPath.indexOf(CmsWorkplace.VFS_PATH_MODULES));
                new CmisSyncThread(rfsPath, vfsPath, atomSession).start();
            }
        }
    }
}
