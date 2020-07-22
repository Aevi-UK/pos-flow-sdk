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

package com.aevi.sdk.pos.flow.paymentservicesample.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.Switch;

import com.aevi.sdk.flow.constants.FlowStages;
import com.aevi.sdk.flow.model.FlowEvent;
import com.aevi.sdk.pos.flow.model.Amounts;
import com.aevi.sdk.pos.flow.model.Card;
import com.aevi.sdk.pos.flow.model.TransactionRequest;
import com.aevi.sdk.pos.flow.model.TransactionResponse;
import com.aevi.sdk.pos.flow.model.TransactionResponseBuilder;
import com.aevi.sdk.pos.flow.model.events.ConfirmationInput;
import com.aevi.sdk.pos.flow.model.events.ConfirmationOption;
import com.aevi.sdk.pos.flow.model.events.ConfirmationRequest;
import com.aevi.sdk.pos.flow.model.events.ConfirmationResponse;
import com.aevi.sdk.pos.flow.model.events.FinalAmountRequest;
import com.aevi.sdk.pos.flow.paymentservicesample.R;
import com.aevi.sdk.pos.flow.paymentservicesample.util.InMemoryStore;
import com.aevi.sdk.pos.flow.sample.AmountFormatter;
import com.aevi.sdk.pos.flow.sample.CardProducer;
import com.aevi.sdk.pos.flow.sample.ui.BaseSampleAppCompatActivity;
import com.aevi.sdk.pos.flow.sample.ui.ModelDisplay;
import com.aevi.sdk.pos.flow.stage.TransactionProcessingModel;
import com.aevi.ui.library.DropDownHelper;
import com.aevi.ui.library.recycler.DropDownSpinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import io.reactivex.disposables.Disposable;

import static com.aevi.sdk.flow.constants.PaymentMethods.PAYMENT_METHOD_CARD;
import static com.aevi.sdk.flow.constants.events.ConfirmationInputTypes.CONFIRMATION_INPUT_TYPE_TEXT;
import static com.aevi.sdk.flow.constants.events.ConfirmationOptionValues.*;
import static com.aevi.sdk.flow.constants.events.ConfirmationTypes.*;
import static com.aevi.sdk.flow.constants.events.FlowEventTypes.*;
import static com.aevi.sdk.pos.flow.sample.ReferenceProvider.getReferences;


public class TransactionProcessingActivity extends BaseSampleAppCompatActivity {

    private static final String APPROVED_RESP_CODE = "00";
    private static final String DECLINED_RESP_CODE = "XX";

    public static final String INTERNAL_ID_KEY = "sampleTransactionReference";

    @BindView(R.id.processed_amounts_spinner)
    DropDownSpinner processedAmountsSpinner;

    @BindView(R.id.payment_methods_spinner)
    DropDownSpinner paymentMethodsSpinner;

    @BindView(R.id.include_card)
    CheckBox includeCardView;

    @BindView(R.id.approve_switch)
    Switch approveSwitch;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private final List<Long> processedAmountsOptions = new ArrayList<>();
    private TransactionRequest transactionRequest;
    private TransactionProcessingModel transactionProcessingModel;
    private ModelDisplay modelDisplay;

