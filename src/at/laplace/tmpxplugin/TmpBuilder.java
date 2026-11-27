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
package at.laplace.tmpxplugin;

import static at.laplace.tmpxplugin.ui.properties.ProjectPrefenceHelper.makeAbsolute;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.statushandlers.StatusManager;

import at.laplace.tmpxplugin.ui.properties.ProjectPrefenceHelper;

/**
 * Builder for ASM-Files.
 * 
 * @author Andy Reek
 * @since 25.11.2005
 */
public class TmpBuilder extends IncrementalProjectBuilder {

    private IProgressMonitor monitor;

    public TmpBuilder() {
        // Leave comments for debugging
        // Activator.getConsole().bringConsoleToFront();
        // Activator.getConsole().println("TmpBuilder created");
    }

    @Override
    protected IProject[] build(int kind, Map<String, String> args,
            IProgressMonitor monitor) throws CoreException {
        // Leave comments for debugging
        // Activator.getConsole().bringConsoleToFront();
        // Activator.getConsole().println("TmpBuilder build");

        this.monitor = monitor;

        try {
            IResourceDelta delta = getDelta(getProject());
            if (kind == IncrementalProjectBuilder.FULL_BUILD
                    || kind == IncrementalProjectBuilder.CLEAN_BUILD
                    || delta == null) {
                fullBuild(monitor);
            } else {
                incrementalBuild(delta, monitor);
            }
        } catch (CoreException ex) {
            Status status = new Status(Status.ERROR, Constants.PLUGIN_ID,
                    "Problems while compiling", ex);
            StatusManager.getManager().handle(status, StatusManager.SHOW);
        }

        return null;
    }

    /**
     * Builds the whole project.
     * 
     * @param monitor
     *            A monitor.
     * @throws CoreException
     */
    private void fullBuild(IProgressMonitor monitor) throws CoreException {
        IProject project = getProject();
        monitor.beginTask(Messages.BUILDING_TITLE, 100);
        monitor.subTask(Messages.BUILDING_TEXT_COMPILE);
        if (project.hasNature(Constants.NATURE_ID)) {
            compileProject(project);
        }
        monitor.done();
        project.refreshLocal(IResource.DEPTH_INFINITE, null);
    }

    @Override
    protected void clean(IProgressMonitor monitor) throws CoreException {
        var project = getProject();
        var settings = ProjectPrefenceHelper.getStore(project);
        String buildDir = ProjectPrefenceHelper.getBuildDir(settings);
        var output = ProjectPrefenceHelper.getOutput(settings);

        // only try to delete the known output and list file
        if (!output.isEmpty()) {
            var file = makeAbsolute(project, buildDir, output);
            deleteFile(file, monitor);
        }

        var listFile = ProjectPrefenceHelper.getListFile(settings).trim();
        if (!listFile.isEmpty()) {
            var file = makeAbsolute(project, buildDir, listFile);
            deleteFile(file, monitor);
        }
    }

    private void deleteFile(String file, IProgressMonitor monitor)
            throws CoreException {
        URI fileUri = Paths.get(file).toUri();
        for (var resource : ResourcesPlugin.getWorkspace().getRoot()
                .findFilesForLocationURI(fileUri)) {
            if (resource.exists()) {
                resource.delete(true, monitor);
            }
        }
    }

    /**
     * Builds only the project delta.
     * 
     * @param delta
     *            The given delta.
     * @param monitor
     *            A monitor.
     * @throws CoreException
     */
    private void incrementalBuild(IResourceDelta delta,
            IProgressMonitor monitor) throws CoreException {
        IProject project = getProject();
        monitor.beginTask(Messages.BUILDING_TITLE, 100);
        monitor.subTask(Messages.BUILDING_TEXT_COMPILE);
        var changeDetector = new MyIncrementalBuildVisitor();
        delta.accept(changeDetector);
        if (changeDetector.hasChanged) {
            compileProject(project);
        }
        monitor.done();
        project.refreshLocal(IResource.DEPTH_INFINITE, null);
    }

