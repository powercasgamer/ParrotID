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
package org.enginehub.squirrelid.util;

import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A simple fluent interface for performing HTTP requests that uses
 * {@link java.net.HttpURLConnection} or {@link javax.net.ssl.HttpsURLConnection}.
 */
public class HttpRequests implements Closeable {

    private static final Logger log = Logger.getLogger(HttpRequests.class.getCanonicalName());
    private static final int READ_TIMEOUT = 1000 * 60 * 10;
    private static final int READ_BUFFER_SIZE = 1024 * 8;

    private final Map<String, String> headers = new HashMap<>();
    private final String method;
    private final URL url;
    private String contentType;
    private byte[] body;
    private HttpURLConnection conn;
    private InputStream inputStream;

    /**
     * Create a new HTTP request.
     *
     * @param method the method
     * @param url the URL
     */
    private HttpRequests(String method, URL url) {
        this.method = method;
        this.url = url;
    }

    /**
     * Perform a GET request.
     *
     * @param url the URL
     * @return a new request object
     */
    public static HttpRequests get(URL url) {
        return request("GET", url);
    }

    /**
     * Perform a POST request.
     *
     * @param url the URL
     * @return a new request object
     */
    public static HttpRequests post(URL url) {
        return request("POST", url);
    }

    /**
     * Perform a request.
     *
     * @param method the method
     * @param url the URL
     * @return a new request object
     */
    public static HttpRequests request(String method, URL url) {
        return new HttpRequests(method, url);
    }

    /**
     * Create a new {@link java.net.URL} and throw a {@link RuntimeException} if the URL
     * is not valid.
     *
     * @param url the url
     * @return a URL object
     * @throws RuntimeException if the URL is invalid
     */
    public static URL url(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a new {@link java.net.URI} and throw a {@link RuntimeException} if the URI
     * is not valid.
     *
     * @param uri the uri
     * @return a URI object
     * @throws RuntimeException if the URI is invalid
     */
    public static URI uri(String uri) {
        try {
            return URI.create(uri);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * URL may contain spaces and other nasties that will cause a failure.
     *
     * @param existing the existing URL to transform
     * @return the new URL, or old one if there was a failure
     */
    private static URL reformat(URL existing) {
        try {
            URL url = new URL(existing.toString());
            URI uri = new URI(
                    url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(),
                    url.getPath(), url.getQuery(), url.getRef());
            url = uri.toURL();
            return url;
        } catch (MalformedURLException | URISyntaxException e) {
            return existing;
        }
    }

    private static void checkInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * Set the content body to a JSON object with the content type of "application/json".
     *
     * @param object the object to serialize as JSON
     * @return this object
     * @throws java.io.IOException if the object can't be mapped
     */
    public HttpRequests bodyJson(Object object) throws IOException {
        contentType = "application/json";
        body = object.toString().getBytes();
        return this;
    }

    /**
     * Submit form data.
     *
     * @param form the form
     * @return this object
     */
    public HttpRequests bodyForm(Form form) {
        contentType = "application/x-www-form-urlencoded";
        body = form.toString().getBytes();
        return this;
    }

    /**
     * Add a header.
     *
     * @param key the header key
     * @param value the header value
     * @return this object
     */
    public HttpRequests header(String key, String value) {
        headers.put(key, value);
        return this;
    }

    /**
     * Execute the request.
     *
     * <p>
     * After execution, {@link #close()} should be called.
     * </p>
     *
     * @return this object
     * @throws java.io.IOException on I/O error
     */
    public HttpRequests execute() throws IOException {
        boolean successful = false;

        try {
            if (conn != null) {
                throw new IllegalArgumentException("Connection already executed");
            }

            conn = (HttpURLConnection) reformat(url).openConnection();

            if (body != null) {
                conn.setRequestProperty("Content-Type", contentType);
                conn.setRequestProperty("Content-Length", Integer.toString(body.length));
                conn.setDoInput(true);
            }

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }

            conn.setRequestMethod(method);
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setReadTimeout(READ_TIMEOUT);

            conn.connect();

            if (body != null) {
                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                out.write(body);
                out.flush();
                out.close();
            }

            inputStream = conn.getResponseCode() == HttpURLConnection.HTTP_OK
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            successful = true;
        } finally {
            if (!successful) {
                close();
            }
        }

        return this;
    }

    /**
     * Require that the response code is one of the given response codes.
     *
     * @param codes a list of codes
     * @return this object
     * @throws java.io.IOException if there is an I/O error or the response code is not expected
     */
    public HttpRequests expectResponseCode(int... codes) throws IOException {
        int responseCode = getResponseCode();

        for (int code : codes) {
            if (code == responseCode) {
                return this;
            }
        }

        close();
        throw new IOException("Did not get expected response code, got " + responseCode + " for " + url);
    }

    /**
     * Get the response code.
     *
     * @return the response code
     * @throws java.io.IOException on I/O error
     */
    public int getResponseCode() throws IOException {
        if (conn == null) {
            throw new IllegalArgumentException("No connection has been made");
        }

        return conn.getResponseCode();
    }

    /**
     * Get the input stream.
     *
     * @return the input stream
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Buffer the returned response.
     *
     * @return the buffered response
     * @throws java.io.IOException on I/O error
     * @throws InterruptedException on interruption
     */
    public BufferedResponse returnContent() throws IOException, InterruptedException {
        if (inputStream == null) {
            throw new IllegalArgumentException("No input stream available");
        }

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int b;
            while ((b = inputStream.read()) != -1) {
                checkInterrupted();
                bos.write(b);
            }
            return new BufferedResponse(bos.toByteArray());
        } finally {
            close();
        }
    }

    /**
     * Save the result to a file.
     *
     * @param file the file
     * @return this object
     * @throws java.io.IOException on I/O error
     * @throws InterruptedException on interruption
     */
    public HttpRequests saveContent(File file) throws IOException, InterruptedException {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);

            saveContent(bos);
        } finally {
            closeQuietly(bos);
            closeQuietly(fos);
        }

