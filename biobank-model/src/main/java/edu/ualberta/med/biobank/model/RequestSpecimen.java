package edu.ualberta.med.biobank.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import edu.ualberta.med.biobank.i18n.Bundle;
import edu.ualberta.med.biobank.i18n.LString;
import edu.ualberta.med.biobank.i18n.Trnc;
import edu.ualberta.med.biobank.model.type.RequestSpecimenState;

@Audited
@Entity
@Table(name = "REQUEST_SPECIMEN")
public class RequestSpecimen extends AbstractModel {
    private static final long serialVersionUID = 1L;
    private static final Bundle bundle = new CommonBundle();

    @SuppressWarnings("nls")
    public static final Trnc NAME = bundle.trnc(
        "model",
        "Requested Specimen",
        "Requested Specimens");

    @SuppressWarnings("nls")
    public static class PropertyName {
        public static final LString CLAIMED_BY = bundle.trc(
            "model",
            "Claimed By").format();
        public static final LString STATE = bundle.trc(
            "model",
            "State").format();
    }

    private RequestSpecimenState state = RequestSpecimenState.AVAILABLE_STATE;
    private String claimedBy;
    private Specimen specimen;
    private Request request;

    @NotNull(message = "{RequestSpecimen.state.NotNull}")
    @Column(name = "STATE", nullable = false)
    @Type(type = "requestSpecimenState")
    public RequestSpecimenState getState() {
        return this.state;
    }

    public void setState(RequestSpecimenState state) {
        this.state = state;
    }

    @Column(name = "CLAIMED_BY", length = 50)
    public String getClaimedBy() {
        return this.claimedBy;
    }

    public void setClaimedBy(String claimedBy) {
        this.claimedBy = claimedBy;
    }

    @NotNull(message = "{RequestSpecimen.specimen.NotNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SPECIMEN_ID", nullable = false)
    public Specimen getSpecimen() {
        return this.specimen;
    }

    public void setSpecimen(Specimen specimen) {
        this.specimen = specimen;
    }

    @NotNull(message = "{RequestSpecimen.request.NotNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUEST_ID", nullable = false)
    public Request getRequest() {
        return this.request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }
}
