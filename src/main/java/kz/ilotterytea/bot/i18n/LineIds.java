package kz.ilotterytea.bot.i18n;

/**
 * Localization line IDs.
 * @author ilotterytea
 * @since 1.3
 */
public enum LineIds {
    // Messages
    MSG_TEST("test.test"),
    NOT_ENOUGH_ARGS("msg.not_enough_arguments"),
    NO_SUBCMD("msg.no_subcommand"),
    NO_RIGHTS("msg.not_enough_rights"),
    HTTP_ERROR("msg.http_response_error"),
    SOMETHING_WENT_WRONG("msg.something_went_wrong"),

    STV("providers.7tv"),
    BTTV("providers.bttv"),
    FFZ("providers.ffz"),
    TTV("providers.ttv"),

    DISCON("placeholders.disconnected"),
    CON("placeholders.connected"),
    TITLE_CHANGE("placeholders.title_change"),
    WENT_LIVE("placeholders.went_live"),
    WENT_OFFLINE("placeholders.went_offline"),
    GAME_CHANGE("placeholders.game_change"),

    TITLE_CHANGE_NOTIFICATION("notification.title"),
    WENT_LIVE_NOTIFICATION("notification.live"),
    WENT_OFFLINE_NOTIFICATION("notification.offline"),
    GAME_CHANGE_NOTIFICATION("notification.game"),

    NEW_EMOTE_WITH_AUTHOR("emoteupdater.new_emote_with_author"),
    REMOVED_EMOTE_WITH_AUTHOR("emoteupdater.removed_emote_with_author"),
    UPDATED_EMOTE_WITH_AUTHOR("emoteupdater.updated_emote_with_author"),
    NEW_EMOTE("emoteupdater.new_emote"),
    REMOVED_EMOTE("emoteupdater.removed_emote"),
    UPDATED_EMOTE("emoteupdater.updated_emote"),

    C_PING_SUCCESS("cmd.ping.response.success"),

    C_SPAM_NOMSG("cmd.spam.response.no_msg"),
    C_SPAM_NOCOUNT("cmd.spam.response.no_count"),

    C_JOIN_NOTFOUND("cmd.join.response.not_found"),
    C_JOIN_ALREADYIN("cmd.join.response.already_in"),
    C_JOIN_SUCCESS("cmd.join.response.success"),
    C_JOIN_SUCCESSCHAT("cmd.join.response.success_to_joined_chat"),

    C_HOLIDAY_SUCCESS("cmd.holiday.response.success"),

    C_ECOUNT_NOEMOTEPROVIDED("cmd.ecount.response.no_emote_provided"),
    C_ECOUNT_NOEMOTEFOUND("cmd.ecount.response.no_emote_found"),
    C_ECOUNT_NOCHANNELEMOTES("cmd.ecount.response.no_channel_emotes"),
    C_ECOUNT_SUCCESS("cmd.ecount.response.success"),

    C_ETOP_NOCHANNELEMOTES("cmd.etop.response.no_channel_emotes"),
    C_ETOP_SUCCESS("cmd.etop.response.success"),

    C_CMD_NOSECONDARG("cmd.cmd.response.no_second_arg"),
    C_CMD_ALREADYEXISTS("cmd.cmd.response.already_exists"),
    C_CMD_DOESNOTEXISTS("cmd.cmd.response.does_not_exists"),
    C_CMD_NOCMDS("cmd.cmd.response.no_cmds"),
    C_CMD_NOTFOUND("cmd.cmd.response.not_found"),
    C_CMD_SUCCESS_LIST("cmd.cmd.response.success.list"),
    C_CMD_SUCCESS_NEW("cmd.cmd.response.success.new"),
    C_CMD_SUCCESS_EDIT("cmd.cmd.response.success.edit"),
    C_CMD_SUCCESS_DELETE("cmd.cmd.response.success.delete"),
    C_CMD_SUCCESS_RENAME("cmd.cmd.response.success.rename"),
    C_CMD_SUCCESS_COPY("cmd.cmd.response.success.copy"),
    C_CMD_SUCCESS_DISABLE("cmd.cmd.response.success.disable"),
    C_CMD_SUCCESS_ENABLE("cmd.cmd.response.success.enable"),

