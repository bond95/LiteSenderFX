package com.bond95.litesender;

/**
 * Created by bohdan on 3/28/16.
 */
abstract public class FeedbackClass {
    abstract void addToList(DeviceListItem item, boolean last);
    abstract void removeFromList(DeviceListItem item);
    abstract void changeLabel(DeviceListItem item);
}
