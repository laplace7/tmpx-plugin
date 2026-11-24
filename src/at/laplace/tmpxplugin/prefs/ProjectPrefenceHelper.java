package at.laplace.tmpxplugin.prefs;

import java.io.File;
import java.util.LinkedList;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import at.laplace.tmpxplugin.Constants;

public class ProjectPrefenceHelper {

	public static ScopedPreferenceStore getStore(IProject project){
		
		ProjectScope projectScope = new ProjectScope(project); 
				
		ScopedPreferenceStore store = new ScopedPreferenceStore(projectScope, "at.laplace.tmpxplugin");
        store.setDefault(Constants.PROJECT_PREFS_CWD_KEY, "src");
        store.setDefault(Constants.PROJECT_PREFS_INPUT_KEY, "main.asm");
        store.setDefault(Constants.PROJECT_PREFS_BUILD_DIRECTORY_KEY, "build");
        store.setDefault(Constants.PROJECT_PREFS_OUTPUT_KEY, "program.prg");
        store.setDefault(Constants.PROJECT_PREFS_LIST_FILE_KEY, "program.lst");
        store.setDefault(Constants.PROJECT_PREFS_OPTIONS_KEY, "");
		store.setDefault(Constants.PROJECT_PREFS_SRC_DIRECTORY_KEY, "src");
		store.setDefault(Constants.PROJECT_PREFS_LIBDIR_DIRECTORY_KEY, "");
		return store;
	}
	
	public static String getCompiler(IPreferenceStore store) {
	    return store.getString(Constants.PROJECT_PREFS_COMPILER_KEY).trim();
	}

	public static String getCwd(IPreferenceStore store) {
        return store.getString(Constants.PROJECT_PREFS_CWD_KEY).trim();
    }
	
    public static String getInput(IPreferenceStore store) {
        return store.getString(Constants.PROJECT_PREFS_INPUT_KEY).trim();
    }
    
    public static String getBuildDir(IPreferenceStore store) {
        return store.getString(Constants.PROJECT_PREFS_BUILD_DIRECTORY_KEY).trim();
    }
    
    public static String getOutput(IPreferenceStore store) {
        return store.getString(Constants.PROJECT_PREFS_OUTPUT_KEY).trim();
    }
    
    public static String getListFile(IPreferenceStore store) {
        return store.getString(Constants.PROJECT_PREFS_LIST_FILE_KEY).trim();
    }
    
    public static String getOptions(IPreferenceStore store) {
        return store.getString(Constants.PROJECT_PREFS_OPTIONS_KEY).trim();
    }
    
    public static String makeAbsolute(IProject project, String... parts) {
        int length = parts.length;
        if (length == 0) {
            throw new IllegalArgumentException("Missing parts");
        }
        
        var base = project.getLocationURI().getRawPath();
        var relativeParts = new LinkedList<String>();
        for (int i = length - 1; i >= 0; i --) {
            var path = parts[i];
            if (new File(path).isAbsolute()) {
                base = path;
                break;
            } else {
                relativeParts.add(0, path);
            }
        }
        
        if (relativeParts.isEmpty()) {
            return base;
        }
        return base + File.separator + relativeParts.stream().collect(Collectors.joining(File.separator));
    }

    
}
