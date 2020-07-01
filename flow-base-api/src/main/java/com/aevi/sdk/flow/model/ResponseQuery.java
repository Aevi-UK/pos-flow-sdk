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

import com.aevi.sdk.flow.BaseApiClient;
import com.aevi.util.json.JsonConverter;
import com.aevi.util.json.Jsonable;

import java.util.Objects;

/**
 * An internal object used to serialise the query data for
 * Response requests via {@link BaseApiClient#queryResponses(ResponseQuery)}
 *
 * See {@link ResponseQueryBuilder} to build a query
 */
public final class ResponseQuery implements Jsonable {

    private final String requestId;
    private final String flowName;
    private final String flowType;
    private final long startDate;
    private final long endDate;
    private final int maxResults;

    private String responseType;

    /*
     * @param requestId The requestId to limit this query to
     * @param flowName   of the flow to get responses for. Can be set to null if not required
     * @param flowType   The type of flow to get responses for. Can be set to null if not required
     * @param startDate  The first/start date to use to restrict the results to
     * @param endDate    The last/end date to use to restrict the results to
     * @param maxResults The maximum number of responses to return
     */
    ResponseQuery(String requestId, String flowName, String flowType, long startDate, long endDate, int maxResults) {
        this.requestId = requestId;
        this.flowName = flowName;
        this.flowType = flowType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxResults = maxResults;
    }

    /**
     * A value is returned here if this query is restricted to a single request Id
     *
     * @return A Request Id or null
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * The flow name to restrict this query to
     *
     * @return A flow name or null
     */
    public String getFlowName() {
        return flowName;
    }

    /**
     * The flow type to filter to in this query
     *
     * @return A flow type or null
     */
    public String getFlowType() {
        return flowType;
    }

    /**
     * The first date to restrict this query to
     *
     * @return A long representing milliseconds since epoch or 0 if unused
     */
    public long getStartDate() {
        return startDate;
    }

    /**
     * The last date to restrict this query to
     *
     * @return A long representing milliseconds since epoch or 0 if unused
     */
    public long getEndDate() {
        return endDate;
    }

    /**
     * The maximum number of results to return
     *
     * @return The max number of results to restrict the query to
     */
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * Returns true if this query has a date range
     *
     * @return True if a date range should be used for this query
     */
    public boolean hasDateRange() {
        return endDate > 0 && startDate > 0;
    }

    @Override
    public String toJson() {
        return JsonConverter.serialize(this);
    }

    public static ResponseQuery fromJson(String json) {
        return JsonConverter.deserialize(json, ResponseQuery.class);
    }

    /**
     * An internal method used by this API to set the response type required for this query.
     *
     * FOR INTERNAL USE ONLY: This method should not be used by external classes
     *
     * @param responseType The class name of the response type to expect for this query
     */
    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getResponseType() {
        return responseType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResponseQuery that = (ResponseQuery) o;
        return startDate == that.startDate &&
                endDate == that.endDate &&
                maxResults == that.maxResults &&
                Objects.equals(requestId, that.requestId) &&
                Objects.equals(flowName, that.flowName) &&
                Objects.equals(flowType, that.flowType) &&
                Objects.equals(responseType, that.responseType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, flowName, flowType, startDate, endDate, maxResults, responseType);
    }
}
