package at.laplace.tmpxplugin;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.CoreException;

public class TmpSaveParticipant implements ISaveParticipant {

	public void doneSaving(ISaveContext arg0) {
	}

	public void prepareToSave(ISaveContext arg0) throws CoreException {
	}

	public void rollback(ISaveContext arg0) {
	}

	public void saving(ISaveContext context) throws CoreException {
		context.needDelta();
	}

}