    private Disposable eventsDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_response_builder);
        ButterKnife.bind(this);
        transactionProcessingModel = TransactionProcessingModel.fromActivity(this);
        transactionRequest = transactionProcessingModel.getTransactionRequest();

        final DropDownHelper dropDownHelper = new DropDownHelper(this);
        long totalAmount = transactionRequest.getAmounts().getTotalAmountValue();
        processedAmountsOptions.addAll(Arrays.asList(0L, totalAmount / 2L, totalAmount));
        List<String> formattedAmountOptions = new ArrayList<>();
        for (Long processedAmountsOption : processedAmountsOptions) {
            formattedAmountOptions
                    .add(formatAmount(processedAmountsOption) + " (" + (int) ((processedAmountsOption / (double) totalAmount) * 100) + "%)");
        }
        dropDownHelper.setupDropDown(processedAmountsSpinner, formattedAmountOptions, false);
        processedAmountsSpinner.setSelection(2);

        dropDownHelper.setupDropDown(paymentMethodsSpinner, R.array.payment_methods);
        setupToolbar(toolbar, R.string.pss_txn_processing);
        modelDisplay = (ModelDisplay) getSupportFragmentManager().findFragmentById(R.id.fragment_request_details);
        if (modelDisplay != null) {
            modelDisplay.showTitle(false);
        }

        subscribeToEvents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventsDisposable != null) {
            eventsDisposable.dispose();
            eventsDisposable = null;
        }
    }

    private void subscribeToEvents() {
        // here we are subscribing to flow com.aevi.sdk.pos.flow.model.events and waiting for a confirmation response to our question, if the user (merchant) chooses to ask it during transaction processing
        eventsDisposable = transactionProcessingModel.getEvents().subscribe(flowEvent -> {
            if (flowEvent.getType().equals(EVENT_CONFIRMATION_RESPONSE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TransactionProcessingActivity.this);

                ConfirmationResponse response = flowEvent.getEventData(ConfirmationResponse.class);
                // in reality we'd check the response value here but for this sample we are just assuming there's one and displaying it
                builder.setTitle(R.string.event_title).setMessage(response.getSelectedValues()[0]);
                builder.create().show();
            } else {
                Log.d("TPA", "Got flow event: " + flowEvent.toJson());
            }
        }, throwable -> {
            Log.e("TPA", "Failed to get com.aevi.sdk.pos.flow.model.events", throwable);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        buildTransactionResponse();
    }

    public String formatAmount(long amount) {
        return AmountFormatter.formatAmount(transactionRequest.getAmounts().getCurrency(), amount);
    }

    @OnCheckedChanged({R.id.approve_switch, R.id.include_card})
    public void onCheckboxChange() {
        buildTransactionResponse();
        processedAmountsSpinner.setEnabled(approveSwitch.isChecked());
        paymentMethodsSpinner.setEnabled(approveSwitch.isChecked());
        includeCardView.setEnabled(approveSwitch.isChecked());
    }

    @OnItemSelected({R.id.processed_amounts_spinner, R.id.payment_methods_spinner})
    public void onSpinnerChange() {
        buildTransactionResponse();
    }

    private void buildTransactionResponse() {
        if (approveSwitch.isChecked()) {
            TransactionResponseBuilder transactionResponseBuilder = transactionProcessingModel.getTransactionResponseBuilder();
            setProcessedAmounts(transactionResponseBuilder)
                    .withOutcomeMessage("User approved manually")
                    .withResponseCode(APPROVED_RESP_CODE)
                    .withCard(getCard())
                    .withReferences(getReferences(transactionRequest))
                    .addReference(TransactionProcessingActivity.INTERNAL_ID_KEY, UUID.randomUUID().toString())
                    .build();
        } else {
            transactionProcessingModel.getTransactionResponseBuilder()
                    .decline("Used declined manually")
                    .withResponseCode(DECLINED_RESP_CODE)
                    .build();
        }
        if (modelDisplay != null) {
            modelDisplay.showTransactionResponse(transactionProcessingModel.getTransactionResponse());
        }
    }

    @OnClick(R.id.send_response)
    public void onSendResponse() {
        InMemoryStore.getInstance().setLastTransactionResponseGenerated(transactionProcessingModel.getTransactionResponse());
        transactionProcessingModel.sendResponse();
        finish();
    }

    @OnClick(R.id.ask_a_question)
    public void onSendFlowEvent() {
        ConfirmationOption[] opts = new ConfirmationOption[2];
        opts[0] = new ConfirmationOption(CONFIRMATION_OPTION_YES, getString(R.string.card_yes));
        opts[1] = new ConfirmationOption(CONFIRMATION_OPTION_NO, getString(R.string.card_no));
        FlowEvent flowEvent = new FlowEvent(EVENT_CONFIRMATION_REQUEST, new ConfirmationRequest(CONFIRMATION_TYPE_CARD, getString(R.string.event_question), opts));
        transactionProcessingModel.sendEvent(flowEvent);
    }

    @OnClick(R.id.request_some_input)
    public void requestSomeInput() {
        ConfirmationRequest confirmationRequest = new ConfirmationRequest(CONFIRMATION_TYPE_SIGNATURE, getString(R.string.event_input_title), new ConfirmationInput(CONFIRMATION_INPUT_TYPE_TEXT, 2, 10));
        FlowEvent flowEvent = new FlowEvent(EVENT_CONFIRMATION_REQUEST, confirmationRequest);
        transactionProcessingModel.sendEvent(flowEvent);
    }

    @OnClick(R.id.request_final_amount)
    public void requestFinalAmount() {
        FlowEvent flowEvent = new FlowEvent(EVENT_FINAL_AMOUNT_REQUEST, new FinalAmountRequest(1000));
        transactionProcessingModel.sendEvent(flowEvent);
    }

    private TransactionResponseBuilder setProcessedAmounts(TransactionResponseBuilder transactionResponseBuilder) {
        int chosenSelection = processedAmountsSpinner.getSelectedItemPosition();
        long amounts = processedAmountsOptions.get(chosenSelection);
        if (amounts == 0) {
            transactionResponseBuilder.approve();
        } else if (amounts < transactionRequest.getAmounts().getTotalAmountValue()) {
            transactionResponseBuilder.approve(new Amounts(amounts, transactionRequest.getAmounts().getCurrency()), PAYMENT_METHOD_CARD);
        } else {
            transactionResponseBuilder.approve(transactionRequest.getAmounts(), PAYMENT_METHOD_CARD);
        }
        return transactionResponseBuilder;
    }

    private Card getCard() {
        if (!includeCardView.isChecked()) {
            return null;
        }

        return CardProducer.getCard(transactionRequest);
    }

    @Override
    protected int getPrimaryColor() {
        return getResources().getColor(R.color.colorPrimary);
    }

    @Override
    protected String getCurrentStage() {
        return FlowStages.TRANSACTION_PROCESSING;
    }

    @Override
    protected Class<?> getRequestClass() {
        return TransactionRequest.class;
    }

    @Override
    protected Class<?> getResponseClass() {
        return TransactionResponse.class;
    }

    @Override
    protected String getModelJson() {
        return transactionProcessingModel.getTransactionResponse().toJson();
    }

    @Override
    protected String getRequestJson() {
        return transactionRequest.toJson();
    }

    @Override
    protected String getHelpText() {
        return getString(R.string.txn_processing_help);
    }
}
