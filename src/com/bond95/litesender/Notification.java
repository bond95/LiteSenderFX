package com.bond95.litesender;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by bohdan on 3/15/16.
 */
public class Notification implements ActionListener {
    private JFrame frame;
    private int showTime;
    private Timer timer;
    private NotificationEvent event;

    public Notification(String title, String body, int time)
    {
        buildNotification(title, body, time);
        timer = new Timer(time, this);
    }
    public void buildNotification(String title, String body, int time)
    {
        showTime = time;
        final JFrame f = new JFrame();
        f.setSize(300,125);
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Dimension arcs = new Dimension(15,15); //Border corners arcs {width,height}, change this to whatever you want
                int width = getWidth();
                int height = getHeight();
                Graphics2D graphics = (Graphics2D) g;
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                //Draws the rounded panel with borders.
                graphics.setColor(getBackground());
                graphics.fillRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height);//paint background
                graphics.setColor(getForeground());
                graphics.drawRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height);//paint border
            }
        };
        p.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0f;
        constraints.weighty = 1.0f;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.BOTH;

        String image = getClass().getResource("images/notification.svg").toExternalForm();
        JLabel headingLabel = new JLabel(title);
        ImageIcon headingIcon = new ImageIcon(image);
        headingLabel .setIcon(headingIcon); // --- use image icon you want to be as heading image.
        headingLabel.setOpaque(false);
        p.add(headingLabel, constraints);
        constraints.gridx++;
        constraints.weightx = 0f;
        constraints.weighty = 0f;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.NORTH;
        JButton cloesButton = new JButton("X");
        cloesButton.setMargin(new Insets(1, 4, 1, 4));
        cloesButton.setFocusable(false);
        cloesButton.addActionListener(this);
        p.add(cloesButton, constraints);
        constraints.gridx = 0;
        constraints.gridy++;
        constraints.weightx = 1.0f;
        constraints.weighty = 1.0f;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.BOTH;
        JLabel messageLabel = new JLabel("<HtMl>"+body);
        p.add(messageLabel, constraints);
        f.add(p);
        f.setUndecorated(true);
        f.setAlwaysOnTop(true);
        f.setBackground(new Color(255, 255, 0, 150));
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame = f;
    }

    public void setNotificationEvent(NotificationEvent ev)
    {
        event = ev;
    }

    public JFrame getNotificationFrame()
    {
        return frame;
    }

    public int getShowTime()
    {
        return showTime;
    }

    public void show(int x, int y)
    {
        timer.start();
        frame.setLocation(y, x);
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        event.recalculatePositions(this);
        frame.dispose();
    }
}
