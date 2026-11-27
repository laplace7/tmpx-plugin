package at.laplace.tmpxplugin;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;


public class Problems {
    
    public static void deleteMarkers(IResource resource) throws CoreException {
        resource.deleteMarkers(Constants.MARKER_ID, true,
                IResource.DEPTH_INFINITE);
    }

    public static void createMarker(IResource resource, String message, int lineNumber,
            int column, int error, int severity) throws CoreException {
        IMarker marker = resource.createMarker(Constants.MARKER_ID);
        if (error >= 0) {
            message = "Error %d: %s".formatted(error, message);
        }
        marker.setAttribute(IMarker.MESSAGE, message);
        marker.setAttribute(IMarker.SEVERITY, severity);
        if (lineNumber >= 0) {
            marker.setAttribute(IMarker.LINE_NUMBER, lineNumber + 1);
        }
        if (column == -1) {
            return;
        }

        if (!(resource instanceof IFile file)) {
            return;
        }
        IDocument document = getDocument(file);
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

    private static IDocument getDocument(IFile file) throws CoreException {
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

}
