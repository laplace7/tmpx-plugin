package at.laplace.tmpxplugin.prefs;

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
        store.setDefault(Constants.PROJECT_PREFS_OPTIONS_KEY, "");
		store.setDefault(Constants.PROJECT_PREFS_SRC_DIRECTORY_KEY, "src");
		store.setDefault(Constants.PROJECT_PREFS_LIBDIR_DIRECTORY_KEY, "");
		return store;
	}
	
	public static String getCompiler(IPreferenceStore store) {
	    return store.getString(Constants.PROJECT_PREFS_COMPILER_KEY);
	}

	public static String getCwd(IPreferenceStore store) {
        return store.getString(Constants.PROJECT_PREFS_CWD_KEY);
    }
	
    public static String getInput(IPreferenceStore store) {
        return store.getString(Constants.PROJECT_PREFS_INPUT_KEY);
    }
    
    public static String getBuildDir(IPreferenceStore store) {
        return store.getString(Constants.PROJECT_PREFS_BUILD_DIRECTORY_KEY);
    }
    
    public static String getOutput(IPreferenceStore store) {
        return store.getString(Constants.PROJECT_PREFS_OUTPUT_KEY);
    }
    
    public static String getOptions(IPreferenceStore store) {
        return store.getString(Constants.PROJECT_PREFS_OPTIONS_KEY);
    }
    
}
