/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.componio.opencms.shell.plugin.mojo;

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
@Mojo(name = "cms_shell_command")
public class CmsShellCommandMojo extends AbstractMojo {

    /**
     * The path to the WEB-INF folder of the OpenCms instance
     */
    /**
     * @parameter expression="${project.build.directory}"
     */
    @Parameter(property = "web_inf", required = true)
    private String web_inf;
    /**
     * one or more cms-shell-commands
     */
    @Parameter(property = "cms_command", required = true)
    private String command;
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
            CmsLog.INIT = LogFactory.getLog("org.opencms.init");
            shell = UtilityHelper.getShell(web_inf, servlet_mapping, additional_commands);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CmsShellCommandMojo.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println(ex);
            return;
        } catch (IllegalAccessException ex) {
            Logger.getLogger(CmsShellCommandMojo.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println(ex);
            return;

        } catch (InstantiationException ex) {
            Logger.getLogger(CmsShellCommandMojo.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println(ex);
            return;
        }
        shell.execute(command);
        shell.exit();
    }
}
