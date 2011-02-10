package edu.ualberta.med.biobank.common.wrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.common.exception.BiobankException;
import edu.ualberta.med.biobank.common.exception.BiobankQueryResultSizeException;
import edu.ualberta.med.biobank.common.peer.SampleTypePeer;
import edu.ualberta.med.biobank.common.wrappers.base.SampleTypeBaseWrapper;
import edu.ualberta.med.biobank.model.Aliquot;
import edu.ualberta.med.biobank.model.SampleType;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public class SampleTypeWrapper extends SampleTypeBaseWrapper {

    public SampleTypeWrapper(WritableApplicationService appService,
        SampleType wrappedObject) {
        super(appService, wrappedObject);
    }

    public SampleTypeWrapper(WritableApplicationService appService) {
        super(appService);
    }

    /**
     * get all sample types in a site for containers which type name contains
     * "typeNameContains" (go recursively inside found containers)
     */
    public static List<SampleTypeWrapper> getSampleTypeForContainerTypes(
        WritableApplicationService appService, SiteWrapper siteWrapper,
        String typeNameContains) throws ApplicationException {
        List<ContainerTypeWrapper> containerTypes = ContainerTypeWrapper
            .getContainerTypesInSite(appService, siteWrapper, typeNameContains,
                false);
        Set<SampleTypeWrapper> sampleTypes = new HashSet<SampleTypeWrapper>();
        for (ContainerTypeWrapper containerType : containerTypes) {
            sampleTypes.addAll(containerType.getSampleTypesRecursively());
        }
        return new ArrayList<SampleTypeWrapper>(sampleTypes);
    }

    /**
     * get all sample types in a site for pallet containers (8*12 size) (go
     * recursively inside found containers)
     */
    public static List<SampleTypeWrapper> getSampleTypeForPallet96(
        WritableApplicationService appService, SiteWrapper siteWrapper)
        throws ApplicationException {
        List<ContainerTypeWrapper> containerTypes = ContainerTypeWrapper
            .getContainerTypesPallet96(appService, siteWrapper);
        Set<SampleTypeWrapper> sampleTypes = new HashSet<SampleTypeWrapper>();
        for (ContainerTypeWrapper containerType : containerTypes) {
            sampleTypes.addAll(containerType.getSampleTypesRecursively());
        }
        return new ArrayList<SampleTypeWrapper>(sampleTypes);
    }

    @Override
    public boolean checkIntegrity() {
        return true;
    }

    @Override
    protected void deleteChecks() throws BiobankException, ApplicationException {
        if (isUsedBySamples()) {
            throw new BiobankCheckException("Unable to delete sample type "
                + getName() + ". Aliquots of this type exists in storage."
                + " Remove all instances before deleting this type.");
        }
    }

    public static List<SampleTypeWrapper> getAllSampleTypes(
        WritableApplicationService appService, boolean sort)
        throws ApplicationException {
        HQLCriteria c = new HQLCriteria("from " + SampleType.class.getName());

        List<SampleType> sampleTypes = appService.query(c);
        List<SampleTypeWrapper> list = new ArrayList<SampleTypeWrapper>();
        for (SampleType type : sampleTypes) {
            list.add(new SampleTypeWrapper(appService, type));
        }
        if (sort)
            Collections.sort(list);
        return list;
    }

    @Override
    protected void persistChecks() throws BiobankException,
        ApplicationException {
        checkNameAndShortNameUnique();
    }

    /**
     * This method should only be called to save the new sample type list.
     */
    public static void persistSampleTypes(
        List<SampleTypeWrapper> addedOrModifiedTypes,
        List<SampleTypeWrapper> typesToDelete) throws BiobankCheckException,
        Exception {
        if (addedOrModifiedTypes != null) {
            for (SampleTypeWrapper ss : addedOrModifiedTypes) {
                ss.persist();
            }
        }
        if (typesToDelete != null) {
            for (SampleTypeWrapper ss : typesToDelete) {
                ss.delete();
            }
        }
    }

    @Override
    public int compareTo(ModelWrapper<SampleType> wrapper) {
        if (wrapper instanceof SampleTypeWrapper) {
            String name1 = wrappedObject.getName();
            String name2 = wrapper.wrappedObject.getName();
            if (name1 != null && name2 != null) {
                return name1.compareTo(name2);
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return getName();
    }

    public boolean isUsedBySamples() throws ApplicationException,
        BiobankException {
        String queryString = "select count(s) from " + Aliquot.class.getName()
            + " as s where s.sampleType=?)";
        HQLCriteria c = new HQLCriteria(queryString,
            Arrays.asList(new Object[] { wrappedObject }));
        List<Long> results = appService.query(c);
        if (results.size() != 1) {
            throw new BiobankQueryResultSizeException();
        }
        return results.get(0) > 0;
    }

    @Override
    public void reload() throws Exception {
        super.reload();
    }

    public void checkNameAndShortNameUnique() throws ApplicationException,
        BiobankException {
        checkNoDuplicates(SampleType.class, SampleTypePeer.NAME.getName(),
            getName(), "A sample type with name");
        checkNoDuplicates(SampleType.class,
            SampleTypePeer.NAME_SHORT.getName(), getNameShort(),
            "A sample type with name short");
    }

}
