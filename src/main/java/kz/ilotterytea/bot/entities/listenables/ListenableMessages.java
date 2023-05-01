package kz.ilotterytea.bot.entities.listenables;

import jakarta.persistence.*;

/**
 * Listenable's messages.
 * @author ilotterytea
 * @version 1.4
 */
@Entity
@Table(name = "listenable_messages")
public class ListenableMessages {
    @Id
    @OneToOne
    @JoinColumn(name = "listenable_id", unique = true, updatable = false, nullable = false)
    private Listenable listenable;

    @Column(name = "live_message")
    private String liveMessage;

    @Column(name = "offline_message")
    private String offlineMessage;

    @Column(name = "title_message")
    private String titleMessage;

    @Column(name = "category_message")
    private String categoryMessage;

    public ListenableMessages(Listenable listenable) {
        this.listenable = listenable;
    }

    public ListenableMessages() {}

    public Listenable getListenable() {
        return listenable;
    }

    public void setListenable(Listenable listenable) {
        this.listenable = listenable;
    }

    public String getLiveMessage() {
        return liveMessage;
    }

    public void setLiveMessage(String liveMessage) {
        this.liveMessage = liveMessage;
    }

    public String getOfflineMessage() {
        return offlineMessage;
    }

    public void setOfflineMessage(String offlineMessage) {
        this.offlineMessage = offlineMessage;
    }

    public String getTitleMessage() {
        return titleMessage;
    }

    public void setTitleMessage(String titleMessage) {
        this.titleMessage = titleMessage;
    }

    public String getCategoryMessage() {
        return categoryMessage;
    }

    public void setCategoryMessage(String categoryMessage) {
        this.categoryMessage = categoryMessage;
    }
}
