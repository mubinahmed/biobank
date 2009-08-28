package edu.ualberta.med.biobank.forms;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.springframework.remoting.RemoteConnectFailureException;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.DatabaseResult;
import edu.ualberta.med.biobank.common.utils.ModelUtils;
import edu.ualberta.med.biobank.common.wrappers.PatientVisitWrapper;
import edu.ualberta.med.biobank.common.wrappers.PatientWrapper;
import edu.ualberta.med.biobank.model.Clinic;
import edu.ualberta.med.biobank.model.PvInfo;
import edu.ualberta.med.biobank.model.PvInfoData;
import edu.ualberta.med.biobank.model.PvSampleSource;
import edu.ualberta.med.biobank.model.Study;
import edu.ualberta.med.biobank.treeview.PatientAdapter;
import edu.ualberta.med.biobank.treeview.PatientVisitAdapter;
import edu.ualberta.med.biobank.widgets.ComboAndQuantity;
import edu.ualberta.med.biobank.widgets.DateTimeWidget;
import edu.ualberta.med.biobank.widgets.PvSampleSourceEntryWidget;
import edu.ualberta.med.biobank.widgets.SelectMultiple;
import edu.ualberta.med.biobank.widgets.listener.BiobankEntryFormWidgetListener;
import edu.ualberta.med.biobank.widgets.listener.MultiSelectEvent;
import gov.nih.nci.system.query.SDKQuery;
import gov.nih.nci.system.query.SDKQueryResult;
import gov.nih.nci.system.query.example.DeleteExampleQuery;
import gov.nih.nci.system.query.example.InsertExampleQuery;
import gov.nih.nci.system.query.example.UpdateExampleQuery;

public class PatientVisitEntryForm extends BiobankEntryForm {
    public static final String ID = "edu.ualberta.med.biobank.forms.PatientVisitEntryForm";

    public static final String MSG_NEW_PATIENT_VISIT_OK = "Creating a new patient visit record.";

    public static final String MSG_PATIENT_VISIT_OK = "Editing an existing patient visit record.";

    public static final String MSG_NO_VISIT_NUMBER = "Visit must have a number";

    private PatientVisitAdapter patientVisitAdapter;

    private PatientVisitWrapper patientVisitWrapper;

    private BiobankEntryFormWidgetListener listener = new BiobankEntryFormWidgetListener() {
        @Override
        public void selectionChanged(MultiSelectEvent event) {
            setDirty(true);
        }
    };

    class CombinedPvInfo {
        PvInfo pvInfo;
        PvInfoData pvInfoData;
        Control control;

        public CombinedPvInfo() {
            pvInfo = null;
            pvInfoData = null;
            control = null;
        }
    }

    private ListOrderedMap combinedPvInfoMap;

    private DateTimeWidget dateDrawn;

    private ComboViewer clinicsComboViewer;

    private PvSampleSourceEntryWidget pvSampleSourceEntryWidget;

    public PatientVisitEntryForm() {
        super();
        combinedPvInfoMap = new ListOrderedMap();
    }

    @Override
    public void init() {
        Assert.isTrue(adapter instanceof PatientVisitAdapter,
            "Invalid editor input: object of type "
                + adapter.getClass().getName());

        patientVisitAdapter = (PatientVisitAdapter) adapter;
        patientVisitWrapper = patientVisitAdapter.getWrapper();
        retrievePatientVisit();
        String tabName;
        if (patientVisitWrapper.isNew()) {
            tabName = "New Patient Visit for patient ";
        } else {
            tabName = "Visit " + patientVisitWrapper.getFormattedDateDrawn();
        }
        setPartName(tabName);
    }

    private void retrievePatientVisit() {
        try {
            patientVisitWrapper.reload();
        } catch (Exception e) {
            SessionManager.getLogger().error(
                "Error while retrieving patient visit "
                    + patientVisitAdapter.getWrapper().getDateDrawn(), e);
        }
    }

    @Override
    protected void createFormContent() throws Exception {
        form.setText("Patient Visit Information");
        form.setMessage(getOkMessage(), IMessageProvider.NONE);
        form.getBody().setLayout(new GridLayout(1, false));
        PatientWrapper patientWrapper = retrievePatient();
        createMainSection(patientWrapper.getStudy());
        createSourcesSection();
        createDatasSection(patientWrapper.getStudy());
        initCancelConfirmWidget(form.getBody());
        if (patientVisitWrapper.isNew()) {
            setDirty(true);
        }
    }

