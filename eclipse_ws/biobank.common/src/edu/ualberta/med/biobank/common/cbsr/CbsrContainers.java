package edu.ualberta.med.biobank.common.cbsr;

import edu.ualberta.med.biobank.common.RowColPos;
import edu.ualberta.med.biobank.common.wrappers.ContainerTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;

public class CbsrContainers {

    public static void createContainers(SiteWrapper site) throws Exception {
        createFreezer01(site);
        createFreezer02(site);
        createFreezer03(site);
        createFreezer04(site);
        createFreezer05(site);
        createCabinet01(site);
        createCabinet02(site);
    }

    private static void createFreezer01(SiteWrapper site) throws Exception {
        ContainerWrapper freezer01 = addTopLevelContainer(site, "01",
            CbsrContainerTypes.getContainerType("Freezer 3x10"));

        ContainerWrapper hotel;
        ContainerTypeWrapper hotelType = CbsrContainerTypes
            .getContainerType("Hotel 17");
        ContainerTypeWrapper palletType = CbsrContainerTypes
            .getContainerType("Box 81");

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 10; ++col) {
                hotel = addContainer(site, hotelType, freezer01, row, col);

                for (int j = 0; j < 17; ++j) {
                    addContainer(site, palletType, hotel, j, 0);
                }
            }
        }
    }

    private static void createFreezer02(SiteWrapper site) throws Exception {
        // ContainerWrapper freezer02 = addTopLevelContainer(site, "02",
        // CbsrContainerTypes.getContainerType("Freezer 4x12"));

        addTopLevelContainer(site, "02", CbsrContainerTypes
            .getContainerType("Freezer 4x12"));
    }

    private static void createFreezer03(SiteWrapper site) throws Exception {
        ContainerWrapper hotel;
        ContainerWrapper freezer03 = addTopLevelContainer(site, "03",
            CbsrContainerTypes.getContainerType("Freezer 5x9"));
        ContainerTypeWrapper hotel13Type = CbsrContainerTypes
            .getContainerType("Hotel 13");
        ContainerTypeWrapper hotel19Type = CbsrContainerTypes
            .getContainerType("Hotel 19");
        ContainerTypeWrapper palletType = CbsrContainerTypes
            .getContainerType("Pallet 96");

        ContainerTypeWrapper[] hotelTypes = new ContainerTypeWrapper[] {
            hotel19Type, hotel13Type, hotel13Type, hotel19Type, hotel13Type,
            hotel19Type, hotel13Type, hotel19Type, hotel19Type, hotel13Type,
            hotel19Type, hotel13Type, hotel13Type, hotel13Type, hotel13Type,
            hotel13Type, hotel19Type, hotel13Type, hotel19Type, hotel13Type,
            hotel19Type, hotel19Type, hotel13Type, hotel19Type, hotel19Type,
            hotel19Type, hotel13Type, hotel19Type, hotel13Type, hotel13Type,
            hotel13Type, hotel19Type, hotel13Type, hotel13Type, hotel13Type,
            hotel19Type, hotel19Type, hotel13Type, hotel19Type, hotel19Type,
            hotel13Type, hotel13Type, hotel19Type, hotel19Type, hotel19Type };

        RowColPos pos = new RowColPos();
        int count = 0;
        for (ContainerTypeWrapper hotelType : hotelTypes) {
            pos.row = count % 5;
            pos.col = count / 5;
            hotel = addContainer(site, hotelType, freezer03, pos.row, pos.col);

            for (int j = 0, n = hotelType.getRowCapacity(); j < n; ++j) {
                addContainer(site, palletType, hotel, j, 0);
            }
            ++count;
        }
    }

    private static void createFreezer04(SiteWrapper site) throws Exception {
        ContainerWrapper freezer04 = addTopLevelContainer(site, "04",
            CbsrContainerTypes.getContainerType("Freezer 3x6"));

        ContainerWrapper hotel;
        ContainerTypeWrapper hotelType = CbsrContainerTypes
            .getContainerType("Hotel 10");
        ContainerTypeWrapper palletType = CbsrContainerTypes
            .getContainerType("Box 81");

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 6; ++col) {
                hotel = addContainer(site, hotelType, freezer04, row, col);

                for (int j = 0; j < 10; ++j) {
                    addContainer(site, palletType, hotel, j, 0);
                }
            }
        }
    }

    private static void createFreezer05(SiteWrapper site) throws Exception {
        ContainerWrapper hotel;
        ContainerWrapper freezer05 = addTopLevelContainer(site, "05",
            CbsrContainerTypes.getContainerType("Freezer 6x12"));
        ContainerTypeWrapper h13Type = CbsrContainerTypes
            .getContainerType("Hotel 13");
        ContainerTypeWrapper h19Type = CbsrContainerTypes
            .getContainerType("Hotel 19");
        ContainerTypeWrapper palletType = CbsrContainerTypes
            .getContainerType("Pallet 96");

        ContainerTypeWrapper[] hotelTypes = new ContainerTypeWrapper[] {
            h19Type, h19Type, h19Type, h19Type, h13Type, h13Type, h13Type,
            h13Type, h19Type, h19Type, h19Type, h19Type, h19Type, h19Type,
            h19Type, h19Type, h19Type, h19Type, h19Type, h19Type, h13Type,
            h13Type, h13Type, h13Type };

        RowColPos pos = new RowColPos();
        int count = 0;
        for (ContainerTypeWrapper hotelType : hotelTypes) {
            pos.row = count % 6;
            pos.col = count / 6;
            hotel = addContainer(site, hotelType, freezer05, pos.row, pos.col);

            for (int j = 0, n = hotelType.getRowCapacity(); j < n; ++j) {
                if ((pos.row == 0) && (pos.col == 0)) {
                    addContainer(site, palletType, hotel, j, 0);
                }
            }
            ++count;
        }
    }

    private static void createCabinet01(SiteWrapper site) throws Exception {
        ContainerTypeWrapper ftaBinType = CbsrContainerTypes
            .getContainerType("FTA Bin");
        ContainerTypeWrapper hairBinType = CbsrContainerTypes
            .getContainerType("Hair Bin");
        ContainerTypeWrapper drawerType = CbsrContainerTypes
            .getContainerType("Drawer 36");
        ContainerTypeWrapper cabinetType = CbsrContainerTypes
            .getContainerType("Cabinet 4 drawer");
        ContainerWrapper cabinet = addTopLevelContainer(site, "01", cabinetType);

        ContainerTypeWrapper[] binTypes = new ContainerTypeWrapper[] {
            // drawer AA
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType };

        ContainerWrapper drawer = addContainer(site, drawerType, cabinet, 0, 0);
        int count = 0;

        for (ContainerTypeWrapper binType : binTypes) {
            addContainer(site, binType, drawer, count, 0);
            ++count;
        }

        binTypes = new ContainerTypeWrapper[] {
            // drawer AB
            hairBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, hairBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType };

        drawer = addContainer(site, drawerType, cabinet, 1, 0);
        count = 0;

        for (ContainerTypeWrapper binType : binTypes) {
            addContainer(site, binType, drawer, count, 0);
            ++count;
        }

        // drawer AC
        binTypes = new ContainerTypeWrapper[] {
            // drawer AC
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            hairBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, hairBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType };

        drawer = addContainer(site, drawerType, cabinet, 2, 0);
        count = 0;
        for (ContainerTypeWrapper binType : binTypes) {
            addContainer(site, binType, drawer, count, 0);
            ++count;
        }

        // drawer AC
        binTypes = new ContainerTypeWrapper[] {
            // drawer AC
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, hairBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, hairBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType };

        drawer = addContainer(site, drawerType, cabinet, 3, 0);
        count = 0;
        for (ContainerTypeWrapper binType : binTypes) {
            addContainer(site, binType, drawer, count, 0);
            ++count;
        }
    }

    private static void createCabinet02(SiteWrapper site) throws Exception {
        ContainerTypeWrapper ftaBinType = CbsrContainerTypes
            .getContainerType("FTA Bin");
        ContainerTypeWrapper hairBinType = CbsrContainerTypes
            .getContainerType("Hair Bin");
        ContainerTypeWrapper drawerType = CbsrContainerTypes
            .getContainerType("Drawer 36");
        ContainerTypeWrapper cabinetType = CbsrContainerTypes
            .getContainerType("Cabinet 4 drawer");
        ContainerWrapper cabinet = addTopLevelContainer(site, "02", cabinetType);

        ContainerTypeWrapper[] binTypes = new ContainerTypeWrapper[] {
            // drawer AA
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            hairBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            hairBinType, hairBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, hairBinType, ftaBinType,
            ftaBinType };

        ContainerWrapper drawer = addContainer(site, drawerType, cabinet, 0, 0);
        int count = 0;

        for (ContainerTypeWrapper binType : binTypes) {
            addContainer(site, binType, drawer, count, 0);
            ++count;
        }

        binTypes = new ContainerTypeWrapper[] {
            // drawer AB
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, hairBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, hairBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, hairBinType, hairBinType, ftaBinType, ftaBinType,
            ftaBinType };

        drawer = addContainer(site, drawerType, cabinet, 1, 0);
        count = 0;

        for (ContainerTypeWrapper binType : binTypes) {
            addContainer(site, binType, drawer, count, 0);
            ++count;
        }

        // drawer AC
        binTypes = new ContainerTypeWrapper[] {
            // drawer AC
            hairBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, hairBinType, hairBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, hairBinType, null, ftaBinType, ftaBinType, ftaBinType };

        drawer = addContainer(site, drawerType, cabinet, 2, 0);
        count = 0;
        for (ContainerTypeWrapper binType : binTypes) {
            if (binType != null) {
                addContainer(site, binType, drawer, count, 0);
            }
            ++count;
        }

        // drawer AC
        binTypes = new ContainerTypeWrapper[] {
            // drawer AC
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, ftaBinType, ftaBinType,
            ftaBinType, ftaBinType, ftaBinType, null, null, null, null, null,
            null, null, ftaBinType, ftaBinType, ftaBinType };

        drawer = addContainer(site, drawerType, cabinet, 3, 0);
        count = 0;
        for (ContainerTypeWrapper binType : binTypes) {
            if (binType != null) {
                addContainer(site, binType, drawer, count, 0);
            }
            ++count;
        }
    }

    private static ContainerWrapper addTopLevelContainer(SiteWrapper site,
        String label, ContainerTypeWrapper type) throws Exception {
        ContainerWrapper container = new ContainerWrapper(site.getAppService());
        container.setLabel(label);
        container.setSite(site);
        container.setContainerType(type);
        container.setActivityStatus("Active");
        container.persist();
        container.reload();
        return container;
    }

    private static ContainerWrapper addContainer(SiteWrapper site,
        ContainerTypeWrapper type, ContainerWrapper parent, int row, int col)
        throws Exception {
        ContainerWrapper container = new ContainerWrapper(site.getAppService());
        container.setSite(site);
        container.setContainerType(type);
        container.setActivityStatus("Active");
        container.setPosition(row, col);
        container.setParent(parent);
        container.persist();
        container.reload();
        return container;
    }

}
