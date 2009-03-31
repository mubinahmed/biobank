package edu.ualberta.med.biobank.forms.input;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import edu.ualberta.med.biobank.treeview.ClinicAdapter;
import edu.ualberta.med.biobank.treeview.Node;
import edu.ualberta.med.biobank.treeview.SiteAdapter;
import edu.ualberta.med.biobank.treeview.StudyAdapter;

public class FormInput implements IEditorInput {
    private Node node;

    public FormInput(Node o) {
        node = o;
    }
    
    public Node getNode() {
        return node;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    @Override
    public String getName() {
        if (node == null) return null;

        String name = node.getName();
        if (name != null) {
            if (node instanceof SiteAdapter) return "Site " + name;
            else if (node instanceof StudyAdapter) return "Study " + name;
            else if (node instanceof ClinicAdapter) return "Clinic " + name;
            else Assert.isTrue(false, "tooltip name for "
                    + node.getClass().getName() + " not implemented");
        }
        else {
            if (node instanceof SiteAdapter) return "New Site";
            else if (node instanceof StudyAdapter) return "New Study";
            else if (node instanceof ClinicAdapter) return "New Clinic";
            else Assert.isTrue(false, "tooltip name for "
                    + node.getClass().getName() + " not implemented");
        }
        return null;
    }
    
    @Override
    public IPersistableElement getPersistable() {
        return null;
    }

    @Override
    public String getToolTipText() {
        return getName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class adapter) {
        return null;
    }
    
    public boolean equals(Object o) {
        if ((node == null) || (o == null)) return false;
        
        if (o instanceof FormInput) {
            if (node.getClass() != ((FormInput)o).node.getClass()) return false;
        }
        return false;
    }

}
