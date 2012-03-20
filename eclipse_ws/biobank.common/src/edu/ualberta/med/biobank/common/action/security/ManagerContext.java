package edu.ualberta.med.biobank.common.action.security;

import java.util.Collections;
import java.util.Set;

import edu.ualberta.med.biobank.model.Group;
import edu.ualberta.med.biobank.model.Role;
import edu.ualberta.med.biobank.model.User;
import edu.ualberta.med.biobank.model.util.IdUtil;

/**
 * Used by {@link UserGetInput} and {@link UserSaveInput} so that the context
 * between getting {@link User} data and saving that data remains constant,
 * preventing assumptions to be made about potentially missing data. This is to
 * avoid problems where the {@link Role}-s, {@link Group}-s, or the manager's
 * power change from getting information to saving it. If power did change,
 * accidental modifications would likely occur.
 * 
 * @author Jonathan Ferland
 */
public class ManagerContext {
    private final User manager;
    private final Set<Integer> roleIds;
    private final Set<Integer> groupIds;

    /**
     * A snapshot of the managing {@link User} needs to be included because it
     * defines the context of the information <em>intended</em> to be saved. In
     * case there were changes to the manager's permissions in the mean time,
     * they might otherwise save things they don't intend to (e.g. if they
     * became more powerful).
     * <p>
     * Similarly, the modifiable {@link Role}-s and {@link Group}-s need to be
     * sent in case some where added or removed since this action was generated.
     * So, these are the sets the manager is aware of at this point.
     * 
     * @param manager the {@link User} that is executing the save
     * @param roles every {@link Role} that <em>can</em> be modified, that the
     *            manager is aware of at this point
     * @param groups every manageable {@link Group} that <em>can</em> be
     *            modified, that the manager is aware of at this point
     */
    public ManagerContext(User manager, Set<Role> roles, Set<Group> groups) {
        this.manager = manager;
        this.roleIds = Collections.unmodifiableSet(IdUtil.getIds(roles));
        this.groupIds = Collections.unmodifiableSet(IdUtil.getIds(groups));
    }

    public User getManager() {
        return manager;
    }

    public Set<Integer> getRoleIds() {
        return roleIds;
    }

    public Set<Integer> getGroupIds() {
        return groupIds;
    }
}
