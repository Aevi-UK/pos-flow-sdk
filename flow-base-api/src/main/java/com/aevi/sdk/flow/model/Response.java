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

/**
 * Response to a generic {@link Request} that contains the outcome and bespoke response data for that request type.
 */
public class Response extends BaseModel {

    private Request originatingRequest;
    private final boolean success;
    private final String outcomeMessage;
    private final AdditionalData responseData;
    private String flowServiceId;
    private boolean processedInBackground;

    // Default constructor for deserialisation
    Response() {
        this("N/A", false, null, new AdditionalData());
    }

    /**
     * Initialise a response with the originating request and outcome details.
     *
     * @param request        The originating request
     * @param success        The success flag of the processing
     * @param outcomeMessage The outcome message
     */
    public Response(Request request, boolean success, String outcomeMessage) {
        this(request, success, outcomeMessage, new AdditionalData());
    }

    /**
     * Initialise a response with the originating request, outcome details and response data.
     *
     * See reference values in the documentation for possible values.
     *
     * @param request        The originating request
     * @param success        The success flag of the processing
     * @param outcomeMessage The outcome message
     * @param responseData   The bespoke response data
     */
    public Response(Request request, boolean success, String outcomeMessage, AdditionalData responseData) {
        this(request.getId(), success, outcomeMessage, responseData);
        this.originatingRequest = request;
    }

    /**
     * Initialise a response with the originating request id, outcome details and response data.
     *
     * See reference values in the documentation for possible values.
     *
     * @param requestId      The originating request id
     * @param success        The success flag of the processing
     * @param outcomeMessage The outcome message
     * @param responseData   The bespoke response data
     */
    public Response(String requestId, boolean success, String outcomeMessage, AdditionalData responseData) {
        super(requestId);
        this.success = success;
        this.outcomeMessage = outcomeMessage != null ? outcomeMessage : "";
        this.responseData = responseData != null ? responseData : new AdditionalData();
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
        responseData.addData(key, values);
    }

    /**
     * Get the request that triggered this response.
     *
     * @return The originating request, if set
     */
    @Nullable
    public Request getOriginatingRequest() {
        return originatingRequest;
    }

    /**
     * Check whether the processing was successful or not
     *
     * @return True if the processing of the request succeeded, false otherwise
     */
    public boolean wasSuccessful() {
        return success;
    }

    /**
     * Get the outcome message that further describes the processing outcome.
     *
     * @return The outcome message
     */
    @NonNull
    public String getOutcomeMessage() {
        return outcomeMessage;
    }

    /**
     * Get the response data bespoke to this request type.
     *
     * See reference values in the documentation for possible values.
     *
     * @return The response data
     */
    @NonNull
    public AdditionalData getResponseData() {
        return responseData;
    }

    /**
     * For internal use.
     *
     * @param originatingRequest The originating request
     */
    public void setOriginatingRequest(Request originatingRequest) {
        this.originatingRequest = originatingRequest;
    }

    /**
     * Get the id of the service that handled this request, if any,
     *
     * This will be null if there was no app that could handle the request or an error occurred.
     *
     * @return The flow service if
     */
    @Nullable
    public String getFlowServiceId() {
        return flowServiceId;
    }

    /**
     * For internal use.
     *
     * @param flowServiceId The flow service id
     */
    public void setFlowServiceId(String flowServiceId) {
        this.flowServiceId = flowServiceId;
    }

    /**
     * Check whether the request was processed as a background flow or not.
     *
     * @return True if processed in the background
     */
    public boolean wasProcessedInBackground() {
        return processedInBackground;
    }

    /**
     * For internal use.
     *
     * @param processedInBackground Processed in background
     */
    public void setProcessedInBackground(boolean processedInBackground) {
        this.processedInBackground = processedInBackground;
    }

    @Override
    public String toJson() {
        return JsonConverter.serialize(this);
    }

    @Override
    public String toString() {
        return "Response{" +
                "originatingRequest=" + originatingRequest +
                ", success=" + success +
                ", outcomeMessage='" + outcomeMessage + '\'' +
                ", responseData=" + responseData +
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

        Response response = (Response) o;
        return success == response.success &&
                Objects.equals(originatingRequest, response.originatingRequest) &&
                Objects.equals(outcomeMessage, response.outcomeMessage) &&
                Objects.equals(responseData, response.responseData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), originatingRequest, success, outcomeMessage, responseData);
    }

    public static Response fromJson(String json) {
        return JsonConverter.deserialize(json, Response.class);
    }
}
