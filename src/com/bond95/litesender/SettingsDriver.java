package com.bond95.litesender;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by bond95 on 7/24/16.
 */
public class SettingsDriver {
    private HashMap<String, String> settings = new HashMap<String, String>();
    private String dir_path;
    private HashMap<String, String> default_settings = new HashMap<String, String>();
    public SettingsDriver() {
        default_settings.put("name", Constants.DEFAULT_DEV_NAME);
        default_settings.put("dir", Constants.DEFAULT_DIR);
        default_settings.put("key", "");
    }

    public void LoadSettings() {
        LogDriver.println("LOAD_SETTINGS", "Load settings");
        File f = new File("settings.dat");

        settings.put("dir", default_settings.get("dir"));
        settings.put("name", default_settings.get("name"));
        settings.put("key", default_settings.get("key"));

        if (!f.exists()) {
            File dir = new File(dir_path);
            LogDriver.println("LOAD_SETTINGS", "Creating dir");
            if (!dir.exists()) {
                dir.mkdir();
            }
            SaveSettings();
        }
        else {
            BufferedReader br = null;
            String temp;
            String[] set;
            try {
                br = new BufferedReader(new FileReader(f));
                while ((temp = br.readLine()) != null) {
                    set= temp.split("=");
                    settings.put(set[0], set[1]);
                    if (set[0] == "dir") {
                        dir_path = set[1];
                    }
                }
                br.close();
            }
            catch (IOException ex)
            {
                System.out.println("File not found");
                return;
            }
        }
    }

    public void SaveSettings() {
        try {
            PrintWriter writer = new PrintWriter("settings.dat", "UTF-8");
            Iterator it = settings.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                writer.write(pair.getKey()+"="+pair.getValue()+"\n");
                System.out.println(pair.getKey() + " = " + pair.getValue());
                //it.remove(); // avoids a ConcurrentModificationException
            }
            writer.flush();
            writer.close();
        }
        catch (IOException ex)
        {
            System.out.println(ex.getMessage());
        }
    }

    public void setDirPath(String dir_path) {
        this.dir_path = dir_path;
    }

    public void setSetting(String key, String value) {
        settings.put(key, value);
    }

    public String getSetting(String key) {
        return settings.get(key);
    }


}
