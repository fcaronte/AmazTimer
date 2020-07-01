package me.micrusa.amaztimer.utils;


import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;


@SuppressWarnings({"WeakerAccess", "CanBeFinal", "UnusedReturnValue", "unused"})
public class file {
    // Settings file name
    private File save_directory;
    private String settings_file_name;
    // Loaded data
    private JSONObject data;

    // Constructor
    public file(String tag, Context context) {
        this.data = null;

        // Get file info
        this.settings_file_name = tag + ".json";
        this.save_directory = context.getExternalFilesDir(null);

        // Load settings
        this.load();
    }

    private void load() {
        File file = new File(this.save_directory, this.settings_file_name);
        if (file.exists()) {
            try {
                // Read text from file
                StringBuilder data = new StringBuilder();
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    data.append(line);
                    data.append('\n');
                }
                reader.close();

                // Parse to json
                this.data = new JSONObject(data.toString());
            } catch (Exception e) {
                Logger.error(e);
                if (this.data == null) {
                    this.data = new JSONObject();
                }
            }
        } else {
            // No previous settings
            this.data = new JSONObject();
        }
    }

    private void save() {
        File file = new File(this.save_directory, this.settings_file_name);
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(this.data.toString());
            writer.close();
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    // Data Getter methods
    public String get(String key) {
        return this.getString(key, "");
    }

    public String get(String key, String defvalue) {
        return this.getString(key, defvalue);
    }

    public int get(String key, int defvalue) {
        return this.getInt(key, defvalue);
    }

    public boolean get(String key, boolean defvalue) {
        return this.getBoolean(key, defvalue);
    }

    public String getString(String key, String defvalue) {
        String value;
        try {
            value = this.data.getString(key);
        } catch (JSONException e) {
            value = defvalue;
        }
        return value;
    }

    public int getInt(String key, int defvalue) {
        int value;
        try {
            value = this.data.getInt(key);
        } catch (JSONException e) {
            value = defvalue;
        }
        return value;
    }

    public boolean getBoolean(String key, boolean defvalue) {
        boolean value;
        try {
            value = this.data.getBoolean(key);
        } catch (JSONException e) {
            value = defvalue;
        }
        return value;
    }


    public boolean set(String key, String value) {
        return this.setString(key, value);
    }

    public boolean set(String key, int value) {
        return this.setInt(key, value);
    }

    public boolean set(String key, boolean value) {
        return this.setBoolean(key, value);
    }

    public boolean setString(String key, String value) {
        // Check if it has the same value
        try {
            if (this.data.getString(key).equals(value)) {
                // Avoid useless writing
                return true;
            }
        } catch (JSONException e) {
            Logger.error(e);
        }

        try {
            this.data.put(key, value);
            this.save();
        } catch (JSONException e) {
            return false;
        }
        return true;
    }

    public boolean setInt(String key, int value) {
        // Check if it has the same value
        try {
            if (this.data.getInt(key) == value) {
                // Avoid useless writing
                return true;
            }
        } catch (JSONException e) {
            Logger.error(e);
        }

        try {
            this.data.put(key, value);
            this.save();
        } catch (JSONException e) {
            return false;
        }
        return true;
    }

    public boolean setBoolean(String key, boolean value) {
        // Check if it has the same value
        try {
            if (this.data.getBoolean(key) == value) {
                // Avoid useless writing
                return true;
            }
        } catch (JSONException e) {
            Logger.error(e);
        }

        try {
            this.data.put(key, value);
            this.save();
        } catch (JSONException e) {
            return false;
        }
        return true;
    }
}
