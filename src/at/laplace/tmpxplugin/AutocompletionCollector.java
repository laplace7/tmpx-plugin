package at.laplace.tmpxplugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.rules.ICharacterScanner;

import at.laplace.tmpxplugin.scanner.ITokenFactory;
import at.laplace.tmpxplugin.scanner.ReuseTokenFactory;
import at.laplace.tmpxplugin.scanner.TmpScanner;
import at.laplace.tmpxplugin.scanner.Token;
import at.laplace.tmpxplugin.scanner.TokenType;

public class AutocompletionCollector implements IResourceChangeListener, IResourceDeltaVisitor, IResourceVisitor{

    public void resourceChanged(IResourceChangeEvent event) {

        switch (event.getType()){
        case IResourceChangeEvent.PRE_DELETE:
            break;
        case IResourceChangeEvent.POST_CHANGE:
        case IResourceChangeEvent.POST_BUILD:
        case IResourceChangeEvent.PRE_REFRESH:
            try {
                event.getDelta().accept(this);
            } catch (CoreException ex){
                //error
                ex.printStackTrace();
            }
            break;
        }
    }

    public boolean visit(IResourceDelta delta) throws CoreException {

        if (delta.getResource() == null || delta.getResource().getProject() == null){
            return true;
        }

        IResource resource = delta.getResource();

        if (delta.getKind() == IResourceDelta.REMOVED){
            return true;
        }

        return visit(resource);

    }


    private void scanfile(final IFile file, final String project) throws CoreException {

        var scanFileJob = new ScanJob("Autocompletion collector job: " + file.getName(), file);
        scanFileJob.setPriority(Job.SHORT);
        scanFileJob.schedule();
    }

    public void init() {

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        try {
            workspace.getRoot().accept(this);
        } catch (CoreException e) {
            e.printStackTrace();
        }

    }

    public boolean visit(IResource resource) throws CoreException {

        if (resource.getType() == IResource.ROOT){
            return true;
        }

        if (resource.getProject() == null
                || resource.isVirtual()
                || !resource.getProject().isAccessible()
                || !resource.getProject().hasNature(Constants.NATURE_ID)){

            return false;
        }

        if (resource.getType() == IResource.PROJECT){
            IProject project = (IProject) resource;
            return project.isOpen();
        }

        if (resource.getType() == IResource.FOLDER){

            IFolder folder = (IFolder) resource;
            return folder.findMember(Constants.NO_FOLDER_SCAN) == null;
        }

        if (resource.getType() == IResource.FILE){
            IFile file = (IFile) resource;
            boolean scan = true;
            IResource res = file.getParent();
            while (scan && res != null && res.getType() == IResource.FOLDER){
                scan &= ((IFolder) res).findMember(Constants.NO_FOLDER_SCAN) == null;
                res = res.getParent();
            }

            String ext = file.getFileExtension();
            if (scan && ext != null && Patterns.EXTENSION_PATTERN_ALL.matcher(ext).matches()){
                String project = resource.getProject().getName();
                scanfile(file, project);
            }
        }

        return resource.getType() == IResource.ROOT;
    }
    
    private final class ScanJob extends WorkspaceJob {
        private final IFile file;
        private List<String> searchpaths = null;
        private Set<String> imports = new HashSet<String>();

        List<String> labels;
        List<String> constig;
        List<String> macros;
        List<String> functions;

        private StringScanner scanner; 
        private TmpScanner<Token> tokenizer;
        private Token token;
        private boolean globalScope;

        private ScanJob(String name, IFile file) {
            super(name);
            this.file = file;
        }

