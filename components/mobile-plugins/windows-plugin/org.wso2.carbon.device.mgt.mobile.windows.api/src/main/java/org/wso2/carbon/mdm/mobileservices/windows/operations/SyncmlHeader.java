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

package org.wso2.carbon.mdm.mobileservices.windows.operations;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.mdm.mobileservices.windows.operations.util.Constants;

/**
 * Represents the header details of a syncml.
 */
@ApiModel(value = "SyncmlHeader", description = "This class carries all information related to SyncmlHeader.")
public class SyncmlHeader {
    @ApiModelProperty(name = "sessionId", value = "SessionId of the syncml Message.", required = true)
    private int sessionId = -1;
    @ApiModelProperty(name = "MsgID", value = "MessageId of the syncml Session.", required = true)
    private int MsgID = -1;
    @ApiModelProperty(name = "target", value = "Target of the syncml Message.(Ex:Device/Server.)", required = true)
    private Target target;
    @ApiModelProperty(name = "source", value = "Source of the Syncml Message.(Ex:Server/Device.)", required = true)
    private Source source;
    @ApiModelProperty(name = "credential", value = "Credentials of the Syncml header.", required = true)
    private Credential credential;
    @ApiModelProperty(name = "hexadecimalSessionId", value = "HexaDecimal SessionId of the syncmlHeader.", required = true)
    private String hexadecimalSessionId;

    public String getHexadecimalSessionId() {
        return hexadecimalSessionId;
    }

    public void setHexadecimalSessionId(String hexSessionId) {
        this.hexadecimalSessionId = hexSessionId;
    }

    public Credential getCredential() {
        return credential;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getMsgID() {
        return MsgID;
    }

    public void setMsgID(int msgID) {
        this.MsgID = msgID;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public void buildSyncmlHeaderElement(Document doc, Element rootElement) {
        Element syncHdr = doc.createElement(Constants.SYNC_HDR);
        rootElement.appendChild(syncHdr);
        Element verDTD = doc.createElement(Constants.VER_DTD);
        verDTD.appendChild(doc.createTextNode(Constants.VER_DTD_VALUE));
        syncHdr.appendChild(verDTD);

        Element verProtocol = doc.createElement(Constants.VER_PROTOCOL);
        verProtocol.appendChild(doc.createTextNode(Constants.VER_PROTOCOL_VALUE));
        syncHdr.appendChild(verProtocol);
        if (getHexadecimalSessionId() != null) {
            Element sessionId = doc.createElement(Constants.SESSION_ID);
            sessionId.appendChild(doc.createTextNode(getHexadecimalSessionId()));
            syncHdr.appendChild(sessionId);
        }
        if (getMsgID() != -1) {
            Element msgId = doc.createElement(Constants.MESSAGE_ID);
            msgId.appendChild(doc.createTextNode(String.valueOf(getMsgID())));
            syncHdr.appendChild(msgId);
        }
        if (getTarget() != null) {
            getTarget().buildTargetElement(doc, syncHdr);
        }
        if (getSource() != null) {
            getSource().buildSourceElement(doc, syncHdr);
        }
        if (getCredential() != null) {
            getCredential().buildCredentialElement(doc, syncHdr);
        }
    }
}
