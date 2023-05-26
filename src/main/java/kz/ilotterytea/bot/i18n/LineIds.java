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
    UNKNOWN_SUBCOMMAND("msg.unknown_subcommand"),
    NO_MESSAGE("msg.no_message"),
    NO_EMOTE_SET("msg.no_emote_set"),
    NO_TWITCH_USER("msg.no_twitch_user"),
    SAME_TWITCH_USER("msg.same_twitch_user"),

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

    EVENTS_MESSAGE("events.message"),
    EVENTS_MESSAGE_SUFFIX("events.message.suffix"),

    NEW_EMOTE_WITH_AUTHOR("emoteupdater.new_emote_with_author"),
    REMOVED_EMOTE_WITH_AUTHOR("emoteupdater.removed_emote_with_author"),
    UPDATED_EMOTE_WITH_AUTHOR("emoteupdater.updated_emote_with_author"),
    NEW_EMOTE("emoteupdater.new_emote"),
    REMOVED_EMOTE("emoteupdater.removed_emote"),
    UPDATED_EMOTE("emoteupdater.updated_emote"),

    C_PING_SUCCESS("cmd.ping.response.success"),

    C_MASSPING_NOTMOD("cmd.massping.response.not_a_moderator"),

    C_SPAM_NOMSG("cmd.spam.response.no_msg"),
    C_SPAM_NOCOUNT("cmd.spam.response.no_count"),

    C_JOIN_NOTFOUND("cmd.join.response.not_found"),
    C_JOIN_ALREADYIN("cmd.join.response.already_in"),
    C_JOIN_SUCCESS("cmd.join.response.success"),
    C_JOIN_SUCCESSCHAT("cmd.join.response.success_to_joined_chat"),

    C_HOLIDAY_SUCCESS("cmd.holiday.response.success"),
    C_HOLIDAY_NOHOLIDAYS("cmd.holiday.response.no_holidays"),
    C_HOLIDAY_NOSEARCHQUERY("cmd.holiday.response.no_search_query"),
    C_HOLIDAY_QUERYSUCCESS("cmd.holiday.response.query_success"),
    C_HOLIDAY_QUERYNOTFOUND("cmd.holiday.response.query_not_found"),

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

    C_NOTIFY_SUBALREADY("cmd.notify.response.sub.already"),
    C_NOTIFY_SUB("cmd.notify.response.sub"),
    C_NOTIFY_UNSUB("cmd.notify.response.unsub"),
    C_NOTIFY_SUBS("cmd.notify.response.subs"),
    C_NOTIFY_LIST("cmd.notify.response.list"),
    C_NOTIFY_NOSUBS("cmd.notify.response.no_subs"),
    C_NOTIFY_NOEVENTS("cmd.notify.response.no_events"),
    C_NOTIFY_NOTEXISTS("cmd.notify.response.not_exists"),
    C_NOTIFY_NOTSUBBED("cmd.notify.response.not_subbed"),
    C_NOTIFY_NOTAVAILABLE("cmd.notify.response.not_available"),

    C_SET_SUCCESS_PREFIX_INFO("cmd.set.response.success.prefix.info"),
    C_SET_SUCCESS_PREFIX_SET("cmd.set.response.success.prefix.set"),
    C_SET_SUCCESS_LOCALE_INFO("cmd.set.response.success.locale.info"),
    C_SET_SUCCESS_LOCALE_LIST("cmd.set.response.success.locale.list"),
    C_SET_SUCCESS_LOCALE_SET("cmd.set.response.success.locale.set"),
    C_SET_SUCCESS_LOCALE_SET_USER("cmd.set.response.success.locale.set.user"),
    C_SET_SUCCESS_NOTIFY7TV_ENABLED("cmd.set.response.success.7tv.enabled"),
    C_SET_SUCCESS_NOTIFY7TV_DISABLED("cmd.set.response.success.7tv.disabled"),

    C_ESIMILARITY_SUCCESS("cmd.esimilarity.response.success"),
    C_ESIMILARITY_NOSIMILARITY("cmd.esimilarity.response.no_similarity"),

    C_MCSERVER_SUCCESS("cmd.mcserver.response.success"),
    C_MCSERVER_SERVERISOFFLINE("cmd.mcserver.response.server_is_offline"),

    C_TIMER_LIST("cmd.timer.response.list"),
    C_TIMER_ALREADYEXISTS("cmd.timer.response.already_exists"),
    C_TIMER_NOTEXISTS("cmd.timer.response.not_exists"),
    C_TIMER_NEW("cmd.timer.response.new"),
    C_TIMER_DELETE("cmd.timer.response.delete"),
    C_TIMER_INFO("cmd.timer.response.info"),
    C_TIMER_MESSAGE("cmd.timer.response.message"),
    C_TIMER_INTERVAL("cmd.timer.response.interval"),
    C_TIMER_NOTANINTERVAL("cmd.timer.response.not_an_interval"),

    C_EVENT_NOEVENTS("cmd.event.response.no_events"),
    C_EVENT_LIST("cmd.event.response.list"),
    C_EVENT_ONCUSTOM("cmd.event.response.on.custom"),
    C_EVENT_ON("cmd.event.response.on"),
    C_EVENT_ONUPDATE("cmd.event.response.on.update"),
    C_EVENT_OFF("cmd.event.response.off"),
    C_EVENT_FLAG("cmd.event.response.flag"),
    C_EVENT_UNFLAG("cmd.event.response.flag"),
    C_EVENT_FLAGNOTEXISTS("cmd.event.response.flag_not_exists"),
    C_EVENT_NOTEXISTS("cmd.event.response.not_exists");

    private final String id;
    LineIds(String id) {
        this.id = id;
    }
    public String getId() { return id; }
}
