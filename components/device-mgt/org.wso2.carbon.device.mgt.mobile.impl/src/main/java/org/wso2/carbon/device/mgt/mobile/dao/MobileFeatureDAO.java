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

package org.wso2.carbon.device.mgt.mobile.dao;

import org.wso2.carbon.device.mgt.mobile.dto.MobileFeature;

import java.util.List;

/**
 * This class represents the key operations associated with persisting mobile feature related
 * information.
 */
public interface MobileFeatureDAO {

	/**
	 * Add a new feature to feature table.
	 *
	 * @param mobileFeature Feature object that holds data related to the feature to be inserted.
	 * @return The id of inserted feature.
	 * @throws MobileDeviceManagementDAOException
	 */
	int addMobileFeature(MobileFeature mobileFeature) throws MobileDeviceManagementDAOException;

	/**
	 * Update a feature in the feature table.
	 *
	 * @param mobileFeature Feature object that holds data has to be updated.
	 * @return The status of the operation. If the update was successful or not.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean updateMobileFeature(MobileFeature mobileFeature) throws MobileDeviceManagementDAOException;

	/**
	 * Delete a feature from feature table when the feature id is given.
	 *
	 * @param featureId Feature id of the feature to be deleted.
	 * @return The status of the operation. If the operationId was successful or not.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean deleteMobileFeatureById(int featureId) throws MobileDeviceManagementDAOException;

	/**
	 * Delete a feature from feature table when the feature code is given.
	 *
	 * @param featureCode Feature code of the feature to be deleted.
	 * @return The status of the operation. If the operationId was successful or not.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean deleteMobileFeatureByCode(String featureCode) throws MobileDeviceManagementDAOException;

	/**
	 * Retrieve a given feature from feature table when the feature id is given.
	 *
	 * @param featureId Feature id of the feature to be retrieved.
	 * @return Feature object that holds data of the feature represented by featureId.
	 * @throws MobileDeviceManagementDAOException
	 */
	MobileFeature getMobileFeatureById(int featureId) throws MobileDeviceManagementDAOException;

	/**
	 * Retrieve a given feature from feature table when the feature code is given.
	 *
	 * @param featureCode Feature code of the feature to be retrieved.
	 * @return Feature object that holds data of the feature represented by featureCode.
	 * @throws MobileDeviceManagementDAOException
	 */
	MobileFeature getMobileFeatureByCode(String featureCode) throws MobileDeviceManagementDAOException;

	/**
	 * Retrieve all the features from plugin specific database.
	 *
	 * @return Feature object list.
	 * @throws MobileDeviceManagementDAOException
	 */

	/**
	 * Retrieve all the features from plugin specific database for a Device Type.
	 * @param deviceType - Device type.
	 * @return Feature object list.
	 * @throws MobileDeviceManagementDAOException
	 */
	List<MobileFeature> getMobileFeatureByDeviceType(String deviceType) throws MobileDeviceManagementDAOException;

	List<MobileFeature> getAllMobileFeatures() throws MobileDeviceManagementDAOException;
}