    C_NOTIFY_NOTHINGCHANGED("cmd.notify.response.nothing_changed"),
    C_NOTIFY_NOLISTENINGCHANNELS("cmd.notify.response.no_listening_channels"),
    C_NOTIFY_NOUSERNAME("cmd.notify.response.no_username_provided"),
    C_NOTIFY_USERNOTFOUND("cmd.notify.response.user_not_found"),
    C_NOTIFY_NOEVENTSPROVIDED("cmd.notify.response.no_events_provided"),
    C_NOTIFY_ALREADYSUB("cmd.notify.response.already_subscribed"),
    C_NOTIFY_NOTSUB("cmd.notify.response.not_subscribed"),
    C_NOTIFY_EXCEEDEDLIMIT("cmd.notify.response.exceeded_limit"),
    C_NOTIFY_ALREADYLISTENING("cmd.notify.response.already_listening"),
    C_NOTIFY_NOMSG("cmd.notify.response.no_message"),
    C_NOTIFY_NOFLAG("cmd.notify.response.no_flags"),
    C_NOTIFY_DOESNOTLISTENING("cmd.notify.response.does_not_listening"),
    C_NOTIFY_SUCCESS_SUBS("cmd.notify.response.success.subs"),
    C_NOTIFY_SUCCESS_SUBSNOONE("cmd.notify.response.success.subs_no_one"),
    C_NOTIFY_SUCCESS_LIST("cmd.notify.response.success.list"),
    C_NOTIFY_SUCCESS_SUB("cmd.notify.response.success.sub"),
    C_NOTIFY_SUCCESS_UNSUB("cmd.notify.response.success.unsub"),
    C_NOTIFY_SUCCESS_UNSUBFULL("cmd.notify.response.success.unsub_full"),
    C_NOTIFY_SUCCESS_ON("cmd.notify.response.success.on"),
    C_NOTIFY_SUCCESS_OFF("cmd.notify.response.success.off"),
    C_NOTIFY_SUCCESS_OFFFULL("cmd.notify.response.success.off_full"),
    C_NOTIFY_SUCCESS_COMMENT_UPDATED("cmd.notify.response.success.comment.updated"),
    C_NOTIFY_SUCCESS_COMMENT_UPDATEDALL("cmd.notify.response.success.comment.updated_all"),
    C_NOTIFY_SUCCESS_COMMENT_REMOVED("cmd.notify.response.success.comment.removed"),
    C_NOTIFY_SUCCESS_COMMENT_REMOVEDALL("cmd.notify.response.success.comment.removed_all"),
    C_NOTIFY_SUCCESS_ICON_UPDATED("cmd.notify.response.success.icon.updated"),
    C_NOTIFY_SUCCESS_ICON_UPDATEDALL("cmd.notify.response.success.icon.updated_all"),
    C_NOTIFY_SUCCESS_ICON_REMOVED("cmd.notify.response.success.icon.removed"),
    C_NOTIFY_SUCCESS_ICON_REMOVEDALL("cmd.notify.response.success.icon.removed_all"),
    C_NOTIFY_SUCCESS_FLAG("cmd.notify.response.success.flag"),
    C_NOTIFY_SUCCESS_FLAGALL("cmd.notify.response.success.flag_all"),
    C_NOTIFY_SUCCESS_UNFLAG("cmd.notify.response.success.unflag"),
    C_NOTIFY_SUCCESS_UNFLAGALL("cmd.notify.response.success.unflag_all"),

    C_SET_SUCCESS_PREFIX_INFO("cmd.set.response.success.prefix.info"),
    C_SET_SUCCESS_PREFIX_SET("cmd.set.response.success.prefix.set"),
    C_SET_SUCCESS_LOCALE_INFO("cmd.set.response.success.locale.info"),
    C_SET_SUCCESS_LOCALE_LIST("cmd.set.response.success.locale.list"),
    C_SET_SUCCESS_LOCALE_SET("cmd.set.response.success.locale.set"),
    C_SET_SUCCESS_LOCALE_SET_USER("cmd.set.response.success.locale.set.user");

    private final String id;
    LineIds(String id) {
        this.id = id;
    }
    public String getId() { return id; }
}
