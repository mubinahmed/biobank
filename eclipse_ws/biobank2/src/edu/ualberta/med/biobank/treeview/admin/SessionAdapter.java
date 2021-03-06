package edu.ualberta.med.biobank.treeview.admin;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.util.StringUtil;
import edu.ualberta.med.biobank.common.wrappers.ClinicWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.UserWrapper;
import edu.ualberta.med.biobank.gui.common.BgcPlugin;
import edu.ualberta.med.biobank.server.applicationservice.BiobankApplicationService;
import edu.ualberta.med.biobank.treeview.AbstractAdapterBase;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class SessionAdapter extends AdapterBase {
    private static final I18n i18n = I18nFactory.getI18n(ClinicAdapter.class);

    @SuppressWarnings("nls")
    private static final String LOGOUT_COMMAND_ID =
        "edu.ualberta.med.biobank.commands.logout";

    public static final int CLINICS_BASE_NODE_ID = 0;

    public static final int SITES_NODE_ID = 1;

    public static final int STUDIES_NODE_ID = 2;

    public static final int RESEARCH_GROUPS_BASE_NODE_ID = 3;

    private BiobankApplicationService appService;

    private final UserWrapper user;
    private final String serverName;

    @SuppressWarnings("nls")
    public SessionAdapter(AdapterBase parent,
        BiobankApplicationService appService, int sessionId, String serverName,
        UserWrapper user) {
        super(parent, null);
        this.appService = appService;
        setId(sessionId);
        if (user.getLogin().isEmpty()) {
            setLabel(serverName);
        } else {
            setLabel(serverName + " [" + user.getLogin() + "]");
        }
        this.serverName = serverName;
        this.user = user;

        addSubNodes();
    }

    private void addSubNodes() {
        if (!SessionManager.getInstance().isConnected()) return;

        addChild(new StudyMasterGroup(this, STUDIES_NODE_ID));
        addChild(new ClinicMasterGroup(this, CLINICS_BASE_NODE_ID));
        ResearchGroupMasterGroup rgroups = new ResearchGroupMasterGroup(this,
            RESEARCH_GROUPS_BASE_NODE_ID);
        addChild(rgroups);
        rgroups.performExpand();
        SiteGroup siteGroup = new SiteGroup(this, SITES_NODE_ID);
        addChild(siteGroup);
        siteGroup.performExpand();
    }

    @Override
    public void rebuild() {
        removeAll();
        addSubNodes();
    }

    @Override
    public BiobankApplicationService getAppService() {
        return appService;
    }

    public void resetAppService() {
        appService = null;
    }

    @Override
    protected String getLabelInternal() {
        return StringUtil.EMPTY_STRING;
    }

    @SuppressWarnings("nls")
    @Override
    public String getTooltipTextInternal() {
        if (appService != null) {
            return i18n.tr("Current server version: {0}",
                appService.getServerVersion());
        }
        return StringUtil.EMPTY_STRING;
    }

    private SiteGroup getSitesGroupNode() {
        SiteGroup adapter = (SiteGroup) getChild(SITES_NODE_ID);
        Assert.isNotNull(adapter);
        return adapter;
    }

    private StudyMasterGroup getStudiesGroupNode() {
        StudyMasterGroup adapter = (StudyMasterGroup) getChild(STUDIES_NODE_ID);
        Assert.isNotNull(adapter);
        return adapter;
    }

    private ClinicMasterGroup getClinicGroupNode() {
        ClinicMasterGroup adapter =
            (ClinicMasterGroup) getChild(CLINICS_BASE_NODE_ID);
        Assert.isNotNull(adapter);
        return adapter;
    }

    private ResearchGroupMasterGroup getResearchGroupGroupNode() {
        ResearchGroupMasterGroup adapter =
            (ResearchGroupMasterGroup) getChild(RESEARCH_GROUPS_BASE_NODE_ID);
        Assert.isNotNull(adapter);
        return adapter;
    }

    @SuppressWarnings("nls")
    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
        MenuItem mi = new MenuItem(menu, SWT.PUSH);
        mi.setText(
            // menu item label.
            i18n.tr("Logout"));
        mi.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                IHandlerService handlerService = (IHandlerService) PlatformUI
                    .getWorkbench().getService(IHandlerService.class);

                try {
                    handlerService.executeCommand(LOGOUT_COMMAND_ID, null);
                } catch (Exception ex) {
                    throw new RuntimeException(LOGOUT_COMMAND_ID + " not found");
                }
            }
        });
    }

    public UserWrapper getUser() {
        return user;
    }

    public String getServerName() {
        return serverName;
    }

    @Override
    public List<AbstractAdapterBase> search(Class<?> searchedClass,
        Integer objectId) {
        return searchChildren(searchedClass, objectId);
    }

    @Override
    protected AdapterBase createChildNode() {
        return new SiteAdapter(this, null);
    }

    @Override
    protected AdapterBase createChildNode(Object child) {
        return null;
    }

    @Override
    protected List<? extends ModelWrapper<?>> getWrapperChildren() {
        return null;
    }

    @Override
    public String getEntryFormId() {
        return null;
    }

    @Override
    public String getViewFormId() {
        return null;
    }

    @SuppressWarnings("nls")
    public List<ClinicWrapper> getClinicCollection() {
        try {
            return ClinicWrapper.getAllClinics(appService);
        } catch (ApplicationException e) {
            BgcPlugin.openAsyncError(
                // dialog title.
                i18n.tr("Unable to load clinics from database"),
                e);
        }
        return null;
    }

    public void addStudy() {
        StudyMasterGroup g = getStudiesGroupNode();
        if (g != null) {
            g.addStudy();
        }
    }

    public void addClinic() {
        ClinicMasterGroup g = getClinicGroupNode();
        if (g != null) {
            g.addClinic();
        }
    }

    public void addSite() {
        SiteGroup s = getSitesGroupNode();
        if (s != null)
            s.addSite();
    }

    public void addResearchGroup() {
        ResearchGroupMasterGroup g = getResearchGroupGroupNode();
        if (g != null) {
            g.addResearchGroup();
        }
    }

    @Override
    public int compareTo(AbstractAdapterBase o) {
        return 0;
    }
}
