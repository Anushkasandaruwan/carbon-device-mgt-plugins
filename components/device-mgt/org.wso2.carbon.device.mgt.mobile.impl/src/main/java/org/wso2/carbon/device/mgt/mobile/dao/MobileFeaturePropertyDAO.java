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

import org.wso2.carbon.device.mgt.mobile.dto.MobileFeatureProperty;

import java.util.List;

/**
 * This class represents the key operations associated with persisting mobile feature property
 * related information.
 */
public interface MobileFeaturePropertyDAO {
	/**
	 * Add a new feature property to feature property table.
	 *
	 * @param mobileFeatureProperty Feature property object that holds data related to the feature
	 *                                 property to be inserted.
	 * @return The status of the operation. If the insert was successful or not.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean addMobileFeatureProperty(MobileFeatureProperty mobileFeatureProperty)
			throws MobileDeviceManagementDAOException;

	/**
	 * Updates a feature property in the feature property table.
	 *
	 * @param mobileFeatureProperty Feature property object that holds data has to be updated.
	 * @return The status of the operation. If the update was successful or not.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean updateMobileFeatureProperty(MobileFeatureProperty mobileFeatureProperty)
			throws MobileDeviceManagementDAOException;

	/**
	 * Deletes a given feature property from feature property table.
	 *
	 * @param property Property of the feature property to be deleted.
	 * @return The status of the operation. If the operationId was successful or not.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean deleteMobileFeatureProperty(String property) throws MobileDeviceManagementDAOException;

	/**
	 * Deletes feature properties of a feature from feature property table.
	 *
	 * @param featureId Feature-id of the feature corresponding properties should be deleted.
	 * @return The status of the operation. If the operationId was successful or not.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean deleteMobileFeaturePropertiesOfFeature(Integer featureId)
			throws MobileDeviceManagementDAOException;

	/**
	 * Retrieves a given feature property from feature property table.
	 *
	 * @param property Property of the feature property to be retrieved.
	 * @return Feature property object that holds data of the feature property represented by propertyId.
	 * @throws MobileDeviceManagementDAOException
	 */
	MobileFeatureProperty getMobileFeatureProperty(String property)
			throws MobileDeviceManagementDAOException;

	/**
	 * Retrieves a list of feature property corresponds to a feature id .
	 *
	 * @param featureId feature id of the feature property to be retrieved.
	 * @return Feature property object that holds data of the feature property represented by propertyId.
	 * @throws MobileDeviceManagementDAOException
	 */
	List<MobileFeatureProperty> getFeaturePropertiesOfFeature(Integer featureId)
			throws MobileDeviceManagementDAOException;

}
