/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.componio.opencms.shell.plugin.utility;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.componio.opencms.shell.plugin.mojo.CmsShellCommandMojo;
import org.opencms.main.CmsShell;
import org.opencms.main.I_CmsShellCommands;

/**
 *
 * @author thomas
 */
public class UtilityHelper {
    
    public static CmsShell getShell(String web_inf, String servlet_mapping, String additional_commands) {
        CmsShell shell = null;
        I_CmsShellCommands commands = getShellCommands(additional_commands);
        shell = new CmsShell(web_inf, (servlet_mapping.isEmpty() ? null : servlet_mapping), null, "${user}@${project}>", commands);
        return shell;
    }
    
    public static I_CmsShellCommands getShellCommands(String additional_commands) {
        I_CmsShellCommands result = null;
        if (additional_commands != null && !additional_commands.isEmpty()) {
            try {
                Class<?> additional = Class.forName(additional_commands);
                result = (I_CmsShellCommands) additional.newInstance();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(CmsShellCommandMojo.class.getName()).log(Level.SEVERE, "Class not found", ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(CmsShellCommandMojo.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(CmsShellCommandMojo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }
    
}