    /**
     * Compiles a given file.
     * 
     * @param file
     *            The file to be compiled.
     * @throws CoreException
     * @throws IOException
     */
    private void compileProject(IProject project) throws CoreException {

        var settings = ProjectPrefenceHelper.getStore(project);
        String buildDir = ProjectPrefenceHelper.getBuildDir(settings);
        IFolder destFolder = project.getFolder(buildDir);

        if (!destFolder.exists()) {
            destFolder.create(IResource.NONE, true, null);
            destFolder.setDerived(true, null);
        }

        String cmdLine[] = createCommandLine(project);
        if (cmdLine == null) {
            Problems.deleteMarkers(project);
            Problems.createMarker(project, Messages.LAUNCH_CONFIG_ERROR, -1, -1, -1, IMarker.SEVERITY_ERROR);
            return;
        }

        var cwd = makeAbsolute(project, ProjectPrefenceHelper.getCwd(settings));
        new TmpLauncher().launch(cwd, cmdLine, project, monitor);
        try {
            destFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
        }
    }

    private static String[] createCommandLine(IProject project) {
        IPreferenceStore preferences = Activator.getDefault()
                .getPreferenceStore();
        IPreferenceStore settings = ProjectPrefenceHelper.getStore(project);

        String compiler = ProjectPrefenceHelper.getCompiler(settings);
        if (compiler == null || (compiler.trim().isEmpty()))
            compiler = preferences
                    .getString(Constants.PREFERENCES_COMPILER_NAME);

        if ((compiler == null) || (compiler.trim().isEmpty()))
            return null;

        compiler = compiler.trim();
        File f = new File(compiler);
        if (!f.exists() || !f.canRead())
            return null;

        String buildDir = ProjectPrefenceHelper.getBuildDir(settings);
        if (buildDir == null || buildDir.trim().isEmpty()) {
            return null;
        }

        var input = ProjectPrefenceHelper.getInput(settings);
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        var output = ProjectPrefenceHelper.getOutput(settings);
        if (output == null || output.trim().isEmpty()) {
            return null;
        }

        // $tmpx -i $source -l $target/$program.lst -o $target/$program.prg
        var cmdArray = new ArrayList<String>();
        cmdArray.add(compiler);

        cmdArray.add("-i");
        cmdArray.add(input);

        var listFile = ProjectPrefenceHelper.getListFile(settings);
        if (!listFile.trim().isEmpty()) {
            var list = makeAbsolute(project, buildDir, listFile);
            cmdArray.add("-l");
            cmdArray.add(list);
        }

        cmdArray.add("-o");
        var target = makeAbsolute(project, buildDir, output);
        cmdArray.add(makeAbsolute(project, target));

        var options = ProjectPrefenceHelper.getOptions(settings).trim();
        if (!options.isEmpty()) {
            cmdArray.add(options);
        }

        return cmdArray.toArray(new String[0]);
    }

    /**
     * Class for visiting all resources when a incremental build is initiated.
     * 
     * @author andre
     * @since 25.11.2005
     */
    private class MyIncrementalBuildVisitor implements IResourceDeltaVisitor {

        boolean hasChanged;

        public boolean visit(IResourceDelta delta) throws CoreException {
            int deltaKind = delta.getKind();

            if ((deltaKind == IResourceDelta.ADDED)
                    || (deltaKind == IResourceDelta.CHANGED)) {
                IResource resource = delta.getResource();
                int resourceType = resource.getType();

                if (resourceType == IResource.FILE) {
                    hasChanged = true;
                    return false;
                } else if (resourceType == IResource.PROJECT) {
                    return resource.getProject().hasNature(Constants.NATURE_ID);
                } else if (resourceType == IResource.FOLDER) {
                    return true;
                }
            }

            return false;
        }
    }
}
