package edu.ualberta.med.biobank.treeview.admin;

import java.util.Collection;

import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.treeview.AbstractClinicGroup;

public class SiteClinicGroup extends AbstractClinicGroup {

    public SiteClinicGroup(SiteAdapter parent, int id) {
        super(parent, id, "Clinics");
    }

    @Override
    protected Collection<? extends ModelWrapper<?>> getWrapperChildren()
        throws Exception {
        SiteAdapter site = getParentFromClass(SiteAdapter.class);
        return site.getWrapper().getWorkingClinicCollection();
    }

}