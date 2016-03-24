/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.arduino.manager.service.impl;

import org.wso2.carbon.apimgt.application.extension.APIManagementProviderService;
import org.wso2.carbon.apimgt.application.extension.dto.ApiApplicationKey;
import org.wso2.carbon.apimgt.application.extension.exception.APIManagerException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.iot.arduino.manager.service.impl.util.APIUtil;
import org.wso2.carbon.device.mgt.iot.arduino.plugin.constants.ArduinoConstants;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.util.ZipArchive;
import org.wso2.carbon.device.mgt.iot.util.ZipUtil;
import org.wso2.carbon.device.mgt.jwt.client.extension.JWTClient;
import org.wso2.carbon.device.mgt.jwt.client.extension.JWTClientManager;
import org.wso2.carbon.device.mgt.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.device.mgt.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.user.api.UserStoreException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ArduinoManagerService {

	@Context  //injected response proxy supporting multiple thread
	private HttpServletResponse response;
	private static final String KEY_TYPE = "PRODUCTION";
	private static ApiApplicationKey apiApplicationKey;

	@Path("manager/device/{device_id}")
	@DELETE
	public void removeDevice(@PathParam("device_id") String deviceId, @Context HttpServletResponse response) {
		DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
		deviceIdentifier.setId(deviceId);
		deviceIdentifier.setType(ArduinoConstants.DEVICE_TYPE);
		try {
			boolean removed = APIUtil.getDeviceManagementService().disenrollDevice(deviceIdentifier);
			if (removed) {
				response.setStatus(Response.Status.OK.getStatusCode());

			} else {
				response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());

			}
		} catch (DeviceManagementException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	@Path("manager/device/{device_id}")
	@PUT
	public boolean updateDevice(@PathParam("device_id") String deviceId, @QueryParam("name") String name,
								@Context HttpServletResponse response) {
		DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
		deviceIdentifier.setId(deviceId);
		deviceIdentifier.setType(ArduinoConstants.DEVICE_TYPE);
		try {
			Device device = APIUtil.getDeviceManagementService().getDevice(deviceIdentifier);
			device.setDeviceIdentifier(deviceId);
			device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());
			device.setName(name);
			device.setType(ArduinoConstants.DEVICE_TYPE);
			boolean updated = APIUtil.getDeviceManagementService().modifyEnrollment(device);
			if (updated) {
				response.setStatus(Response.Status.OK.getStatusCode());
			} else {
				response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
			}
			return updated;
		} catch (DeviceManagementException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return false;
		}
	}

	@Path("manager/device/{device_id}")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Device getDevice(@PathParam("device_id") String deviceId) {
		DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
		deviceIdentifier.setId(deviceId);
		deviceIdentifier.setType(ArduinoConstants.DEVICE_TYPE);
		try {
			return APIUtil.getDeviceManagementService().getDevice(deviceIdentifier);
		} catch (DeviceManagementException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return null;
		}
	}

	@Path("manager/devices")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Device[] getArduinoDevices() {
		try {
			List<Device> userDevices = APIUtil.getDeviceManagementService().getDevicesOfUser(
					APIUtil.getAuthenticatedUser());
			ArrayList<Device> userDevicesforArduino = new ArrayList<>();
			for (Device device : userDevices) {
				if (device.getType().equals(ArduinoConstants.DEVICE_TYPE) &&
						device.getEnrolmentInfo().getStatus().equals(EnrolmentInfo.Status.ACTIVE)) {
					userDevicesforArduino.add(device);
				}
			}
			return userDevicesforArduino.toArray(new Device[]{});
		} catch (DeviceManagementException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return null;
		}
	}

	@Path("manager/device/download")
	@GET
	@Produces("application/octet-stream")
	public Response downloadSketch(@QueryParam("deviceName") String customDeviceName) {
		try {
			ZipArchive zipFile = createDownloadFile(APIUtil.getAuthenticatedUser(), customDeviceName);
			Response.ResponseBuilder rb = Response.ok(zipFile.getZipFile());
			rb.header("Content-Disposition", "attachment; filename=\"" + zipFile.getFileName() + "\"");
			return rb.build();
		} catch (IllegalArgumentException ex) {
			return Response.status(400).entity(ex.getMessage()).build();//bad request
		} catch (DeviceManagementException ex) {
			return Response.status(500).entity(ex.getMessage()).build();
		} catch (DeviceControllerException ex) {
			return Response.status(500).entity(ex.getMessage()).build();
		} catch (JWTClientException ex) {
			return Response.status(500).entity(ex.getMessage()).build();
		} catch (APIManagerException ex) {
			return Response.status(500).entity(ex.getMessage()).build();
		} catch (UserStoreException ex) {
			return Response.status(500).entity(ex.getMessage()).build();
		}
	}

	@Path("manager/device/generate_link")
	@GET
	public Response generateSketchLink(@QueryParam("deviceName") String deviceName) {
		try {
			ZipArchive zipFile = createDownloadFile(APIUtil.getAuthenticatedUser(), deviceName);
			Response.ResponseBuilder rb = Response.ok(zipFile.getDeviceId());
			return rb.build();
		} catch (IllegalArgumentException ex) {
			return Response.status(400).entity(ex.getMessage()).build();//bad request
		} catch (DeviceManagementException ex) {
			return Response.status(500).entity(ex.getMessage()).build();
		} catch (JWTClientException ex) {
			return Response.status(500).entity(ex.getMessage()).build();
		} catch (DeviceControllerException ex) {
			return Response.status(500).entity(ex.getMessage()).build();
		} catch (APIManagerException ex) {
			return Response.status(500).entity(ex.getMessage()).build();
		} catch (UserStoreException ex) {
			return Response.status(500).entity(ex.getMessage()).build();
		}
	}

	private ZipArchive createDownloadFile(String owner, String deviceName)
			throws DeviceManagementException, JWTClientException, DeviceControllerException, APIManagerException,
				   UserStoreException {
		if (owner == null) {
			throw new IllegalArgumentException("Error on createDownloadFile() Owner is null!");
		}
		//create new device id
		String deviceId = shortUUID();
		String applicationUsername =
				PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration()
						.getAdminUserName();
		if (apiApplicationKey == null) {
			APIManagementProviderService apiManagementProviderService = APIUtil.getAPIManagementProviderService();
			String[] tags = {ArduinoConstants.DEVICE_TYPE};
			apiApplicationKey = apiManagementProviderService.generateAndRetrieveApplicationKeys(
					ArduinoConstants.DEVICE_TYPE, tags, KEY_TYPE, applicationUsername, true);
		}
		JWTClient jwtClient = JWTClientManager.getInstance().getJWTClient();
		String scopes = "device_type_" + ArduinoConstants.DEVICE_TYPE + " device_" + deviceId;
		AccessTokenInfo accessTokenInfo = jwtClient.getAccessToken(apiApplicationKey.getConsumerKey(),
																   apiApplicationKey.getConsumerSecret(), owner,
																   scopes);
		//create token
		String accessToken = accessTokenInfo.getAccess_token();
		String refreshToken = accessTokenInfo.getRefresh_token();
		//Register the device with CDMF
		boolean status = register(deviceId, deviceName);
		if (!status) {
			String msg = "Error occurred while registering the device with " + "id: " + deviceId + " owner:" + owner;
			throw new DeviceManagementException(msg);
		}
		ZipUtil ziputil = new ZipUtil();
		ZipArchive zipFile = ziputil.createZipFile(owner, APIUtil.getTenantDomainOftheUser(),
												   ArduinoConstants.DEVICE_TYPE, deviceId,
												   deviceName, accessToken, refreshToken);
		zipFile.setDeviceId(deviceId);
		return zipFile;
	}

	private static String shortUUID() {
		UUID uuid = UUID.randomUUID();
		long l = ByteBuffer.wrap(uuid.toString().getBytes(StandardCharsets.UTF_8)).getLong();
		return Long.toString(l, Character.MAX_RADIX);
	}

	private boolean register(String deviceId, String name) {
		DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
		deviceIdentifier.setId(deviceId);
		deviceIdentifier.setType(ArduinoConstants.DEVICE_TYPE);
		try {
			if (APIUtil.getDeviceManagementService().isEnrolled(deviceIdentifier)) {
				response.setStatus(Response.Status.CONFLICT.getStatusCode());
				return false;
			}
			Device device = new Device();
			device.setDeviceIdentifier(deviceId);
			EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
			enrolmentInfo.setDateOfEnrolment(new Date().getTime());
			enrolmentInfo.setDateOfLastUpdate(new Date().getTime());
			enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
			enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
			device.setName(name);
			device.setType(ArduinoConstants.DEVICE_TYPE);
			enrolmentInfo.setOwner(APIUtil.getAuthenticatedUser());
			device.setEnrolmentInfo(enrolmentInfo);
			boolean added = APIUtil.getDeviceManagementService().enrollDevice(device);
			if (added) {
				response.setStatus(Response.Status.OK.getStatusCode());
			} else {
				response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
			}
			return added;
		} catch (DeviceManagementException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return false;
		}
	}
}