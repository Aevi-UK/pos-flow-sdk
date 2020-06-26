/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.aevi.sdk.flow.model;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.aevi.util.json.JsonConverter;

import java.util.Objects;
import java.util.UUID;

/**
 * Generic request that at minimum contains a request type and optionally flow name and bespoke request data.
 *
 * If a flow name is set, the flow with that name will be explicitly used. If not set, the request type will be used to look up eligible flows
 * and one will be selected either automatically or via user interaction.
 */
public class Request extends BaseModel {

    private final String requestType;
    private final AdditionalData requestData;
    private String flowName;
    private String deviceId;
    private String targetAppId;
    private String source;
    private boolean processInBackground;

    // Default constructor for deserialisation
    Request() {
        this("");
    }

    /**
     * Initialise this request with a request type and empty data.
     *
     * The request type will be used to assign the correct flow for this request.
     *
     * Please see {@link #setFlowName(String)} to explicitly specify what flow to use.
     *
     * See reference values in the documentation for possible values.
     *
     * @param requestType The request type
     */
    public Request(String requestType) {
        this(requestType, new AdditionalData());
    }

    /**
     * Initialise this request with a request type and data.
     *
     * The request type will be used to assign the correct flow for this request.
     *
     * Please see {@link #setFlowName(String)} to explicitly specify what flow to use.
     *
     * See reference values in the documentation for possible values.
     *
     * @param requestType The request type
     * @param requestData The data for the request
     */
    public Request(String requestType, AdditionalData requestData) {
        this(UUID.randomUUID().toString(), requestType, requestData);
    }

    private Request(String id, String requestType, AdditionalData requestData) {
        super(id);
        this.requestType = requestType;
        this.requestData = requestData;
    }

    /**
     * Get the request type which indicates what function this request represents.
     *
     * @return The request type
     */
    @NonNull
    public String getRequestType() {
        return requestType;
    }

    /**
     * Explicitly set the name of the flow to process this request.
     *
     * This can and should be used in cases where there is more than one flow for the provided request type.
     *
     * @param flowName The name of the flow to use
     */
    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    /**
     * Get the name of the flow which will process this request, if any.
     *
     * If not set, the request type will be used to map this to a flow.
     *
     * @return The request flow
     */
    @Nullable
    public String getFlowName() {
        return flowName;
    }

    /**
     * Get the id of the device that should be used for customer interactions, if any.
     *
     * @return The device to use for this request, if any.
     */
    @Nullable
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Optionally set what device to use for interactions with the customer.
     *
     * See the relevant client interface for how to retrieve the list of available devices.
     *
     * Setting this means that all customer facing activities will be run on that device.
     *
     * Note that it is possible that devices will be disconnected in between querying for the list and this request being handled, in which
     * case it will fall back to the default device selection mechanism. See docs for more details.
     *
     * @param deviceId The id of the customer facing device
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Set the application to target for this request.
     *
     * @param targetAppId The id of the application to target
     */
    public void setTargetAppId(String targetAppId) {
        this.targetAppId = targetAppId;
    }

    /**
     * Get the source for this request if given
     *
     * @return The id of the application that started (sourced) this request
     */
    @Nullable
    public String getSource() {
        return source;
    }

    /**
     * Set the source for this request
     *
     * @param source The id of the application starting this request
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Get the application target id.
     *
     * @return The application target id
     */
    @Nullable
    public String getTargetAppId() {
        return targetAppId;
    }

    /**
     * Convenience wrapper for adding additional data.
     *
     * See {@link AdditionalData#addData(String, Object[])} for more info.
     *
     * @param key    The key to use for this data
     * @param values An array of values for this data
     * @param <T>    The type of object this data is an array of
     */
    public <T> void addAdditionalData(String key, T... values) {
        requestData.addData(key, values);
    }

    /**
     * Get the request data.
     *
     * See reference values in the documentation for possible values.
     *
     * @return The request data
     */
    @NonNull
    public AdditionalData getRequestData() {
        return requestData;
    }

    /**
     * Check whether this request should be handled in the background (without showing any user interface).
     *
     * If set, applications must not launch any UI (activities) as part of handling this request.
     * If it is not possible to handle the request in this manner, a response indicating failure to process should be sent back.
     *
     * @return True if this request should be handled without showing UI
     */
    public boolean shouldProcessInBackground() {
        return processInBackground;
    }

    /**
     * Specify that the request (and hence the flow) should be processed in the background.
     *
     * This means that the flow will be invisible to any merchant/customer. Do note however that you must ensure that the type of
     * request *can* be handled without user interaction, as otherwise it will be rejected by the receiving application.
     *
     * @param processInBackground True to make this request be processed in the background
     */
    public void setProcessInBackground(boolean processInBackground) {
        this.processInBackground = processInBackground;
    }

    @Override
    public String toJson() {
        return JsonConverter.serialize(this);
    }

    @Override
    public String toString() {
        return "Request{" +
                "requestType='" + requestType + '\'' +
                ", requestData=" + requestData +
                ", flowName='" + flowName + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", targetAppId='" + targetAppId + '\'' +
                ", source='" + source + '\'' +
                ", processInBackground=" + processInBackground +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        return doEquals(o, false);
    }

    @Override
    public boolean equivalent(Object o) {
        return doEquals(o, true);
    }

    private boolean doEquals(Object o, boolean equiv) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (equiv && !super.doEquivalent(o)) {
            return false;
        } else if (!equiv && !super.equals(o)) {
            return false;
        }

        Request request = (Request) o;
        return processInBackground == request.processInBackground &&
                Objects.equals(requestType, request.requestType) &&
                Objects.equals(requestData, request.requestData) &&
                Objects.equals(flowName, request.flowName) &&
                Objects.equals(deviceId, request.deviceId) &&
                Objects.equals(targetAppId, request.targetAppId) &&
                Objects.equals(source, request.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), requestType, requestData, flowName, deviceId, targetAppId, processInBackground, source);
    }

    public static Request fromJson(String json) {
        return JsonConverter.deserialize(json, Request.class);
    }

    /**
     * For internal use.
     *
     * @param id          Id
     * @param requestType Request flow
     * @param requestData Request data
     * @return Request
     */
    public static Request fromExternalId(String id, String requestType, AdditionalData requestData) {
        return new Request(id, requestType, requestData);
    }
}
