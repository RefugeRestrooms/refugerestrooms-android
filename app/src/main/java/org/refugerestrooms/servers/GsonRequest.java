package org.refugerestrooms.servers;

/**
 * Created by Refuge Restrooms
 */
import java.io.UnsupportedEncodingException;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Volley GET request which parses JSON server response into Java object.
 */
public class GsonRequest<T> extends Request<T> {

    /** JSON parsing engine */
    protected final Gson gson;

    /** class of type of response */
    protected final Class<T> clazz;

    /** result listener */
    private final Listener<T> listener;

    public GsonRequest(String url, Class<T> clazz, Listener<T> listener,
                       ErrorListener errorListener) {
        super(Method.GET, url, errorListener);

        this.clazz = clazz;
        this.listener = listener;
        this.gson = new Gson();
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(
                    gson.fromJson(json, clazz), HttpHeaderParser.parseCacheHeaders(response));

        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }
}

