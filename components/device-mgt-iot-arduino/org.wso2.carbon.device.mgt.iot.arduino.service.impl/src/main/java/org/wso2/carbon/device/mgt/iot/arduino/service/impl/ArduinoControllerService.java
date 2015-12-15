/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.arduino.service.impl;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.service.DeviceAnalyticsService;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.arduino.service.impl.util.DeviceJSON;
import org.wso2.carbon.device.mgt.iot.arduino.service.impl.util.ArduinoMQTTSubscriber;
import org.wso2.carbon.device.mgt.iot.arduino.plugin.constants.ArduinoConstants;
import org.wso2.carbon.device.mgt.iot.DeviceController;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.*;

public class ArduinoControllerService {

	private static Log log = LogFactory.getLog(ArduinoControllerService.class);

	private static Map<String, LinkedList<String>> replyMsgQueue = new HashMap<>();
	private static Map<String, LinkedList<String>> internalControlsQueue = new HashMap<>();
	private static ArduinoMQTTSubscriber arduinoMQTTSubscriber;
	private static final String TEMPERATURE_STREAM_DEFINITION = "org.wso2.iot.devices.temperature";
	private final String SUPER_TENANT = "carbon.super";

	public void setMqttArduinoSubscriber(ArduinoMQTTSubscriber arduinoMQTTSubscriber) {
		ArduinoControllerService.arduinoMQTTSubscriber = arduinoMQTTSubscriber;
		try {
			arduinoMQTTSubscriber.connectAndSubscribe();
		} catch (DeviceManagementException e) {
			log.error(e.getErrorMessage());
		}
	}

	public ArduinoMQTTSubscriber getMqttArduinoSubscriber() {
		return arduinoMQTTSubscriber;
	}

	public static Map<String, LinkedList<String>> getReplyMsgQueue() {
		return replyMsgQueue;
	}

	public static Map<String, LinkedList<String>> getInternalControlsQueue() {
		return internalControlsQueue;
	}

	/*    Service to switch arduino bulb (pin 13) between "ON" and "OFF"
			   Called by an external client intended to control the Arduino */
	@Path("/bulb/{deviceId}/{state}")
	@POST
	public void switchBulb(@QueryParam("owner") String owner,
						   @PathParam("deviceId") String deviceId,
						   @PathParam("state") String state,
						   @Context HttpServletResponse response) {

		String switchToState = state.toUpperCase();

		if (!switchToState.equals(ArduinoConstants.STATE_ON) && !switchToState.equals(
				ArduinoConstants.STATE_OFF)) {
			log.error("The requested state change shoud be either - 'ON' or 'OFF'");
			response.setStatus(HttpStatus.SC_BAD_REQUEST);
			return;
		}

		try {
			DeviceController deviceController = new DeviceController();
			boolean result = deviceController.publishMqttControl(owner,
																 ArduinoConstants.DEVICE_TYPE,
																 deviceId, "BULB", switchToState);
			if (result) {
				response.setStatus(HttpStatus.SC_ACCEPTED);

			} else {
				response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);

			}

		} catch (DeviceControllerException e) {
			response.setStatus(HttpStatus.SC_UNAUTHORIZED);

		}

	}

	/*    Service to poll the control-queue for the controls sent to the Arduino
			   Called by the Arduino device  */
	@Path("/readcontrols/{deviceId}")
	@GET
	public String readControls(@QueryParam("owner") String owner,
							   @PathParam("deviceId") String deviceId,
							   @Context HttpServletResponse response) {
		String result;
		LinkedList<String> deviceControlList = internalControlsQueue.get(deviceId);

		if (deviceControlList == null) {
			result = "No controls have been set for device " + deviceId + " of owner " + owner;
			response.setStatus(HttpStatus.SC_NO_CONTENT);
		} else {
			try {
				result = deviceControlList.remove(); //returns the  head value
				response.setStatus(HttpStatus.SC_ACCEPTED);

			} catch (NoSuchElementException ex) {
				result = "There are no more controls for device " + deviceId + " of owner " +
						owner;
				response.setStatus(HttpStatus.SC_NO_CONTENT);
			}
		}
		if (log.isDebugEnabled()) {
			log.debug(result);
		}

		return result;
	}


	/*Service to push all the sensor data collected by the Arduino
		   Called by the Arduino device  */
	@Path("/pushdata")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void pushData(final DeviceJSON dataMsg, @Context HttpServletResponse response) {

		float temperature = dataMsg.value;

		if (log.isDebugEnabled()) {
			log.debug("Recieved Temperature Data Value: " + temperature + " degrees C");
		}

		PrivilegedCarbonContext.startTenantFlow();
		PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		ctx.setTenantDomain(SUPER_TENANT, true);
		DeviceAnalyticsService deviceAnalyticsService = (DeviceAnalyticsService) ctx
				.getOSGiService(DeviceAnalyticsService.class, null);
		Object metdaData[] = {dataMsg.owner, ArduinoConstants.DEVICE_TYPE, dataMsg.deviceId,
				System.currentTimeMillis()};
		Object payloadData[] = {temperature};
		try {
			deviceAnalyticsService.publishEvent(TEMPERATURE_STREAM_DEFINITION, "1.0.0",
												metdaData, new Object[0], payloadData);
		} catch (DataPublisherConfigurationException e) {
			log.error("Error on connecting to data publisher");
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);

		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
	}
}
