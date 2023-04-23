package kz.ilotterytea.bot.thirdpartythings.seventv.eventapi.schemas;

import com.google.gson.annotations.SerializedName;

/**
 * @author ilotterytea
 * @since 1.0
 */
public class ResumeData {
    private Boolean success;
    @SerializedName("dispatchesReplayed")
    private Integer dispatchesReplayed;
    @SerializedName("subscriptionsRestored")
    private Integer subscriptionsRestored;

    public ResumeData() {}

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getDispatchesReplayed() {
        return dispatchesReplayed;
    }

    public void setDispatchesReplayed(Integer dispatchesReplayed) {
        this.dispatchesReplayed = dispatchesReplayed;
    }

    public Integer getSubscriptionsRestored() {
        return subscriptionsRestored;
    }

    public void setSubscriptionsRestored(Integer subscriptionsRestored) {
        this.subscriptionsRestored = subscriptionsRestored;
    }
}