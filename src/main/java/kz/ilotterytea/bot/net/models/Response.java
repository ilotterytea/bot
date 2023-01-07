package kz.ilotterytea.bot.net.models;

/**
 * Response.
 * @author ilotterytea
 * @since 1.0
 */
public class Response {
    /** The status code of the Response. */
    private final int code;
    /** The method used in the Response. */
    private final String method;
    /** The Response result. */
    private final String response;

    public Response(
            int statusCode,
            String method,
            String response
    ) {
        this.code = statusCode;
        this.method = method;
        this.response = response;
    }

    public int getCode() {
        return code;
    }

    public String getMethod() {
        return method;
    }

    public String getResponse() {
        return response;
    }
}