        return this;
    }

    /**
     * Save the result to an output stream.
     *
     * @param out the output stream
     * @return this object
     * @throws java.io.IOException on I/O error
     * @throws InterruptedException on interruption
     */
    public HttpRequests saveContent(OutputStream out) throws IOException, InterruptedException {
        BufferedInputStream bis;

        try {
            bis = new BufferedInputStream(inputStream);

            byte[] data = new byte[READ_BUFFER_SIZE];
            int len;
            while ((len = bis.read(data, 0, READ_BUFFER_SIZE)) >= 0) {
                out.write(data, 0, len);
                checkInterrupted();
            }
        } finally {
            close();
        }

        return this;
    }

    @Override
    public void close() throws IOException {
        if (conn != null) {
            conn.disconnect();
        }
    }

    /**
     * Used with {@link #bodyForm(Form)}.
     */
    public static final class Form {
        public final List<String> elements = new ArrayList<>();

        private Form() {
        }

        /**
         * Create a new form.
         *
         * @return a new form
         */
        public static Form form() {
            return new Form();
        }

        /**
         * Add a key/value to the form.
         *
         * @param key the key
         * @param value the value
         * @return this object
         */
        public Form add(String key, String value) {
            elements.add(URLEncoder.encode(key, StandardCharsets.UTF_8)
                    + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8));
            return this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (String element : elements) {
                if (first) {
                    first = false;
                } else {
                    builder.append("&");
                }
                builder.append(element);
            }
            return builder.toString();
        }
    }

    /**
     * Used to buffer the response in memory.
     */
    public static class BufferedResponse {
        private final byte[] data;

        private BufferedResponse(byte[] data) {
            this.data = data;
        }

        /**
         * Return the result as bytes.
         *
         * @return the data
         */
        public byte[] asBytes() {
            return data;
        }

        /**
         * Return the result as a string.
         *
         * @param encoding the encoding
         * @return the string
         * @throws java.io.IOException on I/O error
         */
        public String asString(String encoding) throws IOException {
            return new String(data, encoding);
        }

        /**
         * Return the result as an instance of the given class that has been
         * deserialized from a JSON payload.
         *
         * @return the object
         * @throws java.io.IOException on I/O error
         */
        public Object asJson() throws IOException {
            return JsonParser.parseString(asString("UTF-8"));
        }

        /**
         * Save the result to a file.
         *
         * @param file the file
         * @return this object
         * @throws java.io.IOException on I/O error
         * @throws InterruptedException on interruption
         */
        public BufferedResponse saveContent(File file) throws IOException, InterruptedException {
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;

            file.getParentFile().mkdirs();

            try {
                fos = new FileOutputStream(file);
                bos = new BufferedOutputStream(fos);

                saveContent(bos);
            } finally {
                closeQuietly(bos);
                closeQuietly(fos);
            }

            return this;
        }

        /**
         * Save the result to an output stream.
         *
         * @param out the output stream
         * @return this object
         * @throws java.io.IOException on I/O error
         * @throws InterruptedException on interruption
         */
        public BufferedResponse saveContent(OutputStream out) throws IOException, InterruptedException {
            out.write(data);

            return this;
        }
    }

}
