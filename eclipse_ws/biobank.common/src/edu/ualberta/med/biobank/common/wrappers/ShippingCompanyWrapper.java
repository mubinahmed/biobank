package edu.ualberta.med.biobank.common.wrappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import edu.ualberta.med.biobank.common.BiobankCheckException;
import edu.ualberta.med.biobank.model.Shipment;
import edu.ualberta.med.biobank.model.ShippingCompany;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;

public class ShippingCompanyWrapper extends ModelWrapper<ShippingCompany> {

    public ShippingCompanyWrapper(WritableApplicationService appService) {
        super(appService);
    }

    public ShippingCompanyWrapper(WritableApplicationService appService,
        ShippingCompany sc) {
        super(appService, sc);
    }

    @Override
    protected void deleteChecks() throws BiobankCheckException,
        ApplicationException, WrapperException {
        List<ShipmentWrapper> shipments = getShipmentCollection();
        if (shipments != null && shipments.size() > 0) {
            throw new BiobankCheckException(
                "Cannot delete this shipping company: shipments are still using it");
        }

    }

    @Override
    protected String[] getPropertyChangeNames() {
        return new String[] { "name", "shipmentCollection" };
    }

    @Override
    public Class<ShippingCompany> getWrappedClass() {
        return ShippingCompany.class;
    }

    @Override
    protected void persistChecks() throws BiobankCheckException,
        ApplicationException, WrapperException {
    }

    public String getName() {
        return wrappedObject.getName();
    }

    public void setName(String name) {
        String old = getName();
        wrappedObject.setName(name);
        propertyChangeSupport.firePropertyChange("name", old, name);
    }

    @Override
    public int compareTo(ModelWrapper<ShippingCompany> o) {
        return getName().compareTo(o.getWrappedObject().getName());
    }

    @SuppressWarnings("unchecked")
    public List<ShipmentWrapper> getShipmentCollection(boolean sort) {
        List<ShipmentWrapper> shipmentCollection = (List<ShipmentWrapper>) propertiesMap
            .get("shipmentCollection");
        if (shipmentCollection == null) {
            Collection<Shipment> children = wrappedObject
                .getShipmentCollection();
            if (children != null) {
                shipmentCollection = new ArrayList<ShipmentWrapper>();
                for (Shipment ship : children) {
                    shipmentCollection
                        .add(new ShipmentWrapper(appService, ship));
                }
                propertiesMap.put("shipmentCollection", shipmentCollection);
            }
        }
        if ((shipmentCollection != null) && sort)
            Collections.sort(shipmentCollection);
        return shipmentCollection;
    }

    public List<ShipmentWrapper> getShipmentCollection() {
        return getShipmentCollection(false);
    }

    public void setShipmentCollection(Collection<Shipment> shipments,
        boolean setNull) {
        Collection<Shipment> old = wrappedObject.getShipmentCollection();
        wrappedObject.setShipmentCollection(shipments);
        propertyChangeSupport.firePropertyChange("shipmentCollection", old,
            shipments);
        if (setNull) {
            propertiesMap.put("shipmentCollection", null);
        }
    }

    public void setShipmentCollection(List<ShipmentWrapper> shipments) {
        Collection<Shipment> shipmentsObjects = new HashSet<Shipment>();
        for (ShipmentWrapper s : shipments) {
            shipmentsObjects.add(s.getWrappedObject());
        }
        setShipmentCollection(shipmentsObjects, false);
        propertiesMap.put("shipmentCollection", shipments);
    }

    public static List<ShippingCompanyWrapper> getShippingCompanies(
        WritableApplicationService appService) throws ApplicationException {
        List<ShippingCompany> objects = appService.search(
            ShippingCompany.class, new ShippingCompany());
        List<ShippingCompanyWrapper> wrappers = new ArrayList<ShippingCompanyWrapper>();
        for (ShippingCompany sc : objects) {
            wrappers.add(new ShippingCompanyWrapper(appService, sc));
        }
        return wrappers;
    }
}
