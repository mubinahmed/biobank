package edu.ualberta.med.biobank.forms;

import java.util.Map;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.springframework.remoting.RemoteConnectFailureException;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.common.RowColPos;
import edu.ualberta.med.biobank.model.PalletCell;
import edu.ualberta.med.biobank.preferences.PreferenceConstants;
import edu.ualberta.med.biobank.validators.ScannerBarcodeValidator;
import edu.ualberta.med.biobank.widgets.CancelConfirmWidget;
import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
import edu.ualberta.med.scannerconfig.scanlib.ScanCell;

public abstract class AbstractPalletAliquotAdminForm extends
    AbstractAliquotAdminForm {

    private Text plateToScanText;
    private Button scanButton;
    private String scanButtonTitle;

    private CancelConfirmWidget cancelConfirmWidget;

    private static IObservableValue plateToScanValue = new WritableValue("", //$NON-NLS-1$
        String.class);
    private IObservableValue canLaunchScanValue = new WritableValue(
        Boolean.TRUE, Boolean.class);
    private IObservableValue scanHasBeenLaunchedValue = new WritableValue(
        Boolean.FALSE, Boolean.class);
    private IObservableValue scanValidValue = new WritableValue(Boolean.TRUE,
        Boolean.class);

    private String currentPlateToScan;

    private boolean rescanMode = false;

    protected Map<RowColPos, PalletCell> cells;

    // the pallet container type name contains this text
    protected String palletNameContains = ""; //$NON-NLS-1$

    private Button scanChoiceSimple;
    private boolean isScanChoiceSimple;

    @Override
    protected void init() {
        super.init();
        IPreferenceStore store = BioBankPlugin.getDefault()
            .getPreferenceStore();
        palletNameContains = store
            .getString(PreferenceConstants.PALLET_SCAN_CONTAINER_NAME_CONTAINS);
    }

    @Override
    public boolean onClose() {
        if (finished || BioBankPlugin.getPlatesEnabledCount() != 1) {
            plateToScanValue.setValue("");
        }
        return super.onClose();
    }

    protected void setRescanMode() {
        scanButton.setText("Retry scan");
        rescanMode = true;
        disableFields();
    }

    protected abstract void disableFields();

    protected boolean canLaunchScan() {
        return scanButton.isEnabled();
    }

    protected void addScanBindings() {
        addBooleanBinding(new WritableValue(Boolean.FALSE, Boolean.class),
            canLaunchScanValue, Messages
                .getString("linkAssign.canLaunchScanValidationMsg")); //$NON-NLS-1$
        addBooleanBinding(new WritableValue(Boolean.FALSE, Boolean.class),
            scanHasBeenLaunchedValue, Messages
                .getString("linkAssign.scanHasBeenLaunchedValidationMsg")); //$NON-NLS-1$
        addBooleanBinding(new WritableValue(Boolean.TRUE, Boolean.class),
            scanValidValue, Messages
                .getString("linkAssign.scanValidValidationMsg")); //$NON-NLS-1$
    }

    protected void createScanButton(Composite parent) {
        scanButtonTitle = Messages.getString("linkAssign.scanButton.text");
        if (BioBankPlugin.isRealScanEnabled()) {
            toolkit.createLabel(parent, "Decode Type:");

            Composite composite = toolkit.createComposite(parent);
            composite.setLayout(new GridLayout(2, false));
            scanChoiceSimple = toolkit.createButton(composite, "Single Scan",
                SWT.RADIO);
            scanChoiceSimple.setSelection(true);
            toolkit.createButton(composite, "Multiple Scan", SWT.RADIO);
        } else {
            createFakeOptions(parent);
            scanButtonTitle = "Fake scan"; //$NON-NLS-1$
        }
        scanButton = toolkit.createButton(parent, scanButtonTitle, SWT.PUSH);
        GridData gd = new GridData();
        gd.horizontalSpan = 3;
        gd.widthHint = 100;
        scanButton.setLayoutData(gd);
        scanButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                internalScanAndProcessResult();
            }
        });
        addScanBindings();
    }

    protected void createPlateToScanField(Composite fieldsComposite) {
        plateToScanText = (Text) createBoundWidgetWithLabel(fieldsComposite,
            Text.class, SWT.NONE, Messages
                .getString("linkAssign.plateToScan.label"), //$NON-NLS-1$
            new String[0], plateToScanValue, new ScannerBarcodeValidator(
                Messages.getString("linkAssign.plateToScan.validationMsg"))); //$NON-NLS-1$
        plateToScanText.addListener(SWT.DefaultSelection, new Listener() {
            public void handleEvent(Event e) {
                if (scanButton.isEnabled()) {
                    internalScanAndProcessResult();
                }
            }
        });
        GridData gd = (GridData) plateToScanText.getLayoutData();
        gd.horizontalAlignment = SWT.FILL;
        if (((GridLayout) fieldsComposite.getLayout()).numColumns == 3) {
            gd.horizontalSpan = 2;
        }
        plateToScanText.setLayoutData(gd);
    }

    protected void createFakeOptions(
        @SuppressWarnings("unused") Composite fieldsComposite) {

    }

    protected void createCancelConfirmWidget() {
        cancelConfirmWidget = new CancelConfirmWidget(form.getBody(), this,
            true);
    }

    protected void internalScanAndProcessResult() {
        saveUINeededInformation();
        IRunnableWithProgress op = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) {
                monitor.beginTask("Scan and process...",
                    IProgressMonitor.UNKNOWN);
                try {
                    if (isRescanMode()) {
                        appendLog("--- Rescan ---");
                    } else {
                        appendLog("--- New Scan session ---");
                    }
                    scanAndProcessResult(monitor);
                } catch (RemoteConnectFailureException exp) {
                    BioBankPlugin.openRemoteConnectErrorMessage();
                    setScanValid(false);
                } catch (Exception e) {
                    BioBankPlugin.openAsyncError(Messages
                        .getString("linkAssign.dialog.scanError.title"), //$NON-NLS-1$
                        e);
                    setScanValid(false);
                    String msg = e.getMessage();
                    if ((msg == null || msg.isEmpty()) && e.getCause() != null) {
                        msg = e.getCause().getMessage();
                    }
                    appendLog("ERROR: " + msg); //$NON-NLS-1$
                }
                monitor.done();
            }
        };
        try {
            new ProgressMonitorDialog(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell()).run(true, false, op);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void saveUINeededInformation() {
        currentPlateToScan = plateToScanValue.getValue().toString();
        if (scanChoiceSimple != null) {
            isScanChoiceSimple = scanChoiceSimple.getSelection();
        }
    }

    protected abstract void scanAndProcessResult(IProgressMonitor monitor)
        throws Exception;

    protected void launchScan(IProgressMonitor monitor) throws Exception {
        monitor.subTask("Launching scan");
        setScanNotLauched(true);
        Map<RowColPos, PalletCell> oldCells = cells;
        appendLogNLS("linkAssign.activitylog.scanning", //$NON-NLS-1$
            currentPlateToScan);
        if (BioBankPlugin.isRealScanEnabled()) {
            int plateNum = BioBankPlugin.getDefault().getPlateNumber(
                currentPlateToScan);
            ScanCell[][] scanCells = null;
            if (isScanChoiceSimple) {
                scanCells = ScannerConfigPlugin.scan(plateNum);
            } else {
                scanCells = ScannerConfigPlugin.scanMultipleDpi(plateNum);
            }
            cells = PalletCell.convertArray(scanCells);
        } else {
            launchFakeScan();
        }
        if (cells != null) {
            if (isRescanMode() && oldCells != null) {
                // rescan: merge previous scan with new in case the scanner
                // wasn't
                // able to scan well
                for (RowColPos rcp : oldCells.keySet()) {
                    PalletCell oldScannedCell = oldCells.get(rcp);
                    PalletCell newScannedCell = cells.get(rcp);
                    if (PalletCell.hasValue(oldScannedCell)
                        && PalletCell.hasValue(newScannedCell)
                        && !oldScannedCell.getValue().equals(
                            newScannedCell.getValue())) {
                        cells = oldCells;
                        throw new Exception(
                            "Scan Aborted: previously scanned aliquot has been replaced. "
                                + "If this is not a re-scan, reset and start again.");
                    }
                    if (PalletCell.hasValue(oldScannedCell)) {
                        cells.put(rcp, oldScannedCell);
                    }
                }
            }
            setScanHasBeenLauched(true);
            appendLogNLS("linkAssign.activitylog.scanRes.total", //$NON-NLS-1$
                cells.keySet().size());
        } else {
            setScanNotLauched(true);
        }
    }

    @SuppressWarnings("unused")
    protected void launchFakeScan() throws Exception {

    }

    @Override
    protected void handleStatusChanged(IStatus status) {
        if (status.getSeverity() == IStatus.OK) {
            form.setMessage(getOkMessage(), IMessageProvider.NONE);
            cancelConfirmWidget.setConfirmEnabled(true);
            setConfirmEnabled(true);
            enableScan(true);
        } else {
            form.setMessage(status.getMessage(), IMessageProvider.ERROR);
            cancelConfirmWidget.setConfirmEnabled(false);
            setConfirmEnabled(false);
            enableScan(isPlateValid());
        }
    }

    protected void setScanNotLauched() {
        scanHasBeenLaunchedValue.setValue(false);
    }

    protected void setScanNotLauched(boolean async) {
        if (async)
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    setScanNotLauched();
                }
            });
        else
            setScanNotLauched();
    }

    protected void setScanValid(final boolean valid) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                scanValidValue.setValue(valid);
            }
        });
    }

    protected void setScanHasBeenLauched() {
        scanHasBeenLaunchedValue.setValue(true);
    }

    protected void setScanHasBeenLauched(boolean async) {
        if (async)
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    setScanHasBeenLauched();
                }
            });
        else
            setScanHasBeenLauched();
    }

    protected boolean isRescanMode() {
        return rescanMode;
    }

    protected void removeRescanMode() {
        scanButton.setText(scanButtonTitle);
        rescanMode = false;
    }

    private void enableScan(boolean enabled) {
        scanButton.setEnabled(enabled);
    }

    protected boolean isPlateValid() {
        return BioBankPlugin.getDefault().isValidPlateBarcode(
            plateToScanText.getText());
    }

    protected void resetPlateToScan() {
        plateToScanText.setText(""); //$NON-NLS-1$
        plateToScanValue.setValue(""); //$NON-NLS-1$
    }

    protected void focusOnCancelConfirmText() {
        cancelConfirmWidget.setFocus();
    }

    protected CancelConfirmWidget getCancelConfirmWidget() {
        return cancelConfirmWidget;
    }

    protected void setCanLaunchScan(boolean canLauch) {
        canLaunchScanValue.setValue(canLauch);
    }
}
