RENAME TABLE clinic_shipment_patient TO shipment_patient;
RENAME TABLE dispatch_shipment_aliquot TO dispatch_aliquot;

ALTER TABLE abstract_shipment
      CHANGE COLUMN DATE_SHIPPED DEPARTED DATETIME NULL DEFAULT NULL COMMENT '';

ALTER TABLE dispatch_aliquot
      CHANGE COLUMN DISPATCH_SHIPMENT_ID DISPATCH_ID INT(11) NOT NULL COMMENT '',
      DROP INDEX FKB1B76907D8CEA57A,
      DROP INDEX FKB1B76907898584F,
      ADD INDEX FK40A7EAC2898584F (ALIQUOT_ID),
      ADD INDEX FK40A7EAC2DE99CA25 (DISPATCH_ID);

ALTER TABLE patient_visit
      CHANGE COLUMN CLINIC_SHIPMENT_PATIENT_ID SHIPMENT_PATIENT_ID INT(11) NOT NULL COMMENT '',
      DROP INDEX FKA09CAF5183AE7BBB,
      ADD INDEX FKA09CAF51859BF35A (SHIPMENT_PATIENT_ID);

ALTER TABLE shipment_patient
      CHANGE COLUMN CLINIC_SHIPMENT_ID SHIPMENT_ID INT(11) NOT NULL COMMENT '',
      DROP INDEX FKF4B18BB7E5B2B216,
      DROP INDEX FKF4B18BB7B563F38F,
      ADD INDEX FK68484540B1D3625 (SHIPMENT_ID),
      ADD INDEX FK68484540B563F38F (PATIENT_ID);
