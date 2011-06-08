package edu.ualberta.med.biobank.common.wrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.common.exception.BiobankDeleteException;
import edu.ualberta.med.biobank.common.exception.BiobankException;
import edu.ualberta.med.biobank.common.exception.BiobankQueryResultSizeException;
import edu.ualberta.med.biobank.common.peer.CenterPeer;
import edu.ualberta.med.biobank.common.peer.CollectionEventPeer;
import edu.ualberta.med.biobank.common.peer.PatientPeer;
import edu.ualberta.med.biobank.common.peer.ProcessingEventPeer;
import edu.ualberta.med.biobank.common.peer.SpecimenPeer;
import edu.ualberta.med.biobank.common.security.User;
import edu.ualberta.med.biobank.common.wrappers.base.PatientBaseWrapper;
import edu.ualberta.med.biobank.model.CollectionEvent;
import edu.ualberta.med.biobank.model.Log;
import edu.ualberta.med.biobank.model.Patient;
import edu.ualberta.med.biobank.model.ProcessingEvent;
import edu.ualberta.med.biobank.server.applicationservice.BiobankApplicationService;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

@SuppressWarnings("unused")
public class PatientWrapper extends PatientBaseWrapper {

    public PatientWrapper(WritableApplicationService appService, Patient patient) {
        super(appService, patient);
    }

    public PatientWrapper(WritableApplicationService appService) {
        super(appService);
    }

    @Override
    protected Patient getNewObject() throws Exception {
        Patient newObject = super.getNewObject();

        Calendar createdAt = Calendar.getInstance();
        createdAt.setTime(new Date());
        createdAt.set(Calendar.SECOND, 0);

        newObject.setCreatedAt(createdAt.getTime());
        return newObject;
    }

    @Override
    protected void persistChecks() throws BiobankException,
        ApplicationException {
        checkNoDuplicates(Patient.class, PatientPeer.PNUMBER.getName(),
            getPnumber(), "A patient with PNumber");
    }

    private static final String PATIENT_QRY = "from " + Patient.class.getName()
        + " where " + PatientPeer.PNUMBER.getName() + "=?";

    /**
     * Search a patient in the site with the given number
     */
    public static PatientWrapper getPatient(
        WritableApplicationService appService, String patientNumber)
        throws ApplicationException {
        HQLCriteria criteria = new HQLCriteria(PATIENT_QRY,
            Arrays.asList(new Object[] { patientNumber }));
        List<Patient> patients = appService.query(criteria);
        if (patients.size() == 1) {
            return new PatientWrapper(appService, patients.get(0));
        }
        return null;
    }

    /**
     * Search a patient in the site with the given number. Will return the
     * patient only if the current user has read access on a site that works
     * with this patient study
     * 
     * @throws Exception
     */
    public static PatientWrapper getPatient(
        WritableApplicationService appService, String patientNumber, User user)
        throws Exception {
        PatientWrapper patient = getPatient(appService, patientNumber);
        if (patient != null) {
            StudyWrapper study = patient.getStudy();
            List<CenterWrapper<?>> centers = new ArrayList<CenterWrapper<?>>(
                study.getSiteCollection(false));
            centers.addAll(study.getClinicCollection());
            if (Collections.disjoint(centers,
                user.getWorkingCenters(appService))) {
                throw new ApplicationException(
                    "Patient "
                        + patientNumber
                        + " exists but you don't have access to it."
                        + " Check studies linked to the sites and clinics you can access.");
            }
        }
        return patient;
    }

    @Override
    protected void deleteDependencies() throws Exception {
        List<CollectionEventWrapper> cevents = getCollectionEventCollection(false);
        for (CollectionEventWrapper cevent : cevents) {
            cevent.delete();
        }
    }

    @Override
    protected void deleteChecks() throws BiobankException, ApplicationException {
        checkNoMoreCollectionEvents();
        if (getAllSpecimensCount(false) > 0)
            throw new BiobankDeleteException("Unable to delete patient "
                + getPnumber()
                + " because patient has specimens stored in database.");
    }

    private void checkNoMoreCollectionEvents() throws BiobankDeleteException {
        List<CollectionEventWrapper> pvs = getCollectionEventCollection(false);
        if (pvs != null && !pvs.isEmpty()) {
            throw new BiobankDeleteException(
                "Collection events are still linked to this patient."
                    + " Delete them before attempting to remove the patient.");
        }
    }

