package com.appycamp.mapmanager.markers;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Zach on 6/17/2015.
 *
 * THESE METHODS SHOULD BE ACCESSED FROM A BACKGROUND THREAD
 */
public class IpManager {

    public static boolean validateIp(String ip){
        try{
            InetAddress.getByName(ip);
        }catch(UnknownHostException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Given a previous IP (initially pass the start IP of the range) will return the next IP if one exists
     * in before we reach the end IP of our range. If we hit the end IP, return null
     *
     * @param previousIp
     * @param endIp
     * @return
     */
    public static String getNextIpInRange(String previousIp, String endIp){
        if(endIp.equals(previousIp) || getIpRangeSize(previousIp, endIp) == 0)
            return null;
        else
            return longToIp(ipToLong(previousIp) + 1);
    }


    /**
     * Will validate our IP range to make sure they are in the appropriate order
     *
     * @param ip1 - 1st IP input into range field
     * @param ip2 - 2nd IP input into range field
     * @return
     */
    public static boolean validateIpRangeOrder(String ip1, String ip2){
        return getIpRangeSize(ip1, ip2) >= 0;
    }

    /**
     * @param ip1
     * @param ip2
     * @return
     */
    public static long getIpRangeSize(String ip1, String ip2){
        return ipToLong(ip2) - ipToLong(ip1);
    }

    /**
     * Utilizes bitwise operations to get the long value of an IP address
     *
     * @param ip
     * @return
     */
    private static long ipToLong(String ip) {
        try {
            byte[] octets = InetAddress.getByName(ip).getAddress();
            long result = 0;
            for (byte octet : octets) {
                result <<= 8;
                result |= octet & 0xff;
            }
            return result;
        } catch(UnknownHostException e){
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Utilizes bitwise operations to get the IP value of a long
     *
     * @param ip
     * @return
     */
    public static String longToIp(long ip) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            sb.insert(0, Long.toString(ip & 0xFF));

            if (i < 3)
                sb.insert(0, '.');
            ip >>= 8;
        }

        return sb.toString();
    }

}
