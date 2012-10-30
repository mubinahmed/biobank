package edu.ualberta.med.biobank.model.center;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import edu.ualberta.med.biobank.model.VersionedLongIdModel;
import edu.ualberta.med.biobank.validator.constraint.NotUsed;
import edu.ualberta.med.biobank.validator.constraint.Unique;
import edu.ualberta.med.biobank.validator.group.PreDelete;
import edu.ualberta.med.biobank.validator.group.PrePersist;

/**
 * Represents a {@link Center}'s paper record of work or processing done on one
 * or more {@link Specimen}s.
 * 
 * @author Jonathan Ferland
 */
@Audited
@Entity
@Table(name = "PROCESSING_EVENT")
@NotUsed.List({
    @NotUsed(by = ProcessingEventInputSpecimen.class, property = "processingEvent", groups = PreDelete.class)
})
@Unique(properties = { "center", "worksheet" }, groups = PrePersist.class)
public class ProcessingEvent
    extends VersionedLongIdModel {
    private static final long serialVersionUID = 1L;

    private Center center;
    private String worksheet;
    private Long timeDone;

    @NotNull(message = "{ProcessingEvent.center.NotNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CENTER_ID", nullable = false)
    public Center getCenter() {
        return this.center;
    }

    public void setCenter(Center center) {
        this.center = center;
    }

    @NotEmpty(message = "{ProcessingEvent.worksheet.NotEmpty}")
    @Column(name = "WORKSHEET", length = 100)
    public String getWorksheet() {
        return this.worksheet;
    }

    public void setWorksheet(String worksheet) {
        this.worksheet = worksheet;
    }

    @NotNull(message = "{ProcessingEvent.timeDone.NotNull}")
    @Column(name = "TIME_DONE", nullable = false)
    public Long getTimeDone() {
        return this.timeDone;
    }

    public void setTimeDone(Long timeDone) {
        this.timeDone = timeDone;
    }
}