    private static final String ALL_SPECIMEN_COUNT_QRY = "select count(spcs) from "
        + CollectionEvent.class.getName()
        + " as cevent join cevent."
        + CollectionEventPeer.ALL_SPECIMEN_COLLECTION.getName()
        + " as spcs where cevent."
        + Property.concatNames(CollectionEventPeer.PATIENT, PatientPeer.ID)
        + "=?";

    public long getAllSpecimensCount(boolean fast) throws ApplicationException,
        BiobankException {
        if (fast) {
            HQLCriteria criteria = new HQLCriteria(ALL_SPECIMEN_COUNT_QRY,
                Arrays.asList(new Object[] { getId() }));
            return getCountResult(appService, criteria);
        }
        long total = 0;
        for (CollectionEventWrapper cevent : getCollectionEventCollection(false))
            total += cevent.getAllSpecimensCount(false);
        return total;
    }

    private static final String SOURCE_SPECIMEN_COUNT_QRY = "select count(spcs) from "
        + CollectionEvent.class.getName()
        + " as cevent join cevent."
        + CollectionEventPeer.ORIGINAL_SPECIMEN_COLLECTION.getName()
        + " as spcs where cevent."
        + Property.concatNames(CollectionEventPeer.PATIENT, PatientPeer.ID)
        + "=?";

    public long getSourceSpecimenCount(boolean fast)
        throws ApplicationException, BiobankException {
        if (fast) {
            HQLCriteria criteria = new HQLCriteria(SOURCE_SPECIMEN_COUNT_QRY,
                Arrays.asList(new Object[] { getId() }));
            return getCountResult(appService, criteria);
        }
        long total = 0;
        for (CollectionEventWrapper cevent : getCollectionEventCollection(false))
            total += cevent.getSourceSpecimensCount(false);
        return total;
    }

    private static final String ALIQUOTED_SPECIMEN_COUNT_QRY = "select count(spcs) from "
        + CollectionEvent.class.getName()
        + " as cevent join cevent."
        + CollectionEventPeer.ALL_SPECIMEN_COLLECTION.getName()
        + " as spcs where cevent."
        + Property.concatNames(CollectionEventPeer.PATIENT, PatientPeer.ID)
        + "=? and spcs."
        + SpecimenPeer.PARENT_SPECIMEN.getName()
        + " is not null";

    public long getAliquotedSpecimenCount(boolean fast)
        throws ApplicationException, BiobankException {
        if (fast) {
            HQLCriteria criteria = new HQLCriteria(
                ALIQUOTED_SPECIMEN_COUNT_QRY,
                Arrays.asList(new Object[] { getId() }));
            return getCountResult(appService, criteria);
        }
        long total = 0;
        for (CollectionEventWrapper cevent : getCollectionEventCollection(false))
            total += cevent.getAliquotedSpecimensCount(false);
        return total;
    }

    @Override
    public int compareTo(ModelWrapper<Patient> wrapper) {
        if (wrapper instanceof PatientWrapper) {
            String number1 = getPnumber();
            String number2 = wrapper.wrappedObject.getPnumber();
            return number1.compareTo(number2);
        }
        return 0;
    }

    @Override
    public String toString() {
        return getPnumber();
    }

    private static final String LAST_7_DAYS_PROCESSING_EVENTS_FOR_CENTER_QRY = "select distinct(pEvent) from "
        + Patient.class.getName()
        + " as patient join patient."
        + PatientPeer.COLLECTION_EVENT_COLLECTION.getName()
        + " as ces join ces."
        + CollectionEventPeer.ALL_SPECIMEN_COLLECTION.getName()
        + " as specimens join specimens."
        + SpecimenPeer.PROCESSING_EVENT.getName()
        + " as pEvent where patient."
        + PatientPeer.ID.getName()
        + "=? and pEvent."
        + Property.concatNames(ProcessingEventPeer.CENTER, CenterPeer.ID)
        + "=? and pEvent."
        + ProcessingEventPeer.CREATED_AT.getName()
        + ">? and pEvent." + ProcessingEventPeer.CREATED_AT.getName() + "<?";

