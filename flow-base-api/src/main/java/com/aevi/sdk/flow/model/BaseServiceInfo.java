package com.aevi.sdk.flow.model;


import java.util.Arrays;

import io.reactivex.annotations.NonNull;

import static com.aevi.sdk.flow.util.Preconditions.checkArgument;

/**
 * Common flags for service info models.
 */
public abstract class BaseServiceInfo extends BaseModel {

    private final String vendor;
    private final String version;
    private final String displayName;
    private final boolean hasAccessibilityMode;
    private final String[] paymentMethods;
    private final String[] supportedCurrencies;
    private final String[] supportedTransactionTypes;
    private final String[] supportedDataKeys;

    protected BaseServiceInfo(String id, String vendor, String version, String displayName, boolean hasAccessibilityMode,
                              String[] paymentMethods, String[] supportedCurrencies, String[] supportedTransactionTypes, String[] supportedDataKeys) {
        super(id);
        this.vendor = vendor;
        this.version = version;
        this.displayName = displayName;
        this.hasAccessibilityMode = hasAccessibilityMode;
        this.paymentMethods = paymentMethods != null ? paymentMethods : new String[0];
        this.supportedCurrencies = supportedCurrencies != null ? supportedCurrencies : new String[0];
        this.supportedTransactionTypes = supportedTransactionTypes != null ? supportedTransactionTypes : new String[0];
        this.supportedDataKeys = supportedDataKeys != null ? supportedDataKeys : new String[0];
        checkArguments();
    }

    private void checkArguments() {
        checkArgument(vendor != null, "Vendor must be set");
        checkArgument(version != null, "Version must be set");
        checkArgument(displayName != null, "Display name must be set");
    }

    /**
     * Gets the service vendor name.
     *
     * @return The service vendor name
     */
    @NonNull
    public String getVendor() {
        return vendor;
    }

    /**
     * Gets the service version.
     *
     * @return The service version string
     */
    @NonNull
    public String getVersion() {
        return version;
    }

    /**
     * Get the name of this service for displaying to users.
     *
     * @return The display name of this service
     */
    @NonNull
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns whether or not this service supports an accessible mode.
     *
     * @return true if the service has an accessible mode.
     */
    public boolean supportsAccessibilityMode() {
        return hasAccessibilityMode;
    }

    /**
     * Gets an array of payment methods supported by the service.
     *
     * See reference values in the documentation for possible values.
     *
     * May be empty.
     *
     * @return An array of supported payment methods
     */
    @NonNull
    public String[] getPaymentMethods() {
        return paymentMethods;
    }

    /**
     * Gets an array of currency codes supported by the service.
     *
     * May be empty.
     *
     * @return An array of String objects indicating the 3-letter ISO 4217 currencies supported by the service.
     */
    @NonNull
    public String[] getSupportedCurrencies() {
        return supportedCurrencies;
    }

    /**
     * Gets an array of transaction types supported by the service.
     *
     * May be empty.
     *
     * See reference values in the documentation for possible values.
     *
     * @return array of transaction types supported by the service.
     */
    @NonNull
    public String[] getSupportedTransactionTypes() {
        return supportedTransactionTypes;
    }

    /**
     * Returns an array of supported request {@link com.aevi.sdk.flow.model.AdditionalData} keys.
     *
     * A request can set various optional and custom flags in the {@link com.aevi.sdk.flow.model.AdditionalData} object.
     * This array will return an array of the keys this service supports.
     *
     * May be empty.
     *
     * See reference values in the documentation for possible values.
     *
     * @return An array of supported AdditionalData keys
     */
    @NonNull
    public String[] getSupportedDataKeys() {
        return supportedDataKeys;
    }

    @Override
    public String toString() {
        return "BaseServiceInfo{" +
                "vendor='" + vendor + '\'' +
                ", version='" + version + '\'' +
                ", displayName='" + displayName + '\'' +
                ", hasAccessibilityMode=" + hasAccessibilityMode +
                ", paymentMethods=" + Arrays.toString(paymentMethods) +
                ", supportedCurrencies=" + Arrays.toString(supportedCurrencies) +
                ", supportedTransactionTypes=" + Arrays.toString(supportedTransactionTypes) +
                ", supportedDataKeys=" + Arrays.toString(supportedDataKeys) +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BaseServiceInfo that = (BaseServiceInfo) o;

        if (hasAccessibilityMode != that.hasAccessibilityMode) return false;
        if (vendor != null ? !vendor.equals(that.vendor) : that.vendor != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        if (displayName != null ? !displayName.equals(that.displayName) : that.displayName != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(paymentMethods, that.paymentMethods)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(supportedCurrencies, that.supportedCurrencies)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(supportedTransactionTypes, that.supportedTransactionTypes)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(supportedDataKeys, that.supportedDataKeys);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (vendor != null ? vendor.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (hasAccessibilityMode ? 1 : 0);
        result = 31 * result + Arrays.hashCode(paymentMethods);
        result = 31 * result + Arrays.hashCode(supportedCurrencies);
        result = 31 * result + Arrays.hashCode(supportedTransactionTypes);
        result = 31 * result + Arrays.hashCode(supportedDataKeys);
        return result;
    }
}