    private PatientWrapper retrievePatient() {
        PatientWrapper patientWrapper = patientVisitAdapter.getParentFromClass(
            PatientAdapter.class).getWrapper();
        try {
            patientWrapper.reload();
        } catch (Exception e) {
            // FIXME
        }
        return patientWrapper;
    }

    private void createMainSection(Study study) throws Exception {
        Composite client = toolkit.createComposite(form.getBody());
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolkit.paintBordersFor(client);

        Label siteLabel = (Label) createWidget(client, Label.class, SWT.NONE,
            "Site");
        FormUtils.setTextValue(siteLabel, patientVisitWrapper
            .getPatientWrapper().getPatient().getStudy().getSite().getName());

        if (patientVisitWrapper.getId() == null) {
            // choose clinic for new visit
            Collection<Clinic> clinics = ModelUtils.getStudyClinicCollection(
                appService, study);
            clinicsComboViewer = createCComboViewerWithNoSelectionValidator(
                client, "Clinic", clinics, "A clinic should be selected");
            if (clinics.size() == 1) {
                clinicsComboViewer.getCCombo().select(0);
            }
            if (patientVisitWrapper.getClinic() != null) {
                for (Clinic clinic : clinics) {
                    if (clinic.getId().equals(
                        patientVisitWrapper.getClinic().getId())) {
                        clinicsComboViewer
                            .setSelection(new StructuredSelection(clinic));
                        break;
                    }
                }
            }
        } else {
            Label clinicLabel = (Label) createWidget(client, Label.class,
                SWT.NONE, "Clinic");
            if (patientVisitWrapper.getClinic() != null) {
                clinicLabel.setText(patientVisitWrapper.getClinic().getName());
            }
        }

        toolkit.createLabel(client, "Date Drawn:", SWT.NONE);
        dateDrawn = new DateTimeWidget(client, SWT.BORDER, patientVisitWrapper
            .getDateDrawn());
        dateDrawn.addSelectionListener(selectionListener);
        dateDrawn.addModifyListener(modifyListener);
        dateDrawn.adaptToToolkit(toolkit, true);
    }

    private void createSourcesSection() {
        Composite client = createSectionWithClient("Sample Storage");

        GridLayout layout = new GridLayout(1, false);
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        pvSampleSourceEntryWidget = new PvSampleSourceEntryWidget(client,
            SWT.NONE, patientVisitWrapper.getPvSampleSourceCollection(),
            toolkit);
        pvSampleSourceEntryWidget.addSelectionChangedListener(listener);
    }

