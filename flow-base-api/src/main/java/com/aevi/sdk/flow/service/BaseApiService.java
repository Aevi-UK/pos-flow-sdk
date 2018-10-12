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

package com.aevi.sdk.flow.service;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.aevi.android.rxmessenger.MessageException;
import com.aevi.android.rxmessenger.activity.NoSuchInstanceException;
import com.aevi.android.rxmessenger.activity.ObservableActivityHelper;
import com.aevi.android.rxmessenger.service.AbstractMessengerService;
import com.aevi.sdk.flow.constants.InternalDataKeys;
import com.aevi.sdk.flow.model.AppMessage;
import com.aevi.sdk.flow.model.InternalData;

import io.reactivex.functions.Consumer;

import static android.content.Intent.*;
import static com.aevi.sdk.flow.constants.ActivityEvents.FINISH;
import static com.aevi.sdk.flow.constants.AppMessageTypes.*;
import static com.aevi.sdk.flow.constants.MessageErrors.*;
import static com.aevi.sdk.flow.model.AppMessage.EMPTY_DATA;

/**
 * Base service for all API service implementations.
 */
public abstract class BaseApiService extends AbstractMessengerService {

    public static final String ACTIVITY_REQUEST_KEY = "request";
    public static final String BACKGROUND_PROCESSING = "backgroundProcessing";

    private final String TAG = getClass().getSimpleName(); // Use class name of implementing service

    private final InternalData internalData;
    private boolean stopServiceOnEndOfStream;

    protected BaseApiService(String apiVersion) {
        internalData = new InternalData(apiVersion);
    }

    /**
     * Set whether or not this service should be stopped after the stream to the client has ended.
     *
     * The default is false, meaning that the service instance will stay active until Android decides to kill it.
     * This means the same service instance may be re-used for multiple requests.
     *
     * If set to true, the service will be stopped after each request and re-started for new ones. This may have a slight performance impact.
     *
     * @param stopServiceOnEndOfStream True to stop service on end of stream, false to keep it running
     */
    public void setStopServiceOnEndOfStream(boolean stopServiceOnEndOfStream) {
        this.stopServiceOnEndOfStream = stopServiceOnEndOfStream;
    }

    @Override
    protected final void handleRequest(String clientMessageId, String message, String packageName) {
        Log.d(TAG, "Received message: " + message);
        AppMessage appMessage = AppMessage.fromJson(message);
        String flowStage = null;
        if (appMessage.getInternalData() != null && appMessage.getInternalData().getAdditionalData() != null) {
            flowStage = appMessage.getInternalData().getAdditionalData().get(InternalDataKeys.FLOW_STAGE);
        }
        if (flowStage == null) {
            flowStage = "UNKNOWN";
        }

        checkVersions(appMessage, internalData);
        switch (appMessage.getMessageType()) {
            case REQUEST_MESSAGE:
                handleRequestMessage(clientMessageId, appMessage.getMessageData(), flowStage);
                break;
            case FORCE_FINISH_MESSAGE:
                onFinish(clientMessageId);
                break;
            default:
                Log.e(TAG, "Unknown message type: " + appMessage.getMessageType() + ". ");
                sendErrorMessageAndFinish(clientMessageId, ERROR_UNKNOWN_MESSAGE_TYPE);
                break;
        }
    }

    static void checkVersions(AppMessage appMessage, InternalData checkWith) {
        // All we do for now is log this - at some point we might want to have specific checks or whatevs
        InternalData senderInternalData = appMessage.getInternalData();
        if (senderInternalData != null) {
            Log.i(BaseApiService.class.getSimpleName(), String.format("Our API version is: %s. Sender API version is: %s",
                    checkWith.getSenderApiVersion(),
                    senderInternalData.getSenderApiVersion()));
        } else {
            Log.i(BaseApiService.class.getSimpleName(), String.format("Our API version is: %s. Sender API version is UNKNOWN!", checkWith.getSenderApiVersion()));
        }
    }

    private void handleRequestMessage(String clientMessageId, String messageData, String flowStage) {
        try {
            sendAck(clientMessageId);
            processRequest(clientMessageId, messageData, flowStage);
        } catch (Throwable t) {
            sendErrorMessageAndFinish(clientMessageId, ERROR_SERVICE_EXCEPTION);
            throw t;
        }
    }

    private void sendAck(String clientMessageId) {
        Log.d(TAG, "Sending ack");
        AppMessage appMessage = new AppMessage(REQUEST_ACK_MESSAGE, internalData);
        sendMessageToClient(clientMessageId, appMessage.toJson());
    }

    public void sendErrorMessageAndFinish(String clientMessageId, String error) {
        Log.d(TAG, "Sending error message: " + error);
        AppMessage errorMessage = new AppMessage(FAILURE_MESSAGE, error, internalData);
        sendMessageToClient(clientMessageId, errorMessage.toJson());
        sendEndStreamMessageToClient(clientMessageId);
    }

    /**
     * Get the API version.
     *
     * The API versioning follows semver rules with major.minor.patch versions.
     *
     * @return The API version
     */
    protected String getApiVersion() {
        return internalData.getSenderApiVersion();
    }

    /**
     * Send notification that this service will process in the background and won't send back any response.
     *
     * Note that you should NOT show any UI after calling this, nor call any of the "finish...Response" methods.
     *
     * This is typically useful for post-transaction / post-flow services that processes the transaction information with no need
     * to show any user interface or augment the transaction.
     *
     * @param clientMessageId The client message id
     */
    public void notifyBackgroundProcessing(String clientMessageId) {
        Log.d(TAG, "notifyBackgroundProcessing");
        internalData.addAdditionalData(BACKGROUND_PROCESSING, "true");
        sendAppMessageAndEndStream(clientMessageId, EMPTY_DATA);
    }

