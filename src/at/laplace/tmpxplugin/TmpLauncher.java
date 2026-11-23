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
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class TmpLauncher {

	public void launch(String cwd, String[] args, IProject project, IProgressMonitor monitor) throws CoreException {
	    try {
            ProcessBuilder pb = new ProcessBuilder(args);
            pb.directory(new File(cwd));
            pb.redirectErrorStream(true);

            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Activator.getConsole().bringConsoleToFront();
                    Activator.getConsole().println(line);
                                        
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
        }
    }

}
