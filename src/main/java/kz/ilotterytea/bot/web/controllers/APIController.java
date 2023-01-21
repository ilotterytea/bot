package kz.ilotterytea.bot.web.controllers;

import com.google.gson.Gson;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import kz.ilotterytea.bot.Huinyabot;
import kz.ilotterytea.bot.SharedConstants;
import kz.ilotterytea.bot.i18n.LineIds;
import kz.ilotterytea.bot.models.TargetModel;
import kz.ilotterytea.bot.models.UserModel;
import kz.ilotterytea.bot.utils.HashUtils;
import kz.ilotterytea.bot.web.models.api.v1.GitHubPushEvent;
import kz.ilotterytea.bot.web.models.api.v1.Payload;
import kz.ilotterytea.bot.web.models.api.v1.github.Commit;

import java.util.Objects;

/**
 * @author ilotterytea
 * @since 1.4
 */
@Controller("/api/v1/")
public class APIController {
    @Get(value = "/target/{id}", produces = MediaType.APPLICATION_JSON)
    HttpResponse<Payload<TargetModel>> getTarget(@Parameter String id) {
        if (Huinyabot.getInstance() == null) {
            return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new Payload<>(
                            500,
                            "Huinyabot not initialized!",
                            null
                    )
            );
        }

        TargetModel target = Huinyabot.getInstance().getTargetCtrl()
                .getAll()
                .values()
                .stream().filter(t -> Objects.equals(t.getAliasId(), id))
                .findFirst()
                .orElse(null);


        if (target == null) {
            return HttpResponse.notFound(new Payload<>(
                    404,
                    "Target ID " + id + " not found!",
                    null
            ));
        }

        return HttpResponse.ok().body(new Payload<>(
                200,
                "Success!",
                target
        ));
    }

    @Get(value = "/user/{id}", produces = MediaType.APPLICATION_JSON)
    HttpResponse<Payload<UserModel>> getUser(@Parameter String id) {
        if (Huinyabot.getInstance() == null) {
            return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new Payload<>(
                            500,
                            "Huinyabot not initialized!",
                            null
                    )
            );
        }

        UserModel user = Huinyabot.getInstance().getUserCtrl()
                .getAll()
                .values()
                .stream().filter(t -> Objects.equals(t.getAliasId(), id))
                .findFirst()
                .orElse(null);


        if (user == null) {
            return HttpResponse.notFound(new Payload<>(
                    404,
                    "User ID " + id + " not found!",
                    null
            ));
        }

        return HttpResponse.ok().body(new Payload<>(
                200,
                "Success!",
                user
        ));
    }

    @Post(value = "/github/webhook", consumes = MediaType.APPLICATION_JSON)
    HttpResponse<Payload> getGitHubPushEvent(HttpRequest<String> request) {
        if (
                Huinyabot.getInstance() == null ||
                        Huinyabot.getInstance().getClient() == null ||
                        Huinyabot.getInstance().getClient().getChat() == null ||
                        Huinyabot.getInstance().getProperties().getOrDefault("GITHUB_WEBHOOK_SECRET_KEY", null) == null ||
                        Huinyabot.getInstance().getProperties().getOrDefault("TWITCH_ANNOUNCE_COMMIT_CHANNEL_NAME", null) == null ||
                        Huinyabot.getInstance().getLocale() == null
        ) {
            return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new Payload<>(
                            500,
                            "Something went wrong!",
                            null
                    )
            );
        }

        if (!request.getBody().isPresent() || !request.getHeaders().contains("x-github-event") || !request.getHeaders().contains("x-hub-signature-256")) {
            return HttpResponse.status(HttpStatus.BAD_REQUEST).body(
                    new Payload<>(
                            400,
                            "No body, GitHub event header or signature header.",
                            null
                    )
            );
        }

        final String EVENT = request.getHeaders().get("x-github-event");

        if (!Objects.equals(EVENT, "push")) {
            return HttpResponse.status(HttpStatus.BAD_REQUEST).body(
                    new Payload<>(
                            400,
                            "The event is not supported.",
                            null
                    )
            );
        }

        final String BODY = request.getBody().get();
        final String REQHASH = request.getHeaders().get("x-hub-signature-256");
        final String HASH = "sha256=" + HashUtils.generateHmac256(
                Huinyabot.getInstance().getProperties().getProperty("GITHUB_WEBHOOK_SECRET_KEY"),
                BODY
        );

        if (!Objects.equals(REQHASH, HASH)) {
            return HttpResponse.status(HttpStatus.UNAUTHORIZED).body(
                    new Payload<>(
                            401,
                            "The keys do not match!",
                            null
                    )
            );
        }

        GitHubPushEvent event = new Gson().fromJson(BODY, GitHubPushEvent.class);
        TargetModel target = Huinyabot.getInstance().getTargetCtrl().getOrDefault(
                Huinyabot.getInstance().getTargetLinks().getOrDefault(
                        Huinyabot.getInstance().getProperties().getProperty("TWITCH_ANNOUNCE_COMMIT_CHANNEL_NAME"),
                        ""
                )
        );

        for (Commit commit : event.getCommits()) {
            Huinyabot.getInstance().getClient().getChat().sendActionMessage(
                    Huinyabot.getInstance().getProperties().getProperty("TWITCH_ANNOUNCE_COMMIT_CHANNEL_NAME"),
                    Huinyabot.getInstance().getLocale().formattedText(
                            (target.getLanguage() == null) ? SharedConstants.DEFAULT_LOCALE_ID : target.getLanguage(),
                            LineIds.GITHUB_PUSH,
                            event.getSender().getLogin(),
                            event.getRepository().getFullName(),
                            event.getAfter().substring(0, 7),
                            commit.getMessage(),
                            (commit.getAdded().size() > 0) ? "+" + commit.getAdded().size() + " " : "",
                            (commit.getRemoved().size() > 0) ? "-" + commit.getRemoved().size() + " " : "",
                            (commit.getModified().size() > 0) ? "*" + commit.getModified().size() + " " : ""
                    )
            );
        }

        return HttpResponse.ok(new Payload<>(
                200,
                "Payload was successfully parsed and resolved!",
                null
        ));
    }
}
