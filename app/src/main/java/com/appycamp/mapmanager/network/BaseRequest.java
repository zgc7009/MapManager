package com.appycamp.mapmanager.network;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A Volley BaseRequest that can handle most request types.
 *
 * @param <T> The type of the response, which is determined by the
 *            ResponseParseStrategy<T>
 */
public class BaseRequest<T> extends Request<T> {

    public static interface ResponseParseStrategy<T> {
        T parseXml(String rawResponse);
    }

    /** A Gson Object for use in all nested parse strategies */
    private static Gson GSON;

    /** Various Parse Strategies */
    public static final ResponseParseStrategy<String> STRING_PARSE_STRAT;

    static {
        GSON = new Gson();
        STRING_PARSE_STRAT = new StringParseStrategy();
    }

    private ResponseParseStrategy<T> mParseStrategy;
    private Listener<T> listener;
    private Map<String, String> params, mHeaders;

    /**
     *
     * Constructs a BaseRequest for dispatching to the Volley Network Queue
     *
     * @param method
     *            - the request method
     * @param url
     *            - the request url
     * @param errorListener
     *            - error response listener
     * @param mListener
     *            - the network response listener
     * @param mParseStrategy
     *            - a parse strategy for returning different datatypes from the
     *            provided json - if the pure JSON is desired pass
     *            BaseRequest.NO_STRATEGY for the full json as a String.
     */
    public BaseRequest(int method, String url, ErrorListener errorListener,
                       Listener<T> mListener, ResponseParseStrategy<T> mParseStrategy) {
        super(method, url, errorListener);
        this.listener = mListener;
        this.mParseStrategy = mParseStrategy;
    }

    public void setHeaders(Map<String, String> headers){
        mHeaders = headers;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if(mHeaders != null)
            return mHeaders;
        return super.getHeaders();
    }

    /**
     * Appends parameters to the base URL of a GET or DELETE request.
     *
     * @param baseUrl
     * @param params
     * @return baseURL with formated parameters
     */
    public static String addParamsToRequestURL(String baseUrl, Map<String, String> params) {
        List<BasicNameValuePair> toAppend = new ArrayList<BasicNameValuePair>(
                params.size());

        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            toAppend.add(new BasicNameValuePair(entry.getKey(), entry
                    .getValue()));
        }

        String paramStr = URLEncodedUtils.format(toAppend, "utf-8");
        String finalURL = baseUrl + "?" + paramStr;

        return finalURL;
    }

    @Override
    public Map<String, String> getParams() throws AuthFailureError {
        return params;
    }

    public void setRequestParams(Map<String, String> params) {
        this.params = params;
    }

    @Override
    protected Map<String, String> getPostParams() throws AuthFailureError {
        return params;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {

        String toParse = "";

        try {
            toParse = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            toParse = new String(response.data);
        }

        T mResponse = null;
        try {
            if(mParseStrategy != null)
                mResponse = mParseStrategy.parseXml(toParse);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.success(mResponse, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    /**
     * Returns the response sent from the server as a JSON string
     */
    public static class StringParseStrategy implements ResponseParseStrategy<String> {
        @Override
        public String parseXml(String rawResponse) {
            return rawResponse;
        }
    }

    /**
     * Returns the response sent form the server in the form of a GSON parsed model class
     *
     * @param <T>
     */
    public static class ClassParseStrategy<T> implements ResponseParseStrategy<T>{

        private Class<T> clazz;

        public ClassParseStrategy(Class<T> clazz){
            this.clazz = clazz;
        }

        @Override
        public T parseXml(String rawResponse) {
            T model = GSON.fromJson(rawResponse, clazz);
            return model;
        }

    }

    /**
     * Returns the response sent form the server in the form of a GSON parsed model class array
     *
     * @param <T>
     */
    public static class ArrayParseStrategy<T> implements ResponseParseStrategy<ArrayList<T>>{
        private Class<T> clazz;

        public ArrayParseStrategy(Class<T> clazz){
            this.clazz = clazz;
        }

        @Override
        public ArrayList<T> parseXml(String rawResponse) {
            JsonParser parser = new JsonParser();
            JsonElement mMainElement = parser.parse(rawResponse);
            JsonArray mElements = null;
            ArrayList<T> modelList = new ArrayList<T>();

            if(mMainElement.isJsonArray()){
                mElements = mMainElement.getAsJsonArray();
            }

            final int count = mElements.size();
            for(int i =0; i < count; i++){
                JsonElement current = mElements.get(i);
                modelList.add(GSON.fromJson(current, clazz));
            }

            return modelList;
        }

    }

}
