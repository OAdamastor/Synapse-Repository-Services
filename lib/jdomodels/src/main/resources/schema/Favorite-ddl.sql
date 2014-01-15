CREATE TABLE `FAVORITE` (
  `FAVORITE_ID` bigint(20) NOT NULL,
  `PRINCIPAL_ID` bigint(20) NOT NULL,
  `NODE_ID` bigint(20) NOT NULL,
  `CREATED_ON` bigint(20) NOT NULL,
  PRIMARY KEY (FAVORITE_ID),
  UNIQUE KEY `UNIQUE_PRINC_NODE` (`PRINCIPAL_ID`,`NODE_ID`),
  CONSTRAINT `FAVORITE_NODE_ID_FK` FOREIGN KEY (`NODE_ID`) REFERENCES `JDONODE` (`ID`) ON DELETE CASCADE,
  CONSTRAINT `FAVORITE_CREATED_BY_FK` FOREIGN KEY (`PRINCIPAL_ID`) REFERENCES `JDOUSERGROUP` (`ID`)
)