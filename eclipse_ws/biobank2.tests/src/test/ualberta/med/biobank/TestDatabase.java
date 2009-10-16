package test.ualberta.med.biobank;

import edu.ualberta.med.biobank.common.BiobankCheckException;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import gov.nih.nci.system.applicationservice.WritableApplicationService;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;

public class TestDatabase {
    protected static WritableApplicationService appService;

    private static final List<String> IGNORE_RETURN_TYPES = new ArrayList<String>() {
        private static final long serialVersionUID = 1L;
        {
            add("java.lang.Class");
            add("java.lang.Object");
            add("java.util.Set");
            add("java.util.List");
        }
    };

    private class GetterInfo {
        Method getMethod;
        Method setMethod;
    }

    @Before
    public void setUp() throws Exception {
        appService = AllTests.appService;
        if (appService == null) {
            AllTests.setUp();
            appService = AllTests.appService;
        }
    }

    public Collection<GetterInfo> getGettersAndSetters(ModelWrapper<?> w) {
        HashMap<String, GetterInfo> map = new HashMap<String, GetterInfo>();
        Method[] methods = w.getClass().getMethods();

        for (Method method : methods) {
            if (method.getName().startsWith("get")
                && !method.getName().equals("getClass")
                && !IGNORE_RETURN_TYPES.contains(method.getReturnType()
                    .getName())
                && !method.getReturnType().getName().startsWith(
                    "edu.ualberta.med.biobank.common.wrappers")) {
                GetterInfo getterInfo = new GetterInfo();
                getterInfo.getMethod = method;
                map.put(method.getName(), getterInfo);
            }
        }

        for (Method method : methods) {
            if (method.getName().startsWith("set")
                && !method.getName().equals("setClass")) {
                String setterName = method.getName();
                String getterName = "g"
                    + setterName.substring(1, setterName.length());
                GetterInfo getterInfo = map.get(getterName);
                if (getterInfo == null) {
                    System.out.println("no getter found for "
                        + w.getClass().getName() + "." + setterName + "()");
                    continue;
                }
                getterInfo.setMethod = method;
            }
        }
        return map.values();
    }

    public void testGettersAndSetters(ModelWrapper<?> w)
        throws BiobankCheckException, Exception {
        Collection<GetterInfo> gettersInfoList = getGettersAndSetters(w);
        for (GetterInfo getterInfo : gettersInfoList) {
            if (getterInfo.setMethod == null) {
                System.out.println("no setter found for "
                    + w.getClass().getName() + "."
                    + getterInfo.getMethod.getName() + "()");
                continue;
            }

            String getReturnType = getterInfo.getMethod.getReturnType()
                .getName();

            Object parameter = null;

            if (getReturnType.equals("java.lang.Boolean")) {
                parameter = new Boolean(true);
            } else if (getReturnType.equals("java.lang.Integer")) {
                parameter = new Integer(1);
            } else if (getReturnType.equals("java.lang.Double")) {
                parameter = new Double(1.0);
            } else if (getReturnType.equals("java.lang.String")) {
                parameter = new String("abcdef");
            } else {
                throw new Exception("return type " + getReturnType
                    + " for method " + getterInfo.getMethod.getName()
                    + " for class " + w.getClass().getName()
                    + " not implemented");
            }

            getterInfo.setMethod.invoke(w, parameter);
            w.persist();
            w.reload();
            Object getResult = getterInfo.getMethod.invoke(w);

            Assert.assertEquals(parameter, getResult);
        }

    }
}
