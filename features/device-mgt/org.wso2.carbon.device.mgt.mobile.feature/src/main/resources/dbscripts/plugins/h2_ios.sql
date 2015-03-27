
-- -----------------------------------------------------
-- Table `MBL_DEVICE`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `MBL_DEVICE` (
  `MOBILE_DEVICE_ID` VARCHAR(45) NOT NULL ,
  `PUSH_TOKEN` VARCHAR(45) NULL DEFAULT NULL ,
  `IMEI` VARCHAR(45) NULL DEFAULT NULL ,
  `IMSI` VARCHAR(45) NULL DEFAULT NULL ,
  `OS_VERSION` VARCHAR(45) NULL DEFAULT NULL ,
  `DEVICE_MODEL` VARCHAR(45) NULL DEFAULT NULL ,
  `VENDOR` VARCHAR(45) NULL DEFAULT NULL ,
  `LATITUDE` VARCHAR(45) NULL DEFAULT NULL,
  `LONGITUDE` VARCHAR(45) NULL DEFAULT NULL,
  `CHALLENGE` VARCHAR(45) NULL DEFAULT NULL,
  `TOKEN` VARCHAR(50) NULL DEFAULT NULL,
  `UNLOCK_TOKEN` VARCHAR(2000) NULL DEFAULT NULL,
  `SERIAL` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`MOBILE_DEVICE_ID`) );


-- -----------------------------------------------------
-- Table `MBL_FEATURE`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `MBL_FEATURE` (
  `FEATURE_ID` INT NOT NULL AUTO_INCREMENT ,
  `DEVICE_TYPE` VARCHAR(45) NOT NULL ,
  `CODE` VARCHAR(45) NOT NULL ,
  `NAME` VARCHAR(100) NULL ,
  `DESCRIPTION` VARCHAR(200) NULL ,
  PRIMARY KEY (`FEATURE_ID`) );

-- -----------------------------------------------------
-- Table `MBL_FEATURE_PROPERTY`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `MBL_FEATURE_PROPERTY` (
  `PROPERTY` VARCHAR(45) NOT NULL ,
  `FEATURE_ID` INT NOT NULL ,
  PRIMARY KEY (`PROPERTY`) ,
  CONSTRAINT `fk_MBL_FEATURE_PROPERTY_MBL_FEATURE1`
    FOREIGN KEY (`FEATURE_ID` )
    REFERENCES `MBL_FEATURE` (`FEATURE_ID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

-- -----------------------------------------------------
-- Inserts
-- -----------------------------------------------------
INSERT INTO MBL_FEATURE (DEVICE_TYPE,NAME,CODE, DESCRIPTION)  VALUES ('android','DEVICE_LOCK','503A','Device Lock'),('android','WIPE','504A','Device Wipe'),('android','CLEARPASSCODE','505A','Clear Passcode'),('android','APPLIST','502A','Get All Applications'),('android','LOCATION','501A','Location'),('android','INFO','500A','Device Information'),('android','NOTIFICATION','506A','Message'),('android','WIFI','507A','Setup Wifi'),('android','CAMERA','508A','Camera Control'),('android','MUTE','513A','Mute Device'),('android','INSTALLAPP','509A','Install Application'),('android','UNINSTALLAPP','510A','Uninstall Application'),('android','ENCRYPT','511A','Encrypt Storage'),('android','APN','512A','APN'),('android','WEBCLIP','518A','Create Webclips'),('android','PASSWORDPOLICY','519A','Passcode Policy'),('android','EMAIL','520A','Email Configuration'),('android','GOOGLECALENDAR','521A','Calender Subscription'),('android','VPN','523A','VPN'),('android','LDAP','524A','LDAP'),('android','CHANGEPASSWORD','526A','Set Passcode'),('android','ENTERPRISEWIPE','527A','Enterprise Wipe'),('android','POLICY','500P','Policy Enforcement'),('android','MONITORING','501P','Policy Monitoring '),('android','BLACKLISTAPPS','528B','Blacklist Apps'),('android','REVOKEPOLICY','502P','Revoke Policy');