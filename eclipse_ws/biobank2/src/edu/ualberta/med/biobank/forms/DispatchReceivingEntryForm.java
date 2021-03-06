package edu.ualberta.med.biobank.forms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.action.scanprocess.CellInfo;
import edu.ualberta.med.biobank.common.action.scanprocess.ShipmentReceiveProcessAction;
import edu.ualberta.med.biobank.common.action.scanprocess.data.ShipmentProcessInfo;
import edu.ualberta.med.biobank.common.action.scanprocess.result.CellProcessResult;
import edu.ualberta.med.biobank.common.action.search.SpecimenByMicroplateSearchAction;
import edu.ualberta.med.biobank.common.action.specimen.SpecimenGetInfoAction;
import edu.ualberta.med.biobank.common.exception.BiobankException;
import edu.ualberta.med.biobank.common.peer.ShipmentInfoPeer;
import edu.ualberta.med.biobank.common.util.InventoryIdUtil;
import edu.ualberta.med.biobank.common.util.StringUtil;
import edu.ualberta.med.biobank.common.wrappers.SpecimenWrapper;
import edu.ualberta.med.biobank.dialogs.dispatch.DispatchReceiveScanDialog;
import edu.ualberta.med.biobank.gui.common.BgcPlugin;
import edu.ualberta.med.biobank.gui.common.widgets.BgcBaseText;
import edu.ualberta.med.biobank.model.Comment;
import edu.ualberta.med.biobank.model.Dispatch;
import edu.ualberta.med.biobank.model.ShipmentInfo;
import edu.ualberta.med.biobank.model.ShippingMethod;
import edu.ualberta.med.biobank.model.Specimen;
import edu.ualberta.med.biobank.model.type.DispatchSpecimenState;
import edu.ualberta.med.biobank.widgets.infotables.CommentsInfoTable;
import edu.ualberta.med.biobank.widgets.trees.DispatchSpecimensTreeTable;

public class DispatchReceivingEntryForm extends AbstractDispatchEntryForm {
    private static final I18n i18n = I18nFactory
        .getI18n(DispatchReceivingEntryForm.class);

    @SuppressWarnings("nls")
    public static final String ID =
        "edu.ualberta.med.biobank.forms.DispatchReceivingEntryForm";
    private DispatchSpecimensTreeTable specimensTree;
    private final List<SpecimenWrapper> receivedOrExtraSpecimens =
        new ArrayList<SpecimenWrapper>();
    private CommentsInfoTable commentEntryTable;

    @SuppressWarnings("nls")
    @Override
    protected void createFormContent() throws Exception {
        form.setText(i18n.tr("Dispatch sent on {0} from ",
            dispatch.getFormattedPackedAt(), dispatch.getSenderCenter()
                .getNameShort()));
        page.setLayout(new GridLayout(1, false));
        page.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createMainSection();
        boolean editSpecimens = true;
        setFirstControl(form);

        if (editSpecimens)
            createSpecimensSelectionActions(page, true);
        specimensTree =
            new DispatchSpecimensTreeTable(page, dispatch,
                editSpecimens);
        specimensTree.addSelectionChangedListener(biobankListener);
        specimensTree.addClickListener();
    }

