/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.componio.opencms.shell.plugin.mojo;
import net.componio.opencms.shell.plugin.utility.UtilityHelper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
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
    @Parameter
    private String web_inf;
    /**
     * one or more cms-shell-commands
     */
    @Parameter
    private String command;
    /**
     * servlet mapping parameter for OpenCms
     */
    @Parameter
    private String servlet_mapping;
    /**
     * class with additional cms-shell-commands to inject (e.g. foo.bar.ClassName)
     */
    private String additional_commands;
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        CmsShell shell = UtilityHelper.getShell(web_inf, servlet_mapping, additional_commands);
        shell.execute(command);
        shell.exit();
    }   
}
