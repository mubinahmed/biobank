package edu.ualberta.med.biobank.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import edu.ualberta.med.biobank.dialogs.SendErrorMessageDialog;

public class SendErrorMail extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        new SendErrorMessageDialog(PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getShell()).open();
        return null;
    }

}
