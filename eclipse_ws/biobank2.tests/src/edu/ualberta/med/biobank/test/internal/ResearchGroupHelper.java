package edu.ualberta.med.biobank.test.internal;

import java.util.ArrayList;
import java.util.List;

import edu.ualberta.med.biobank.common.wrappers.ActivityStatusWrapper;
import edu.ualberta.med.biobank.common.wrappers.ResearchGroupWrapper;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;

public class ResearchGroupHelper extends CenterHelper {

    private static List<ResearchGroupWrapper> createdResearchGroups = new ArrayList<ResearchGroupWrapper>();

    public static ResearchGroupWrapper newResearchGroup(String name,
        boolean addToCreatedList) throws Exception {
        ResearchGroupWrapper researchGroup = new ResearchGroupWrapper(
            appService);
        researchGroup.setName(name);
        if (name != null) {
            if (name.length() <= 50) {
                researchGroup.setNameShort(name);
            } else {
                researchGroup.setNameShort(name.substring(name.length() - 49));
            }
        }
        researchGroup.setCity("");
        researchGroup.setActivityStatus(ActivityStatusWrapper
            .getActivityStatus(appService,
                ActivityStatusWrapper.ACTIVE_STATUS_STRING));
        if (addToCreatedList) {
            createdResearchGroups.add(researchGroup);
        }
        return researchGroup;
    }

    public static ResearchGroupWrapper addResearchGroup(String name,
        StudyWrapper study, boolean addToCreatedList) throws Exception {
        ResearchGroupWrapper researchGroup = newResearchGroup(name,
            addToCreatedList);
        researchGroup.setStudy(study);
        researchGroup.persist();
        return researchGroup;
    }

    public static ResearchGroupWrapper addResearchGroup(String name,
        boolean addToCreatedList) throws Exception {
        ResearchGroupWrapper researchGroup = newResearchGroup(name,
            addToCreatedList);
        researchGroup.setStudy(StudyHelper.addStudy(name + "Study", true));
        researchGroup.persist();
        return researchGroup;
    }

    public static List<ResearchGroupWrapper> addResearchGroups(String name,
        int count) throws Exception {
        List<ResearchGroupWrapper> researchGroups = new ArrayList<ResearchGroupWrapper>();
        for (int i = 0; i < count; i++) {
            researchGroups.add(addResearchGroup(name + i, true));
        }
        return researchGroups;
    }

    public static void deleteCreatedResearchGroups() throws Exception {
        for (ResearchGroupWrapper researchGroup : createdResearchGroups) {
            deleteCenterDependencies(researchGroup);
        }
        deleteResearchGroups(createdResearchGroups);
        createdResearchGroups.clear();
    }
}
