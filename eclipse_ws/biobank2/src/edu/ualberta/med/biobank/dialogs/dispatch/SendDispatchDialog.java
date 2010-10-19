package edu.ualberta.med.biobank.dialogs.dispatch;

import java.util.Date;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.DispatchWrapper;
import edu.ualberta.med.biobank.common.wrappers.ShippingMethodWrapper;
import edu.ualberta.med.biobank.dialogs.BiobankDialog;
import edu.ualberta.med.biobank.validators.DateNotNulValidator;
import edu.ualberta.med.biobank.widgets.BiobankText;
import edu.ualberta.med.biobank.widgets.utils.ComboSelectionUpdate;

public class SendDispatchDialog extends BiobankDialog {

    private static final String TITLE = "Dispatching aliquots";
    private DispatchWrapper shipment;

    public SendDispatchDialog(Shell parentShell,
        DispatchWrapper shipment) {
        super(parentShell);
        this.shipment = shipment;
    }

    @Override
    protected String getTitleAreaMessage() {
        return "Fill the following fields to complete the shipment";
    }

    @Override
    protected String getTitleAreaTitle() {
        return TITLE;
    }

    @Override
    protected String getDialogShellTitle() {
        return TITLE;
    }

    @Override
    protected void createDialogAreaInternal(Composite parent) throws Exception {
        Composite contents = new Composite(parent, SWT.NONE);
        contents.setLayout(new GridLayout(2, false));
        contents.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        ShippingMethodWrapper selectedShippingMethod =
            shipment.getShippingMethod();
        widgetCreator.createComboViewer(contents, "Shipping Method",
            ShippingMethodWrapper.getShippingMethods(SessionManager
                .getAppService()), selectedShippingMethod, null,
            new ComboSelectionUpdate() {
                @Override
                public void doSelection(Object selectedObject) {
                    shipment
                        .setShippingMethod((ShippingMethodWrapper) selectedObject);
                }
            });

        createBoundWidgetWithLabel(contents, BiobankText.class, SWT.NONE,
            "Waybill", null,
            BeansObservables.observeValue(shipment, "waybill"), null);

        Date date = new Date();
        shipment.setDeparted(date);
        widgetCreator.createDateTimeWidget(contents, "Departed", date,
            BeansObservables.observeValue(shipment, "departed"),
            new DateNotNulValidator("Date shipped should be set"));
    }

}
