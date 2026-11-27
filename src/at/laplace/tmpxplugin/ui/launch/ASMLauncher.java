/*
 Kick Assembler plugin - An Eclipse plugin for convenient Kick Assembling
 Copyright (c) 2012 - P-a Backstrom <pa.backstrom@gmail.com>

 Based on ASMPlugin - http://sourceforge.net/projects/asmplugin/
 Copyright (c) 2006 - Andy Reek, D. Mitte

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */ 
package at.laplace.tmpxplugin.ui.launch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jface.preference.IPreferenceStore;

import at.laplace.tmpxplugin.Activator;
import at.laplace.tmpxplugin.Constants;


/**
 * Launcher for executable files.
 * 
 * @author Andy Reek
 * @since 13.02.2006
 */
public class ASMLauncher extends LaunchConfigurationDelegate {

	/**
	 * {@inheritDoc}
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {	    
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		String debugger = store.getString(Constants.PREFERENCES_DEBUGGER_NAME);
		String params = store.getString(Constants.PREFERENCES_DEBUGGER_PARAMS);

		if (mode.equalsIgnoreCase("debug") && (debugger == null || debugger.trim().isEmpty())) {
		    IStatus status = Status.error("Debugger not set!");
            throw new CoreException(status);
		}

		if (params == null) {
			params = "";
		}

		String launchWorkingDirectory = configuration.getAttribute(Constants.LAUNCH_WORKING_DIRECTORY, "");
		if (launchWorkingDirectory == null || (launchWorkingDirectory = launchWorkingDirectory.trim()).isEmpty()){
            IStatus status = Status.error("Working directory not set!");
            throw new CoreException(status);
		}

		var launchFile = new File(configuration.getAttribute(Constants.LAUNCH_FILE, ""));

		if (!launchFile.isAbsolute()) {
		    launchFile = new File(launchWorkingDirectory, launchFile.getPath());
		}
		
		if (!launchFile.canRead()) {
            IStatus status = Status.error("Launch file is not readable: " + launchFile);
            throw new CoreException(status);
		}
		
		List<String> cmds = new ArrayList<String>();
		cmds.add(debugger.trim());

		for (String param: params.trim().split("\\n")){
		    param = param.trim();
			if (!param.isEmpty()){
				cmds.add(param);
			}
		}

		File configFile = new File(launchWorkingDirectory, "moncommands.vice");
        if (writeMonitorCommands(configFile, mode, launchFile)) {
            cmds.add("-moncommands");
            cmds.add(configFile.getAbsolutePath());
        }

		cmds.add(launchFile.getPath());

		Process process = null;
		try {
	        var commandArray = cmds.toArray(new String[0]);
		    process = Runtime.getRuntime().exec(commandArray, null, new File(launchWorkingDirectory));
		} catch(IOException e) {
		    var command = cmds.stream().collect(Collectors.joining(" "));
            IStatus status = Status.error("Error launching command: " + command);
            throw new CoreException(status);		    
		}
		
        IProcess eclipseProcess = DebugPlugin.newProcess(launch, process, "VICE", new HashMap<>());
        launch.addProcess(eclipseProcess);
	}
	
	private static boolean writeMonitorCommands(File file, String mode, File launchFile) {
        String vs = launchFile.getPath().replace(".prg", ".vs").replace(".PRG", ".VS");
        File vsFile = new File(vs);
        if (vsFile.exists() || mode.equalsIgnoreCase("debug")){
            if (file.exists()){
                file.delete();
            }
            
            try (var fileWriter = new FileWriter(file);
                    var writer = new BufferedWriter(fileWriter)) {
    
                if (vsFile.exists()){
                    writer.write("ll \"");
                    writer.write(vs);
                    writer.write("\"");
                    writer.newLine();
                }
    
                if (mode.equalsIgnoreCase("debug")){
                    writer.write("break .breakpoint");
                    writer.newLine();
                    for (int i = 1; i < 9; i++){
                        writer.write("break .breakpoint"+i);
                        writer.newLine();
                    }
                }
                writer.flush();
                return true;
            } catch (IOException e) {
                Activator.getDefault().getLog().log(
                        new Status(Status.INFO, Constants.PLUGIN_ID, "Error" + e.getMessage()));
                return false;
            }
        }	 
        return false;
	}

}
