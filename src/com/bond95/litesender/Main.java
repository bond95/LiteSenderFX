package com.bond95.litesender;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;

import javax.imageio.ImageIO;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;

public class Main extends Application {
    private int windowWidth = 300;
    private int windowHeight = 300;
    private Stage stage;

    private Random random = new Random(); // нам нужен рандом!!

    private ComboBox<DeviceListItem> comboBox;
    private TextField nameField;
    private HashMap<String, String[]> av_devices = new HashMap<String, String[]>();
    private String key = "";

    private LiteSender liteSender;

    private Timer timer;

    private NotificationDriver notificationDriver;

    private String iconImageLoc;


    @Override
    public void start(final Stage primaryStage) throws Exception {

        stage = primaryStage;

        // Create tray icon
        iconImageLoc = getClass().getResource("images/tray_icon.png").toExternalForm();
        addAppToTray();
        Platform.setImplicitExit(false);

        timer = new Timer(10000, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                liteSender.sendNameChange();
            }

        });
        timer.setRepeats(false);
        timer.stop();

        liteSender = new LiteSender();

        notificationDriver = new NotificationDriver();

        liteSender.setNotificationDriver(notificationDriver);
        // Start LiteSender
        liteSender.Start();

        primaryStage.setTitle("LiteSender");
        primaryStage.setScene(createScene());
        key = liteSender.getKey();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent t) {
                primaryStage.hide();
            }

        });
        primaryStage.show();
        liteSender.getLocalDevices();
    }

    private Scene createScene() {
        VBox root = new VBox();
        Scene scene = new Scene(root, windowWidth, windowHeight);

        // Create combo box for devices
        ObservableList<String> options =
                FXCollections.observableArrayList();
        comboBox = new ComboBox<DeviceListItem>();

        comboBox.setPromptText("Choose device");

        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.setStyle("-fx-fill-width: true;");

        comboBox.setCellFactory(new Callback<ListView<DeviceListItem>, ListCell<DeviceListItem>>() {
            @Override
            public ListCell<DeviceListItem> call(ListView<DeviceListItem> p) {
                return new ListCell<DeviceListItem>() {

                    @Override
                    protected void updateItem(DeviceListItem item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setText("");
                        } else {
                            setText(item.getName());
                        }
                    }
                };
            }
        });

        comboBox.setConverter(new StringConverter<DeviceListItem>() {

            @Override
            public String toString(DeviceListItem comboBoxItem) {
                if (comboBoxItem == null) {
                    return "";
                } else {
                    return comboBoxItem.getName();
                }
            }

            @Override
            public DeviceListItem fromString(String s) {
                return null;
            }
        });

        comboBox.valueProperty().addListener(new ChangeListener<DeviceListItem>() {
            @Override
            public void changed(ObservableValue<? extends DeviceListItem> ov, DeviceListItem t, DeviceListItem t1) {
                System.out.println(t1);

                liteSender.setSelectedItem(t1);

            }
        });

        // Set feedback class for communication with LiteSender
        liteSender.setFeedback(new FeedbackClass() {
            @Override
            void addToList(DeviceListItem item, boolean last) {
                final DeviceListItem item2 = item;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        comboBox.getItems().add(item2);
                    }
                });
            }

            @Override
            void removeFromList(DeviceListItem item) {
                final DeviceListItem item2 = item;

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        comboBox.getItems().remove(item2);
                    }
                });
            }

            void changeLabel(DeviceListItem item) {
                final DeviceListItem it = item;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        int index = comboBox.getItems().indexOf(it);
                        boolean selected = comboBox.getSelectionModel().isSelected(index);
                        comboBox.getItems().remove(index);
                        comboBox.getItems().add(it);
                        if (selected) {
                            comboBox.getSelectionModel().selectLast();
                        }
                    }
                });
            }
        });

        root.getChildren().add(comboBox);

        nameField = new TextField(liteSender.getName());

        nameField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String t, String t1) {
                liteSender.setName(t1);
                System.out.println(t1);
                timer.restart();
            }
        });

        root.getChildren().add(nameField);

        // Create drag&drop panel
        String image = getClass().getResource("images/drag_n_drop.png").toExternalForm();

        Pane canvas = new Pane();
        canvas.setStyle(//"-fx-background-color: white;" +
                "-fx-border-color: black; " +
                        "-fx-border-style: dashed; " +
                        "-fx-border-width: 8px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-background-image: url('" + image + "');" +
                        "-fx-background-size: 20% auto;" +
                        "-fx-background-repeat: stretch;" +
                        "-fx-background-position: center center;");

        canvas.setMaxHeight(Double.MAX_VALUE);
        canvas.setMaxWidth(Double.MAX_VALUE);

        canvas.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
            }
        });

        // Dropping over surface
        canvas.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    String filePath = null;
                    for (File file : db.getFiles()) {
                        filePath = file.getAbsolutePath();

                        System.out.println(filePath);
                        liteSender.sendFiles(file);
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });

        VBox.setVgrow(canvas, Priority.ALWAYS);

        root.getChildren().add(canvas);

        return scene;

    }

    public NotificationDriver getNotificationDriver() {
        return notificationDriver;
    }

    /**
     * Sets up a system tray icon for the application.
     */
    private void addAppToTray() {
        try {
            // ensure awt toolkit is initialized.
            java.awt.Toolkit.getDefaultToolkit();

            // app requires system tray support, just exit if there is no support.
            if (!java.awt.SystemTray.isSupported()) {
                System.out.println("No system tray support, application exiting.");
                Platform.exit();
            }

            // set up a system tray icon.
            final java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
            URL imageLoc = new URL(
                    iconImageLoc
            );
            java.awt.Image image = ImageIO.read(imageLoc);
            final java.awt.TrayIcon trayIcon = new java.awt.TrayIcon(image);

            // if the user double-clicks on the tray icon, show the main app stage.
            trayIcon.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (stage != null) {
                                stage.show();
                                stage.toFront();
                            }
                        }
                    });
                }
            });

            // if the user selects the default menu item (which includes the app name),
            // show the main app stage.
            java.awt.MenuItem openItem = new java.awt.MenuItem("hello, world");
            openItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (stage != null) {
                                stage.show();
                                stage.toFront();
                            }
                        }
                    });
                }
            });

            // the convention for tray icons seems to be to set the default icon for opening
            // the application stage in a bold font.
            java.awt.Font defaultFont = java.awt.Font.decode(null);
            java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);
            openItem.setFont(boldFont);

            // to really exit the application, the user must go to the system tray icon
            // and select the exit option, this will shutdown JavaFX and remove the
            // tray icon (removing the tray icon will also shut down AWT).
            java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
            exitItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    liteSender.Close();
                    Platform.exit();
                    tray.remove(trayIcon);
                    System.exit(0);
                }
            });

            // setup the popup menu for the application.
            final java.awt.PopupMenu popup = new java.awt.PopupMenu();
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);


            // add the application tray icon to the system tray.
            tray.add(trayIcon);
        } catch (java.awt.AWTException e) {
            System.out.println("Unable to init system tray");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Unable to init system tray");
            e.printStackTrace();
        }
    }

//    public void SetItems(HashMap<String, String[]> al) {
//        final HashMap<String, String[]> al2 = al;
//        final String key2 = this.key;
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                av_devices.clear();
//                comboBox.valueProperty().set(null);
//                comboBox.getItems().clear();
//                comboBox.getSelectionModel().clearAndSelect(0);
//                for (String key : al2.keySet()) {
//                    av_devices.put(key, al2.get(key));
//                    if (!al2.get(key)[0].equals(key2)) {
//                        comboBox.getItems().add(key);
//                    }
//                }
//            }
//        });
//    }

    public static void main(String[] args) {
        launch(args);
    }
}
