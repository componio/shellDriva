/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.opencms.main.CmsShell;
import org.opencms.main.I_CmsShellCommands;

/**
 *
 * @author Thomas
 */
public class CmsShellMain {

    /**
     * prints out how this class should be used with explanation of the different
     * arguments
     */
    public static void help() {
        System.out.println("Please use for example following Syntax:");
        System.out.println("CmsShellMain -webInf \"./WEB-INF\" -script \"script1.txt\" -additional \"package1.package2.ShellCommands\"");
        System.out.println("Following options are possible");
        System.out.println("-webInf \"Path to WebInf folder\" (mandatory)");
        System.out.println("-additional \"package path for class with additional shell commands\"");
        System.out.println("-script \"CmsShell script to be executed\" (mandatory)");
    }

    /**
     * executes a CmsShell script on the opencms system, which was passed as argument
     * following arguments have to be passed
     * -webInf "Path to WebInf folder" (mandatory)
       -additional "package path for class with additional shell commands"
       -script "CmsShell script to be executed" (mandatory)
     * @param args
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException {

        if (args.length == 0) {
            System.out.println("No arguments assigned");
            help();
            System.exit(1);
        }

        String webInf = "";
        String additionalClass = "";
        String scriptName = "";
        I_CmsShellCommands additionalShellCommands = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-webInf")) {
                webInf = args[i + 1];
            }
            if (args[i].equalsIgnoreCase("-script")) {
                scriptName = args[i + 1];
            }
            if (args[i].equalsIgnoreCase("-additional")) {
                additionalClass = args[i + 1];
            }
        }
        if (webInf.isEmpty() || !(new File(webInf)).exists()) {
            System.out.println("WebInf Folder does not exist: " + webInf);
            help();
            System.exit(1);
        }
        if (scriptName.isEmpty() || !(new File(scriptName)).exists()) {
            System.out.println("Script does not exist: " + scriptName);
            help();
            System.exit(1);
        }
        if (!additionalClass.isEmpty()) {
            try {
                Class<?> additional = Class.forName(additionalClass);
                additionalShellCommands = (I_CmsShellCommands) additional.newInstance();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                help();
                System.exit(1);
            }
        }

        CmsShell shell = new CmsShell(webInf, null, null, "${user}@${project}>", additionalShellCommands);
        File script = new File(scriptName);
        FileInputStream stream = new FileInputStream(script);
        shell.start(stream);
        shell.exit();
        System.exit(0);
    }
}
