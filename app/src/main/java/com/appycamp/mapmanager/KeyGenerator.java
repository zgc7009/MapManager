package com.appycamp.mapmanager;

/**
 * Created by Zach on 6/14/2015.
 */
public class KeyGenerator {

    public static String generateKey(String suffix){
        return KeyGenerator.class.getPackage().getName() + ".KEY." + suffix;
    }
}
