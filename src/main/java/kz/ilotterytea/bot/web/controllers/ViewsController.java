package kz.ilotterytea.bot.web.controllers;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.views.View;

/**
 * @author ilotterytea
 * @since 1.4
 */
@Controller("/")
public class ViewsController {

    @View("home")
    @Get("/")
    HttpResponse home() {
        return HttpResponse.ok();
    }
}
