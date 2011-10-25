package edu.ualberta.med.biobank.common.permission.site;

import org.hibernate.Session;

import edu.ualberta.med.biobank.common.action.ActionUtil;
import edu.ualberta.med.biobank.common.permission.Permission;
import edu.ualberta.med.biobank.common.permission.PermissionEnum;
import edu.ualberta.med.biobank.model.Site;
import edu.ualberta.med.biobank.model.User;

public class SiteUpdatePermission implements Permission {

    private static final long serialVersionUID = 1L;
    private Integer siteId;

    public SiteUpdatePermission(Integer siteId) {
        this.siteId = siteId;
    }

    @Override
    public boolean isAllowed(User user, Session session) {
        Site site = ActionUtil.sessionGet(session, Site.class, siteId);
        return PermissionEnum.SITE_UPDATE.isAllowed(user, site);
    }

}
