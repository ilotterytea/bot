package kz.ilotterytea.bot.entities.listenables;

import jakarta.persistence.*;

/**
 * @author ilotterytea
 * @version 1.0
 */
@Entity
@Table(name = "listenable_icons")
public class ListenableIcons {
    @Id
    @OneToOne
    @JoinColumn(name = "listenable_id", unique = true, updatable = false, nullable = false)
    private Listenable listenable;

    @Column(name = "live_icon")
    private String liveIcon;

    @Column(name = "offline_icon")
    private String offlineIcon;

    @Column(name = "title_icon")
    private String titleIcon;

    @Column(name = "category_icon")
    private String categoryIcon;

    public ListenableIcons(Listenable listenable) {
        this.listenable = listenable;
    }

    public ListenableIcons() {}

    public Listenable getListenable() {
        return listenable;
    }

    public void setListenable(Listenable listenable) {
        this.listenable = listenable;
    }

    public String getLiveIcon() {
        return liveIcon;
    }

    public void setLiveIcon(String liveIcon) {
        this.liveIcon = liveIcon;
    }

    public String getOfflineIcon() {
        return offlineIcon;
    }

    public void setOfflineIcon(String offlineIcon) {
        this.offlineIcon = offlineIcon;
    }

    public String getTitleIcon() {
        return titleIcon;
    }

    public void setTitleIcon(String titleIcon) {
        this.titleIcon = titleIcon;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }
}
