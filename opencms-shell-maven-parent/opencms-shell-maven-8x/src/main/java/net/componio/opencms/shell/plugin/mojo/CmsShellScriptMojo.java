/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.componio.opencms.shell.plugin.mojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.componio.opencms.shell.plugin.utility.UtilityHelper;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsShell;

/**
 *
 * @author thomas
 */
@Mojo(name = "cms_shell_script")
public class CmsShellScriptMojo extends AbstractMojo {

    /**
     * The path to the WEB-INF folder of the OpenCms instance
     */
    @Parameter(property = "web_inf", required = true)
    private String web_inf;
    /**
     * cms-shell-script
     */
    @Parameter(property = "cms_script", required = true)
    private String cms_script;
    /**
     * servlet mapping parameter for OpenCms
     */
    @Parameter(property = "servlet_mapping", required = false)
    private String servlet_mapping;
    /**
     * class with additional cms-shell-commands to inject (e.g.
     * foo.bar.ClassName)
     */
    @Parameter(property = "additional_command_class", required = false)
    private String additional_commands;

    public void execute() throws MojoExecutionException, MojoFailureException {
        CmsShell shell;
        try {
            //CmsLog.INIT = LogFactory.getLog("org.opencms.init");
            shell = UtilityHelper.getShell(web_inf, servlet_mapping, additional_commands);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CmsShellScriptMojo.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println(ex);
            return;
        } catch (IllegalAccessException ex) {
            Logger.getLogger(CmsShellScriptMojo.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println(ex);
            return;

        } catch (InstantiationException ex) {
            Logger.getLogger(CmsShellScriptMojo.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println(ex);
            return;
        }
        File script = new File(cms_script);
        FileInputStream stream;
        try {
            stream = new FileInputStream(script);
            shell.start(stream);
            shell.exit();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CmsShellScriptMojo.class.getName()).log(Level.SEVERE, "File not found: " + cms_script, ex);
        }
    }
}
