package edu.ualberta.med.biobank.common.scanprocess;

import edu.ualberta.med.biobank.common.util.NotAProxy;

public enum CellStatus implements NotAProxy {
    NOT_INITIALIZED,
    INITIALIZED,
    FULL,
    FREE_LOCATIONS,
    EMPTY,
    FILLED,
    NEW,
    MOVED,
    MISSING,
    ERROR,
    NO_TYPE,
    TYPE,
    IN_SHIPMENT_EXPECTED,
    IN_SHIPMENT_ADDED,
    DUPLICATE_SCAN,
    IN_SHIPMENT_RECEIVED,
    EXTRA;

    private CellStatus() {
    }

    public CellStatus mergeWith(CellStatus newStatus) {
        switch (this) {
        case EMPTY:
            return newStatus;
        case FILLED:
        case MOVED:
            if (newStatus == MISSING || newStatus == ERROR) {
                return newStatus;
            }
            return this;
        case ERROR:
            return ERROR;
        case MISSING:
            if (newStatus == ERROR) {
                return ERROR;
            }
            return MISSING;
        default:
            break;
        }
        return EMPTY;
    }

}