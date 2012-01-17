package edu.ualberta.med.biobank.common.action.security;

import java.util.Map;
import java.util.Set;

import edu.ualberta.med.biobank.common.action.Action;
import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.action.IdResult;
import edu.ualberta.med.biobank.common.action.exception.ActionException;
import edu.ualberta.med.biobank.common.action.exception.NullPropertyException;
import edu.ualberta.med.biobank.common.permission.security.UserManagementPermission;
import edu.ualberta.med.biobank.common.util.SetDifference;
import edu.ualberta.med.biobank.model.Center;
import edu.ualberta.med.biobank.model.Membership;
import edu.ualberta.med.biobank.model.Permission;
import edu.ualberta.med.biobank.model.Principal;
import edu.ualberta.med.biobank.model.Role;
import edu.ualberta.med.biobank.model.Study;

public class MembershipSaveAction implements Action<IdResult> {
    private static final long serialVersionUID = 1L;

    private Integer membershipId;
    private Integer principalId;
    private Set<Integer> roleIds;
    private Set<Integer> permissionIds;
    private Integer centerId;
    private Integer studyId;
    private Membership membership = null;

    public void setId(Integer id) {
        this.membershipId = id;
    }

    public void setPrincipalId(Integer principalId) {
        this.principalId = principalId;
    }

    public void setRoleIds(Set<Integer> roleIds) {
        this.roleIds = roleIds;
    }

    public void setPermissionIds(Set<Integer> permissionIds) {
        this.permissionIds = permissionIds;
    }

    public void setCenterId(Integer centerId) {
        this.centerId = centerId;
    }

    public void setStudyId(Integer studyId) {
        this.studyId = studyId;
    }

    @Override
    public boolean isAllowed(ActionContext context) throws ActionException {
        return new UserManagementPermission().isAllowed(null);
    }

    @Override
    public IdResult run(ActionContext context) throws ActionException {
        if (roleIds == null) {
            throw new NullPropertyException(Membership.class,
                "role ids cannot be null");
        }
        if (permissionIds == null) {
            throw new NullPropertyException(Membership.class,
                "permission ids cannot be null");
        }

        membership =
            context.get(Membership.class, membershipId, new Membership());
        membership.setPrincipal(context.load(Principal.class, principalId));
        membership.setCenter(context.load(Center.class, centerId));
        membership.setStudy(context.load(Study.class, studyId));

        saveRoles(context);
        savePermissions(context);

        context.getSession().saveOrUpdate(membership);
        context.getSession().flush();

        return new IdResult(membership.getId());
    }

    /*
     * Membership to Role association is unidirectional.
     */
    private void saveRoles(ActionContext context) {
        Map<Integer, Role> roles = context.load(Role.class, roleIds);

        SetDifference<Role> rolesDiff = new SetDifference<Role>(
            membership.getRoleCollection(), roles.values());
        membership.setRoleCollection(rolesDiff.getAddSet());
    }

    /*
     * Membership to Permission association is unidirectional.
     */
    private void savePermissions(ActionContext context) {
        Map<Integer, Permission> permissions =
            context.load(Permission.class, permissionIds);

        SetDifference<Permission> permissionsDiff =
            new SetDifference<Permission>(
                membership.getPermissionCollection(), permissions.values());
        membership.setPermissionCollection(permissionsDiff.getAddSet());
    }
}
