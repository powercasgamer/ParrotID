package dev.mizule.squirrelid.core.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class HttpUtil {

    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    public static final JSONParser JSON_PARSER = new JSONParser();

    public static <T> HttpResponse<T> request(final Function<HttpRequest.Builder, HttpRequest> requestConsumer, final HttpResponse.BodyHandler<T> bodyHandler) throws IOException, InterruptedException {
        return HTTP_CLIENT.send(requestConsumer.apply(HttpRequest.newBuilder()), bodyHandler);
    }

    public static <T> CompletableFuture<HttpResponse<T>> requestAsync(final Function<HttpRequest.Builder, HttpRequest> requestConsumer, final HttpResponse.BodyHandler<T> bodyHandler) throws IOException, InterruptedException {
        return HTTP_CLIENT.sendAsync(requestConsumer.apply(HttpRequest.newBuilder()), bodyHandler);
    }

    public static JSONObject parseJson(final String json) {
        JSONObject jsonElement;
        try {
            jsonElement = (JSONObject) JSON_PARSER.parse(json);
        } catch (final Exception exception) {
            throw new RuntimeException(exception);
        }
        return jsonElement;
    }
}
