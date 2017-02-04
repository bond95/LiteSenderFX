package com.bond95.litesender;

/**
 * Created by bohdan on 3/28/16.
 */
public class DeviceListItem {
    private String key;
    private String name;
    private String ip;

    public DeviceListItem(String key, String name, String ip) {
        this.key = key;
        this.name = name;
        this.ip = ip;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setName(String name) {
        this.name = name;
    }

}
