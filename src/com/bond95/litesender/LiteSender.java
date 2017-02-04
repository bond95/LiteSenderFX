/**
 * Created by bohdan on 05.10.15.
 */
package com.bond95.litesender;

import java.io.*;
import java.lang.Thread;
import java.net.*;
import java.util.*;

public class LiteSender {

    // Definition of global values and items that are part of the GUI.
    private String dir_path = "./test_dir";
    private String name = "standard_1";
    private String key = "";
    private int port;
    final static private String alphabet = "qwertyuiopasdfghjklzxcvbnm";
    static private Main mainWindow;
    private SettingsDriver settings = new SettingsDriver();
    private DeviceListItem selectedItem;
    private ArrayList<Thread> threads;
    private ArrayList<DeviceListItem> av;
    private FeedbackClass feedback;
    private final int STARTING = 1;
    private final int CLOSING = 2;
    private final int CHANGE_NAME = 3;
    private NotificationDriver notificationDriver;

    public LiteSender() {
        selectedItem = null;
        port = 8888;
        threads = new ArrayList<Thread>();
        av = new ArrayList<DeviceListItem>();
    }

    public void setNotificationDriver(NotificationDriver notificationDriver1) {
        notificationDriver = notificationDriver1;
    }

    /**
     * Starting server
     */
    public void Start() {
        settings.LoadSettings();
        BroadcastLighter();
        LiteServer();
        getKeyFromSettings();
        getLocalDevices();
        //mainWindow.SetKey(key);
        //mainWindow.addSettingsChangedListener(new MainSettingsListener());
        //mainWindow.addFileDropListener(new FileDropedListener());
        //getLocalDevices();
        //notificationDriver = new NotificationDriver();

    }

    /**
     * Send broadcast request and get answers from other devices, and put it into list
     */
    public void getLocalDevices() {
        final HashMap<String, String[]> al = new HashMap<String, String[]>();
        final int timeout = 3000;
        final Main[] mainWindow1 = new Main[1];
        mainWindow1[0] = mainWindow;
        Thread uiThread = new Thread("UIHandler") {
            @Override
            public void run() {
                al.clear();
                DatagramSocket c;
                byte[] buffer = ("HI SERVER, MY NAME IS:" + settings.getSetting("name") + ":" + key).getBytes();
                String resp_phrase = "HELLO MY NAME IS:";
                try {

                    c = sendingBroadcast(buffer);

                    LogDriver.println("LOCATOR", "LOCATOR>>> Done looping over all network interfaces. Now waiting for a reply!");


                    c.setSoTimeout(timeout);
                    //Wait for a response
                    byte[] recvBuf = new byte[15000];
                    DatagramPacket receivePacket;
                    receivePacket = new DatagramPacket(recvBuf, recvBuf.length);

                    al.clear();

                    for (int i = 0; i < 10; i++) {
                        try {
                            c.receive(receivePacket);

                            //We have a response
                            LogDriver.println("LOCATOR", "LOCATOR>>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());

                            //Check if the message is correct
                            String message = new String(receivePacket.getData()).trim();
                            if (message.startsWith(resp_phrase)) {
                                parseRequest(message, receivePacket, STARTING);
                            }
                            receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                        } catch (Exception e) {
                            break;
                        }
                    }
                    //Close the port!
                    c.close();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }

            }
        };
        threads.add(uiThread);
        uiThread.start();
    }

    /**
     * Sending to all devices by broadcast exit notification
     */
    private void sendExitRequest() {
        Thread uiThread = new Thread("UIHandler") {
            @Override
            public void run() {
                DatagramSocket c;
                byte[] buffer = ("HI SERVER, I QUITING:" + key).getBytes();
                try {
                    c = sendingBroadcast(buffer);

                    LogDriver.println("LOCATOR", "LOCATOR>>> Done looping over all network interfaces. Now waiting for a reply!");

                    //Close the port!
                    c.close();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }

            }
        };
        threads.add(uiThread);
        uiThread.start();
    }

    /**
     * Method for sending broadcast requests
     *
     * @param buffer
     * @return
     * @throws IOException
     */
    private DatagramSocket sendingBroadcast(byte[] buffer) throws IOException {
        final int socket = 9509;
        //Open a random port to send the package
        DatagramSocket c = new DatagramSocket();
        c.setBroadcast(true);

        //Try the 255.255.255.255 first
        try {
            DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), socket);
            c.send(sendPacket);
            LogDriver.println("LOCATOR", "LOCATOR>>> Request packet sent to: 255.255.255.255 (DEFAULT)");
        } catch (Exception e) {
        }

