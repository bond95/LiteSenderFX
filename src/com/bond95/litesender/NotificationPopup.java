package com.bond95.litesender;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;

/**
 * Created by bond95 on 7/15/16.
 */

/**
 * Class for create notification
 */
public class NotificationPopup {
    private static Stage stage;

    public static void setStage(Stage st) {
        stage = st;
//        stage.getX().setY
    }

    public static Popup createPopup(final String message) {
        final Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        Label label = new Label(message);
        label.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                popup.hide();
            }
        });
        URL url = NotificationPopup.class.getResource("css/styles.css");
        label.getStylesheets().add(url.toExternalForm());
        label.getStyleClass().add("popup");
        popup.getContent().add(label);
        return popup;
    }

    public static void showPopupMessage(final String message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                final Popup popup = createPopup(message);

                popup.setOnShown(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent e) {
                        popup.setX(stage.getX() + stage.getWidth() / 2 - popup.getWidth() / 2);
                        popup.setY(stage.getY() + stage.getHeight() / 2 - popup.getHeight() / 2);
                    }
                });
                popup.show(stage);
            }
        });
    }
}
