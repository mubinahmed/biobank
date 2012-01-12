package edu.ualberta.med.biobank.test;

import edu.ualberta.med.biobank.client.util.ServiceConnection;
import edu.ualberta.med.biobank.common.action.Action;
import edu.ualberta.med.biobank.common.action.ActionCallback;
import edu.ualberta.med.biobank.common.action.ActionResult;
import edu.ualberta.med.biobank.common.action.Dispatcher;
import edu.ualberta.med.biobank.test.action.IActionExecutor;

public class TestingDispatcher implements Dispatcher {
    @Override
    public <T extends ActionResult> T exec(Action<T> action) {
        T result = null;
        try {
            IActionExecutor service = ServiceConnection
                .getAppService(
                    System.getProperty("server", "http://localhost:8080")
                        + "/biobank", "testuser", "test");
            result = service.exec(action);
        } catch (Exception e) {
            // TODO: handle this better by (1) declaring thrown exception(s)?
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public <T extends ActionResult> boolean exec(Action<T> action,
        ActionCallback<T> callback) {
        boolean success = false;
        try {
            IActionExecutor service = ServiceConnection
                .getAppService(
                    System.getProperty("server", "http://localhost:8080")
                        + "/biobank", "testuser", "test");
            T result = service.exec(action);
            success = true;
            callback.onSuccess(result);
        } catch (Throwable caught) {
            callback.onFailure(caught);
        }
        return success;
    }
}