        // Broadcast the message over all the network interfaces
        Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue; // Don't want to broadcast to the loopback interface
            }

            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress broadcast = interfaceAddress.getBroadcast();
                if (broadcast == null) {
                    continue;
                }

                // Send the broadcast package!
                try {
                    DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, broadcast, socket);
                    c.send(sendPacket);
                } catch (Exception e) {
                }

                LogDriver.println("LOCATOR", "LOCATOR>>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
            }
        }
        return c;
    }

    /**
     * Boradcast server
     */
    private void BroadcastLighter() {
        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramSocket socket;
                try {
                    //Keep a socket open to listen to all the UDP trafic that is destined for this port
                    socket = new DatagramSocket(9509);
                    socket.setSoTimeout(10000);
                    while (!Thread.interrupted()) {
                        try {
                            LogDriver.println(getClass().getName(), ">>>Ready to receive broadcast packets!");

                            //Receive a packet
                            byte[] recvBuf = new byte[15000];
                            DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                            socket.receive(packet);

                            //Packet received
                            LogDriver.println(getClass().getName(), ">>>Discovery packet received from: " + packet.getAddress().getHostAddress());
                            LogDriver.println(getClass().getName(), ">>>Packet received; data: " + new String(packet.getData()));


                            //See if the packet holds the right command (message)
                            String message = new String(packet.getData()).trim();
                            DatagramPacket resp = getRequest(message, packet);
                            if (resp != null) {
                                socket.send(resp);
                            }

                        } catch (InterruptedIOException e) {
                            continue;
                        }
                    }
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });
        threads.add(myThread);
        myThread.start();
    }

    /**
     * Method to proccess request
     *
     * @param message
     * @param packet
     * @return
     */
    private DatagramPacket getRequest(String message, DatagramPacket packet) {
        if (settings.getSetting("name").length() > 0) {
            name = settings.getSetting("name");
        }

        //Hello request
        if (message.startsWith("HI SERVER, MY NAME IS:")) {
            parseRequest(message, packet, STARTING);
            byte[] sendData = ("HELLO MY NAME IS:" + name + ":" + key).getBytes();

            //Send a response
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());

            LogDriver.println(getClass().getName(), ">>>Sent packet to: " + sendPacket.getAddress().getHostAddress());
            return sendPacket;
        }

        //Exit request
        if (message.startsWith("HI SERVER, I QUITING:")) {
            parseRequest(message, packet, CLOSING);

        }

        //Changing name request
        if (message.startsWith("HI, I CHANGED MY NAME:")) {
            parseRequest(message, packet, CHANGE_NAME);
        }
        return null;
    }

    /**
     * Method for parsing request
     *
     * @param message
     * @param receivePacket
     * @param request_type
     */
    private void parseRequest(String message, DatagramPacket receivePacket, int request_type) {
        String[] list = message.split(":");
        boolean found = false;
        int founded_index = -1;
        String key = "";
        if (request_type == CLOSING) {
            key = list[1];
        } else if (request_type == STARTING || request_type == CHANGE_NAME) {
            key = list[2];
        }
        for (int i = 0; i < av.size(); i++) {
            if (av.get(i).getKey().equals(key)) {
                founded_index = i;
                found = true;
                break;
            }
        }
        switch (request_type) {
            case STARTING:
                if (!found && !key.equals(this.key)) {
                    DeviceListItem item = new DeviceListItem(list[2], list[1], receivePacket.getAddress().getHostAddress());
                    av.add(item);
                    boolean last = false;
                    if (settings.getSetting("last_device").equals(item.getKey())) {
                        selectedItem = item;
                        last = true;
                    }
                    this.feedback.addToList(item, last);
                } else if (founded_index != -1) {
                    av.get(founded_index).setIp(receivePacket.getAddress().getHostAddress());
                }
                break;
            case CLOSING:
                if (found) {
                    if (selectedItem.equals(av.get(founded_index))) {
                        selectedItem = null;
                    }
                    this.feedback.removeFromList((av.remove(founded_index)));
                }
                break;
            case CHANGE_NAME:
                if (found) {
                    LogDriver.println("CHANGE_NAME", "Work");
                    av.get(founded_index).setName(list[1]);
                    this.feedback.changeLabel(av.get(founded_index));
                }
        }
    }

    /**
     * LiteServer getting files
     */
    private void LiteServer() {
        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Create socket of the server
                    ServerSocket ss = new ServerSocket(port);
                    ss.setSoTimeout(10000);
                    LogDriver.println("SERVER", "Waiting for a client...");
                    while (!Thread.interrupted()) {
                        try {
                            Socket socket = ss.accept();
                            socket.setSoTimeout(10 * 1000);
                            LogDriver.println("SERVER", "Got a client :) ... Finally, someone saw me through all the cover!");

                            // Get input and output streams
                            InputStream sin = socket.getInputStream();
                            OutputStream sout = socket.getOutputStream();

                            // Convert them for better workign with objects
                            ObjectInputStream in = new ObjectInputStream(sin);
                            ObjectOutputStream out = new ObjectOutputStream(sout);

                            getFile(in, out);

                            socket.close();
                        } catch (SocketException x) {
                            x.printStackTrace();
                        } catch (InterruptedIOException e) {
                            continue;
                        }
                    }
                    ss.close();
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        });
        threads.add(myThread);
        myThread.start();
    }

    /**
     * Method for sending files
     *
     * @param file
     */
    public void sendFiles(File file) {
        if (selectedItem != null) {
            final DeviceListItem send_info = selectedItem;
            final File aFile = file;
            Thread myThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket = null;
                        LogDriver.println("SEND_FILE", "Start sending...");
                        //Create socket for sending file
                        try {
                            InetAddress ipAddress = InetAddress.getByName(send_info.getIp());
                            socket = new Socket(ipAddress, port);
                        } catch (Exception x) {
                            LogDriver.println("SEND_FILE", "socket error");
                            x.printStackTrace();
                        }
                        // Get input and output streams
                        InputStream sin = socket.getInputStream();
                        OutputStream sout = socket.getOutputStream();

                        // Convert them for better workign with objects
                        ObjectInputStream in = new ObjectInputStream(sin);
                        ObjectOutputStream out = new ObjectOutputStream(sout);

                        // Create new protocol object, and put all needed information in it
                        ProtocolObject obj = new ProtocolObject();
                        obj.setName(aFile.getName());
                        obj.setSize(aFile.length());
                        obj.setHash(FileChecksum.createChecksum(aFile));
                        out.writeObject(obj);

                        FileInputStream din = new FileInputStream(aFile);
                        byte[] buffer = new byte[64 * 1024];
                        int count;

                        //Send file
                        while ((count = din.read(buffer)) != -1) {
                            out.write(buffer, 0, count);
                        }
                        out.flush();

                        // Waiting for response, and proccess response
                        ResponseObject resp = new ResponseObject();
                        in.readObject(resp);
                        if (resp.getFlag() == 0) {
                            notificationDriver.addNotification(new Notification("Error", "Error while sending file", 5000));
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception x) {
                        System.out.println(0);
                        x.printStackTrace();
                    }
                }
            });
            myThread.start();
        }
    }

    /**
     * Get file from socket stream
     *
     * @param in
     * @param out
     * @throws IOException
     */
    public void getFile(ObjectInputStream in, ObjectOutputStream out) throws IOException {
        ProtocolObject obj = new ProtocolObject();
        in.readObject(obj);
        byte[] line = new byte[64 * 1024];
        String filename = obj.getName();
        long size = obj.getSize();
        LogDriver.println("GET_FILE", "Filename: " + filename);
        LogDriver.println("GET_FILE", "Size: " + String.valueOf(size));
        LogDriver.println("GET_FILE", "File path: " + settings.getSetting("dir") + "/" + filename);

        int count = 0;
        int total = 0;
        FileOutputStream outF = new FileOutputStream(settings.getSetting("dir") + "/" + filename);
        while ((count = in.read(line)) != -1) {
            total += count;
            outF.write(line, 0, count);
            if (total == size) {
                break;
            }
        }
        outF.flush();
        outF.close();
        ResponseObject resp = new ResponseObject();
        resp.setHash(obj.getHash());
        if (total != size) {
            File f = new File(settings.getSetting("dir") + "/" + filename);
            f.delete();
            resp.setFlag((byte) 0);
            out.writeObject(resp);
            out.flush();
        } else {
            resp.setFlag((byte) 1);
            notificationDriver.addNotification(new Notification("New file", "You have new file " + filename, 3000));
//            NotificationPopup.showPopupMessage("You have new file "+filename);
//            out.writeShort(1);
            out.writeObject(resp);
            out.flush();
        }
    }

    /**
     * Get device key from settings or generate it if it not exists
     */
    private void getKeyFromSettings() {
        if (settings.getSetting("key").length() == 0) {
            long time = System.currentTimeMillis();
            String first_time = Long.toString(time);
            String time_two = Long.toString(System.currentTimeMillis());
            String smaller = first_time.length() > time_two.length() ? time_two : first_time;
            String bigger = first_time.length() <= time_two.length() ? time_two : first_time;
            ;
            byte[] result = new byte[smaller.length()];
            for (int i = 0; i < smaller.length(); i++) {
                int num = (smaller.charAt(i) - '0') + (bigger.charAt(i) - '0');
                result[i] = (byte) alphabet.charAt(num);
            }
            LogDriver.println("GET_KEY", result.toString());
            settings.setSetting("key", result.toString());
        }
        key = settings.getSetting("key");
        LogDriver.println("GET_KEY", key);
        settings.SaveSettings();
    }


    /**
     * Send request for name change
     */
    public void sendNameChange() {
        final String name = getName();
        Thread uiThread = new Thread("UIHandler") {
            @Override
            public void run() {
                DatagramSocket c;
                byte[] buffer = ("HI, I CHANGED MY NAME:" + name + ":" + key).getBytes();
                try {
                    c = sendingBroadcast(buffer);

                    LogDriver.println("LOCATOR", "LOCATOR>>> Done looping over all network interfaces. Now waiting for a reply!");

                    //Close the port!
                    c.close();
                } catch (IOException ex) {
                    LogDriver.println("LOCATOR", ex.getMessage());
                }

            }
        };
        threads.add(uiThread);
        uiThread.start();
    }

    /**
     * Set selected device
     *
     * @param item
     */
    public void setSelectedItem(DeviceListItem item) {
        selectedItem = item;
        settings.setSetting("last_device", item.getKey());
        settings.SaveSettings();
    }

    /**
     * Get device name
     *
     * @return
     */
    public String getName() {
        return settings.getSetting("name");
    }

    /**
     * Set device name
     *
     * @param name
     */
    public void setName(String name) {
        settings.setSetting("name", name);
        settings.SaveSettings();
    }

    /**
     * Get device key
     *
     * @return
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Get settings
     *
     * @return
     */
    private SettingsDriver getSettings() {
        return settings;
    }

    /**
     * Set feedback object for communicating across the app
     *
     * @param feedback
     */
    public void setFeedback(FeedbackClass feedback) {
        this.feedback = feedback;
    }

    /**
     * Method that end LiteServer work
     */
    public void Close() {
        sendExitRequest();
        for (int i = 0; i < threads.size(); i++) {
            threads.get(i).interrupt();
        }
    }

    /**
     * Get list of available devices
     *
     * @return
     */
    public ArrayList<DeviceListItem> getDevices() {
        return av;
    }

//    public void setPath(String path) {
//        dir_path = path;
//        File dir = new File(dir_path);
//        LogDriver.println("LOAD_SETTINGS", "Creating dir");
//        if (!dir.exists()) {
//            dir.mkdir();
//        }
//        LogDriver.println("LOAD_SETTINGS", "Creating settings");
//        settings.setSetting("dir", dir_path);
//        settings.setSetting("name", "standart_1");
//        settings.SaveSettings();
//    }

}