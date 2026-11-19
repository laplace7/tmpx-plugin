package at.laplace.tmpxplugin.ui;


import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import at.laplace.tmpxplugin.Constants;
import at.laplace.tmpxplugin.Messages;
import at.laplace.tmpxplugin.prefs.ProjectPrefenceHelper;

public class BuildPathPropertyPage extends PropertyPage {

	private ScopedPreferenceStore store;

	private FileFieldEditor compiler;
    private DirectoryFieldEditor workingDirectory;
    private FileFieldEditor inputFile;
    private DirectoryFieldEditor buildPath;
    private FileFieldEditor outputFile;
    private StringFieldEditor options;
	
	@Override
	protected Control createContents(Composite parent) {

		final IProject project = (IProject) super.getElement();

		final String projectPath = project.getLocation().toOSString();

		store = ProjectPrefenceHelper.getStore(project);
		
    	compiler = createCompiler(parent, projectPath);
    	workingDirectory = createWorkingDirectory(parent, projectPath);
    	inputFile = createInputFile(parent, projectPath);
    	buildPath = createBuildPath(parent, projectPath);
        outputFile = createOutputFile(parent, projectPath);
    	options = createCreateOptions(parent);

		return parent;
	}

    private FileFieldEditor createCompiler(Composite parent, String projectPath) {
        var  comp = new Composite(parent, SWT.NONE);
        var layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        comp.setLayout(layout);
        comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        var compiler = new FileFieldEditor(Constants.PROJECT_PREFS_COMPILER_KEY, Messages.COMPILER_NAME,
                                                       true, comp);
        compiler.setEmptyStringAllowed(true);
        compiler.setFileExtensions(new String[]{"", "*.exe"});
        compiler.setFilterPath(new File(projectPath));
        compiler.setPreferenceStore(store);
        compiler.load();
        return compiler;
	}
    
    private DirectoryFieldEditor createWorkingDirectory(Composite parent, String projectPath) {
        Composite buildPathComp = new Composite(parent, SWT.NONE);
        buildPathComp.setLayout(new GridLayout(3, false));
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        buildPathComp.setLayoutData(gridData);

        var cwd = new DirectoryFieldEditor(Constants.PROJECT_PREFS_CWD_KEY, Messages.COMPILER_CWD,  buildPathComp) {
            
            @Override
            public void setStringValue(String value) {
                value = tryMakeRelative(projectPath, value);
                super.setStringValue(value);
            }
            
        };
        
        cwd.setFilterPath(new File(projectPath));
        cwd.setPreferenceStore(store);
        cwd.load();
        
        return cwd;
        
    }
    
    private FileFieldEditor createInputFile(Composite parent, String projectPath) {
        var  comp = new Composite(parent, SWT.NONE);
        var layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        comp.setLayout(layout);
        comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        var inputFile = new FileFieldEditor(Constants.PROJECT_PREFS_INPUT_KEY, Messages.COMPILER_INPUT,
                                                       true, comp) {
            @Override
            public void setStringValue(
                    String value) {
                value = tryMakeRelative(projectPath, value);
                super.setStringValue(value);
            }
            
        };
        
        inputFile.setEmptyStringAllowed(true);
        inputFile.setFileExtensions(new String[]{"*.asm", "*.s"});
        inputFile.setFilterPath(new File(projectPath));
        inputFile.setPreferenceStore(store);
        inputFile.load();     
        return inputFile;
    }

    private DirectoryFieldEditor createBuildPath(Composite parent, String projectPath) {
        Composite buildPathComp = new Composite(parent, SWT.NONE);
        buildPathComp.setLayout(new GridLayout(3, false));
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        buildPathComp.setLayoutData(gridData);

        var buildPath = new DirectoryFieldEditor(Constants.PROJECT_PREFS_BUILD_DIRECTORY_KEY, Messages.COMPILER_BUILD_DIR,  buildPathComp){
            
            @Override
            public void setStringValue(String value) {
                value = tryMakeRelative(projectPath, value);
                super.setStringValue(value);
            }
            
        };
        
        buildPath.setFilterPath(new File(projectPath));
        buildPath.setPreferenceStore(store);
        buildPath.load();

        return buildPath;
    }

    private FileFieldEditor createOutputFile(Composite parent, String projectPath) {
        var  comp = new Composite(parent, SWT.NONE);
        var layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        comp.setLayout(layout);
        comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        var outputFile = new FileFieldEditor(Constants.PROJECT_PREFS_OUTPUT_KEY, Messages.COMPILER_OUTPUT,
                                                       true, comp) {
            @Override
            public void setStringValue(
                    String value) {
                value = tryMakeRelative(projectPath, value);
                super.setStringValue(value);
            }
            
        };
        
        outputFile.setEmptyStringAllowed(true);
        outputFile.setFileExtensions(new String[]{"", "*.prg"});
        outputFile.setFilterPath(new File(projectPath));
        outputFile.setPreferenceStore(store);
        outputFile.load();     
        return outputFile;
    }

    private StringFieldEditor createCreateOptions(Composite parent) {
        var comp = new Composite(parent, SWT.NONE);
        var layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        comp.setLayout(layout);
        comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        var options = new StringFieldEditor(Constants.PROJECT_PREFS_OPTIONS_KEY, Messages.COMPILER_OPTIONS, comp);
        options.setEmptyStringAllowed(true);
        options.setPreferenceStore(store);
        options.load();
        return options;
    }
    
    static String tryMakeRelative(String root, String path) {
        if (path == null) {
            return null;
        }
        
        if (path.startsWith(root)){
            path = path.substring(root.length());
            if (path.startsWith(File.separator)){
                path = path.substring(File.separator.length());
            }
        } 
        return path;
    }

	@Override
	protected void performApply() {
		doStore();
		super.performApply();
	}

	private void doStore() {
	    
        compiler.store();
	    workingDirectory.store();
	    inputFile.store();
	    buildPath.store();
	    outputFile.store();
	    options.store();
	    
		try {
			store.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean performOk() {
		doStore();
		return super.performOk();
	}

}
