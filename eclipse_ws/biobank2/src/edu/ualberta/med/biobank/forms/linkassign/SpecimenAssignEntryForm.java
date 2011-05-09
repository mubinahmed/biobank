package edu.ualberta.med.biobank.forms.linkassign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.springframework.remoting.RemoteConnectFailureException;

import edu.ualberta.med.biobank.BiobankPlugin;
import edu.ualberta.med.biobank.Messages;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.common.exception.BiobankException;
import edu.ualberta.med.biobank.common.peer.ContainerPeer;
import edu.ualberta.med.biobank.common.scanprocess.data.AssignProcessData;
import edu.ualberta.med.biobank.common.scanprocess.data.ProcessData;
import edu.ualberta.med.biobank.common.util.RowColPos;
import edu.ualberta.med.biobank.common.wrappers.ActivityStatusWrapper;
import edu.ualberta.med.biobank.common.wrappers.CollectionEventWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenWrapper;
import edu.ualberta.med.biobank.dialogs.select.SelectParentContainerDialog;
import edu.ualberta.med.biobank.forms.listener.EnterKeyToNextFieldListener;
import edu.ualberta.med.biobank.logs.BiobankLogger;
import edu.ualberta.med.biobank.validators.AbstractValidator;
import edu.ualberta.med.biobank.validators.NonEmptyStringValidator;
import edu.ualberta.med.biobank.validators.StringLengthValidator;
import edu.ualberta.med.biobank.widgets.BiobankText;
import edu.ualberta.med.biobank.widgets.grids.ContainerDisplayWidget;
import edu.ualberta.med.biobank.widgets.grids.ScanPalletDisplay;
import edu.ualberta.med.biobank.widgets.grids.ScanPalletWidget;
import edu.ualberta.med.biobank.widgets.grids.cell.PalletCell;
import edu.ualberta.med.biobank.widgets.grids.cell.UICellStatus;
import edu.ualberta.med.biobank.widgets.utils.ComboSelectionUpdate;
import edu.ualberta.med.scannerconfig.dmscanlib.ScanCell;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class SpecimenAssignEntryForm extends AbstractLinkAssignEntryForm {

    public static final String ID = "edu.ualberta.med.biobank.forms.SpecimenAssignEntryForm"; //$NON-NLS-1$

    private static BiobankLogger logger = BiobankLogger
        .getLogger(SpecimenAssignEntryForm.class.getName());

    private static boolean singleMode = false;

    private static final String INVENTORY_ID_BINDING = "inventoryId-binding"; //$NON-NLS-1$

    private static final String NEW_SINGLE_POSITION_BINDING = "newSinglePosition-binding"; //$NON-NLS-1$

    private static final String PRODUCT_BARCODE_BINDING = "productBarcode-binding"; //$NON-NLS-1$

    private static final String LABEL_BINDING = "label-binding"; //$NON-NLS-1$

    private static final String PALLET_TYPES_BINDING = "palletType-binding"; //$NON-NLS-1$

    protected static boolean useScanner = true;

    // parents of either the specimen in single mode or the pallet/box in
    // multiple mode. First container, is the direct parent, second is the
    // parent parent, etc...
    private List<ContainerWrapper> parentContainers;

    // for single specimen assign
    private BiobankText inventoryIdText;
    protected boolean inventoryIdModified;
    private Label oldSinglePositionLabel;
    private BiobankText oldSinglePositionText;
    private Label oldSinglePositionCheckLabel;
    private AbstractValidator oldSinglePositionCheckValidator;
    private BiobankText oldSinglePositionCheckText;
    private Label newSinglePositionLabel;
    private StringLengthValidator newSinglePositionValidator;
    private BiobankText newSinglePositionText;
    protected boolean positionTextModified;
    private Label thirdSingleParentLabel;
    private Label secondSingleParentLabel;
    private ContainerDisplayWidget thirdSingleParentWidget;
    private ContainerDisplayWidget secondSingleParentWidget;
    private Composite singleVisualisation;

    // for multiple specimens assign
    private ScanPalletWidget palletWidget;
    private ContainerWrapper currentMultipleContainer;
    private Composite multipleVisualisation;
    protected boolean palletproductBarcodeTextModified;
    private NonEmptyStringValidator productBarcodeValidator;
    protected boolean isModifyingMultipleFields;
    // private IObservableValue multipleValidationMade = new WritableValue(
    // Boolean.TRUE, Boolean.class);
    private NonEmptyStringValidator palletLabelValidator;
    private BiobankText palletPositionText;
    protected boolean useNewProductBarcode;
    private ContainerWrapper containerToRemove;
    private ComboViewer palletTypesViewer;
    private ContainerDisplayWidget freezerWidget;
    private ContainerDisplayWidget hotelWidget;
    private Label freezerLabel;
    private Label hotelLabel;
    private Label palletLabel;
    protected boolean palletPositionTextModified;
    private List<ContainerTypeWrapper> palletContainerTypes;
    private BiobankText palletproductBarcodeText;
    private boolean saveEvenIfMissing;
    private boolean isFakeScanLinkedOnly;
    private Button fakeScanLinkedOnlyButton;
    private Composite multipleOptionsFields;
    private Composite fakeScanComposite;
    private Button useScannerButton;
    private Label palletproductBarcodeLabel;
    // pallet found with given product barcode
    private ContainerWrapper oldPalletFoundWithProductBarcode;
    private boolean isNewMultipleContainer;
    private boolean checkingMultipleContainerPosition;

    @Override
    protected void init() throws Exception {
        super.init();
        setCanLaunchScan(true);
        currentMultipleContainer = new ContainerWrapper(appService);
        initPalletValues();
    }

    /**
     * Multiple. initialize pallet
     */
    private void initPalletValues() {
        try {
            currentMultipleContainer.initObjectWith(new ContainerWrapper(
                appService));
            currentMultipleContainer.reset();
            currentMultipleContainer.setActivityStatus(ActivityStatusWrapper
                .getActiveActivityStatus(appService));
            currentMultipleContainer.setSite(SessionManager.getUser()
                .getCurrentWorkingSite());
        } catch (Exception e) {
            logger.error("Error while reseting pallet values", e); //$NON-NLS-1$
        }
    }

    @Override
    protected String getActivityTitle() {
        return Messages.getString("SpecimenAssign.activity.title"); //$NON-NLS-1$
    }

    @Override
    public BiobankLogger getErrorLogger() {
        return logger;
    }

    @Override
    protected String getFormTitle() {
        return Messages.getString("SpecimenAssign.form.title"); //$NON-NLS-1$
    }

    @Override
    protected boolean isSingleMode() {
        return singleMode;
    }

    @Override
    protected void setSingleMode(boolean single) {
        singleMode = single;
    }

    @Override
    protected String getOkMessage() {
        return Messages.getString("SpecimenAssign.okmessage"); //$NON-NLS-1$
    }

    @Override
    public String getNextOpenedFormID() {
        return ID;
    }

    @Override
    protected void createCommonFields(Composite commonFieldsComposite) {
        BiobankText siteLabel = createReadOnlyLabelledField(
            commonFieldsComposite, SWT.NONE,
            Messages.getString("SpecimenAssign.site.label")); //$NON-NLS-1$
        siteLabel.setText(SessionManager.getUser().getCurrentWorkingCenter()
            .getNameShort());
    }

    @Override
    protected int getLeftSectionWidth() {
        return 450;
    }

    @Override
    protected void createSingleFields(Composite parent) {
        Composite fieldsComposite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        fieldsComposite.setLayout(layout);
        toolkit.paintBordersFor(fieldsComposite);
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        fieldsComposite.setLayoutData(gd);

        // inventoryID
        inventoryIdText = (BiobankText) createBoundWidgetWithLabel(
            fieldsComposite,
            BiobankText.class,
            SWT.NONE,
            Messages.getString("SpecimenAssign.single.inventoryId.label"), new String[0], //$NON-NLS-1$
            singleSpecimen,
            "inventoryId", //$NON-NLS-1$
            new NonEmptyStringValidator(Messages
                .getString("SpecimenAssign.single.inventoryId.validator.msg")), //$NON-NLS-1$
            INVENTORY_ID_BINDING);
        inventoryIdText.addKeyListener(textFieldKeyListener);
        inventoryIdText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (inventoryIdModified)
                    try {
                        retrieveSingleSpecimenData();
                    } catch (Exception ex) {
                        BiobankPlugin.openError("Move - specimen error", ex); //$NON-NLS-1$
                        focusControl(inventoryIdText);
                    }
                inventoryIdModified = false;
            }
        });
        inventoryIdText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                inventoryIdModified = true;
                displayPositions(false);
            }
        });
        createPositionFields(fieldsComposite);
    }

    @Override
    protected void createContainersVisualisation(Composite parent) {
        createMultipleVisualisation(parent);
        createSingleVisualisation(parent);
    }

    /**
     * 
     * Single assign. Search the specimen, if find it, display related
     * information
     */
    protected void retrieveSingleSpecimenData() throws Exception {
        String inventoryId = inventoryIdText.getText();
        if (inventoryId.isEmpty()) {
            return;
        }
        // FIXME only for old Cabinet... SHould we still search for that?
        // if search for 'AAAA' exist in a freezer, but we also have 'CAAAA'...
        // if (inventoryId.length() == 4) {
        // // compatibility with old cabinet specimens imported
        // // 4 letters specimens are now C+4letters
        //            inventoryId = "C" + inventoryId; //$NON-NLS-1$
        // }
        reset();
        singleSpecimen.setInventoryId(inventoryId);
        inventoryIdText.setText(inventoryId);
        oldSinglePositionCheckText.setText("?"); //$NON-NLS-1$

        appendLog(Messages.getString(
            "SpecimenAssign.single.activitylog.gettingInfoId", //$NON-NLS-1$
            singleSpecimen.getInventoryId()));
        SpecimenWrapper foundSpecimen = SpecimenWrapper.getSpecimen(appService,
            singleSpecimen.getInventoryId(), SessionManager.getUser());
        if (foundSpecimen == null) {
            throw new Exception(Messages.getString(
                "SpecimenAssign.single.inventoryId.error", //$NON-NLS-1$
                singleSpecimen.getInventoryId()));
        }
        singleSpecimen.initObjectWith(foundSpecimen);
        if (singleSpecimen.isUsedInDispatch()) {
            throw new Exception(
                Messages
                    .getString("SpecimenAssign.single.specimen.transit.error")); //$NON-NLS-1$
        }
        String positionString = singleSpecimen.getPositionString(true, false);
        if (positionString == null) {
            displayOldSingleFields(false);
            positionString = Messages.getString("SpecimenAssign.position.none"); //$NON-NLS-1$
            newSinglePositionText.setFocus();
        } else {
            displayOldSingleFields(true);
            oldSinglePositionCheckText.setText(oldSinglePositionCheckText
                .getText());
            oldSinglePositionCheckText.setFocus();
        }
        oldSinglePositionText.setText(positionString);
        appendLog(Messages.getString(
            "SpecimenAssign.single.activitylog.specimenInfo", //$NON-NLS-1$
            singleSpecimen.getInventoryId(), positionString));
    }

    /**
     * Single assign: Some fields will be displayed only if the specimen has
     * already a position
     */
    private void createPositionFields(Composite fieldsComposite) {
        // for move mode: display old position retrieved from database
        oldSinglePositionLabel = widgetCreator.createLabel(fieldsComposite,
            Messages.getString("SpecimenAssign.single.old.position.label")); //$NON-NLS-1$
        oldSinglePositionText = (BiobankText) widgetCreator.createBoundWidget(
            fieldsComposite, BiobankText.class, SWT.NONE,
            oldSinglePositionLabel, new String[0], null, null);
        oldSinglePositionText.setEnabled(false);
        oldSinglePositionText
            .addKeyListener(EnterKeyToNextFieldListener.INSTANCE);

        // for move mode: field to enter old position. Check needed to be sure
        // nothing is wrong with the specimen
        oldSinglePositionCheckLabel = widgetCreator.createLabel(
            fieldsComposite, Messages
                .getString("SpecimenAssign.single.old.position.check.label")); //$NON-NLS-1$
        oldSinglePositionCheckValidator = new AbstractValidator(
            Messages
                .getString("SpecimenAssign.single.old.position.check.validation.msg")) { //$NON-NLS-1$
            @Override
            public IStatus validate(Object value) {
                if (value != null && !(value instanceof String)) {
                    throw new RuntimeException(
                        "Not supposed to be called for non-strings."); //$NON-NLS-1$
                }

                if (value != null) {
                    String s = (String) value;
                    if (s.equals(oldSinglePositionText.getText())) {
                        hideDecoration();
                        return Status.OK_STATUS;
                    }
                }
                showDecoration();
                return ValidationStatus.error(errorMessage);
            }
        };
        oldSinglePositionCheckText = (BiobankText) widgetCreator
            .createBoundWidget(fieldsComposite, BiobankText.class, SWT.NONE,
                oldSinglePositionCheckLabel, new String[0], new WritableValue(
                    "", String.class), oldSinglePositionCheckValidator); //$NON-NLS-1$
        oldSinglePositionCheckText
            .addKeyListener(EnterKeyToNextFieldListener.INSTANCE);

        // for all modes: position to be assigned to the specimen
        newSinglePositionLabel = widgetCreator.createLabel(fieldsComposite,
            Messages.getString("SpecimenAssign.single.position.label")); //$NON-NLS-1$
        newSinglePositionValidator = new StringLengthValidator(4,
            Messages.getString("SpecimenAssign.single.position.validationMsg")); //$NON-NLS-1$
        displayOldSingleFields(false);
        newSinglePositionText = (BiobankText) widgetCreator.createBoundWidget(
            fieldsComposite, BiobankText.class, SWT.NONE,
            newSinglePositionLabel, new String[0], new WritableValue("", //$NON-NLS-1$
                String.class), newSinglePositionValidator,
            NEW_SINGLE_POSITION_BINDING);
        newSinglePositionText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (positionTextModified
                    && newSinglePositionValidator
                        .validate(newSinglePositionText.getText()) == Status.OK_STATUS) {
                    BusyIndicator.showWhile(PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow().getShell().getDisplay(),
                        new Runnable() {
                            @Override
                            public void run() {
                                initContainersFromPosition(
                                    newSinglePositionText, false, null);
                                checkPositionAndSpecimen();
                            }
                        });
                }
                positionTextModified = false;
            }
        });
        newSinglePositionText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                positionTextModified = true;
                // resultShownValue.setValue(Boolean.FALSE);
                displayPositions(false);
            }
        });
        newSinglePositionText
            .addKeyListener(EnterKeyToNextFieldListener.INSTANCE);
        displayOldSingleFields(false);
    }

    /**
     * Single assign: show or hide old positions fields
     */
    private void displayOldSingleFields(boolean displayOld) {
        widgetCreator.showWidget(oldSinglePositionLabel, displayOld);
        widgetCreator.showWidget(oldSinglePositionText, displayOld);
        widgetCreator.showWidget(oldSinglePositionCheckLabel, displayOld);
        widgetCreator.showWidget(oldSinglePositionCheckText, displayOld);
        if (displayOld) {
            newSinglePositionLabel.setText(Messages
                .getString("SpecimenAssign.single.new.position.label") //$NON-NLS-1$
                + ":"); //$NON-NLS-1$
        } else {
            newSinglePositionLabel.setText(Messages
                .getString("SpecimenAssign.single.position.label") //$NON-NLS-1$
                + ":"); //$NON-NLS-1$
            oldSinglePositionCheckText.setText(oldSinglePositionText.getText());
        }
        page.layout(true, true);
    }

    /**
     * Search possible parents from the position text. Is used both by single
     * and multiple assign.
     * 
     * @param positionText the position to use for initialisation
     * @param isContainerPosition if true, the position is a full container
     *            position, if false, it is a full specimen position
     */
    protected void initContainersFromPosition(BiobankText positionText,
        boolean isContainerPosition, ContainerTypeWrapper type) {
        parentContainers = null;
        try {
            parentContainers = null;
            List<ContainerWrapper> foundContainers = ContainerWrapper
                .getPossibleContainersFromPosition(appService,
                    SessionManager.getUser(), positionText.getText(),
                    isContainerPosition, type);
            if (foundContainers.size() == 1) {
                initParentContainers(foundContainers.get(0));
            } else if (foundContainers.size() > 1) {
                SelectParentContainerDialog dlg = new SelectParentContainerDialog(
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getShell(), foundContainers);
                dlg.open();
                if (dlg.getSelectedContainer() == null) {
                    StringBuffer sb = new StringBuffer();
                    for (ContainerWrapper cont : foundContainers) {
                        sb.append(cont.getFullInfoLabel());
                    }
                    BiobankPlugin
                        .openError(
                            Messages
                                .getString("SpecimenAssign.single.checkParent.error.toomany.title"), //$NON-NLS-1$
                            Messages
                                .getString(
                                    "SpecimenAssign.single.checkParent.error.toomany.msg", //$NON-NLS-1$
                                    sb.toString()));
                    focusControl(positionText);
                } else
                    initParentContainers(dlg.getSelectedContainer());
            }
        } catch (BiobankException be) {
            BiobankPlugin
                .openError(
                    Messages
                        .getString("SpecimenAssign.container.init.position.error.title"), //$NON-NLS-1$
                    be);
            appendLog(Messages.getString(
                "SpecimenAssign.single.activitylog.checkParent.error", //$NON-NLS-1$
                be.getMessage()));
            focusControl(positionText);
        } catch (Exception ex) {
            BiobankPlugin
                .openError(
                    Messages
                        .getString("SpecimenAssign.container.init.position.error.title"), //$NON-NLS-1$
                    ex);
            focusControl(positionText);
        }
    }

    /**
     * Initialise parents
     */
    private void initParentContainers(ContainerWrapper bottomContainer) {
        parentContainers = new ArrayList<ContainerWrapper>();
        ContainerWrapper parent = bottomContainer;
        while (parent != null) {
            parentContainers.add(parent);
            parent = parent.getParentContainer();
        }
        StringBuffer parentMsg = new StringBuffer();
        for (int i = parentContainers.size() - 1; i >= 0; i--) {
            parent = parentContainers.get(i);
            String label = parent.getPositionString();
            if (label == null)
                label = parent.getLabel();
            parentMsg.append(label);
            if (i != 0)
                parentMsg.append("|"); //$NON-NLS-1$
        }
        appendLog(Messages.getString(
            "SpecimenAssign.activitylog.containers.init", //$NON-NLS-1$
            parentMsg.toString()));
    }

    /**
     * Single assign. Check can really add to the position
     */
    protected void checkPositionAndSpecimen() {
        BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
            @Override
            public void run() {
                try {
                    appendLog("----"); //$NON-NLS-1$
                    String positionString = newSinglePositionText.getText();
                    if (parentContainers == null
                        || parentContainers.size() == 0) {
                        displayPositions(false);
                        return;
                    }
                    appendLog(Messages
                        .getString(
                            "SpecimenAssign.single.activitylog.checkingPosition", positionString)); //$NON-NLS-1$
                    singleSpecimen.setSpecimenPositionFromString(
                        positionString, parentContainers.get(0));
                    if (singleSpecimen.isPositionFree(parentContainers.get(0))) {
                        singleSpecimen.setParent(parentContainers.get(0));
                        displayPositions(true);
                        cancelConfirmWidget.setFocus();
                    } else {
                        BiobankPlugin.openError(
                            Messages
                                .getString("SpecimenAssign.single.position.error.msg"), //$NON-NLS-1$
                            Messages
                                .getString(
                                    "SpecimenAssign.single.checkStatus.error", positionString, //$NON-NLS-1$
                                    parentContainers.get(0).getLabel()));
                        appendLog(Messages
                            .getString(
                                "SpecimenAssign.single.activitylog.checkPosition.error", //$NON-NLS-1$
                                positionString, parentContainers.get(0)
                                    .getLabel()));
                        focusControl(newSinglePositionText);
                        return;
                    }
                    setDirty(true);
                } catch (RemoteConnectFailureException exp) {
                    BiobankPlugin.openRemoteConnectErrorMessage(exp);
                } catch (BiobankCheckException bce) {
                    BiobankPlugin.openError(
                        "Error while checking position", bce); //$NON-NLS-1$
                    appendLog("ERROR: " + bce.getMessage()); //$NON-NLS-1$
                    // resultShownValue.setValue(Boolean.FALSE);
                    focusControl(inventoryIdText);
                } catch (Exception e) {
                    BiobankPlugin.openError("Error while checking position", e); //$NON-NLS-1$
                    focusControl(newSinglePositionText);
                }
            }
        });
    }

    /**
     * single assign. Display containers
     */
    private void displayPositions(boolean show) {
        if (singleMode) {
            if (secondSingleParentWidget != null) {
                widgetCreator.showWidget(secondSingleParentWidget, show);
                widgetCreator.showWidget(secondSingleParentLabel, show);
            }
            if (thirdSingleParentWidget != null) {
                widgetCreator.showWidget(thirdSingleParentLabel, show);
                widgetCreator.showWidget(thirdSingleParentWidget, show);
            }
            if (show) {
                if (parentContainers != null && parentContainers.size() >= 3) {
                    ContainerWrapper thirdParent = parentContainers.get(2);
                    ContainerWrapper secondParent = parentContainers.get(1);
                    ContainerWrapper firstParent = parentContainers.get(0);
                    thirdSingleParentWidget.setContainerType(thirdParent
                        .getContainerType());
                    thirdSingleParentWidget.setSelection(secondParent
                        .getPositionAsRowCol());
                    thirdSingleParentLabel.setText(thirdParent.getLabel());
                    secondSingleParentWidget.setContainer(secondParent);
                    secondSingleParentWidget.setSelection(firstParent
                        .getPositionAsRowCol());
                    secondSingleParentLabel.setText(secondParent.getLabel());
                }
            }
            showVisualisation(show);
            page.layout(true, true);
            book.reflow(true);
        }
    }

    private void createSingleVisualisation(Composite parent) {
        singleVisualisation = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        singleVisualisation.setLayout(layout);
        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        singleVisualisation.setLayoutData(gd);

        thirdSingleParentLabel = toolkit.createLabel(singleVisualisation, ""); //$NON-NLS-1$
        secondSingleParentLabel = toolkit.createLabel(singleVisualisation, ""); //$NON-NLS-1$

        ContainerTypeWrapper thirdSingleParentType = null;
        ContainerTypeWrapper secondSingleParentType = null;
        thirdSingleParentWidget = new ContainerDisplayWidget(
            singleVisualisation);
        thirdSingleParentWidget.setContainerType(thirdSingleParentType, true);
        toolkit.adapt(thirdSingleParentWidget);
        GridData gdDrawer = new GridData();
        gdDrawer.verticalAlignment = SWT.TOP;
        thirdSingleParentWidget.setLayoutData(gdDrawer);

        secondSingleParentWidget = new ContainerDisplayWidget(
            singleVisualisation);
        secondSingleParentWidget.setContainerType(secondSingleParentType, true);
        toolkit.adapt(secondSingleParentWidget);

        displayPositions(false);
    }

    private void createMultipleVisualisation(Composite parent) {
        multipleVisualisation = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(3, false);
        multipleVisualisation.setLayout(layout);
        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        multipleVisualisation.setLayoutData(gd);

        Composite freezerComposite = toolkit
            .createComposite(multipleVisualisation);
        layout = new GridLayout(1, false);
        layout.horizontalSpacing = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        freezerComposite.setLayout(layout);
        GridData gdFreezer = new GridData();
        gdFreezer.horizontalSpan = 3;
        gdFreezer.horizontalAlignment = SWT.RIGHT;
        freezerComposite.setLayoutData(gdFreezer);
        freezerLabel = toolkit.createLabel(freezerComposite, "Freezer"); //$NON-NLS-1$
        freezerLabel.setLayoutData(new GridData());
        freezerWidget = new ContainerDisplayWidget(freezerComposite);
        freezerWidget.initDisplayFromType(true);
        toolkit.adapt(freezerWidget);
        freezerWidget.setDisplaySize(ScanPalletDisplay.PALLET_WIDTH, 100);

        Composite hotelComposite = toolkit
            .createComposite(multipleVisualisation);
        layout = new GridLayout(1, false);
        layout.horizontalSpacing = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        hotelComposite.setLayout(layout);
        hotelComposite.setLayoutData(new GridData());
        hotelLabel = toolkit.createLabel(hotelComposite, "Hotel"); //$NON-NLS-1$
        hotelWidget = new ContainerDisplayWidget(hotelComposite);
        hotelWidget.initDisplayFromType(true);
        toolkit.adapt(hotelWidget);
        hotelWidget.setDisplaySize(100,
            ScanPalletDisplay.PALLET_HEIGHT_AND_LEGEND);

        Composite palletComposite = toolkit
            .createComposite(multipleVisualisation);
        layout = new GridLayout(1, false);
        layout.horizontalSpacing = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        palletComposite.setLayout(layout);
        palletComposite.setLayoutData(new GridData());
        palletLabel = toolkit.createLabel(palletComposite, "Pallet"); //$NON-NLS-1$
        palletWidget = new ScanPalletWidget(palletComposite,
            UICellStatus.DEFAULT_PALLET_SCAN_ASSIGN_STATUS_LIST);
        toolkit.adapt(palletWidget);
        palletWidget.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                manageDoubleClick(e);
            }
        });
        showOnlyPallet(true);

        createScanTubeAloneButton(multipleVisualisation);
    }

    @Override
    protected void createMultipleFields(Composite parent)
        throws ApplicationException {
        multipleOptionsFields = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        multipleOptionsFields.setLayout(layout);
        toolkit.paintBordersFor(multipleOptionsFields);
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        multipleOptionsFields.setLayoutData(gd);

        productBarcodeValidator = new NonEmptyStringValidator(
            Messages
                .getString("SpecimenAssign.multiple.productBarcode.validationMsg"));//$NON-NLS-1$
        palletLabelValidator = new NonEmptyStringValidator(
            Messages
                .getString("SpecimenAssign.multiple.palletLabel.validationMsg"));//$NON-NLS-1$

        widgetCreator.createLabel(multipleOptionsFields,
            Messages.getString("SpecimenAssign.useScanner.check.label")); //$NON-NLS-1$
        useScannerButton = toolkit.createButton(multipleOptionsFields,
            "", SWT.CHECK); //$NON-NLS-1$
        useScannerButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setUseScanner(useScannerButton.getSelection());
            }
        });

        palletproductBarcodeLabel = widgetCreator.createLabel(
            multipleOptionsFields,
            Messages.getString("SpecimenAssign.multiple.productBarcode.label")); //$NON-NLS-1$)
        palletproductBarcodeText = (BiobankText) createBoundWidget(
            multipleOptionsFields, BiobankText.class, SWT.NONE,
            palletproductBarcodeLabel, null, currentMultipleContainer,
            ContainerPeer.PRODUCT_BARCODE.getName(), productBarcodeValidator,
            PRODUCT_BARCODE_BINDING);
        palletproductBarcodeText.addKeyListener(textFieldKeyListener);
        gd = new GridData();
        gd.horizontalAlignment = SWT.FILL;
        palletproductBarcodeText.setLayoutData(gd);

        palletproductBarcodeText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (palletproductBarcodeTextModified
                    && productBarcodeValidator.validate(
                        currentMultipleContainer.getProductBarcode()).equals(
                        Status.OK_STATUS)) {
                    boolean ok = checkMultipleScanBarcode();
                    setCanLaunchScan(ok);
                    if (!ok)
                        focusControl(palletproductBarcodeText);
                }
                palletproductBarcodeTextModified = false;
            }
        });
        palletproductBarcodeText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (!checkingMultipleContainerPosition) {
                    palletproductBarcodeTextModified = true;
                    palletTypesViewer.setInput(null);
                    currentMultipleContainer.setContainerType(null);
                    palletPositionText.setEnabled(true);
                    palletPositionText.setText("");
                }
            }
        });

        palletPositionText = (BiobankText) createBoundWidgetWithLabel(
            multipleOptionsFields,
            BiobankText.class,
            SWT.NONE,
            Messages.getString("SpecimenAssign.multiple.palletLabel.label"), null, //$NON-NLS-1$
            currentMultipleContainer, ContainerPeer.LABEL.getName(),
            palletLabelValidator, LABEL_BINDING);
        palletPositionText.addKeyListener(EnterKeyToNextFieldListener.INSTANCE);
        gd = new GridData();
        gd.horizontalAlignment = SWT.FILL;
        palletPositionText.setLayoutData(gd);
        palletPositionText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (palletPositionText.isEnabled()
                    && palletPositionTextModified
                    && palletLabelValidator.validate(
                        currentMultipleContainer.getLabel()).equals(
                        Status.OK_STATUS)) {
                    checkingMultipleContainerPosition = true;
                    boolean ok = checkMultipleContainerPosition();
                    setCanLaunchScan(ok);
                    if (!ok)
                        focusControl(palletPositionText);
                    checkingMultipleContainerPosition = false;
                }
                palletPositionTextModified = false;
            }
        });
        palletPositionText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (!isModifyingMultipleFields) {
                    palletPositionTextModified = true;
                    palletTypesViewer.setInput(null);
                    currentMultipleContainer.setContainerType(null);
                }
            }
        });

        createPalletTypesViewer(multipleOptionsFields);

        createPlateToScanField(multipleOptionsFields);

        createScanButton(parent);
    }

    private boolean checkMultipleContainerPosition() {
        initContainersFromPosition(palletPositionText, true, null);
        if (parentContainers == null)
            return false;
        try {
            ContainerWrapper parent = parentContainers.get(0);
            ContainerWrapper containerAtPosition = parent
                .getChildByLabel(currentMultipleContainer.getLabel());
            List<ContainerTypeWrapper> possibleTypes = null;
            ContainerTypeWrapper typeSelection = null;
            boolean enableCombo = true;
            if (containerAtPosition == null) {
                // free position for the container
                parent.addChild(
                    currentMultipleContainer.getLabel().replaceAll(
                        parent.getLabel(), ""), currentMultipleContainer);
                possibleTypes = get96Types(parent.getContainerType()
                    .getChildContainerTypeCollection());
                if (possibleTypes.size() == 1) {
                    typeSelection = possibleTypes.get(0);
                }
            } else {
                String barcodeAtPosition = containerAtPosition
                    .getProductBarcode();
                if (barcodeAtPosition != null && !barcodeAtPosition.isEmpty()) {
                    if (!barcodeAtPosition.equals(currentMultipleContainer
                        .getProductBarcode())) {
                        BiobankPlugin
                            .openError(
                                Messages
                                    .getString("SpecimenAssign.multiple.dialog.positionUsed.error.title"), //$NON-NLS-1$
                                Messages
                                    .getString(
                                        "SpecimenAssign.multiple.dialog.positionUsed.error.msg", //$NON-NLS-1$
                                        barcodeAtPosition,
                                        currentMultipleContainer.getSite()
                                            .getNameShort())); //$NON-NLS-1$
                        appendLog(Messages
                            .getString(
                                "SpecimenAssign.multiple.activitylog.pallet.positionUsedMsg", //$NON-NLS-1$
                                barcodeAtPosition, currentMultipleContainer
                                    .getLabel(), currentMultipleContainer
                                    .getSite().getNameShort()));
                        return false;
                    }
                } else {
                    enableCombo = false;
                    // not barcode before: use barcode entered by user
                    if (containerAtPosition.hasSpecimens()) {
                        // Position already physically used but no barcode was
                        // set (old database compatibility)
                        appendLog(Messages
                            .getString(
                                "SpecimenAssign.multiple.activitylog.pallet.positionUsedWithNoProductBarcode", //$NON-NLS-1$
                                currentMultipleContainer.getLabel(),
                                containerAtPosition.getContainerType()
                                    .getName(), currentMultipleContainer
                                    .getProductBarcode()));
                    } else {
                        // Position initialised but not physically used
                        appendLog(Messages
                            .getString(
                                "SpecimenAssign.multiple.activitylog.pallet.positionInitialized", //$NON-NLS-1$
                                currentMultipleContainer.getLabel(),
                                containerAtPosition.getContainerType()
                                    .getName()));
                    }
                }
                String newBarcode = currentMultipleContainer
                    .getProductBarcode();
                typeSelection = containerAtPosition.getContainerType();
                possibleTypes = get96Types(Arrays.asList(typeSelection));
                currentMultipleContainer.initObjectWith(containerAtPosition);
                currentMultipleContainer.reset();
                containerAtPosition.reload();
                currentMultipleContainer.setLabel(containerAtPosition
                    .getLabel());
                if (newBarcode != null) {
                    palletproductBarcodeText.setText(newBarcode);
                }
            }
            palletTypesViewer.getCombo().setEnabled(enableCombo);
            palletTypesViewer.setInput(possibleTypes);
            if (possibleTypes.size() == 0) {
                BiobankPlugin.openAsyncError("96", "no 96 pallet types");
                typeSelection = null;
                return false;
            }
            if (typeSelection == null)
                palletTypesViewer.getCombo().deselectAll();
            else
                palletTypesViewer.setSelection(new StructuredSelection(
                    typeSelection));
        } catch (Exception ex) {
            BiobankPlugin.openError(Messages
                .getString("SpecimenAssign.multiple.validation.error.title"), //$NON-NLS-1$
                ex);
            appendLog(Messages.getString(
                "SpecimenAssign.multiple.activitylog.error", //$NON-NLS-1$
                ex.getMessage()));
            return false;
        } finally {
            checkingMultipleContainerPosition = false;
        }
        return true;
    }

    private List<ContainerTypeWrapper> get96Types(
        List<ContainerTypeWrapper> childContainerTypeCollection) {
        if (useScanner) {
            List<ContainerTypeWrapper> palletTypes = new ArrayList<ContainerTypeWrapper>();
            for (ContainerTypeWrapper type : childContainerTypeCollection) {
                if (type.isPallet96())
                    palletTypes.add(type);
            }
            return palletTypes;
        }
        return childContainerTypeCollection;
    }

    protected boolean checkMultipleScanBarcode() {
        try {
            ContainerWrapper palletFoundWithProductBarcode = ContainerWrapper
                .getContainerWithProductBarcodeInSite(appService,
                    currentMultipleContainer.getSite(),
                    currentMultipleContainer.getProductBarcode());
            isNewMultipleContainer = palletFoundWithProductBarcode == null;
            if (palletFoundWithProductBarcode != null) {
                // a container with this barcode exists
                if (!palletFoundWithProductBarcode.isPallet96()) {
                    BiobankPlugin
                        .openAsyncError(
                            Messages
                                .getString("SpecimenAssign.multiple.validation.error.title"),
                            Messages
                                .getString("SpecimenAssign.barcode.notPallet.error.msg"));
                    return false;
                }
                if (!palletPositionText.getText().isEmpty()
                    && !palletPositionText.getText().equals(
                        palletFoundWithProductBarcode.getLabel())) {
                    // a label was entered but is different from the one set to
                    // the pallet retrieved
                    BiobankPlugin
                        .openAsyncError(
                            Messages
                                .getString("SpecimenAssign.multiple.validation.error.title"),
                            Messages
                                .getString(
                                    "SpecimenAssign.barcode.exists.different.position.error.msg",
                                    palletFoundWithProductBarcode
                                        .getProductBarcode(),
                                    palletFoundWithProductBarcode
                                        .getFullInfoLabel()));
                    return false;
                }

                currentMultipleContainer
                    .initObjectWith(palletFoundWithProductBarcode);
                currentMultipleContainer.reset();

                // display the type, which can't be modified.
                palletTypesViewer.getCombo().setEnabled(false);
                palletTypesViewer.setInput(Arrays
                    .asList(palletFoundWithProductBarcode.getContainerType()));
                palletTypesViewer.setSelection(new StructuredSelection(
                    palletFoundWithProductBarcode.getContainerType()));
                appendLog(Messages
                    .getString(
                        "SpecimenAssign.multiple.activitylog.pallet.productBarcode.exists", //$NON-NLS-1$
                        currentMultipleContainer.getProductBarcode(),
                        palletFoundWithProductBarcode.getLabel(),
                        currentMultipleContainer.getSite().getNameShort(),
                        palletFoundWithProductBarcode.getContainerType()
                            .getName()));
                // can't modify the position if exists already
                palletPositionText.setEnabled(false);
                focusPlateToScan();
            }
        } catch (Exception ex) {
            BiobankPlugin
                .openError(
                    Messages
                        .getString("SpecimenAssign.multiple.validation.error.title"), ex); //$NON-NLS-1$
            appendLog(Messages.getString(
                "SpecimenAssign.multiple.activitylog.error", //$NON-NLS-1$
                ex.getMessage()));
            return false;
        }
        return true;
    }

    @Override
    protected void defaultInitialisation() {
        useScannerButton.setSelection(useScanner);
        setUseScanner(useScanner);
    }

    @Override
    protected void setUseScanner(boolean use) {
        useScanner = use;
        showPlateToScanField(use && !singleMode);
        widgetCreator.showWidget(scanButton, use);
        widgetCreator.showWidget(palletproductBarcodeLabel, use);
        widgetCreator.showWidget(palletproductBarcodeText, use);
        widgetCreator.setBinding(PRODUCT_BARCODE_BINDING, use);
        if (fakeScanComposite != null)
            widgetCreator.showWidget(fakeScanComposite, use);
        showScanTubeAloneSwitch(use);
        if (palletTypesViewer != null) {
            palletTypesViewer.setInput(null);
            palletTypesViewer.getCombo().deselectAll();
        }
        if (use) {
            if (isScanTubeAloneMode())
                // want to deactivate it at first in scan mode
                toggleScanTubeAloneMode();
        } else {
            palletproductBarcodeText.setText("");
            currentMultipleContainer.setContainerType(null);
            setScanHasBeenLaunched(true);
            setScanValid(true);
            if (!isScanTubeAloneMode())
                // want to activate tube alone mode if do not use the scanner
                toggleScanTubeAloneMode();

        }
        reset(false);
        super.setUseScanner(use);
        page.layout(true, true);
    }

    /**
     * assign multiple
     */
    private void showOnlyPallet(boolean onlyPallet) {
        widgetCreator.showWidget(freezerLabel, !onlyPallet);
        widgetCreator.showWidget(freezerWidget, !onlyPallet);
        widgetCreator.showWidget(hotelLabel, !onlyPallet);
        widgetCreator.showWidget(hotelWidget, !onlyPallet);
        page.layout(true, true);
    }

    /**
     * assign multiple
     */
    private void showOnlyPallet(final boolean show, boolean async) {
        if (async) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    showOnlyPallet(show);
                }
            });
        } else {
            showOnlyPallet(show);
        }
    }

    /**
     * assign multiple: list of container types
     */
    private void createPalletTypesViewer(Composite parent)
        throws ApplicationException {
        initPalletContainerTypes();
        palletTypesViewer = widgetCreator.createComboViewer(parent, Messages
            .getString("SpecimenAssign.multiple.palletType.label"), //$NON-NLS-1$
            null, null, Messages
                .getString("SpecimenAssign.multiple.palletType.validationMsg"), //$NON-NLS-1$
            true, PALLET_TYPES_BINDING, new ComboSelectionUpdate() {
                @Override
                public void doSelection(Object selectedObject) {
                    currentMultipleContainer
                        .setContainerType((ContainerTypeWrapper) selectedObject);
                    palletTypesViewer.getCombo().setFocus();
                    if (!useScanner)
                        displayPalletPositions();
                }
            });
    }

    /**
     * Multiple assign. Initialise list of possible pallets (8*12)
     */
    private void initPalletContainerTypes() throws ApplicationException {
        palletContainerTypes = ContainerTypeWrapper.getContainerTypesPallet96(
            appService, currentMultipleContainer.getSite());
        if (palletContainerTypes.size() == 0) {
            BiobankPlugin
                .openAsyncError(
                    Messages
                        .getString("SpecimenAssign.multiple.dialog.noPalletFoundError.title"), //$NON-NLS-1$
                    Messages
                        .getString("SpecimenAssign.multiple.dialog.noPalletFoundError.msg" //$NON-NLS-1$
                        ));
        }
    }

    @Override
    protected void enableFields(boolean enable) {
        super.enableFields(enable);
        multipleOptionsFields.setEnabled(enable);
    }

    @Override
    protected boolean fieldsValid() {
        if (singleMode)
            return true;
        IStructuredSelection selection = (IStructuredSelection) palletTypesViewer
            .getSelection();
        return isPlateValid()
            && productBarcodeValidator.validate(
                palletproductBarcodeText.getText()).equals(Status.OK_STATUS)
            && palletLabelValidator.validate(palletPositionText.getText())
                .equals(Status.OK_STATUS) && selection.size() > 0;
    }

    @Override
    protected void saveForm() throws Exception {
        if (singleMode)
            saveSingleSpecimen();
        else
            saveMultipleSpecimens();
        setFinished(false);
    }

    private void saveMultipleSpecimens() throws Exception {
        if (saveEvenIfMissing) {
            if (containerToRemove != null) {
                containerToRemove.delete();
            }
            currentMultipleContainer.persist();
            String productBarcode = currentMultipleContainer
                .getProductBarcode();
            String containerType = currentMultipleContainer.getContainerType()
                .getName();
            String palletLabel = currentMultipleContainer.getLabel();
            String siteName = currentMultipleContainer.getSite().getNameShort();
            if (isNewMultipleContainer)
                appendLog(Messages.getString(
                    "SpecimenAssign.multiple.activitylog.pallet.added", //$NON-NLS-1$
                    productBarcode, containerType, palletLabel, siteName));
            int totalNb = 0;
            StringBuffer sb = new StringBuffer(
                Messages
                    .getString("SpecimenAssign.multiple.activilylog.save.start")); //$NON-NLS-1$
            try {
                Map<RowColPos, PalletCell> cells = getCells();
                for (Entry<RowColPos, PalletCell> entry : cells.entrySet()) {
                    RowColPos rcp = entry.getKey();
                    PalletCell cell = entry.getValue();
                    if (cell != null
                        && (cell.getStatus() == UICellStatus.NEW || cell
                            .getStatus() == UICellStatus.MOVED)) {
                        SpecimenWrapper specimen = cell.getSpecimen();
                        if (specimen != null) {
                            specimen.setPosition(rcp);
                            specimen.setParent(currentMultipleContainer);
                            specimen.persist();
                            String posStr = specimen.getPositionString(true,
                                false);
                            if (posStr == null) {
                                posStr = Messages
                                    .getString("SpecimenAssign.position.none"); //$NON-NLS-1$
                            }
                            computeActivityLogMessage(sb, cell, specimen,
                                posStr);
                            totalNb++;
                        }
                    }
                }
            } catch (Exception ex) {
                setScanHasBeenLaunched(false, true);
                throw ex;
            }
            appendLog(sb.toString());
            appendLog(Messages.getString(
                "SpecimenAssign.multiple.activitylog.save.summary", totalNb, //$NON-NLS-1$
                currentMultipleContainer.getLabel(), currentMultipleContainer
                    .getSite().getNameShort()));
            setFinished(false);
        }
    }

    private void computeActivityLogMessage(StringBuffer sb, PalletCell cell,
        SpecimenWrapper specimen, String posStr) {
        CollectionEventWrapper visit = specimen.getCollectionEvent();
        sb.append(Messages.getString(
            "SpecimenAssign.multiple.activitylog.specimen.assigned", //$NON-NLS-1$
            posStr, currentMultipleContainer.getSite().getNameShort(), cell
                .getValue(), specimen.getSpecimenType().getName(), visit
                .getPatient().getPnumber(), visit.getVisitNumber()));
    }

    private void saveSingleSpecimen() throws Exception {
        singleSpecimen.persist();
    }

    @Override
    protected ProcessData getProcessData() {
        return new AssignProcessData(currentMultipleContainer);
    }

    @Override
    public void reset() throws Exception {
        super.reset();
        parentContainers = null;
        // resultShownValue.setValue(Boolean.FALSE);
        // the 2 following lines are needed. The validator won't update if don't
        // do that (why ?)
        inventoryIdText.setText("**"); //$NON-NLS-1$ 
        inventoryIdText.setText(""); //$NON-NLS-1$
        oldSinglePositionText.setText(""); //$NON-NLS-1$
        oldSinglePositionCheckText.setText(""); //$NON-NLS-1$
        newSinglePositionText.setText(""); //$NON-NLS-1$
        displayOldSingleFields(false);

        showOnlyPallet(true);
        form.layout(true, true);
        if (!singleMode) {
            palletproductBarcodeText.setFocus();
            setCanLaunchScan(false);
        }

        singleSpecimen.reset(); // reset internal values
        setDirty(false);
        if (singleMode)
            inventoryIdText.setFocus();
        else if (useScanner)
            palletproductBarcodeText.setFocus();
        else
            palletPositionText.setFocus();
    }

    @Override
    public void reset(boolean resetAll) {
        super.reset(resetAll);
        String productBarcode = ""; //$NON-NLS-1$
        String label = ""; //$NON-NLS-1$
        ContainerTypeWrapper type = null;

        if (!resetAll) { // keep fields values
            productBarcode = palletproductBarcodeText.getText();
            label = palletPositionText.getText();
            type = currentMultipleContainer.getContainerType();
        } else {
            if (palletTypesViewer != null) {
                palletTypesViewer.setInput(null);
                palletTypesViewer.getCombo().deselectAll();
            }
            removeRescanMode();
            freezerWidget.setSelection(null);
            hotelWidget.setSelection(null);
            palletWidget.setCells(null);
        }
        setScanHasBeenLaunched(isSingleMode() || !useScanner);
        initPalletValues();

        palletproductBarcodeText.setText(productBarcode);
        productBarcodeValidator.validate(productBarcode);
        palletPositionText.setText(label);
        palletLabelValidator.validate(label);
        currentMultipleContainer.setContainerType(type);
        if (resetAll) {
            setDirty(false);
            useNewProductBarcode = false;
        }
    }

    @Override
    protected void setBindings(boolean isSingleMode) {
        setCanLaunchScan(isSingleMode);
        widgetCreator.setBinding(INVENTORY_ID_BINDING, isSingleMode);
        widgetCreator.setBinding(NEW_SINGLE_POSITION_BINDING, isSingleMode);
        widgetCreator.setBinding(PRODUCT_BARCODE_BINDING, !isSingleMode);
        widgetCreator.setBinding(LABEL_BINDING, !isSingleMode);
        widgetCreator.setBinding(PALLET_TYPES_BINDING, !isSingleMode);
        super.setBindings(isSingleMode);
    }

    @Override
    protected void showSingleComposite(boolean single) {
        widgetCreator.showWidget(multipleVisualisation, !single);
        widgetCreator.showWidget(singleVisualisation, single);
        if (single)
            setFirstControl(inventoryIdText);
        else
            setFirstControl(palletproductBarcodeText);
        super.showSingleComposite(single);
    }

    /**
     * Multiple assign
     */
    protected void manageDoubleClick(MouseEvent e) {
        if (isScanTubeAloneMode()) {
            scanTubeAlone(e);
        } else {
            PalletCell cell = (PalletCell) ((ScanPalletWidget) e.widget)
                .getObjectAtCoordinates(e.x, e.y);
            if (cell != null) {
                switch (cell.getStatus()) {
                case ERROR:
                    // do something ?
                    break;
                case MISSING:
                    SessionManager.openViewForm(cell.getExpectedSpecimen());
                    break;
                }
            }
        }
    }

    /**
     * Multiple assign
     */
    @Override
    protected boolean canScanTubeAlone(PalletCell cell) {
        return super.canScanTubeAlone(cell)
            || cell.getStatus() == UICellStatus.MISSING;
    }

    /**
     * Multiple assign
     */
    @Override
    protected void launchScanAndProcessResult() {
        super.launchScanAndProcessResult();
        page.layout(true, true);
        book.reflow(true);
        cancelConfirmWidget.setFocus();
    }

    /**
     * Multiple assign
     */
    @Override
    protected void beforeScanThreadStart() {
        showOnlyPallet(false, false);
        currentMultipleContainer.setSite(SessionManager.getUser()
            .getCurrentWorkingSite());
        currentMultipleContainer
            .setContainerType((ContainerTypeWrapper) ((IStructuredSelection) palletTypesViewer
                .getSelection()).getFirstElement());
        isFakeScanLinkedOnly = fakeScanLinkedOnlyButton != null
            && fakeScanLinkedOnlyButton.getSelection();
    }

    /**
     * Multiple assign
     */
    @Override
    protected void afterScanAndProcess(Integer rowOnly) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                cancelConfirmWidget.setFocus();
                displayPalletPositions();
                palletWidget.setCells(getCells());
                setDirty(true);
                setRescanMode();
                page.layout(true, true);
                form.reflow(true);
            }
        });
    }

    /**
     * Multiple assign
     */
    protected void displayPalletPositions() {
        if (currentMultipleContainer.hasParentContainer()) {
            ContainerWrapper hotelContainer = currentMultipleContainer
                .getParentContainer();
            ContainerWrapper freezerContainer = hotelContainer
                .getParentContainer();

            if (freezerContainer != null) {
                freezerLabel.setText(freezerContainer.getFullInfoLabel());
                freezerWidget.setContainerType(freezerContainer
                    .getContainerType());
                freezerWidget
                    .setSelection(hotelContainer.getPositionAsRowCol());
                freezerWidget.redraw();
            }

            hotelLabel.setText(hotelContainer.getFullInfoLabel());
            hotelWidget.setContainerType(hotelContainer.getContainerType());
            hotelWidget.setSelection(currentMultipleContainer
                .getPositionAsRowCol());
            hotelWidget.redraw();

            palletLabel.setText(currentMultipleContainer.getLabel());

            palletWidget.setContainerType(
                currentMultipleContainer.getContainerType(),
                ScanPalletDisplay.SAMPLE_WIDTH);

            showOnlyPallet(false);
        }
    }

    /**
     * Multiple assign
     */
    @Override
    protected Map<RowColPos, PalletCell> getFakeScanCells() throws Exception {
        if (oldPalletFoundWithProductBarcode != null) {
            Map<RowColPos, PalletCell> palletScanned = new HashMap<RowColPos, PalletCell>();
            for (RowColPos pos : currentMultipleContainer.getSpecimens()
                .keySet()) {
                if (pos.row != 0 && pos.col != 2) {
                    palletScanned.put(pos,
                        new PalletCell(new ScanCell(pos.row, pos.col,
                            currentMultipleContainer.getSpecimens().get(pos)
                                .getInventoryId())));
                }
            }
            return palletScanned;
        } else {
            if (isFakeScanLinkedOnly) {
                return PalletCell.getRandomSpecimensNotAssigned(appService,
                    currentMultipleContainer.getSite().getId());
            }
            return PalletCell.getRandomSpecimensAlreadyAssigned(appService,
                currentMultipleContainer.getSite().getId());
        }
    }

    @Override
    protected void doBeforeSave() throws Exception {
        if (!singleMode) {
            saveEvenIfMissing = true;
            if (currentScanState == UICellStatus.MISSING) {
                boolean save = BiobankPlugin
                    .openConfirm(
                        Messages
                            .getString("SpecimenAssign.multiple.dialog.reallySave.title"), //$NON-NLS-1$
                        Messages
                            .getString("SpecimenAssign.multiple.dialog.saveWithMissing.msg")); //$NON-NLS-1$
                if (!save) {
                    setDirty(true);
                    saveEvenIfMissing = false;
                }
            }
        }
    }

    /**
     * Multiple assign
     */
    @Override
    protected void createFakeOptions(Composite fieldsComposite) {
        fakeScanComposite = toolkit.createComposite(fieldsComposite);
        fakeScanComposite.setLayout(new GridLayout());
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        fakeScanComposite.setLayoutData(gd);
        fakeScanLinkedOnlyButton = toolkit.createButton(fakeScanComposite,
            "Select linked only specimens", SWT.RADIO); //$NON-NLS-1$
        fakeScanLinkedOnlyButton.setSelection(true);
        toolkit.createButton(fakeScanComposite,
            "Select linked and assigned specimens", SWT.RADIO); //$NON-NLS-1$
    }

    @Override
    protected boolean initializeWithSingle() {
        return isSingleMode();
    }

    @Override
    protected Composite getFocusedComposite(boolean single) {
        if (single)
            return inventoryIdText;
        if (useScanner)
            return palletproductBarcodeText;
        return palletPositionText;
    }
}