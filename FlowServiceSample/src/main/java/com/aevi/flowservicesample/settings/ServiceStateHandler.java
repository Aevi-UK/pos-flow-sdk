package com.aevi.flowservicesample.settings;


import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.aevi.flowservicesample.FlowServiceInfoProvider;
import com.aevi.flowservicesample.R;
import com.aevi.flowservicesample.service.*;
import com.aevi.sdk.pos.flow.model.PaymentStage;

import static android.content.pm.PackageManager.*;

public class ServiceStateHandler {

    public static void enableDisableService(Context context, String preferenceKey, boolean enable) {
        PackageManager packageManager = context.getPackageManager();
        int enableDisableFlag = enable ? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED;
        packageManager.setComponentEnabledSetting(getComponentFromPreferenceKey(context, preferenceKey), enableDisableFlag, DONT_KILL_APP);

        FlowServiceInfoProvider.notifyServiceInfoChange(context);
    }

    public static boolean isStageEnabled(Context context, PaymentStage paymentStage) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        switch (paymentStage) {
            case PRE_FLOW:
                return sharedPreferences.getBoolean(context.getString(R.string.pref_preflow), false);
            case SPLIT:
                return sharedPreferences.getBoolean(context.getString(R.string.pref_split), false);
            case PRE_TRANSACTION:
            case POST_CARD_READING:
                return sharedPreferences.getBoolean(context.getString(R.string.pref_prepayment), false);
            case POST_TRANSACTION:
                return sharedPreferences.getBoolean(context.getString(R.string.pref_postpayment), false);
            case POST_FLOW:
                return sharedPreferences.getBoolean(context.getString(R.string.pref_postflow), false);
            default:
                return false;
        }
    }

    private static ComponentName getComponentFromPreferenceKey(Context context, String preferenceKey) {

        String service = null;
        if (preferenceKey.equals(context.getString(R.string.pref_preflow))) {
            service = PreFlowService.class.getName();
        } else if (preferenceKey.equals(context.getString(R.string.pref_split))) {
            service = SplitService.class.getName();
        } else if (preferenceKey.equals(context.getString(R.string.pref_prepayment))) {
            service = PrePaymentService.class.getName();
        } else if (preferenceKey.equals(context.getString(R.string.pref_postpayment))) {
            service = PostPaymentService.class.getName();
        } else if (preferenceKey.equals(context.getString(R.string.pref_postflow))) {
            service = PostFlowService.class.getName();
        }

        return new ComponentName(context.getPackageName(), service);
    }
}
