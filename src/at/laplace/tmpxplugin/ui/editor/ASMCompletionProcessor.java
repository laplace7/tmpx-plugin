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
package at.laplace.tmpxplugin.ui.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import at.laplace.tmpxplugin.Activator;
import at.laplace.tmpxplugin.Constants;
import at.laplace.tmpxplugin.ui.editor.PrefixExtractor.Prefix;
import at.laplace.tmpxplugin.ui.editor.PrefixExtractor.PrefixType;

/**
 * Class for content assist. Create the content assist list.
 * 
 * @author Daniel Mitte
 * @since 13.02.2006
 */
public class ASMCompletionProcessor implements IContentAssistProcessor {

    private static Image labelImage;
    private static Image macroImage;
    private static Image defaultImage;
    private static Image procedureImage;
    private static List<String> directives;
    private static List<String> instructions;

    static {
        labelImage = new Image(Display.getCurrent(),
                Activator.getFilePathFromPlugin("tree_label.gif"));
        macroImage = new Image(Display.getCurrent(),
                Activator.getFilePathFromPlugin("tree_macro.gif"));
        defaultImage = new Image(Display.getCurrent(),
                Activator.getFilePathFromPlugin("tree_default.gif"));
        procedureImage = new Image(Display.getCurrent(),
                Activator.getFilePathFromPlugin("tree_procedure.gif"));
        directives = new ArrayList<String>();
        for (String key : ASMInstructionSet.getDirectives().keySet()) {
            directives.add(key.substring(1) + " ");
        }
        instructions = new ArrayList<String>();
        for (String key : ASMInstructionSet.getInstructions().keySet()) {
            instructions.add(key + " ");
        }
    }

    private final ASMEditor editor;

    public ASMCompletionProcessor(ASMEditor editor) {
        this.editor = editor;
    }

