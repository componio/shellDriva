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
@Mojo(name = "cms_shell_script") 
public class CmsShellScriptMojo extends AbstractMojo {
    
    /**
     * The path to the WEB-INF folder of the OpenCms instance
     */
    @Parameter
    private String web_inf;
    /**
     * cms-shell-script
     */
    @Parameter
    private String cms_script;
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
        File script = new File(cms_script);
        FileInputStream stream;
        try {
            stream = new FileInputStream(script);
            shell.execute(stream);
            shell.exit();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CmsShellScriptMojo.class.getName()).log(Level.SEVERE,"File not found: " + cms_script , ex);
        }
    }   
}
