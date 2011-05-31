package edu.ualberta.med.biobank.common.wrappers.checks;

import java.text.MessageFormat;

import org.hibernate.Session;

import edu.ualberta.med.biobank.common.wrappers.BiobankCheck;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.Property;
import edu.ualberta.med.biobank.server.applicationservice.exceptions.BiobankSessionException;
import edu.ualberta.med.biobank.server.applicationservice.exceptions.NullPropertyException;

class CheckNotNull<E> extends BiobankCheck<E> {
    private static final long serialVersionUID = 1L;
    private static final String EXCEPTION_STRING = "The {0} of {1} {2} must be defined (cannot be null).";

    private final Property<?, E> property;

    protected CheckNotNull(ModelWrapper<E> wrapper, Property<?, E> property) {
        super(wrapper);
        this.property = property;
    }

    @Override
    public Object doAction(Session session) throws BiobankSessionException {
        E model = getModel();
        Object value = property.get(model);

        if (value == null) {
            String propertyName = Format.propertyName(property);
            String modelClass = Format.modelClass(getModelClass());
            String modelString = getModelString();

            String msg = MessageFormat.format(EXCEPTION_STRING, propertyName,
                modelClass, modelString);

            throw new NullPropertyException(msg);
        }

        return null;
    }
}
