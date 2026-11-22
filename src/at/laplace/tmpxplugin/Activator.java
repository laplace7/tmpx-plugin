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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import at.laplace.tmpxplugin.editor.ASMEditor;
import at.laplace.tmpxplugin.ui.OutputConsole;


/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends AbstractUIPlugin implements BundleListener {

	private static Object init = null;

	/**
	 * The shared instance.
	 */
	private static Activator plugin;

	/**
	 * The TemplateStore.
	 */
	private static TemplateStore templateStore;

	/**
	 * The Output-Console.
	 */
	private static OutputConsole console = new OutputConsole();

	//Hack to init Constants class.
	@SuppressWarnings("unused")
	private static Constants constants = new Constants();

	private static AutocompletionCollector autocompletionCollector = new AutocompletionCollector();

	/**
	 * The constructor.
	 */
	public Activator() {
		super();
		plugin = this;
	}

	public AutocompletionCollector getAutoCompletionCollector(){
		return autocompletionCollector;
	}

	/**
	 * {@inheritDoc}
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this; 

		ResourcesPlugin.getWorkspace().addResourceChangeListener(autocompletionCollector);

		context.addBundleListener(this);

		ISaveParticipant saveParticipant = new TmpSaveParticipant();
		ISavedState lastState =
				ResourcesPlugin.getWorkspace().addSaveParticipant(Constants.PLUGIN_ID, saveParticipant);
		if (lastState != null) {
			lastState.processResourceChangeEvents(autocompletionCollector);
		}	
		
	}

	public void bundleChanged(BundleEvent arg0) {

		if (arg0.getType() == BundleEvent.STARTED ){

			boolean run = false;
			synchronized (this) {
				if (init == null){
					init = "INITIALIZED";
					run = true;
				}
			}

			if (run){
				Job job = new Job("Autocollector init") {
					protected IStatus run(IProgressMonitor monitor) {
						autocompletionCollector.init();  
						return Status.OK_STATUS;
					}

				};
				job.setPriority(Job.LONG);
				job.schedule(); 		
			}
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		context.removeBundleListener(this);
		plugin = null; 
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(autocompletionCollector);

	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return The plug-in.
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Loads and returns the TemplateStore.
	 * 
	 * @return The TemplateStore.
	 */
	public static TemplateStore getTemplateStore() {
		if (templateStore == null) {
			templateStore = new TemplateStore(ASMEditor.getContextTypeRegistry(), plugin.getPreferenceStore(),
					Constants.PROPERTY_TEMPLATES);
		}

		try {
			templateStore.load();
		} catch (IOException e) {
			plugin.getLog().log(new Status(Status.ERROR, Constants.PLUGIN_ID, Status.OK, Messages.LOADTEMPSTORE_ERROR, e));
		}

		return templateStore;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path The path.
	 * 
	 * @return The image descriptor.
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Constants.PLUGIN_ID, path);
	}

	/**
	 * Returns the absolut path of a entrie from the plugin's directory.
	 * 
	 * @param entrie a file or directory (don't use "dir1\dir2" or "dir1\file1")
	 * 
	 * @return Returns the path from the plugin.
	 */
	public static String getFilePathFromPlugin(String entrie) {
		URL url = null;
		IPath path = null;
		String result = "";

		Enumeration<URL> enu = Activator.getDefault().getBundle().findEntries("/", entrie, true);
		if (enu.hasMoreElements()) {
			url = (URL) enu.nextElement();
		}

		if (url == null) {
			return "";
		}

		try {
			path = new Path(FileLocator.toFileURL(url).getPath());
			result = path.makeAbsolute().toOSString();
		} catch (Exception e) {
			result = "";
		}

		return result;
	}

	/**
	 * Return the Output-Console object for text output.
	 * 
	 * @return Output-Console object
	 */
	public static OutputConsole getConsole() {
		return console;
	}
    
    static Attributes[] DEFAULT_ATTRIBUTES = {
            // dark theme
            /*
            attr(Constants.PREFERENCES_TEXTCOLOR_DEFAULT, 212, 212, 212, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_INSTRUCTION, 86, 156, 214, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_INSTRUCTION_BRANCH, 218, 112, 214, true, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_DIRECTIVE, 197, 134, 192, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_DIRECTIVE_BRANCH, 229, 192, 123, true, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_DIRECTIVE_BLOCK, 78, 201, 176, true, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_CONSTANT, 181, 206, 168, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_STRING, 206, 145, 120, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_MACRO, 86, 156, 214, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_OPERATOR, 160, 168, 176, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_COMMENT, 106, 153, 85, false, true),
             */
            // light theme
            attr(Constants.PREFERENCES_TEXTCOLOR_DEFAULT, 36, 41, 46, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_INSTRUCTION, 0, 92, 197, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_INSTRUCTION_BRANCH, 175, 0, 219, true, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_DIRECTIVE, 111, 66, 193, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_DIRECTIVE_BLOCK, 0, 112, 112, true, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_DIRECTIVE_BRANCH, 179, 89, 0, true, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_CONSTANT, 9, 134, 88, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_STRING, 163, 21, 21, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_MACRO, 0, 92, 197, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_OPERATOR, 90, 98, 106, false, false),
            attr(Constants.PREFERENCES_TEXTCOLOR_COMMENT, 106, 115, 125, false, true),
    };

	/**
	 * {@inheritDoc}
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		Display display = Display.getCurrent();
		
		for (var attr : DEFAULT_ATTRIBUTES) {
		    var color = new Color(display, attr.r, attr.g, attr.b);
		    var bold = attr.bold;
		    var italic = attr.italic;
	        String textAttribute = TextAttributeConverter.textAttributesToPreferenceData(color, bold, italic);
	        store.setDefault(attr.key, textAttribute);		    
		}

		store.setDefault(Constants.PREFERENCES_COMPILER_NAME,"tmpx");
		store.setDefault(Constants.PREFERENCES_COMPILER_AFO,false);
		store.setDefault(Constants.PREFERENCES_COMPILER_SYMBOLS, true);
		store.setDefault(Constants.PREFERENCES_COMPILER_VICESYMBOLS, true);

		store.setDefault(Constants.PROPERTY_TEMPLATES,
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						+ "<templates>"
						+ "<template autoinsert=\"true\" context=\"asm.editor.context\" deleted=\"false\" description=\"Basic Upstart.\" enabled=\"true\" name=\"BasicUpstart\">.pc = $$0801 \"Basic Upstart\"&#10;:BasicUpstart(start)&#10;.pc = $$0810 \"Program\"&#10;start:&#10;</template>"
						+ "</templates>");

		store.setDefault(Constants.PREFERENCES_COMPILER_PARAMS, "");
		store.setDefault(Constants.PREFERENCES_LINKER_NAME, "");
		store.setDefault(Constants.PREFERENCES_LINKER_PARAMS, "");
		store.setDefault(Constants.PREFERENCES_LINKER_EXT, "");
		store.setDefault(Constants.PREFERENCES_DEBUGGER_NAME, "x64");
		store.setDefault(Constants.PREFERENCES_DEBUGGER_PARAMS, "-truedrive");
	}
	
	record Attributes(String key, int r, int g, int b, boolean bold, boolean italic) {
	}
	
	static Attributes attr(String key, int r, int g, int b, boolean bold, boolean italic) {
	    return new Attributes(key, r, g, b, bold, italic);
	}

}
