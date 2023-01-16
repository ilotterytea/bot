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
    C_CMD_SUCCESS_ENABLE("cmd.cmd.response.success.enable");

    private final String id;
    LineIds(String id) {
        this.id = id;
    }
    public String getId() { return id; }
}
