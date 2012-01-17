package edu.ualberta.med.biobank.common.permission.patient;

import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.common.permission.Permission;
import edu.ualberta.med.biobank.common.permission.PermissionEnum;
import edu.ualberta.med.biobank.model.Patient;

public class PatientMergePermission implements Permission {
    private static final long serialVersionUID = 1L;
    private Integer patientId1;
    private Integer patientId2;

    /**
     * can be called from an action
     */
    public PatientMergePermission(Integer patientId1, Integer patientId2) {
        this.patientId1 = patientId1;
        this.patientId2 = patientId2;
    }

    @Override
    public boolean isAllowed(ActionContext context) {
        Patient patient1 = context.load(Patient.class,
            patientId1);
        // both patients are supposed to be in the same study for the merge
        return PermissionEnum.PATIENT_MERGE
            .isAllowed(context.getUser(), patient1.getStudy())
            && new PatientUpdatePermission(patientId1).isAllowed(null)
            && new PatientDeletePermission(patientId2).isAllowed(null);
    }
}
