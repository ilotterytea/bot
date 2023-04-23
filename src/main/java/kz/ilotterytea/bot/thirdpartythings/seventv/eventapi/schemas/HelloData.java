package kz.ilotterytea.bot.thirdpartythings.seventv.eventapi.schemas;

import com.google.gson.annotations.SerializedName;

/**
 * @author ilotterytea
 * @since 1.0
 */
public class HelloData {
    @SerializedName("heartbeat_interval")
    private Integer heartbeatInterval;
    @SerializedName("session_id")
    private String sessionId;
    @SerializedName("subscription_limit")
    private Integer subscriptionLimit;

    public HelloData() {}

    public Integer getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(Integer heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getSubscriptionLimit() {
        return subscriptionLimit;
    }

    public void setSubscriptionLimit(Integer subscriptionLimit) {
        this.subscriptionLimit = subscriptionLimit;
    }
}
