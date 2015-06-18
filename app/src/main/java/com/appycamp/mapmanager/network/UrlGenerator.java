package com.appycamp.mapmanager.network;

import android.text.TextUtils;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Zach on 6/16/2015.
 */
public class UrlGenerator {

    private static final String BASE_URL = "http://api.ipinfodb.com/v3/ip-";
    private static final String API_KEY_PARAM = "/?key=71f4efd15cfd822fd0882d753e13970f182448174e11d8ef0517a253e1e6c153";
    private static final String FORMAT_PARAM = "&format=json";

    public enum Precision{
        CITY, COUNTRY
    }

    private static int[] ips = {0, 0, 0, 0};

    public static StringBuilder getBaseUrl(Precision precision){
        return new StringBuilder(BASE_URL)
                .append((precision == null ? Precision.CITY.name() : precision.name()).toLowerCase())
                .append(API_KEY_PARAM);
    }

    public static String getGenericLocationUrl(Precision precision){
        return getBaseUrl(precision)
                .append(FORMAT_PARAM).toString();
    }

    /**
     * Will get a networking url for an ip specific request
     *
     * @param precision
     * @param ip
     * @return - null if bad ip
     */
    public static String getIpSpecificUrl(Precision precision, String ip){
        if(TextUtils.isEmpty(ip))
            return null;

        try {
            InetAddress.getByName(ip);
        } catch (UnknownHostException e){
            return null;
        }

        return new StringBuilder(getBaseUrl(precision))
                .append("&ip=")
                .append(ip)
                .append(FORMAT_PARAM)
                .toString();
    }

}