    // used in scan link and cabinet link
    public List<ProcessingEventWrapper> getLast7DaysProcessingEvents(
        CenterWrapper<?> center) throws ApplicationException {
        Calendar cal = Calendar.getInstance();
        // today at midnight
        cal.add(Calendar.DATE, 1);
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date endDate = cal.getTime();
        // 7 days ago, at midnight
        cal.add(Calendar.DATE, -8);
        Date startDate = cal.getTime();
        HQLCriteria criteria = new HQLCriteria(
            LAST_7_DAYS_PROCESSING_EVENTS_FOR_CENTER_QRY,
            Arrays.asList(new Object[] { getId(), center.getId(), startDate,
                endDate }));
        List<ProcessingEvent> res = appService.query(criteria);
        return ModelWrapper.wrapModelCollection(appService, res,
            ProcessingEventWrapper.class);
    }

    @Override
    protected Log getLogMessage(String action, String site, String details) {
        Log log = new Log();
        log.setAction(action);
        log.setCenter(site);
        log.setPatientNumber(getPnumber());
        log.setDetails(details);
        log.setType("Patient");
        return log;
    }

    /**
     * merge patient2 into this patient
     */
    public void merge(PatientWrapper patient2) throws Exception {
        reload();
        patient2.reload();
        if (getStudy().equals(patient2.getStudy())) {
            List<CollectionEventWrapper> cevents = patient2
                .getCollectionEventCollection(false);

            if (!cevents.isEmpty()) {
                patient2.removeFromCollectionEventCollection(cevents);
                Set<CollectionEventWrapper> toAdd = new HashSet<CollectionEventWrapper>();
                boolean merged = false;
                for (CollectionEventWrapper p2event : cevents) {
                    for (CollectionEventWrapper p1event : getCollectionEventCollection(false))
                        if (p1event.getVisitNumber().equals(
                            p2event.getVisitNumber())) {
                            // merge collection event
                            p1event.merge(p2event);
                            merged = true;
                        }
                    if (!merged)
                        toAdd.add(p2event);
                    merged = false;
                }

                for (CollectionEventWrapper addMe : toAdd) {
                    addMe.setPatient(this);
                    addMe.persist();
                }

                persist();
                patient2.delete();

                ((BiobankApplicationService) appService).logActivity("merge",
                    null, patient2.getPnumber(), null, null,
                    patient2.getPnumber() + " --> " + getPnumber(), "Patient");
                ((BiobankApplicationService) appService).logActivity("merge",
                    null, getPnumber(), null, null, getPnumber() + " <-- "
                        + patient2.getPnumber(), "Patient");
            }
        } else {
            throw new BiobankCheckException(
                "Cannot merge patients from different studies.");
        }
    }

    public List<CollectionEventWrapper> getCollectionEventCollection(
        boolean sort, final boolean ascending) {
        List<CollectionEventWrapper> cEvents = getCollectionEventCollection(false);
        if (sort) {
            Collections.sort(cEvents, new Comparator<CollectionEventWrapper>() {
                @Override
                public int compare(CollectionEventWrapper ce1,
                    CollectionEventWrapper ce2) {
                    if (ascending) {
                        return ce1.compareTo(ce2);
                    }
                    return ce2.compareTo(ce1);
                }
            });
        }
        return cEvents;
    }

    public List<ProcessingEventWrapper> getProcessingEventCollection(
        boolean originalOnly) {
        List<CollectionEventWrapper> ces = getCollectionEventCollection(false);
        Set<ProcessingEventWrapper> pes = new HashSet<ProcessingEventWrapper>();
        for (CollectionEventWrapper ce : ces)
            if (originalOnly)
                addProcessingEvents(pes,
                    ce.getOriginalSpecimenCollection(false));
            else
                addProcessingEvents(pes, ce.getAllSpecimenCollection(false));
        return new ArrayList<ProcessingEventWrapper>(pes);
    }

    private void addProcessingEvents(Set<ProcessingEventWrapper> pes,
        List<SpecimenWrapper> specimens) {
        for (SpecimenWrapper spec : specimens) {
            if (spec.getProcessingEvent() != null)
                pes.add(spec.getProcessingEvent());
        }
    }

    private static final String CEVENT_COUNT_QRY = "select count(cevent) from "
        + CollectionEvent.class.getName() + " as cevent where cevent."
        + Property.concatNames(CollectionEventPeer.PATIENT, PatientPeer.ID)
        + "=?";

    public Long getCollectionEventCount(boolean fast)
        throws BiobankQueryResultSizeException, ApplicationException {
        if (fast) {
            HQLCriteria criteria = new HQLCriteria(CEVENT_COUNT_QRY,
                Arrays.asList(new Object[] { getId() }));
            return getCountResult(appService, criteria);
        }
        return (long) getCollectionEventCollection(false).size();
    }

}