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
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import at.laplace.tmpxplugin.Constants;
import at.laplace.tmpxplugin.Messages;
import at.laplace.tmpxplugin.prefs.ProjectPrefenceHelper;

public class BuildPropertyPage extends PropertyPage {

    private static final String[] COMPILER_EXTENSIONS = { "*", "*.bat", "*.cmd", "*.exe", "*.ps1", "*.sh"  };
    private static final String[] INPUT_FILE_EXTENSIONS = { "*.asm", "*.s", "*.h" };
    private static final String[] OUTPUT_FILE_EXTENSIONS = { "*", "*.prg" };
    private static final String[] LIST_FILE_EXTENSIONS = { "*", "*.lst" };
    
	private ScopedPreferenceStore store;

	private FileFieldEditor compiler;
    private DirectoryFieldEditor workingDirectory;
    private FileFieldEditor inputFile;
    private DirectoryFieldEditor buildPath;
    private FileFieldEditor outputFile;
    private FileFieldEditor listFile;
    private StringFieldEditor options;
	
	@Override
	protected Control createContents(Composite parent) {

		final IProject project = (IProject) super.getElement();

		final String projectPath = project.getLocation().toOSString();

		store = ProjectPrefenceHelper.getStore(project);
    	compiler = createProjectRelativeFile(Constants.PROJECT_PREFS_COMPILER_KEY, Messages.COMPILER_NAME, parent, projectPath, COMPILER_EXTENSIONS);
        createLabel(Messages.COMPILER_DESCRIPTION, parent);
    	workingDirectory = createProjectRelateiveDirectory(Constants.PROJECT_PREFS_CWD_KEY, Messages.COMPILER_CWD, parent, projectPath);
        createLabel(Messages.COMPILER_CWD_DESCRIPTION, parent);
    	inputFile = createProjectRelativeFile(Constants.PROJECT_PREFS_INPUT_KEY, Messages.COMPILER_INPUT, parent, projectPath, INPUT_FILE_EXTENSIONS);
        createLabel(Messages.COMPILER_INPUT_DESCRIPTION, parent);
    	buildPath = createProjectRelateiveDirectory(Constants.PROJECT_PREFS_BUILD_DIRECTORY_KEY, Messages.COMPILER_BUILD_DIR, parent, projectPath);
        createLabel(Messages.COMPILER_BUILD_DIR_DESCRIPTION, parent);
        outputFile = createProjectRelativeFile(Constants.PROJECT_PREFS_OUTPUT_KEY, Messages.COMPILER_OUTPUT, parent, projectPath, OUTPUT_FILE_EXTENSIONS);
        listFile = createProjectRelativeFile(Constants.PROJECT_PREFS_LIST_FILE_KEY, Messages.COMPILER_LIST_FILE, parent, projectPath, LIST_FILE_EXTENSIONS);
    	options = createCreateOptions(parent);

		return parent;
	}
	
	private void createLabel(String text, Composite parent) {
        var label = new Label(parent, SWT.LEFT);
        label.setText(text);	    
	}
    
    private DirectoryFieldEditor createProjectRelateiveDirectory(String prefsKey, String label, Composite parent, String projectPath) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(3, false));
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        composite.setLayoutData(gridData);

        var directory = new DirectoryFieldEditor(prefsKey, label, composite) {
            
            @Override
            public void setStringValue(String value) {
                value = tryMakeRelative(projectPath, value);
                super.setStringValue(value);
            }
            
        };
        
        directory.setFilterPath(new File(projectPath));
        directory.setPreferenceStore(store);
        directory.load();
        return directory;
    }
    
    private FileFieldEditor createProjectRelativeFile(String prefsKey, String label, Composite parent, String projectPath, String[] extensions) {
        var  comp = new Composite(parent, SWT.NONE);
        var layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        comp.setLayout(layout);
        comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        var file = new FileFieldEditor(prefsKey, label, true, comp) {
            @Override
            public void setStringValue(
                    String value) {
                value = tryMakeRelative(projectPath, value);
                super.setStringValue(value);
            }
            
        };
        
        file.setEmptyStringAllowed(true);
        file.setFileExtensions(extensions);
        file.setFilterPath(new File(projectPath));
        file.setPreferenceStore(store);
        file.load();     
        return file;
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
	    listFile.store();
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