    /**
     * Finish without passing any response back.
     *
     * This is the preferred approach for any case where no response data was generated.
     *
     * @param clientMessageId The client message id
     */
    protected void finishWithNoResponse(String clientMessageId) {
        Log.d(TAG, "finishWithNoResponse");
        sendAppMessageAndEndStream(clientMessageId, EMPTY_DATA);
    }

    /**
     * Finish with a valid response.
     *
     * @param clientMessageId The client message id
     * @param response        The response object
     */
    public void finishWithResponse(String clientMessageId, String response) {
        Log.d(TAG, "finishWithResponse");
        sendAppMessageAndEndStream(clientMessageId, response);
    }

    protected void sendAppMessageAndEndStream(String clientMessageId, String responseData) {
        AppMessage appMessage = new AppMessage(RESPONSE_MESSAGE, responseData, internalData);
        sendMessageToClient(clientMessageId, appMessage.toJson());
        sendEndStreamMessageToClient(clientMessageId);
        if (stopServiceOnEndOfStream) {
            stopSelf();
        }
    }

    /**
     * Called when there is a new request to process.
     *
     * @param clientMessageId The client message id
     * @param request         The request to be processed
     * @param flowStage       The flow stage this request is being called for
     */
    protected abstract void processRequest(@NonNull String clientMessageId, @NonNull String request, @NonNull String flowStage);

    /**
     * Called when your application needs to abort what it is doing and finish any Activity that is running.
     *
     * As part of this callback, you need to;
     *
     * 1. Finish any Activity. If you launched it via the {@link #launchActivity(Class, String, String)} method, you can use {@link #finishLaunchedActivity(String)} to ask it to finish itself, provided that the activity has registered with the {@link ObservableActivityHelper} for finish events.
     *
     * 2. Finish the service with {@link #finishWithNoResponse(String)} as any response data would be ignored at this stage.
     *
     * @param clientMessageId The client message id
     */
    protected abstract void onFinish(@NonNull String clientMessageId);

    /**
     * Helper to launch an activity with the request passed in.
     *
     * The request will be passed in the intent as a string extra with the key "request".
     *
     * @param activityCls     The activity that should handle the request.
     * @param clientMessageId The id that was passed to {@link #processRequest(String, String, String)}
     * @param request         The request model.
     * @param extras          Extras to add to the intent
     */
    public void launchActivity(Class<? extends Activity> activityCls, final String clientMessageId, final String request, final Bundle extras) {
        Intent intent = new Intent(this, activityCls);
        launchActivity(intent, clientMessageId, request, extras);
    }

    public void launchActivity(Intent intent, final String clientMessageId, final String request, final Bundle extras) {
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (request != null) {
            intent.putExtra(ACTIVITY_REQUEST_KEY, request);
        }
        intent.putExtra(ObservableActivityHelper.INTENT_ID, clientMessageId);
        if (extras != null) {
            intent.putExtras(extras);
        }
        ObservableActivityHelper<String> helper = ObservableActivityHelper.createInstance(this, intent);
        subscribeToLifecycle(helper);
        helper.startObservableActivity().subscribe(new Consumer<String>() {
            @Override
            public void accept(@NonNull String response) throws Exception {
                finishWithResponse(clientMessageId, response);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                handleActivityException(clientMessageId, throwable);
            }
        });
    }

    /**
     * Can be overriden to handle when the activity responds with an error / exception.
     */
    protected void handleActivityException(String clientMessageId, Throwable throwable) {
        if (throwable instanceof MessageException) {
            MessageException me = (MessageException) throwable;
            sendErrorMessageToClient(clientMessageId, me.getCode(), me.getMessage());
        } else {
            sendErrorMessageAndFinish(clientMessageId, throwable.getMessage());
        }
    }

    public void launchActivity(Class<? extends Activity> activityCls, final String clientMessageId, final String request) {
        launchActivity(activityCls, clientMessageId, request, new Bundle());
    }

    public void subscribeToLifecycle(ObservableActivityHelper<?> helper) {
        Log.d(TAG, "Subscribing to lifecycle events");
        helper.onLifecycleEvent().subscribe(new Consumer<Lifecycle.Event>() {
            @Override
            public void accept(Lifecycle.Event event) throws Exception {
                Log.d(TAG, "Received lifecycle event: " + event);
                onActivityLifecycleEvent(event);
            }
        });
    }

    /**
     * Can be overridden to receive lifecycle events from the activity started via {@link #launchActivity(Class, String)}.
     *
     * @param event
     */
    protected void onActivityLifecycleEvent(@NonNull Lifecycle.Event event) {
        // Default no-op
    }

    /**
     * Helper to launch an activity with no request but that needs to send a response.
     *
     * @param activityCls     The activity to start
     * @param clientMessageId The id that was passed to {@link #processRequest(String, String, String)}
     */
    protected void launchActivity(Class<? extends Activity> activityCls, final String clientMessageId) {
        launchActivity(activityCls, clientMessageId, null);
    }

    /**
     * Finish an activity launched via {@link #launchActivity(Class, String, String)}.
     *
     * Note that the activity must have subscribed via ObservableActivityHelper.registerForEvents().
     *
     * @param clientMessageId The id that was used to call {@link #launchActivity(Class, String, String)}.
     */
    protected void finishLaunchedActivity(String clientMessageId) {
        try {
            ObservableActivityHelper<String> helper = ObservableActivityHelper.getInstance(clientMessageId);
            helper.sendEventToActivity(FINISH);
        } catch (NoSuchInstanceException e) {
            // Ignore
        }
    }
}
