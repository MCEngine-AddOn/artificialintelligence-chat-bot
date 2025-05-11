package io.github.mcengine.addon.artificialintelligence.chatbot.util;

import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Async task that sends player input to the AI API and sends the response back.
 */
public class ChatBotTask extends BukkitRunnable {

    private final Plugin plugin;
    private final Player player;
    private final String platform;
    private final String model;
    private final String message;

    /**
     * Constructs a new ChatBotTask.
     *
     * @param plugin   The plugin instance.
     * @param player   The player in conversation.
     * @param platform The AI platform to use.
     * @param model    The model name to use.
     * @param message  The message sent by the player.
     */
    public ChatBotTask(Plugin plugin, Player player, String platform, String model, String message) {
        this.plugin = plugin;
        this.player = player;
        this.platform = platform;
        this.model = model;
        this.message = message;
    }

    /**
     * Executes the API call and sends the response back to the player.
     */
    @Override
    public void run() {
        MCEngineArtificialIntelligenceApi api = MCEngineArtificialIntelligenceApi.getApi();
        String response;

        try {
            // Use entire chat history as prompt
            String fullPrompt = ChatBotManager.get(player) + "[Player]: " + message;
            response = api.getResponse(platform, model, fullPrompt);
        } catch (Exception e) {
            Bukkit.getScheduler().runTask(plugin, () ->
                player.sendMessage("§c[ChatBot] Failed: " + e.getMessage())
            );
            ChatBotManager.setWaiting(player, false);
            return;
        }

        String playerPrompt = "[Player]: " + message;
        String aiReply = "[Ai]: " + response;

        Bukkit.getScheduler().runTask(plugin, () -> {
            player.sendMessage("§e[ChatBot]§r " + response);
            ChatBotManager.append(player, playerPrompt);
            ChatBotManager.append(player, aiReply);
            ChatBotManager.setWaiting(player, false);
        });
    }
}
