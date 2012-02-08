package edu.ualberta.med.biobank.common.action.container;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;

import edu.ualberta.med.biobank.common.action.Action;
import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.action.exception.ActionException;
import edu.ualberta.med.biobank.common.permission.container.ContainerReadPermission;
import edu.ualberta.med.biobank.model.Container;

public class ContainerGetChildrenAction implements
    Action<ContainerChildrenResult> {
    private static final long serialVersionUID = 1L;

    // This query has to initialise specimenPositionCollection due to the
    // tree adapter needing to know this to display additional menu selections
    // when a right click is done on a container node.
    @SuppressWarnings("nls")
    private static final String SELECT_CHILD_CONTAINERS_HQL =
        "SELECT container"
            + " FROM " + Container.class.getName() + " container"
            + " INNER JOIN FETCH container.containerType containerType"
            + " INNER JOIN FETCH container.site site"
            + " LEFT JOIN container.specimenPositionCollection"
            + " WHERE container.position.parentContainer.id = ?";

    private final Integer parentContainerId;

    public ContainerGetChildrenAction(Integer parentContainerId) {
        this.parentContainerId = parentContainerId;
    }

    @Override
    public boolean isAllowed(ActionContext context) throws ActionException {
        return new ContainerReadPermission(parentContainerId)
            .isAllowed(context);
    }

    @Override
    public ContainerChildrenResult run(ActionContext context)
        throws ActionException {
        ArrayList<Container> childContainers = new ArrayList<Container>(0);

        Query query =
            context.getSession().createQuery(SELECT_CHILD_CONTAINERS_HQL);
        query.setParameter(0, parentContainerId);

        @SuppressWarnings("unchecked")
        List<Container> results = query.list();
        if (results != null) {
            childContainers.addAll(results);
        }

        return new ContainerChildrenResult(childContainers);
    }

}
