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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public class TmpLauncher {

    public void launch(String cwd, String[] args, IProject project,
            IProgressMonitor monitor) throws CoreException {

        final var problems = new ArrayList<Problem>();

        try {
            Activator.getConsole().bringConsoleToFront();
            Activator.getConsole().println("Command: %s".formatted(
                    Stream.of(args).collect(Collectors.joining(" "))));
            Activator.getConsole().println("CWD: %s".formatted(cwd));

            clearProblems(project);

            ProcessBuilder pb = new ProcessBuilder(args);
            pb.directory(new File(cwd));
            pb.redirectErrorStream(true);

            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(),
                            StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Activator.getConsole().bringConsoleToFront();
                    Activator.getConsole().println(line);

                    addProblem(line, problems);

                    if (monitor.isCanceled()) {
                        process.destroyForcibly();
                        break;
                    }
                }
            }

            int exitCode = process.waitFor();
            Activator.getConsole().bringConsoleToFront();
            if (exitCode != 0) {
                Activator.getConsole().print("tmpx EXIT-Code " + exitCode);
            } else {
                Activator.getConsole().println("Done compiling");
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            Activator.getConsole().println("Error starting tmpx");
        } finally {
            markProblems(project, cwd, problems);
        }
    }

    record Problem(String file, int line, int col, int error, String message) {
    }

    private void addProblem(String text, List<Problem> problems) {
        var matcher = Patterns.ERROR_PATTERN.matcher(text);
        if (!matcher.matches()) {
            return;
        }
        try {
            var file = matcher.group(1);
            var line = Integer.parseInt(matcher.group(2)) - 1;
            var error = Integer.parseInt(matcher.group(3));
            var message = matcher.group(4);

            int col = -1;
            matcher = Patterns.AT_COL_PATTERN.matcher(message);
            if (matcher.matches()) {
                col = Integer.parseInt(matcher.group(1)) - 1;
            }
            var problem = new Problem(file, line, col, error, message);

            problems.add(problem);
        } catch (NumberFormatException e) {
            // ignored
        }
    }

    private void markProblems(IProject project, String cwd,
            List<Problem> problems) throws CoreException {
        for (var problem : problems) {
            URI fileUri = Paths.get(cwd + File.separator + problem.file)
                    .toUri();
            for (var resource : ResourcesPlugin.getWorkspace().getRoot()
                    .findFilesForLocationURI(fileUri)) {
                reportProblem(resource, problem.message, problem.line,
                        problem.col, problem.error, IMarker.SEVERITY_ERROR);
            }
        }
    }

    private void reportProblem(IFile resource, String message, int lineNumber,
            int column, int error, int severity) throws CoreException {
        IMarker marker = resource.createMarker(Constants.MARKER_ID);
        marker.setAttribute(IMarker.MESSAGE, message);
        marker.setAttribute(IMarker.MESSAGE, "Error " + error);
        marker.setAttribute(IMarker.SEVERITY, severity);
        if (lineNumber >= 0) {
            marker.setAttribute(IMarker.LINE_NUMBER, lineNumber + 1);
        }
        if (column == -1) {
            return;
        }

        IDocument document = getDocument(resource);
        if (document == null) {
            return;
        }
        try {
            var region = document.getLineInformation(lineNumber);
            if (column < region.getLength()) {
                int offset = region.getOffset() + column;
                marker.setAttribute(IMarker.CHAR_START, offset);
                marker.setAttribute(IMarker.CHAR_END, offset + 1);
            }
        } catch (BadLocationException e) {
            // ignored
        }
    }

    public static IDocument getDocument(IFile file) throws CoreException {
        ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
        IPath path = file.getFullPath();

        manager.connect(path, LocationKind.IFILE, new NullProgressMonitor());
        try {
            ITextFileBuffer buffer = manager.getTextFileBuffer(path,
                    LocationKind.IFILE);
            return buffer != null ? buffer.getDocument() : null;
        } finally {
            manager.disconnect(path, LocationKind.IFILE,
                    new NullProgressMonitor());
        }
    }

    private void clearProblems(IResource resource) throws CoreException {
        resource.deleteMarkers(Constants.MARKER_ID, true,
                IResource.DEPTH_INFINITE);
    }

}