    private void createDatasSection(Study study) {
        if (study.getPvInfoCollection().size() > 0) {
            Composite client = createSectionWithClient("Others informations");

            for (PvInfo pvInfo : study.getPvInfoCollection()) {
                CombinedPvInfo combinedPvInfo = new CombinedPvInfo();
                combinedPvInfo.pvInfo = pvInfo;
                combinedPvInfoMap.put(pvInfo.getId(), combinedPvInfo);
            }

            Collection<PvInfoData> pvDataCollection = patientVisitWrapper
                .getPvInfoDataCollection();
            if (pvDataCollection != null) {
                for (PvInfoData pvInfoData : pvDataCollection) {
                    Integer key = pvInfoData.getPvInfo().getId();
                    CombinedPvInfo combinedPvInfo = (CombinedPvInfo) combinedPvInfoMap
                        .get(key);
                    Assert.isNotNull(combinedPvInfo);
                    combinedPvInfo.pvInfoData = pvInfoData;
                }
            }

            MapIterator it = combinedPvInfoMap.mapIterator();
            while (it.hasNext()) {
                it.next();
                CombinedPvInfo combinedPvInfo = (CombinedPvInfo) it.getValue();
                int typeId = combinedPvInfo.pvInfo.getPvInfoType().getId();
                String possibleValues = combinedPvInfo.pvInfo
                    .getPossibleValues();
                String value = null;

                String[] pvalArr = null;
                if (possibleValues != null) {
                    pvalArr = possibleValues.split(";");
                }

                if (combinedPvInfo.pvInfoData != null) {
                    value = combinedPvInfo.pvInfoData.getValue();
                }

                Label labelWidget = toolkit.createLabel(client,
                    combinedPvInfo.pvInfo.getLabel() + ":", SWT.LEFT);
                labelWidget.setLayoutData(new GridData(
                    GridData.VERTICAL_ALIGN_BEGINNING));

                switch (typeId) {
                case 1: // number
                    combinedPvInfo.control = toolkit.createText(client, value,
                        SWT.LEFT);
                    break;

                case 2: // text
                    combinedPvInfo.control = toolkit.createText(client, value,
                        SWT.LEFT | SWT.MULTI);
                    break;

                case 3: // date_time
                    SimpleDateFormat sdf = new SimpleDateFormat(
                        BioBankPlugin.DATE_TIME_FORMAT);

                    Date date = new Date();
                    if (value != null) {
                        try {
                            date = sdf.parse(value);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    DateTimeWidget w = new DateTimeWidget(client, SWT.NONE,
                        date);
                    w.addSelectionListener(selectionListener);
                    w.addModifyListener(modifyListener);
                    w.adaptToToolkit(toolkit, true);
                    combinedPvInfo.control = w;
                    break;

                case 4: // select_single
                    combinedPvInfo.control = createComboSection(client,
                        pvalArr, value);
                    break;

                case 5: // select_multiple
                    SelectMultiple s = new SelectMultiple(client, SWT.BORDER,
                        pvalArr);
                    s.adaptToToolkit(toolkit, true);
                    if (value != null) {
                        s.setSelections(value.split(";"));
                    }
                    combinedPvInfo.control = s;
                    break;

                case 6: // select_single_and_quantity_1_5_1
                    ComboAndQuantity c = new ComboAndQuantity(client,
                        SWT.BORDER);
                    c.adaptToToolkit(toolkit, true);
                    if (pvalArr != null) {
                        c.addValues(pvalArr);
                    }
                    if (value != null) {
                        String[] values = value.split(" ");
                        Assert.isTrue(values.length == 2);
                        c.setText(values[0], Integer.parseInt(values[1]));
                    }
                    combinedPvInfo.control = c;
                    break;

                default:
                    Assert.isTrue(false, "Invalid pvInfo type: " + typeId);
                }

                if (combinedPvInfo.control != null) {
                    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
                    if (typeId == 2) {
                        gd.heightHint = 40;
                    }
                    combinedPvInfo.control.setLayoutData(gd);
                    controls.put(combinedPvInfo.pvInfo.getLabel(),
                        combinedPvInfo.control);
                }
            }
        }
    }

    private Control createComboSection(Composite client, String[] values,
        String selected) {

        Combo combo = new Combo(client, SWT.READ_ONLY);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        if (values != null) {
            combo.setItems(values);
        }

        combo.addSelectionListener(selectionListener);
        combo.addModifyListener(modifyListener);

        if (selected != null) {
            int count = 0;
            for (String value : values) {
                if (selected.equals(value)) {
                    combo.select(count);
                    break;
                }
                ++count;
            }
        }

        toolkit.adapt(combo, true, true);

        return combo;
    }

    @Override
    protected String getOkMessage() {
        if (patientVisitWrapper.isNew()) {
            return MSG_NEW_PATIENT_VISIT_OK;
        }
        return MSG_PATIENT_VISIT_OK;
    }

    @Override
    protected void saveForm() throws Exception {
        PatientAdapter patientAdapter = (PatientAdapter) patientVisitAdapter
            .getParent();
        patientVisitWrapper.setPatientWrapper(patientAdapter.getWrapper());
        if (clinicsComboViewer != null) {
            IStructuredSelection clinicSelection = (IStructuredSelection) clinicsComboViewer
                .getSelection();
            if (clinicSelection != null && clinicSelection.size() > 0) {
                patientVisitWrapper.setClinic((Clinic) clinicSelection
                    .getFirstElement());
            } else {
                patientVisitWrapper.setClinic(null);
            }
        }
        patientVisitWrapper.setDateDrawn(dateDrawn.getDate());

        savePvSampleSources();

        // FIXME get csm_user_id and set it to the Patient Visit at insert

        DatabaseResult res = patientVisitWrapper.persist();
        if (res != DatabaseResult.OK) {
            BioBankPlugin.openAsyncError("Save Problem", res.getMessage());
            setDirty(true);
        }

        // FIXME samplesources and pv infos datas could be done in the patient
        // visit persists method (if we send the information in parameters)
        savePvInfoData();
        patientAdapter.performExpand();
    }

    private void savePvSampleSources() throws Exception {
        Collection<PvSampleSource> ssCollection = pvSampleSourceEntryWidget
            .getPvSampleSources();
        SDKQuery query;
        SDKQueryResult result;

        removeDeletedPvSampleSources(ssCollection);

        Collection<PvSampleSource> savedSsCollection = new HashSet<PvSampleSource>();
        for (PvSampleSource ss : ssCollection) {
            ss.setPatientVisit(patientVisitWrapper.getWrappedObject());
            if ((ss.getId() == null) || (ss.getId() == 0)) {
                query = new InsertExampleQuery(ss);
            } else {
                query = new UpdateExampleQuery(ss);
            }
            result = appService.executeQuery(query);
            savedSsCollection.add((PvSampleSource) result.getObjectResult());
        }
        patientVisitWrapper.setPvSampleSourceCollection(savedSsCollection);
    }

    private void removeDeletedPvSampleSources(
        Collection<PvSampleSource> ssCollection) {
        // no need to remove if patientVisit is not yet in the database
        if (patientVisitWrapper.getId() == null)
            return;

        List<Integer> selectedPvSampleSourceIds = new ArrayList<Integer>();
        for (PvSampleSource ss : ssCollection) {
            selectedPvSampleSourceIds.add(ss.getId());
        }

        SDKQuery query;
        try {
            for (PvSampleSource ss : patientVisitWrapper
                .getPvSampleSourceCollection()) {
                if (!selectedPvSampleSourceIds.contains(ss.getId())) {
                    query = new DeleteExampleQuery(ss);
                    appService.executeQuery(query);
                }
            }
        } catch (final RemoteConnectFailureException exp) {
            BioBankPlugin.openRemoteConnectErrorMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void savePvInfoData() throws Exception {
        Collection<PvInfoData> pvDataCollection = new HashSet<PvInfoData>();

        MapIterator it = combinedPvInfoMap.mapIterator();
        while (it.hasNext()) {
            it.next();
            CombinedPvInfo combinedPvInfo = (CombinedPvInfo) it.getValue();
            String value = "";

            if (combinedPvInfo.control instanceof Text) {
                value = ((Text) combinedPvInfo.control).getText();
            } else if (combinedPvInfo.control instanceof Combo) {
                if (combinedPvInfo.pvInfo.getPossibleValues() != null) {
                    String[] options = combinedPvInfo.pvInfo
                        .getPossibleValues().split(";");
                    int index = ((Combo) combinedPvInfo.control)
                        .getSelectionIndex();
                    if (index >= 0) {
                        Assert.isTrue(index < options.length,
                            "Invalid combo box selection " + index);
                        value = options[index];
                    }
                }
            } else if (combinedPvInfo.control instanceof DateTimeWidget) {
                value = ((DateTimeWidget) combinedPvInfo.control).getText();
            } else if (combinedPvInfo.control instanceof ComboAndQuantity) {
                value = ((ComboAndQuantity) combinedPvInfo.control).getText();
            } else if (combinedPvInfo.control instanceof SelectMultiple) {
                String[] values = ((SelectMultiple) combinedPvInfo.control)
                    .getSelections();
                value = StringUtils.join(values, ";");
            }

            if ((value == null) || (value.length() == 0))
                continue;

            PvInfoData pvInfoData;

            if (combinedPvInfo.pvInfoData == null) {
                pvInfoData = new PvInfoData();
                pvInfoData.setPvInfo(combinedPvInfo.pvInfo);
                pvInfoData.setPatientVisit(patientVisitWrapper
                    .getWrappedObject());
            } else {
                pvInfoData = combinedPvInfo.pvInfoData;
            }
            pvInfoData.setValue(value);
            pvDataCollection.add(pvInfoData);
        }

        if (pvDataCollection.size() == 0)
            return;

        SDKQuery query;
        SDKQueryResult result;
        Collection<PvInfoData> savedPvDataCollection = new HashSet<PvInfoData>();
        Iterator<PvInfoData> itr = pvDataCollection.iterator();

        while (itr.hasNext()) {
            PvInfoData pvInfoData = itr.next();
            if (pvInfoData.getId() == null) {
                query = new InsertExampleQuery(pvInfoData);
            } else {
                query = new UpdateExampleQuery(pvInfoData);
            }

            result = appService.executeQuery(query);
            savedPvDataCollection.add((PvInfoData) result.getObjectResult());
        }

        patientVisitWrapper.setPvInfoDataCollection(pvDataCollection);
    }

    @Override
    public void cancelForm() {

    }

    @Override
    public String getNextOpenedFormID() {
        return PatientVisitViewForm.ID;
    }
}
