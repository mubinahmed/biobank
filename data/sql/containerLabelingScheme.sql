LOCK TABLES `CONTAINER_LABELING_SCHEME` WRITE;
INSERT INTO `CONTAINER_LABELING_SCHEME` (ID, NAME, MIN_CHARS, MAX_CHARS, MAX_ROWS, MAX_COLS, MAX_CAPACITY) VALUES
( 1, "SBS Standard",2,3,16,24,384),
( 2, "CBSR 2 char alphabetic",2,2,null,null,576),
( 3, "2 char numeric",2,2,null,null,99),
( 4, "Dewar",2,2,2,2,4),
( 5, "Box81",2,2,9,9,81);
UNLOCK TABLES;