    @SuppressWarnings("nls")
    private void createMainSection() {
        Composite client = toolkit.createComposite(page);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolkit.paintBordersFor(client);

        BgcBaseText senderLabel = createReadOnlyLabelledField(client, SWT.NONE,
            Dispatch.PropertyName.SENDER_CENTER.toString());
        setTextValue(senderLabel, dispatch.getSenderCenter().getName());
        BgcBaseText receiverLabel = createReadOnlyLabelledField(client,
            SWT.NONE, Dispatch.PropertyName.RECEIVER_CENTER.toString());
        setTextValue(receiverLabel, dispatch.getReceiverCenter().getName());
        BgcBaseText departedLabel = createReadOnlyLabelledField(client,
            SWT.NONE, i18n.tr("Departed"));
        setTextValue(departedLabel, dispatch.getFormattedPackedAt());
        BgcBaseText shippingMethodLabel = createReadOnlyLabelledField(client,
            SWT.NONE, ShippingMethod.NAME.singular().toString());
        setTextValue(shippingMethodLabel, dispatch.getShipmentInfo()
            .getShippingMethod() == null ? StringUtil.EMPTY_STRING : dispatch
            .getShipmentInfo()
            .getShippingMethod().getName());
        BgcBaseText waybillLabel = createReadOnlyLabelledField(client,
            SWT.NONE, ShipmentInfo.PropertyName.WAYBILL.toString());
        setTextValue(waybillLabel, dispatch.getShipmentInfo().getWaybill());
        createDateTimeWidget(client, i18n.tr("Date received"), null,
            dispatch.getShipmentInfo(),
            ShipmentInfoPeer.RECEIVED_AT.getName(), null);

        createCommentSection();

    }

    @SuppressWarnings("nls")
    private void createCommentSection() {
        Composite client =
            createSectionWithClient(Comment.NAME.plural().toString());
        GridLayout gl = new GridLayout(2, false);

        client.setLayout(gl);
        commentEntryTable =
            new CommentsInfoTable(client,
                dispatch.getCommentCollection(false));
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        commentEntryTable.setLayoutData(gd);
        createBoundWidgetWithLabel(client, BgcBaseText.class,
            SWT.MULTI, i18n.tr("Add a comment"), null, comment, "message", null);

    }

    @Override
    protected void openScanDialog() {
        DispatchReceiveScanDialog dialog = new DispatchReceiveScanDialog(
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
            dispatch, dispatch.getReceiverCenter());
        dialog.open();
        if (dispatch.hasNewSpecimens() || dispatch.hasSpecimenStatesChanged())
            setDirty(true);
        reloadSpecimens();
    }

    @SuppressWarnings("nls")
    @Override
    protected void doSpecimenTextAction(String inventoryId) {
        try {
            doSpecimenTextAction(inventoryId, true);
        } catch (Exception e) {
            BgcPlugin.openAsyncError(
                i18n.tr("Problem with specimen"), e);
        }
    }

    @SuppressWarnings("nls")
    @Override
    protected void doMicroplateTextAction(String microplateId) {
        if (InventoryIdUtil.isFormatMicroplate(microplateId)) {
            try {
                ArrayList<String> ids = SessionManager
                    .getAppService().doAction(
                        new SpecimenByMicroplateSearchAction(microplateId)).getList();
                if (ids.isEmpty()) {
                    BgcPlugin.openAsyncError(
                        i18n.tr("Microplate does not exist or has no specimens"));
                }
                else {
                    for (String id : ids) {
                        doSpecimenTextAction(id, true);
                    }
                }
            } catch (Exception e) {
                BgcPlugin.openAsyncError(
                    i18n.tr("Problem adding microplate specimens"), e);
            }
        }
        else {
            BgcPlugin.openAsyncError(
                i18n.tr("Microplate ID format not valid"));
        }
    }

