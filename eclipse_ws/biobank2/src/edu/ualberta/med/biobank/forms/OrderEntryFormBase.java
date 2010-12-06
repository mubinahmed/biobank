package edu.ualberta.med.biobank.forms;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Section;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.common.util.OrderState;
import edu.ualberta.med.biobank.common.wrappers.OrderWrapper;
import edu.ualberta.med.biobank.treeview.order.OrderAdapter;
import edu.ualberta.med.biobank.widgets.BiobankText;
import edu.ualberta.med.biobank.widgets.OrderAliquotsTreeTable;

public class OrderEntryFormBase extends BiobankFormBase {

    public static final String ID = "edu.ualberta.med.biobank.forms.OrderEntryFormBase";
    private OrderWrapper order;
    private OrderAliquotsTreeTable aliquotsTree;

    // private OrderAliquotsTreeTable aliquotsTree;

    @Override
    protected void createFormContent() throws Exception {
        form.setText("Order placed on " + order.getSubmitted()
            + "KDCS Research Group");
        page.setLayout(new GridLayout(1, false));
        page.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createMainSection();

    }

    private void createMainSection() {
        Composite client = toolkit.createComposite(page);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolkit.paintBordersFor(client);

        BiobankText studyLabel = createReadOnlyLabelledField(client, SWT.NONE,
            "Study");
        setTextValue(studyLabel, order.getStudy());

        BiobankText researchGroupLabel = createReadOnlyLabelledField(client,
            SWT.NONE, "Research Group");
        setTextValue(researchGroupLabel, order.getStudy().getResearchGroup()
            .getNameShort());
        BiobankText siteLabel = createReadOnlyLabelledField(client, SWT.NONE,
            "Site");
        setTextValue(siteLabel, order.getSite().getNameShort());
        BiobankText submittedLabel = createReadOnlyLabelledField(client,
            SWT.NONE, "Order Submitted");
        setTextValue(submittedLabel, order.getSubmitted());
        BiobankText orderNumberLabel = createReadOnlyLabelledField(client,
            SWT.NONE, "Order Number");
        setTextValue(orderNumberLabel, order.getId());
        BiobankText acceptedLabel = createReadOnlyLabelledField(client,
            SWT.NONE, "Date received");
        setTextValue(acceptedLabel, order.getAccepted());
        createReadOnlyLabelledField(client, SWT.NONE, "Comments");
        Section s = createSection("Aliquots");

        Composite c = new Composite(s, SWT.NONE);
        c.setLayout(new GridLayout());
        createAliquotsSelectionActions(c, false);
        aliquotsTree = new OrderAliquotsTreeTable(c, null, true, true);

        s.setClient(c);

        Button button = new Button(c, SWT.PUSH);
        Integer orderState = ((OrderWrapper) adapter.getModelObject())
            .getState();
        if (orderState.equals(OrderState.APPROVED))
            button.setText("Accept Order");
        else if (orderState.equals(OrderState.ACCEPTED))
            button.setText("Mark as filled");
        else if (orderState.equals(OrderState.FILLED))
            button.setText("Mark as shipped");
        else if (orderState.equals(OrderState.SHIPPED))
            button.setText("Close");
        else
            BioBankPlugin.openAsyncError("Invalid State",
                "This state does not belong in the client");

    }

    @SuppressWarnings("unused")
    protected void createAliquotsSelectionActions(Composite composite,
        boolean setAsFirstControl) {
        Composite addComposite = toolkit.createComposite(composite);
        addComposite.setLayout(new GridLayout(5, false));
        toolkit.createLabel(addComposite, "Enter inventory ID to add:");
        final BiobankText newAliquotText = new BiobankText(addComposite,
            SWT.NONE, toolkit);
        Button addButton = toolkit.createButton(addComposite, "", SWT.PUSH);
        addButton.setImage(BioBankPlugin.getDefault().getImageRegistry()
            .get(BioBankPlugin.IMG_ADD));
        addButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                newAliquotText.setFocus();
                newAliquotText.setText("");
            }
        });
        toolkit.createLabel(addComposite, "or open scan dialog:");
        Button openScanButton = toolkit
            .createButton(addComposite, "", SWT.PUSH);
        openScanButton.setImage(BioBankPlugin.getDefault().getImageRegistry()
            .get(BioBankPlugin.IMG_DISPATCH_SHIPMENT_ADD_ALIQUOT));
        openScanButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            }
        });
    }

    @Override
    protected void init() throws Exception {
        Assert.isNotNull(adapter, "Adapter should be no null");
        Assert.isTrue((adapter instanceof OrderAdapter),
            "Invalid editor input: object of type "
                + adapter.getClass().getName());
        this.order = (OrderWrapper) adapter.getModelObject();
        setPartName("New Order");
    }
}
