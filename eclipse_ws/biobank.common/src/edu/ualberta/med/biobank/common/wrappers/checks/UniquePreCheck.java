package edu.ualberta.med.biobank.common.wrappers.checks;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import edu.ualberta.med.biobank.common.util.HibernateUtil;
import edu.ualberta.med.biobank.common.util.StringUtil;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.Property;
import edu.ualberta.med.biobank.common.wrappers.actions.UncachedAction;
import edu.ualberta.med.biobank.common.wrappers.property.GetterInterceptor;
import edu.ualberta.med.biobank.common.wrappers.property.LazyLoaderInterceptor;
import edu.ualberta.med.biobank.server.applicationservice.exceptions.BiobankSessionException;
import edu.ualberta.med.biobank.server.applicationservice.exceptions.DuplicatePropertySetException;

/**
 * Checks that the {@link Collection} of {@link Property}-s is unique for the
 * model object in the {@link ModelWrapper}, excluding the instance itself (if
 * it is already persisted).
 * 
 * @author jferland
 * 
 * @param <E>
 */
public class UniquePreCheck<E> extends UncachedAction<E> {
    private static final long serialVersionUID = 1L;
    private static final String HQL = "SELECT COUNT(*) FROM {0} o WHERE ({1}) = ({2}) {3}";
    private static final String EXCEPTION_STRING = "There already exists a {0} with property value(s) ({1}) for ({2}), respectively. These field(s) must be unique.";

    private final Collection<Property<?, ? super E>> properties;

    /**
     * 
     * @param wrapper {@link ModelWrapper} which holds the model object
     * @param properties to ensure uniqueness on
     */
    public UniquePreCheck(ModelWrapper<E> wrapper,
        Collection<Property<?, ? super E>> properties) {
        super(wrapper);
        this.properties = properties;
    }

    @Override
    public void doUncachedAction(Session session) throws BiobankSessionException {
        Query query = getQuery(session);
        Long count = HibernateUtil.getCountFromQuery(query);

        if (count > 0) {
            throwException();
        }
    }

    private void throwException() throws DuplicatePropertySetException {
        String modelClass = Format.modelClass(getModelClass());
        String values = Format.propertyValues(getModel(), properties);
        String names = Format.propertyNames(properties);

        String msg = MessageFormat.format(EXCEPTION_STRING, modelClass, values,
            names);

        throw new DuplicatePropertySetException(msg);
    }

    private Query getQuery(Session session) {
        String modelName = getModelClass().getName();
        String propertyNames = StringUtil.join(getPropertyNames(), ", ");
        String valueBindings = getValueBindings();
        String notSelfCondition = getNotSelfCondition();

        String hql = MessageFormat.format(HQL, modelName, propertyNames,
            valueBindings, notSelfCondition);

        Query query = session.createQuery(hql);
        setParameters(session, query);

        return query;
    }

    private String getValueBindings() {
        StringBuilder sb = new StringBuilder();

        for (int i = 1, n = properties.size(); i <= n; i++) {
            sb.append("?");
            if (i < n) {
                sb.append(",");
            }
        }

        return sb.toString();
    }

    private void setParameters(Session session, Query query) {
        List<Object> values = getValues(session);
        for (int i = 0, n = values.size(); i < n; i++) {
            query.setParameter(i, values.get(i));
        }
    }

    private String getNotSelfCondition() {
        String idCheck = "";

        Integer id = getModelId();
        if (id != null) {
            String idName = getIdProperty().getName();
            idCheck = " AND " + idName + " <> " + id;
        }

        return idCheck;
    }

    private List<String> getPropertyNames() {
        List<String> propertyNames = new ArrayList<String>();

        for (Property<?, ? super E> property : properties) {
            propertyNames.add(property.getName());
        }

        return propertyNames;
    }

    private List<Object> getValues(Session session) {
        List<Object> values = new ArrayList<Object>();
        E model = getModel();

        for (Property<?, ? super E> property : properties) {
            GetterInterceptor lazyLoad = new LazyLoaderInterceptor(session, 1);
            Object value = property.get(model, lazyLoad);
            values.add(value);
        }

        return values;
    }
}