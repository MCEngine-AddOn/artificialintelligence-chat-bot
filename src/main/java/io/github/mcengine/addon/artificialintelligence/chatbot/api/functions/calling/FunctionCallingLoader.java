package io.github.mcengine.addon.artificialintelligence.chatbot.api.functions.calling;

import io.github.mcengine.addon.artificialintelligence.chatbot.api.functions.calling.json.FunctionCallingJson;
import io.github.mcengine.addon.artificialintelligence.chatbot.api.functions.calling.util.FunctionCallingLoaderUtilTime;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class FunctionCallingLoader {

    private final List<FunctionRule> mergedRules = new ArrayList<>();
    private final List<LowercaseRule> lowercaseRules = new ArrayList<>();

    public FunctionCallingLoader(Plugin plugin) {
        IFunctionCallingLoader loader = new FunctionCallingJson(
                new java.io.File(plugin.getDataFolder(), "addons/MCEngineChatBot/data/")
        );
        mergedRules.addAll(loader.loadFunctionRules());

        for (FunctionRule rule : mergedRules) {
            List<String> lowerMatch = new ArrayList<>();
            for (String pattern : rule.match) {
                lowerMatch.add(pattern.toLowerCase());
            }
            lowercaseRules.add(new LowercaseRule(rule, lowerMatch));
        }
    }

    public List<String> match(Player player, String input) {
        String lowerInput = input.toLowerCase().trim();
        List<String> results = new ArrayList<>();

        for (LowercaseRule rule : lowercaseRules) {
            for (String pattern : rule.lowerMatch) {
                if (lowerInput.contains(pattern) || pattern.contains(lowerInput)) {
                    results.add(applyPlaceholders(rule.original.response, player));
                    break;
                }
            }
        }

        return results;
    }

    private String applyPlaceholders(String response, Player player) {
        Map<String, String> placeholders = new HashMap<>();

        // Static placeholders
        placeholders.put("{player_name}", player.getName());
        placeholders.put("{player_uuid}", player.getUniqueId().toString());
        placeholders.put("{time_server}", FunctionCallingLoaderUtilTime.getFormattedTime(TimeZone.getDefault()));
        placeholders.put("{time_utc}", FunctionCallingLoaderUtilTime.getFormattedTime(TimeZone.getTimeZone("UTC")));
        placeholders.put("{time_gmt}", FunctionCallingLoaderUtilTime.getFormattedTime(TimeZone.getTimeZone("GMT")));

        // Named zones
        String[][] zones = {
                {"{time_new_york}", "America/New_York"},
                {"{time_london}", "Europe/London"},
                {"{time_tokyo}", "Asia/Tokyo"},
                {"{time_bangkok}", "Asia/Bangkok"},
                {"{time_sydney}", "Australia/Sydney"},
                {"{time_paris}", "Europe/Paris"},
                {"{time_berlin}", "Europe/Berlin"},
                {"{time_singapore}", "Asia/Singapore"},
                {"{time_los_angeles}", "America/Los_Angeles"},
                {"{time_toronto}", "America/Toronto"},
        };

        for (String[] zone : zones) {
            placeholders.put(zone[0], FunctionCallingLoaderUtilTime.getFormattedTime(zone[1]));
        }

        // Dynamic GMT/UTC zones
        for (int hour = -12; hour <= 14; hour++) {
            for (int min : new int[]{0, 30, 45}) {
                TimeZone tz = TimeZone.getTimeZone(String.format("GMT%+03d:%02d", hour, min));
                String time = FunctionCallingLoaderUtilTime.getFormattedTime(tz);

                placeholders.put(FunctionCallingLoaderUtilTime.getZoneLabel("utc", hour, min), time);
                placeholders.put(FunctionCallingLoaderUtilTime.getZoneLabel("gmt", hour, min), time);
            }
        }

        // Replace all placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            response = response.replace(entry.getKey(), entry.getValue());
        }

        return response;
    }

    private static class LowercaseRule {
        final FunctionRule original;
        final List<String> lowerMatch;

        LowercaseRule(FunctionRule original, List<String> lowerMatch) {
            this.original = original;
            this.lowerMatch = lowerMatch;
        }
    }
}
