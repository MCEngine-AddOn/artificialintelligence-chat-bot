package io.github.mcengine.addon.artificialintelligence.chatbot.api.functions.calling.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.mcengine.addon.artificialintelligence.chatbot.api.functions.calling.FunctionRule;
import io.github.mcengine.addon.artificialintelligence.chatbot.api.functions.calling.IFunctionCallingLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Loads function calling rules recursively from all `data.json` files under the specified root folder.
 * If the directory does not exist, creates it and writes default data.json.
 */
public class FunctionCallingJson implements IFunctionCallingLoader {

    private final File rootFolder;

    public FunctionCallingJson(File rootFolder) {
        this.rootFolder = rootFolder;
        ensureDefaultDataJsonExists();
    }

    @Override
    public List<FunctionRule> loadFunctionRules() {
        List<FunctionRule> allRules = new ArrayList<>();
        try {
            List<File> jsonFiles = listAllDataJsonFiles(rootFolder);
            Gson gson = new Gson();
            Type type = new TypeToken<List<FunctionRule>>() {}.getType();

            for (File file : jsonFiles) {
                try (FileReader reader = new FileReader(file)) {
                    List<FunctionRule> rules = gson.fromJson(reader, type);
                    if (rules != null) {
                        allRules.addAll(rules);
                    }
                } catch (Exception e) {
                    System.err.println("❌ Failed to load: " + file.getPath());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allRules;
    }

    private List<File> listAllDataJsonFiles(File folder) {
        List<File> result = new ArrayList<>();
        if (folder == null || !folder.exists()) return result;

        File[] files = folder.listFiles();
        if (files == null) return result;

        for (File file : files) {
            if (file.isDirectory()) {
                result.addAll(listAllDataJsonFiles(file)); // Recurse
            } else if (file.getName().equalsIgnoreCase("data.json")) {
                result.add(file);
            }
        }

        return result;
    }

    private void ensureDefaultDataJsonExists() {
        try {
            File defaultFile = new File(rootFolder, "data.json");

            // If folder doesn't exist at all, create it and data.json
            if (!rootFolder.exists()) {
                if (rootFolder.mkdirs()) {
                    writeDefaultDataJson(defaultFile);
                }
            }

            // If folder exists but data.json does not, do NOT write anything
            // This avoids overwriting or interfering with existing sub-data folders
            if (rootFolder.exists() && !defaultFile.exists() && isFolderEmpty(rootFolder)) {
                writeDefaultDataJson(defaultFile);
            }

        } catch (Exception e) {
            System.err.println("⚠️ Failed to create default data.json in: " + rootFolder.getPath());
            e.printStackTrace();
        }
    }

    private void writeDefaultDataJson(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            List<FunctionRule> defaultRules = Arrays.asList(
                    new FunctionRule(
                            Arrays.asList("What game am I playing right now?", "Which game am I currently playing?"),
                            "You are playing Minecraft right now."
                    ),
                    new FunctionRule(
                            Arrays.asList("What plugin is this?", "Which plugin is running?"),
                            "This plugin is MCEngine Artificial Intelligence."
                    )
            );

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(defaultRules, writer);
            System.out.println("✅ Created default function rules at: " + file.getPath());

        } catch (Exception e) {
            System.err.println("⚠️ Could not write default data.json to: " + file.getPath());
            e.printStackTrace();
        }
    }

    private boolean isFolderEmpty(File folder) {
        File[] files = folder.listFiles();
        return files == null || files.length == 0;
    }
}