        @Override
        public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {


            if (monitor == null){
                monitor = new NullProgressMonitor();
            }

            labels = new ArrayList<String>();
            constig = new ArrayList<String>();
            macros = new ArrayList<String>();
            functions = new ArrayList<String>();
            
            scanner = new StringScanner();
            ITokenFactory<Token> tokenFactory = new ReuseTokenFactory();
            tokenizer = new TmpScanner<Token>(tokenFactory);
            // local scopes are between: .block ... .bend and .macro .endm
            globalScope = true;
         
            try (var inputStreamReader = new InputStreamReader(file.getContents(true));
                    var reader = new BufferedReader(inputStreamReader ,8192)) {
                String line = null;
                monitor.beginTask("Scanning file: " + file.getName(), 1);
                while ((line = reader.readLine()) != null){

                    if(monitor.isCanceled())
                        throw new OperationCanceledException();
                    
                    if (line.isEmpty()) {
                        continue;
                    }
                    
                    scanner.setLine(line);
                    nextToken();
                    
                    scanLine();
                }

                // TODO remove
                Activator.getConsole().println("File %s, labels %d, macros %d, constants %d"
                        .formatted(file.getFullPath().toOSString(), labels.size(), macros.size(), constig.size()));
                
                Collections.sort(labels);
                Collections.sort(macros);
                Collections.sort(constig);
                Collections.sort(functions);

                synchronized (file) {
                    file.setSessionProperty(Constants.LABELS_SESSION_KEY, labels);
                    file.setSessionProperty(Constants.MACROS_SESSION_KEY, macros);
                    file.setSessionProperty(Constants.CONST_SESSION_KEY, constig);
                    file.setSessionProperty(Constants.FUNCTIONS_SESSION_KEY, functions);
                    file.setSessionProperty(Constants.IMPORTS_SESSION_KEY, imports);
                }

                monitor.worked(1);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                monitor.done();
            }

            return Status.OK_STATUS;
        }

        private void nextToken() {
            token = tokenizer.evaluate(scanner);
        }
        
        private void scanLine() {
            String label = null;
            if (token.type == TokenType.OTHER) {
                label = token.text;
                // consume the "label" token
                nextToken();
            }
            
            // Two cases:
            // 1) skip the white spaces after the label
            // 2) the line starts with white spaces, skip them
            if (token.type == TokenType.WHITESPACE) {
                nextToken();
            }

            if (globalScope && label != null) {
                addLabel(label);
            }
            
            if (beginLocalScope()) {
                globalScope = false;
            } else if (endLocalScope()) {
                globalScope = true;
            }
        }
        
        private void addLabel(String label) {
            // label
            // label = value
            // label .macro or .segment
            if (token.type == TokenType.DIRECTIVE) {
                switch (token.text) {
                case ".macro":
                case ".segment":
                    // the label of a macro/segment must be on the same line
                    macros.add("#" + label);
                    break;
                case ".var":
                    constig.add(label);
                    break;
                case ".include":
                    labels.add(label);
                    break;
                default:
                    labels.add(label);
                }
            } else if (token.type == TokenType.OPERATOR && token.text.equals("=")) {
                constig.add(label);
            } else {
                // this is either a regular label,
                // or the label of a block, we do not
                // distinguish between those here
                labels.add(label);
            }            
        }
        
        private boolean beginLocalScope() {
            return token.type == TokenType.DIRECTIVE && (".block".equals(token.text) || ".macro".equals(token.text));
        }
        
        private boolean endLocalScope() {
            return token.type == TokenType.DIRECTIVE && (".bend".equals(token.text) || ".endm".equals(token.text));            
        }
        
        protected void addImport(String filename) {
            
            if (searchpaths == null) {
                searchpaths = initSearchPaths();
            }
        
            for (int i = 0; i < searchpaths.size(); i++ ){
                String folder = searchpaths.get(i);
                if (!"".equals(folder)){
                    IPath importPath = file.getProject().getFolder(folder).getProjectRelativePath().append(filename);
                    IFile importedFile = file.getProject().getFile(importPath);
                    if (importedFile.exists()){
                        imports.add(importPath.toString());
                    }
                }

            }
        }

        private List<String> initSearchPaths() {

            List<String> searchpaths = new ArrayList<String>();
            return searchpaths;
        }
    }

    private static class StringScanner implements ICharacterScanner {
        String line;
        int length;
        int column;
        
        StringScanner() {
        }
        
        void setLine(String line) {
            column = 0;
            this.line = line;
            length = line.length();
        }

        @Override
        public char[][] getLegalLineDelimiters() {
            return null;
        }

        @Override
        public int getColumn() {
            return column;
        }

        @Override
        public int read() {
            if (column < length) {
                int c = line.codePointAt(column);
                column ++;
                return c;
            }
            return -1;
        }

        @Override
        public void unread() {
            column--;            
        }
    }


}