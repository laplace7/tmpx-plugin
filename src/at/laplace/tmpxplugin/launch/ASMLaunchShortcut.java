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
package at.laplace.tmpxplugin.launch;

import java.io.FileNotFoundException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.statushandlers.StatusManager;

import at.laplace.tmpxplugin.Activator;
import at.laplace.tmpxplugin.Constants;
import at.laplace.tmpxplugin.Messages;
import at.laplace.tmpxplugin.Patterns;
import at.laplace.tmpxplugin.prefs.ProjectPrefenceHelper;


/**
 * LaunchShortcut for executable files.
 * 
 * @author Andy Reek
 * @since 13.02.2006
 */
public class ASMLaunchShortcut implements ILaunchShortcut {

	/**
	 * {@inheritDoc}
	 */
	public void launch(ISelection selection, String mode) {
		if (!(selection instanceof IStructuredSelection structuredSelection)) {
			return;
		}

		Object firstSelection = structuredSelection.getFirstElement();

		if (!(firstSelection instanceof IFile)) {
			Activator.getDefault().getLog().log(
					new Status(Status.ERROR, Constants.PLUGIN_ID, Status.OK,
							Messages.LAUNCH_ERROR, new FileNotFoundException("Selection is not an IFile " + firstSelection)));
			return;
		}
		
		IFile file = (IFile) firstSelection;
		var extension = file.getFileExtension();
		boolean isProgram = extension != null && Patterns.EXTENSION_PATTERN_PROGRAM.matcher(extension).matches();
		boolean isSource = extension != null && Patterns.EXTENSION_PATTERN_MAINFILES.matcher(extension).matches();
		if (extension == null || !(isProgram || isSource)){
			Activator.getDefault().getLog().log(
					new Status(Status.ERROR, Constants.PLUGIN_ID, Status.OK,
							Messages.LAUNCH_ERROR, new FileNotFoundException("Selection does not have the right extension "  + file.getName())));
			return;
		}

        var project = file.getProject();
        var settings = ProjectPrefenceHelper.getStore(project);
        String buildDir = ProjectPrefenceHelper.getBuildDir(settings);        
        var output = ProjectPrefenceHelper.getOutput(settings);
        
        var absBuildDir = ProjectPrefenceHelper.makeAbsolute(project, buildDir);
        var absOutputFile = ProjectPrefenceHelper.makeAbsolute(project, buildDir, output);
        
        if (buildDir.isEmpty() || output.isEmpty()) {
            Status status = new Status(Status.ERROR, Constants.PLUGIN_ID, Messages.LAUNCH_CONFIG_ERROR);
            StatusManager.getManager().handle(status,StatusManager.SHOW);
            return;
        }

		try {
		    var config = findLaunchConfig(absBuildDir, absOutputFile);
		    if (config == null) {
		        var name = Messages.LAUNCH_CONFIG_NAME.formatted(output, project.getName());
		        config = createLaunchConfig(name, absBuildDir, absOutputFile);
		    }
			DebugUITools.launch(config, mode);
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(
					new Status(Status.ERROR, Constants.PLUGIN_ID, Status.OK,
							Messages.LAUNCH_ERROR, e));
		}
	}
	
	private ILaunchConfiguration findLaunchConfig(String buildDir, String output) throws CoreException {
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType type = manager.getLaunchConfigurationType(Constants.LAUNCH_CONFIGURATION_TYPE_ID);
        ILaunchConfiguration[] configs = manager.getLaunchConfigurations(type);

        for (ILaunchConfiguration config : configs) {
            if (config.getAttribute(Constants.LAUNCH_FILE, "").equalsIgnoreCase(output) &&
                config.getAttribute(Constants.LAUNCH_WORKING_DIRECTORY,"").equalsIgnoreCase(buildDir)
                    ) {
                return config;
            }
        }
        return null;
	}
	
	private ILaunchConfiguration createLaunchConfig(String name, String buildDir, String output) throws CoreException {
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType type = manager.getLaunchConfigurationType(Constants.LAUNCH_CONFIGURATION_TYPE_ID);
        ILaunchConfigurationWorkingCopy copy = type.newInstance(
                null,
                manager.generateUniqueLaunchConfigurationNameFrom(name));
        copy.setAttribute(Constants.LAUNCH_FILE, output);
        copy.setAttribute(Constants.LAUNCH_WORKING_DIRECTORY, buildDir);
        copy.setAttribute(Constants.LAUNCH_ARGUMENTS, "");
        ILaunchConfiguration config = copy.doSave();
        return config;
	}

	/**
	 * {@inheritDoc}
	 */
	public void launch(IEditorPart editor, String mode) {
		IEditorInput input = editor.getEditorInput();
		ISelection selection = new StructuredSelection(input.getAdapter(IFile.class));
		launch(selection, mode);
	}
}
