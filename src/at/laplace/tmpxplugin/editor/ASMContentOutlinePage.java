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
package at.laplace.tmpxplugin.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import at.laplace.tmpxplugin.Activator;
import at.laplace.tmpxplugin.Constants;
import at.laplace.tmpxplugin.Messages;
import at.laplace.tmpxplugin.Patterns;


/**
 * Outlinepage for the ASM-Editor.
 * 
 * @author Andy Reek
 * @since 13.02.2006
 */
public class ASMContentOutlinePage extends ContentOutlinePage {

	private IEditorInput input;

	private ASMEditor editor;

	private ContentProvider contentProvider;

	/**
	 * The constructor.
	 * 
	 * @param editor An ASM-Editor.
	 */
	public ASMContentOutlinePage(ASMEditor editor) {
		this.editor = editor;
		contentProvider = new ContentProvider();
	}

	public List<String> getLabels() {
		return this.contentProvider.getLabels();
	}
	public List<String> getMacros() {
		return this.contentProvider.getMacros();
	}
	public List<String> getConsts() {
		return this.contentProvider.getConsts();
	}
	public List<String> getFunctions() {
		return this.contentProvider.getFunctions();
	}
	
	/**
	 * Sets the input for the outlinepage.
	 * 
	 * @param input The new input.
	 */
	public void setInput(IEditorInput input) {
		this.input = input;

		final TreeViewer viewer = getTreeViewer();

		if ((viewer != null) && (viewer.getContentProvider() != null)) {
			editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					Control control = viewer.getControl();
					control.setRedraw(false);

					ITextSelection textselect = (ITextSelection) editor.getSelectionProvider().getSelection();

					TreeObject treeobj = getSelectedTreeObject(viewer);
					viewer.setInput(ASMContentOutlinePage.this.input);
					viewer.refresh();
					viewer.expandAll();
					selectTreeObject(viewer, treeobj);

					editor.getSelectionProvider().setSelection(textselect);

					control.setRedraw(true);
				}
			});
		}
	}

	/**
	 * Returns the selected element in the tree viewer.
	 * 
	 * @param viewer The tree viewer.
	 * 
	 * @return The selected element.
	 */
	private TreeObject getSelectedTreeObject(TreeViewer viewer) {
		ISelection selection = viewer.getSelection();

		if (!selection.isEmpty()) {
			if (selection instanceof IStructuredSelection) {
				Object object = ((IStructuredSelection) selection).getFirstElement();

				if (object instanceof TreeObject) {
					return (TreeObject) object;
				}
			}
		}

		return null;
	}

	/**
	 * Select a given TreeObject in the given TreeViewer.
	 * 
	 * @param viewer The given TreeViewer.
	 * @param treeobj The given TreeObject.
	 */
	private void selectTreeObject(TreeViewer viewer, TreeObject treeobj) {
		if (treeobj == null) {
			return;
		}

		IContentProvider icp = viewer.getContentProvider();
		if (icp instanceof ContentProvider) {
			ContentProvider cp = (ContentProvider) icp;
			TreeObject fto = cp.findEqualTreeObject(treeobj);

			if (fto != null) {
				ArrayList<Object> newSelection = new ArrayList<Object>();
				newSelection.add(fto);
				viewer.setSelection(new StructuredSelection(newSelection));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);

		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(this.contentProvider);
		viewer.setLabelProvider(new ASMLabelProvider());
		viewer.addSelectionChangedListener(this);
		viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);

		if (input != null) {
			viewer.setInput(input);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);

		ISelection selection = event.getSelection();

		if (selection.isEmpty()) {
			editor.resetHighlightRange();
		} else {
			if (selection instanceof IStructuredSelection) {
				Object object = ((IStructuredSelection) selection).getFirstElement();

				if (object instanceof TreeObject) {
					try {
						Position position = (Position) ((TreeObject) object).getData();

						if (position != null) {
							editor.setHighlightRange(position.offset, 0, true);
							editor.getSelectionProvider().setSelection(new TextSelection(position.offset, position.length));
						}
					} catch (IllegalArgumentException x) {
						editor.resetHighlightRange();
					}
				}
			}
		}
	}

	/**
	 * The ContentProvider for the TreeViewer in ContentOutlinePage.
	 * 
	 * @author Andy Reek
	 * @since 08.09.2006
	 */
	private class ContentProvider implements ITreeContentProvider {


		private IEditorInput input;

		private TreeObject functions = new TreeObject(Messages.TREEOBJECT_PROCEDURE_NAME,
				Constants.TREEOBJECT_TYPE_ROOT_PROCEDURE);

		private TreeObject macros = new TreeObject(Messages.TREEOBJECT_MACRO_NAME, Constants.TREEOBJECT_TYPE_ROOT_MACRO);

		private TreeObject consts = new TreeObject(Messages.TREEOBJECT_CONST_NAME, Constants.TREEOBJECT_TYPE_ROOT_CONST);

		private TreeObject labels = new TreeObject(Messages.TREEOBJECT_LABEL_NAME, Constants.TREEOBJECT_TYPE_ROOT_LABEL);

		public List<String> getLabels(){

			List<String> list = removeEqualSigns(getChildren(labels, Constants.TREEOBJECT_TYPE_LABEL));
			Collections.sort(list);

			return list;
		}

		public List<String> getConsts(){

			List<String> list = removeEqualSigns(getChildren(consts, Constants.TREEOBJECT_TYPE_CONST));
			Collections.sort(list);

			return list;
		}
		
		public List<String> getFunctions() {
		
			List<String> list = getChildren(functions, Constants.TREEOBJECT_TYPE_PROCEDURE);
			Collections.sort(list);
			return list;
		}

		
		public List<String> getMacros() {

			List<String> list = getChildren(macros, Constants.TREEOBJECT_TYPE_MACRO);

			Collections.sort(list);
			return list;
		}

		protected List<String> removeEqualSigns(List<String> list) {
			List<String> cleanLabels = new ArrayList<String>();
			{

				Pattern p = Patterns.EQUALS_PATTERN;
				for (String s: list){
					cleanLabels.add(p.matcher(s).replaceAll(""));
				}
			}
			return cleanLabels;
		}

	
		private List<String> getChildren(TreeObject treeObject, int type){
			return getChildrenAsList(type, treeObject.getChildren());
		}

		protected List<String> getChildrenAsList(int typeFilter,
				Object[] children) {
			List<String> list = new ArrayList<String>();
			if (children != null){
				for (Object segment : children){
					if (((TreeObject)segment).getType()==typeFilter){
						list.add(segment.toString());
					}else {
						Object[] labels = ((TreeObject) segment).getChildren();
						if (labels !=null){
							for (Object label: labels){
								list.add(label.toString());
							}
						}
					}
				}
			}

			return list;

		}



		/**
		 * {@inheritDoc}
		 */
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof TreeObject) {
				return ((TreeObject) parentElement).getChildren();
			}

			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public Object getParent(Object element) {
			if (element instanceof TreeObject) {
				return ((TreeObject) element).getParent();
			}

			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean hasChildren(Object element) {
			if (element instanceof TreeObject) {
				return (((TreeObject) element).getChildren().length > 0);
			}

			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public Object[] getElements(Object inputElement) {
			if (inputElement == input) {
				ArrayList<TreeObject> objects = new ArrayList<TreeObject>();

				if (labels.getChildren().length > 0) {
					objects.add(labels);
				}

				if (functions.getChildren().length > 0) {
					objects.add(functions);
				}

				if (macros.getChildren().length > 0) {
					objects.add(macros);
				}
				
				if (consts.getChildren().length >0) {
					objects.add(consts);
				}

				return objects.toArray(new Object[0]);
			}

			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public void dispose() {
			input = null;
		}

		/**
		 * {@inheritDoc}
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (oldInput instanceof IEditorInput) {
				input = null;
			}

			if (newInput instanceof IEditorInput) {
				input = (IEditorInput) newInput;
				parse();
			}
		}

        private static TreeObject findChild(TreeObject tree, Object object) {
            Object[] o = tree.getChildren();

            for (int i = 0; i < o.length; i++) {
                if (o[i] instanceof TreeObject to) {
                    if (to.equals(object)) {
                        return to;
                    }
                }
            }
            return null;
        }
		
		/**
		 * Finds an existing TreeObject in the tree model.
		 * 
		 * @param treeobj The given TreeObject.
		 * 
		 * @return The TreeObject, if found. Or null if not found.
		 */
		private TreeObject findEqualTreeObject(TreeObject treeobj) {
			if (treeobj == null) {
				return null;
			}

			if (functions.equals(treeobj)) {
				return functions;
			}

			if (macros.equals(treeobj)) {
				return macros;
			}

			if (labels.equals(treeobj)) {
				return labels;
			}
			
			if (consts.equals(treeobj)) {
				return consts;
			}

			var child = findChild(functions, treeobj);
			if (child != null) {
			    return child;
			}
			
            child = findChild(macros, treeobj);
            if (child != null) {
                return child;
            }
            
            child = findChild(labels, treeobj);
            if (child != null) {
                return child;
            }
            
            child = findChild(consts, treeobj);
            if (child != null) {
                return child;
            }

			return null;
		}
		
		private void parseDefine(Set<String> names, String stringLineLower, String stringLine, int lineOffset) {
            if (stringLineLower.contains("=") || stringLineLower.contains(".var")) {                            
                var matcher = Patterns.CONSTVAR_PATTERN.matcher(stringLine);
                
                if (matcher.matches()) {
                    var name = matcher.group(1);
                    names.add(name);
                    var value = matcher.group(3);
                    var child = new TreeObject(name + " = " + value,Constants.TREEOBJECT_TYPE_CONST);
                    child.setData(new Position(lineOffset, name.length()));
                    
                    consts.addChild(child);
                }
            }
		}
		
		private void parseBlock(Set<String> names, String previousLine, String stringLineLower, int prevLineOffset) {
            if (previousLine != null && stringLineLower.contains(".block")) {
                var matcher = Patterns.FUNCTION_PATTERN.matcher(previousLine);

                if (matcher.find()) {
                    var name = matcher.group(1);
                    names.add(name);
                    var child = new TreeObject(name, Constants.TREEOBJECT_TYPE_PROCEDURE);
                    child.setData(new Position(prevLineOffset, name.length()));

                    functions.addChild(child);
                }
            }
		}
		
		private void parseMacro(Set<String> names, String stringLine, int lineOffset) {
            if (stringLine.contains(".macro") || stringLine.contains(".segment")) {
                var matcher = Patterns.MACRO_PATTERN.matcher(stringLine);

                if (matcher.find()) {
                    var name = matcher.group(1);
                    names.add(name);
                    var child = new TreeObject(name, Constants.TREEOBJECT_TYPE_MACRO);
                    child.setData(new Position(lineOffset, name.length()));
                    macros.addChild(child);
                }
            }
		}
		
		private void parseLabel(Set<String> names, String stringLine, int lineOffset) {
            var matcher = Patterns.LABEL_PATTERN.matcher(stringLine);
            if (matcher.matches()) {
                var name = matcher.group(1);
                if (names.contains(name)) {
                    return;
                }
                var child = new TreeObject(name, Constants.TREEOBJECT_TYPE_LABEL);
                child.setData(new Position(lineOffset, name.length()));
                labels.addChild(child);
            }
		}

		/**
		 * Parse the new input and build up the tree.
		 */
		private void parse() {
			functions.setChildren(null);
			macros.setChildren(null);
			labels.setChildren(null);
			consts.setChildren(null);
			
			IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());

			if (document != null) {
			    var names = new HashSet<String>();
				int lines = document.getNumberOfLines();
				int prevLineOffset, lineOffset, linelen;
				String previousLine = "";
				String stringLine = "";
				String stringLineLower = "";

				prevLineOffset = 0;
				lineOffset = 0;

				for (int line = 0; line < lines; line++) {
					try {
						lineOffset = document.getLineOffset(line);
						linelen = document.getLineLength(line);
						stringLine = document.get(lineOffset, linelen);

						stringLineLower = stringLine.toLowerCase();
						
                        parseDefine(names, stringLineLower, stringLine, lineOffset);
						parseBlock(names, previousLine, stringLineLower, prevLineOffset);
						parseMacro(names, stringLine, lineOffset);
						parseLabel(names, previousLine, prevLineOffset);						
					}
					catch (BadLocationException e) {
						Activator.getDefault().getLog().log(
								new Status(Status.ERROR, Constants.PLUGIN_ID, Status.OK,
										Messages.BADLOCATION_ERROR, e));
					} finally {
                        previousLine = stringLine;
                        prevLineOffset = lineOffset;					    
					}
				}
				parseLabel(names, previousLine, prevLineOffset);
			}
		}
	}

}
