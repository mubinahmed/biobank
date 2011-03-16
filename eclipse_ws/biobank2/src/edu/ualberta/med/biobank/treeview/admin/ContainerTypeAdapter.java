package edu.ualberta.med.biobank.treeview.admin;

import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;

import edu.ualberta.med.biobank.common.wrappers.ContainerTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.forms.ContainerTypeEntryForm;
import edu.ualberta.med.biobank.forms.ContainerTypeViewForm;
import edu.ualberta.med.biobank.treeview.AdapterBase;

public class ContainerTypeAdapter extends AdapterBase {

    public ContainerTypeAdapter(AdapterBase parent,
        ContainerTypeWrapper containerType) {
        super(parent, containerType);
    }

    public ContainerTypeWrapper getContainerType() {
        return (ContainerTypeWrapper) modelObject;
    }

    @Override
    protected String getLabelInternal() {
        ContainerTypeWrapper containerType = getContainerType();
        Assert.isNotNull(containerType, "container type is null");
        return containerType.getName();
    }

    @Override
    public String getTooltipText() {
        ContainerTypeWrapper type = getContainerType();
        if (type != null) {
            SiteWrapper site = type.getSite();
            if (site != null) {
                return site.getNameShort() + " - "
                    + getTooltipText("Container Type");
            }
        }
        return getTooltipText("Container Type");

    }

    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
        addEditMenu(menu, "Container Type");
        addViewMenu(menu, "Container Type");
        addDeleteMenu(menu, "Container Type");
    }

    @Override
    protected String getConfirmDeleteMessage() {
        return "Are you sure you want to delete this container type?";
    }

    @Override
    public boolean isDeletable() {
        return internalIsDeletable();
    }

    @Override
    protected AdapterBase createChildNode() {
        return null;
    }

    @Override
    protected AdapterBase createChildNode(ModelWrapper<?> child) {
        return null;
    }

    @Override
    protected Collection<? extends ModelWrapper<?>> getWrapperChildren()
        throws Exception {
        return null;
    }

    @Override
    protected int getWrapperChildCount() throws Exception {
        return 0;
    }

    @Override
    public String getEntryFormId() {
        return ContainerTypeEntryForm.ID;
    }

    @Override
    public String getViewFormId() {
        return ContainerTypeViewForm.ID;
    }

}
