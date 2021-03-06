package edu.ualberta.med.biobank.treeview.report;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.ReportWrapper;
import edu.ualberta.med.biobank.model.Entity;
import edu.ualberta.med.biobank.model.Report;
import edu.ualberta.med.biobank.treeview.AbstractAdapterBase;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import edu.ualberta.med.biobank.treeview.listeners.AdapterChangedEvent;

// TODO: delete AbstractReportsGroup
public class ReportEntityGroup extends AdapterBase {
    private static final I18n i18n = I18nFactory
        .getI18n(ReportEntityGroup.class);

    private final AbstractReportGroup parent;
    private final Entity entity;

    public ReportEntityGroup(AbstractReportGroup parent, int id, Entity entity) {
        super(parent, id, entity.getName(), true);

        this.parent = parent;
        this.entity = entity;
    }

    @SuppressWarnings("nls")
    @Override
    public void openViewForm() {
        Assert.isTrue(false, "should not be called");
    }

    @Override
    public void executeDoubleClick() {
        performExpand();
    }

    @Override
    public String getTooltipTextInternal() {
        return null;
    }

    @SuppressWarnings("nls")
    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
        if (parent.isModifiable()) {
            MenuItem mi = new MenuItem(menu, SWT.PUSH);
            mi.setText(i18n.tr("New {0} Report", entity.getName()));
            mi.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    openNewReport();
                }
            });
        }
    }

    @Override
    public void notifyListeners(AdapterChangedEvent event) {
        getParent().notifyListeners(event);
    }

    @Override
    public String getViewFormId() {
        return null;
    }

    @Override
    public String getEntryFormId() {
        return null;
    }

    @Override
    public List<AbstractAdapterBase> search(Class<?> searchedClass,
        Integer objectId) {
        return searchChildren(searchedClass, objectId);
    }

    @Override
    protected AdapterBase createChildNode() {
        return new ReportAdapter(this, null);
    }

    @Override
    protected AdapterBase createChildNode(Object child) {
        Assert.isTrue(child instanceof ReportWrapper);
        return new ReportAdapter(this, (ReportWrapper) child);
    }

    @Override
    protected List<? extends ModelWrapper<?>> getWrapperChildren() throws Exception {
        List<ReportWrapper> reports = new ArrayList<ReportWrapper>();
        for (ReportWrapper report : parent.getReports()) {
            if (entity.getId().equals(report.getEntity().getId())) {
                reports.add(report);
            }
        }
        return reports;
    }

    @Override
    protected String getLabelInternal() {
        return null;
    }

    @SuppressWarnings("nls")
    private void openNewReport() {
        if (!SessionManager.getInstance().isConnected()) {
            throw new IllegalStateException("user is not logged in");
        }

        ReportWrapper report = new ReportWrapper(SessionManager.getAppService());

        Report rawReport = report.getWrappedObject();
        rawReport.setUser(SessionManager.getUser().getWrappedObject());
        rawReport.setEntity(entity);

        ReportAdapter reportAdapter = new ReportAdapter(this, report);
        reportAdapter.openEntryForm();
    }

    @Override
    public int compareTo(AbstractAdapterBase o) {
        return 0;
    }
}
