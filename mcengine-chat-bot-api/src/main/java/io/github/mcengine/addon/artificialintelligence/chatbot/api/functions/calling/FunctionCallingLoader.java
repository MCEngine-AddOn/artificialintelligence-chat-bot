package io.github.mcengine.addon.artificialintelligence.chatbot.api.functions.calling;

import io.github.mcengine.addon.artificialintelligence.chatbot.api.functions.calling.json.FunctionCallingJson;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

import static io.github.mcengine.addon.artificialintelligence.chatbot.api.functions.calling.util.FunctionCallingLoaderUtilTime.*;

public class FunctionCallingLoader {

    private final List<FunctionRule> mergedRules = new ArrayList<>();

    public FunctionCallingLoader(Plugin plugin) {
        IFunctionCallingLoader loader = new FunctionCallingJson(
                new java.io.File(plugin.getDataFolder(), "addons/MCEngineChatBot/data/")
        );
        mergedRules.addAll(loader.loadFunctionRules());
    }

    public List<String> match(Player player, String input) {
        List<String> results = new ArrayList<>();
        String lowerInput = input.toLowerCase().trim();

        for (FunctionRule rule : mergedRules) {
            for (String pattern : rule.match) {
                String lowerPattern = pattern.toLowerCase();
                if (lowerInput.contains(lowerPattern) || lowerPattern.contains(lowerInput)) {
                    String resolved = applyPlaceholders(rule.response, player);
                    results.add(resolved);
                    break;
                }
            }
        }

        return results;
    }

    private String applyPlaceholders(String response, Player player) {
        response = response
                .replace("{player_name}", player.getName())
                .replace("{player_uuid}", player.getUniqueId().toString())
                .replace("{time_server}", getFormattedTime(TimeZone.getDefault()))
                .replace("{time_utc}", getFormattedTime(TimeZone.getTimeZone("UTC")))
                .replace("{time_gmt}", getFormattedTime(TimeZone.getTimeZone("GMT")));

        Map<String, String> namedZones = Map.ofEntries(
                Map.entry("{time_new_york}", getFormattedTime("America/New_York")),
                Map.entry("{time_london}", getFormattedTime("Europe/London")),
                Map.entry("{time_tokyo}", getFormattedTime("Asia/Tokyo")),
                Map.entry("{time_bangkok}", getFormattedTime("Asia/Bangkok")),
                Map.entry("{time_sydney}", getFormattedTime("Australia/Sydney")),
                Map.entry("{time_paris}", getFormattedTime("Europe/Paris")),
                Map.entry("{time_berlin}", getFormattedTime("Europe/Berlin")),
                Map.entry("{time_singapore}", getFormattedTime("Asia/Singapore")),
                Map.entry("{time_los_angeles}", getFormattedTime("America/Los_Angeles")),
                Map.entry("{time_toronto}", getFormattedTime("America/Toronto"))
        );

        for (Map.Entry<String, String> entry : namedZones.entrySet()) {
            response = response.replace(entry.getKey(), entry.getValue());
        }

        for (int hour = -12; hour <= 14; hour++) {
            for (int min : new int[]{0, 30, 45}) {
                String utcLabel = getZoneLabel("utc", hour, min);
                String gmtLabel = getZoneLabel("gmt", hour, min);
                TimeZone tz = TimeZone.getTimeZone(String.format("GMT%+03d:%02d", hour, min));
                String time = getFormattedTime(tz);
                response = response.replace(utcLabel, time);
                response = response.replace(gmtLabel, time);
            }
        }

        return response;
    }
}
