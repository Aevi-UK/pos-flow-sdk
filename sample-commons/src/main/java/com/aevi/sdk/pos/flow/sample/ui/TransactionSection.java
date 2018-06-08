package com.aevi.sdk.pos.flow.sample.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import com.aevi.sdk.pos.flow.model.TransactionResponse;
import com.aevi.sdk.pos.flow.sample.AmountFormatter;
import com.aevi.sdk.pos.flow.sample.R;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

import static com.aevi.sdk.pos.flow.sample.ui.ModelDetailsFragment.*;

public class TransactionSection extends StatelessSection {

    private static final int NUM_TXN_RESPONSE_FIELDS = 5;

    private final Context context;
    private final List<Pair<String, String>> overviewInfo;
    private final List<List<Pair<String, String>>> responseInfoList;
    private final List<TransactionResponse> transactionResponses;
    private final int index;
    private final int totalItems;

    public TransactionSection(Context context, List<Pair<String, String>> overviewInfo, List<TransactionResponse> transactionResponses, int index) {
        super(SectionParameters.builder().itemResourceId(R.layout.snippet_list_item)
                .headerResourceId(R.layout.snippet_list_header).build());
        this.context = context;
        this.overviewInfo = overviewInfo;
        this.transactionResponses = transactionResponses;
        responseInfoList = new ArrayList<>();
        createResponseInfoList();
        totalItems = overviewInfo.size() + transactionResponses.size() * (NUM_TXN_RESPONSE_FIELDS + 1); // +1 for response header
        this.index = index;
    }

    private void createResponseInfoList() {
        for (TransactionResponse transactionResponse : transactionResponses) {
            List<Pair<String, String>> responseInfo = createCommonTransactionResponseInfo(context, transactionResponse);
            if (transactionResponse.getAmountsProcessed() != null) {
                responseInfo.add(getStringPair(context, R.string.total_amount_processed,
                        AmountFormatter.formatAmount(transactionResponse.getAmountsProcessed().getCurrency(),
                                transactionResponse.getAmountsProcessed().getTotalAmountValue())));
                responseInfo.add(getStringPair(context, R.string.payment_method, transactionResponse.getPaymentMethod()));
            } else {
                responseInfo.add(getStringPair(context, R.string.total_amount_processed, 0));
                responseInfo.add(getStringPair(context, R.string.payment_method, "N/A"));
            }
            responseInfoList.add(responseInfo);
        }
    }

    @Override
    public int getContentItemsTotal() {
        return totalItems;
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new SectionHeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        SectionHeaderViewHolder sectionHeaderViewHolder = (SectionHeaderViewHolder) holder;
        sectionHeaderViewHolder.title.setText(context.getString(R.string.transaction_index, index));
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new LabelValueViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        LabelValueViewHolder viewHolder = (LabelValueViewHolder) holder;
        Pair<String, String> labelValue;
        if (position < overviewInfo.size()) {
            labelValue = overviewInfo.get(position);
            setupViewForOverview(viewHolder);
        } else {
            int relativePos = position - overviewInfo.size();
            int responseWithHeader = NUM_TXN_RESPONSE_FIELDS + 1;
            int responseIndex = relativePos / responseWithHeader;
            int fieldIndex = relativePos % responseWithHeader;
            if (fieldIndex == 0) {
                setupViewForResponseHeader(viewHolder, responseIndex);
                labelValue = new Pair<>(context.getString(R.string.response_index, (responseIndex + 1)), "");
            } else {
                setupViewForResponseValues(viewHolder, responseIndex);
                labelValue = responseInfoList.get(responseIndex).get(fieldIndex - 1);
            }
        }
        viewHolder.label.setText(labelValue.first);
        viewHolder.value.setText(labelValue.second);
    }

    private void setupViewForOverview(LabelValueViewHolder viewHolder) {

    }

    private void setupViewForResponseHeader(LabelValueViewHolder viewHolder, int responseIndex) {
        int bg = responseIndex % 2 == 0 ? R.color.colorTxnResponseStripeEven : R.color.colorTxnResponseStripeOdd;
        viewHolder.itemView.setBackgroundColor(context.getResources().getColor(bg));
        viewHolder.value.setVisibility(View.INVISIBLE);
    }

    private void setupViewForResponseValues(LabelValueViewHolder viewHolder, int responseIndex) {
        int bg = responseIndex % 2 == 0 ? R.color.colorTxnResponseStripeEven : R.color.colorTxnResponseStripeOdd;
        viewHolder.itemView.setBackgroundColor(context.getResources().getColor(bg));
        if (viewHolder.label.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) viewHolder.label.getLayoutParams();
            p.setMargins(context.getResources().getDimensionPixelSize(R.dimen.txn_response_offset), 0, 0, 0);
            viewHolder.label.setLayoutParams(p);
        }
    }
}