package com.bond95.litesender;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by bohdan on 3/15/16.
 */

/**
 * Notification driver, for crossplatform notification connecting
 */
public class NotificationDriver {
    private ArrayList<Notification> q;

    public NotificationDriver() {
        q = new ArrayList<Notification>();
    }

    public void addNotification(Notification noti) {
        final ArrayList<Notification> queue = q;
        noti.setNotificationEvent(new NotificationEvent() {
            @Override
            public void recalculatePositions(Notification n) {
                queue.remove(n);
                Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();// size of the screen
                for (int i = 0; i < queue.size(); i++) {

                    Notification temp = queue.get(i);
                    JFrame frame = temp.getNotificationFrame();
                    Insets toolHeight = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());
                    frame.setLocation(scrSize.width - frame.getWidth() - 50, toolHeight.top + ((25 + 125) * i) + 25);
                }
            }
        });
        q.add(noti);
        JFrame frame = noti.getNotificationFrame();
        Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();// size of the screen
        Insets toolHeight = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());// height of the task bar
        noti.show(toolHeight.top + ((25 + 125) * (q.size() - 1)) + 25, scrSize.width - frame.getWidth() - 50);
    }
}
