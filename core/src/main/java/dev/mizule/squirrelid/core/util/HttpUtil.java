/*
 * SquirrelID, a UUID library for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) SquirrelID team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dev.mizule.squirrelid.core.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class HttpUtil {

    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    public static final JsonParser JSON_PARSER = new JsonParser();

    public static <T> HttpResponse<T> request(final Function<HttpRequest.Builder, HttpRequest> requestConsumer, final HttpResponse.BodyHandler<T> bodyHandler) throws IOException, InterruptedException {
        return HTTP_CLIENT.send(requestConsumer.apply(HttpRequest.newBuilder()), bodyHandler);
    }

    public static <T> CompletableFuture<HttpResponse<T>> requestAsync(final Function<HttpRequest.Builder, HttpRequest> requestConsumer, final HttpResponse.BodyHandler<T> bodyHandler) throws IOException, InterruptedException {
        return HTTP_CLIENT.sendAsync(requestConsumer.apply(HttpRequest.newBuilder()), bodyHandler);
    }

    public static JsonObject parseJson(final String json) {
        JsonElement jsonElement;
        try {
            jsonElement = JSON_PARSER.parse(json);
        } catch (final Exception exception) {
            throw new RuntimeException(exception);
        }
        return jsonElement.isJsonObject() ? jsonElement.getAsJsonObject() : null;
    }
}