    /**
     * {@inheritDoc}
     */
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
            int offset) {
        ITextSelection selection = (ITextSelection) viewer
                .getSelectionProvider().getSelection();

        int selectionOffset = offset;

        if (selection.getOffset() != offset) {
            selectionOffset = selection.getOffset();
        }

        Prefix prefixType = getPrefix(viewer, selectionOffset);

        String prefix = prefixType.prefix();

        Region region = new Region(selectionOffset - prefix.length(),
                prefix.length() + selection.getLength());

        ICompletionProposal[] keyWordsProposals = computeWordProposals(viewer,
                region, prefixType, offset);

        return keyWordsProposals;
    }

    /**
     * Computes the word proposals.
     * 
     * @param viewer
     *            The viewer.
     * @param region
     *            The region.
     * @param prefixType
     *            The prefix.
     * @param cursorOffset
     *            The offset of the cursor.
     * 
     * @return A list of the word proposal.
     */
    private ICompletionProposal[] computeWordProposals(ITextViewer viewer,
            Region region, Prefix prefixType, int cursorOffset) {
        ArrayList<ICompletionProposal> proposalList = new ArrayList<ICompletionProposal>();

        int offset = region.getOffset();
        String smprefix = prefixType.prefix().toLowerCase();

        ASMContentOutlinePage outline = editor != null ? editor.getOutline()
                : null;
        /* Add things from outline, which is constantly updated */
        if (prefixType.type() != PrefixType.DIRECTIVE && outline != null) {
            addToProposalList(region, proposalList, offset, smprefix,
                    outline.getLabels(), null, labelImage);
        }

        if (prefixType.type() == PrefixType.UNKNOWN) {
            if (outline != null) {
                List<String> macros = new ArrayList<String>();
                List<String> tempMacros = outline.getMacros();
                for (String m : tempMacros)
                    macros.add(m); // TODO prefix "#" required?

                addToProposalList(region, proposalList, offset, smprefix,
                        macros, null, macroImage);
            }

            addToProposalList(region, proposalList, offset, smprefix,
                    instructions, null, defaultImage);

            if (outline != null) {
                addToProposalList(region, proposalList, offset, smprefix,
                        outline.getConsts(), null, defaultImage);
            }

            if (outline != null) {
                addToProposalList(region, proposalList, offset, smprefix,
                        outline.getFunctions(), null, procedureImage);
            }

            addToProposalList(region, proposalList, offset, smprefix,
                    ASMInstructionSet.getBlocks().keySet(), null, defaultImage);

            var input = editor.getEditorInput();
            var currentIFile = input.getAdapter(IFile.class);            
            if (currentIFile == null)
                return null;

            IProject project = currentIFile.getProject();

            addDependencyItems(region, proposalList, offset, smprefix,
                    prefixType, currentIFile, project, new HashSet<String>());

        } else if (prefixType.type() == PrefixType.DIRECTIVE) {
            addToProposalList(region, proposalList, offset, smprefix,
                    directives, null, defaultImage);

        }

        if (proposalList.isEmpty()) {
            return null;
        }

        return proposalList.toArray(new ICompletionProposal[0]);
    }

    @SuppressWarnings("unchecked")
    protected void addDependencyItems(Region region,
            ArrayList<ICompletionProposal> proposalList, int offset,
            String smprefix, Prefix prefixType, IFile currentIFile,
            IProject project, HashSet<String> visited) {

        Set<String> imports = null;
        try {
            imports = (Set<String>) currentIFile
                    .getSessionProperty(Constants.IMPORTS_SESSION_KEY);
        } catch (CoreException e) {
            e.printStackTrace();
        }

        if (imports != null) {

            Iterator<String> it = imports.iterator();
            while (it.hasNext()) {
                String filename = it.next();
                IResource member = project.findMember(filename);
                if (member.getType() == IResource.FILE && member != null
                        && member.exists() && member.isAccessible()) {
                    IFile file = (IFile) member;
                    if (!visited.contains(file.getLocation().toOSString())) {
                        visited.add(file.getLocation().toOSString());
                        addToProposalList(region, proposalList, offset,
                                smprefix, prefixType, file);
                        // Recurse
                        addDependencyItems(region, proposalList, offset,
                                smprefix, prefixType, file, project, visited);
                    }
                } else {
                    it.remove();
                }
            }
        }
    }

    private void addToProposalList(Region region,
            ArrayList<ICompletionProposal> proposalList, int offset,
            String smprefix, Prefix prefixType, IFile file) {

        try {
            var source = file.getName();
            @SuppressWarnings("unchecked")
            List<String> labels = (List<String>) file
                    .getSessionProperty(Constants.LABELS_SESSION_KEY);
            addToProposalList(region, proposalList, offset, smprefix, labels,
                    source, labelImage);

            if (prefixType.type() == PrefixType.LABEL)
                return;
            @SuppressWarnings("unchecked")
            List<String> macros = (List<String>) file
                    .getSessionProperty(Constants.MACROS_SESSION_KEY);
            addToProposalList(region, proposalList, offset, smprefix, macros,
                    source, macroImage);

            @SuppressWarnings("unchecked")
            List<String> constig = (List<String>) file
                    .getSessionProperty(Constants.CONST_SESSION_KEY);
            addToProposalList(region, proposalList, offset, smprefix, constig,
                    source, defaultImage);

            @SuppressWarnings("unchecked")
            List<String> functions = (List<String>) file
                    .getSessionProperty(Constants.FUNCTIONS_SESSION_KEY);
            addToProposalList(region, proposalList, offset, smprefix, functions,
                    source, procedureImage);

        } catch (CoreException e) {
            e.printStackTrace();
        }

    }

    protected void addToProposalList(Region region,
            ArrayList<ICompletionProposal> proposalList, int offset,
            String smprefix, Collection<String> labels, String source,
            Image image) {

        if (labels == null)
            return;

        for (String label : labels) {

            if (label.toLowerCase().startsWith(smprefix)) {

                int cursorpos = label.indexOf('(') + 1;
                if (cursorpos == 0 || label.indexOf(')') == cursorpos) {
                    cursorpos = label.length();
                }

                var displayString = source == null 
                        ? label 
                        : "%s [%s]".formatted(label, source);
                proposalList.add(new CompletionProposal(label, offset,
                        region.getLength(), cursorpos, image,
                        displayString, null, null));
            }
        }
    }

    /**
     * Returns the prefix.
     * 
     * @param viewer
     *            The viewer.
     * @param offset
     *            The offset.
     * 
     * @return The prefix.
     */
    private Prefix getPrefix(ITextViewer viewer, int offset) {
        try {
            IDocument document = viewer.getDocument();        
            var region = document.getLineInformationOfOffset(offset);
            var line = document.get(region.getOffset(), region.getLength());
            offset = offset - region.getOffset();            
            return PrefixExtractor.extract(line, offset);            
        } catch (BadLocationException e) {
            return new Prefix("", PrefixType.UNKNOWN);
        }
    }

    /**
     * {@inheritDoc}
     */
    public IContextInformation[] computeContextInformation(ITextViewer viewer,
            int offset) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public char[] getCompletionProposalAutoActivationCharacters() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getErrorMessage() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }
}
