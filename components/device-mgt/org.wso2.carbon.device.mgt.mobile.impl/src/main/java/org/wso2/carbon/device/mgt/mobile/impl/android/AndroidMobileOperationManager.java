/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.mobile.impl.android;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.FeatureManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.mobile.AbstractMobileOperationManager;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.mobile.dao.MobileFeatureDAO;
import org.wso2.carbon.device.mgt.mobile.dao.MobileFeaturePropertyDAO;
import org.wso2.carbon.device.mgt.mobile.dto.*;
import org.wso2.carbon.device.mgt.mobile.util.MobileDeviceManagementUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AndroidMobileOperationManager extends AbstractMobileOperationManager {

    private static final Log log = LogFactory.getLog(AndroidMobileOperationManager.class);

    @Override
    public boolean addOperation(Operation operation,
                                List<DeviceIdentifier> devices) throws OperationManagementException {
        boolean status = false;
        try {
            MobileDeviceOperationMapping mobileDeviceOperationMapping;
            MobileOperation mobileOperation =
                    MobileDeviceManagementUtil.convertToMobileOperation(operation);
            int operationId =
                    MobileDeviceManagementDAOFactory.getMobileOperationDAO().addMobileOperation(mobileOperation);
            if (operationId > 0) {
                for (MobileOperationProperty operationProperty : mobileOperation.getProperties()) {
                    operationProperty.setOperationId(operationId);
                    status =
                            MobileDeviceManagementDAOFactory.getMobileOperationPropertyDAO().addMobileOperationProperty(
                                    operationProperty);
                }
                for (DeviceIdentifier deviceIdentifier : devices) {
                    mobileDeviceOperationMapping = new MobileDeviceOperationMapping();
                    mobileDeviceOperationMapping.setOperationId(operationId);
                    mobileDeviceOperationMapping.setDeviceId(deviceIdentifier.getId());
                    mobileDeviceOperationMapping.setStatus(MobileDeviceOperationMapping.Status.NEW);
                    status = MobileDeviceManagementDAOFactory.getMobileDeviceOperationDAO()
                            .addMobileDeviceOperationMapping(mobileDeviceOperationMapping);
                }
            }
        } catch (MobileDeviceManagementDAOException e) {
            throw new OperationManagementException("Error while adding an operation " + operation.getCode() +
                    "to Android devices", e);
        }
        return status;
    }

    @Override
    public List<Operation> getOperations(DeviceIdentifier deviceIdentifier)
            throws OperationManagementException {
        List<Operation> operations = new ArrayList<Operation>();
        List<MobileDeviceOperationMapping> mobileDeviceOperationMappings;
        List<MobileOperationProperty> operationProperties;
        MobileOperation mobileOperation;
        try {
            mobileDeviceOperationMappings =
                    MobileDeviceManagementDAOFactory.getMobileDeviceOperationDAO()
                            .getAllMobileDeviceOperationMappingsOfDevice(deviceIdentifier.getId());
            if (mobileDeviceOperationMappings.size() > 0) {
                List<Integer> operationIds =
                        MobileDeviceManagementUtil.getMobileOperationIdsFromMobileDeviceOperations(
                                mobileDeviceOperationMappings);
                for (Integer operationId : operationIds) {
                    mobileOperation =
                            MobileDeviceManagementDAOFactory.getMobileOperationDAO().getMobileOperation(operationId);
                    operationProperties =
                            MobileDeviceManagementDAOFactory.getMobileOperationPropertyDAO()
                                    .getAllMobileOperationPropertiesOfOperation(operationId);
                    mobileOperation.setProperties(operationProperties);
                    operations.add(MobileDeviceManagementUtil.convertMobileOperationToOperation(mobileOperation));
                }
            }
        } catch (MobileDeviceManagementDAOException e) {
            throw new OperationManagementException("Error while fetching the operations for the android device '" +
                    deviceIdentifier.getId() + "'", e);
        }
        return operations;
    }

    @Override
    public List<Operation> getPendingOperations(DeviceIdentifier deviceIdentifier)
            throws OperationManagementException {
        List<Operation> operations = new ArrayList<Operation>();
        List<MobileDeviceOperationMapping> mobileDeviceOperationMappings;
        List<MobileOperationProperty> operationProperties;
        MobileOperation mobileOperation;
        try {
            //Get the list of pending operations for the given device
            mobileDeviceOperationMappings =
                    MobileDeviceManagementDAOFactory.getMobileDeviceOperationDAO()
                            .getAllPendingOperationMappingsOfMobileDevice(deviceIdentifier.getId());
            //Go through each operation mapping for retrieving the data corresponding to each operation
            for (MobileDeviceOperationMapping operation : mobileDeviceOperationMappings) {
                //Get the MobileOperation data
                mobileOperation =
                        MobileDeviceManagementDAOFactory.getMobileOperationDAO().getMobileOperation(
                                operation.getOperationId());
                //Get properties of the operation
                operationProperties =
                        MobileDeviceManagementDAOFactory.getMobileOperationPropertyDAO().
                                getAllMobileOperationPropertiesOfOperation(operation.getOperationId());
                mobileOperation.setProperties(operationProperties);
                operations.add(MobileDeviceManagementUtil.convertMobileOperationToOperation(mobileOperation));
                //Update the MobileDeviceOperationMapping data to the In-Progress state
                operation.setStatus(MobileDeviceOperationMapping.Status.INPROGRESS);
                operation.setSentDate(new Date().getTime());
                MobileDeviceManagementDAOFactory.getMobileDeviceOperationDAO()
                        .updateMobileDeviceOperationMappingToInProgress(operation.getDeviceId(),
                                operation.getOperationId());
            }
        } catch (MobileDeviceManagementDAOException e) {
            throw new OperationManagementException("Error while fetching the operations for the android device '" +
                    deviceIdentifier.getId() + "'", e);
        }
        return operations;
    }

    @Override
    public List<Feature> getFeatures(String deviceType) throws FeatureManagementException {
        MobileFeatureDAO featureDAO = MobileDeviceManagementDAOFactory.getFeatureDAO();
        MobileFeaturePropertyDAO featurePropertyDAO =
                MobileDeviceManagementDAOFactory.getFeaturePropertyDAO();
        List<Feature> features = new ArrayList<Feature>();
        try {
            List<MobileFeature> mobileFeatures =
                    featureDAO.getMobileFeatureByDeviceType(deviceType);
            for (MobileFeature mobileFeature : mobileFeatures) {
                Feature feature = new Feature();
                feature.setId(mobileFeature.getId());
                feature.setDeviceType(mobileFeature.getDeviceType());
                feature.setName(mobileFeature.getName());
                feature.setDescription(mobileFeature.getDescription());
                List<Feature.MetadataEntry> metadataEntries =
                        new ArrayList<Feature.MetadataEntry>();
                List<MobileFeatureProperty> properties =
                        featurePropertyDAO.getFeaturePropertiesOfFeature(mobileFeature.getId());
                for (MobileFeatureProperty property : properties) {
                    Feature.MetadataEntry metaEntry = new Feature.MetadataEntry();
                    metaEntry.setId(property.getFeatureID());
                    metaEntry.setValue(property.getProperty());
                    metadataEntries.add(metaEntry);
                }
                feature.setMetadataEntries(metadataEntries);
                features.add(feature);
            }
        } catch (MobileDeviceManagementDAOException e) {
            throw new FeatureManagementException("Error while fetching the features for the device type '" +
                    deviceType + "'", e);
        }
        return features;
    }

}