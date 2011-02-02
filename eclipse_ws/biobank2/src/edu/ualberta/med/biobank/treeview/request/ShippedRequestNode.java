package edu.ualberta.med.biobank.treeview.request;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;

import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.RequestWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.treeview.AdapterBase;

public class ShippedRequestNode extends AdapterBase {

    private SiteWrapper site;
    private static final String NODE_INFO = "Requests that have been shipped, pending closure by researcher";

    public ShippedRequestNode(AdapterBase parent, int id, SiteWrapper site) {
        super(parent, id, "Shipped", true, false);
        this.site = site;
    }

    @Override
    protected String getLabelInternal() {
        return NODE_INFO;
    }

    @Override
    public String getTooltipText() {
        return NODE_INFO;
    }

    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
    }

    @Override
    protected AdapterBase createChildNode() {
        return new RequestAdapter(this, null);
    }

    @Override
    protected AdapterBase createChildNode(ModelWrapper<?> child) {
        return new RequestAdapter(this, (RequestWrapper) child);
    }

    @Override
    protected Collection<? extends ModelWrapper<?>> getWrapperChildren()
        throws Exception {
        site.reset();
        return site.getShippedRequestCollection();
    }

    @Override
    protected int getWrapperChildCount() throws Exception {
        return getWrapperChildren().size();
    }

    @Override
    public String getViewFormId() {
        return null;
    }

    @Override
    public String getEntryFormId() {
        return null;
    }

    // FIXME merge
    // @Override
    // <<<<<<<
    // HEAD:eclipse_ws/biobank2/src/edu/ualberta/med/biobank/treeview/order/NewOrderNode.java
    // public void rebuild() {
    // for (AdapterBase adaper : getChildren()) {
    // adaper.rebuild();
    // }
    // }

    @Override
    public List<AdapterBase> search(Object searchedObject) {
        return searchChildren(searchedObject);
    }

}