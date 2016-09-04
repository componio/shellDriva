/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.componio.opencms.shell.plugin.utility;

import org.opencms.main.CmsShell;
import org.opencms.main.I_CmsShellCommands;

/**
 *
 * @author thomas
 */
public class UtilityHelper {
    
    public static CmsShell getShell(String web_inf, String servlet_mapping, String additional_commands) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        CmsShell shell = null;
        I_CmsShellCommands commands = getShellCommands(additional_commands);
        shell = new CmsShell(web_inf, (servlet_mapping.isEmpty() ? null : servlet_mapping), null, "${user}@${project}>", commands);
        return shell;
    }
    
    public static I_CmsShellCommands getShellCommands(String additional_commands) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        I_CmsShellCommands result = null;
        if (additional_commands != null && !additional_commands.isEmpty()) {   
                Class<?> additional = Class.forName(additional_commands);
                result = (I_CmsShellCommands) additional.newInstance();           
        }
        return result;
    }
    
}
