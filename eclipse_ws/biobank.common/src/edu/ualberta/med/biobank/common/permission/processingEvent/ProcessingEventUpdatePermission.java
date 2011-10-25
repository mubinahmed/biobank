package edu.ualberta.med.biobank.common.permission.processingEvent;

import org.hibernate.Session;

import edu.ualberta.med.biobank.common.permission.Permission;
import edu.ualberta.med.biobank.common.permission.PermissionEnum;
import edu.ualberta.med.biobank.model.User;

public class ProcessingEventUpdatePermission implements Permission {
    private static final long serialVersionUID = 1L;
    private Integer peventId;

    public ProcessingEventUpdatePermission(Integer peventId) {
        this.peventId = peventId;
    }

    @Override
    public boolean isAllowed(User user, Session session) {
        // FIXME specific study or center ?
        return PermissionEnum.PROCESSING_EVENT_UPDATE
            .isAllowed(user);
    }
}