    /**
     * when called from gui, errors will show a dialog. Otherwise, will throw an exception
     */
    @SuppressWarnings("nls")
    protected void doSpecimenTextAction(String inventoryId, boolean showMessages)
        throws Exception {
        Assert.isNotNull(SessionManager.getUser().getCurrentWorkingCenter());
        try {
            CellProcessResult res = (CellProcessResult) SessionManager
                .getAppService().doAction(
                    new ShipmentReceiveProcessAction(
                        new ShipmentProcessInfo(null, dispatch, false),
                        SessionManager.getUser().getCurrentWorkingCenter()
                            .getId(),
                        new CellInfo(-1, -1, inventoryId, null),
                        Locale.getDefault()));
            SpecimenWrapper specimen = null;
            if (res.getCell().getSpecimenId() != null) {
                Specimen spec = SessionManager.getAppService()
                    .doAction(new SpecimenGetInfoAction(res
                        .getCell().getSpecimenId())).getSpecimen();
                specimen =
                    new SpecimenWrapper(SessionManager.getAppService(), spec);
            }
            switch (res.getCell().getStatus()) {
            case IN_SHIPMENT_EXPECTED:
                dispatch.receiveSpecimens(Arrays.asList(specimen));
                receivedOrExtraSpecimens.add(specimen);
                reloadSpecimens();
                setDirty(true);
                break;
            case IN_SHIPMENT_RECEIVED:
                if (showMessages)
                    BgcPlugin
                        .openInformation(
                            i18n.tr("Specimen already accepted"),
                            i18n.tr(
                                "Specimen with inventory id {0} is already in received list.",
                                inventoryId));
                break;
            case EXTRA:
                if (showMessages)
                    BgcPlugin.openInformation(
                        i18n.tr("Specimen not found"),
                        i18n.tr(
                            "Specimen with inventory id {0} has not been found in this dispatch. It will be moved into the extra-pending list.",
                            inventoryId));
                if (specimen == null) {
                    if (showMessages)
                        BgcPlugin.openAsyncError(
                            i18n.tr("Problem with specimen"),
                            i18n.tr("Specimen is extra but object is null"));
                    else
                        throw new Exception(i18n.tr("Specimen is extra but object is null"));
                    break;
                }
                dispatch.addSpecimens(Arrays.asList(specimen),
                    DispatchSpecimenState.EXTRA);
                receivedOrExtraSpecimens.add(specimen);
                reloadSpecimens();
                setDirty(true);
                break;
            default:
                if (showMessages)
                    BgcPlugin.openInformation(
                        i18n.tr("Problem with specimen"),
                        res.getCell().getInformation().toString());
                else
                    throw new Exception(i18n.tr("Problem with specimen"));
            }
        } catch (Exception e) {
            if (showMessages)
                BgcPlugin.openAsyncError(
                    i18n.tr("Error receiving the specimen"), e);
            else
                throw e;
        }
    }

    @SuppressWarnings("nls")
    @Override
    protected String getOkMessage() {
        // title area message.
        return i18n.tr("Receiving dispatch");
    }

    @Override
    public String getNextOpenedFormId() {
        return DispatchViewForm.ID;
    }

    @SuppressWarnings("nls")
    @Override
    protected String getTextForPartName() {
        // tab name
        return i18n.tr("Dispatch sent on {0}", dispatch
            .getShipmentInfo().getPackedAt());
    }

    @Override
    protected void reloadSpecimens() {
        specimensTree.refresh();
    }

    @Override
    protected boolean needToTryAgainIfConcurrency() {
        return true;
    }

    @SuppressWarnings("nls")
    @Override
    protected void doTrySettingAgain() throws Exception {
        dispatch.reloadDispatchSpecimens();
        Map<String, String> problems = new HashMap<String, String>();
        // work on a copy of the list to avoid concurrency pb on list
        List<SpecimenWrapper> receveidOrExtrasCopy =
            new ArrayList<SpecimenWrapper>(
                receivedOrExtraSpecimens);
        receivedOrExtraSpecimens.clear();
        for (SpecimenWrapper spec : receveidOrExtrasCopy) {
            try {
                doSpecimenTextAction(spec.getInventoryId(), false);
            } catch (Exception ex) {
                problems.put(spec.getInventoryId(), ex.getMessage());
            }
        }

        if (problems.size() != 0) {
            StringBuffer msg = new StringBuffer();
            for (Entry<String, String> entry : problems.entrySet()) {
                if (msg.length() > 0)
                    msg.append("\n");
                msg.append(entry.getKey()).append(": ")
                    .append(entry.getValue());
            }
            throw new BiobankException(
                i18n.tr("Error trying to add again all specimens. If you save only those specimens won't be added:\n")
                    + msg.toString());
        }
    }

}
