package io.github.mcengine.addon.artificialintelligence.chatbot;

import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import io.github.mcengine.api.artificialintelligence.addon.*;
import io.github.mcengine.addon.artificialintelligence.chatbot.command.ChatBotCommand;
import io.github.mcengine.addon.artificialintelligence.chatbot.listener.ChatBotListener;
import io.github.mcengine.addon.artificialintelligence.chatbot.tabcompleter.ChatBotTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Field;
import java.util.List;

/**
 * ChatBot AddOn for the MCEngineArtificialIntelligence plugin.
 *
 * This AddOn registers the `/chatbot` command with tab completion for interacting
 * with AI models. It dynamically injects the command into Bukkit's CommandMap without
 * requiring `plugin.yml` definitions, allowing developers to use it as a modular AddOn.
 *
 * Command usage:
 * <pre>
 *     /chatbot {platform} {model} {message...}
 * </pre>
 */
public class ChatBot implements IMCEngineArtificialIntelligenceAddOn {

    /**
     * Invoked by the core plugin to initialize this AddOn.
     *
     * @param plugin The plugin instance used for context and logging.
     */
    @Override
    public void onLoad(Plugin plugin) {
        MCEngineArtificialIntelligenceAddOnLogger logger = new MCEngineArtificialIntelligenceAddOnLogger(plugin, "MCEngineChatBot");

        try {
            // Register listener
            PluginManager pluginManager = Bukkit.getPluginManager();
            pluginManager.registerEvents(new ChatBotListener(plugin), plugin);

            // Reflectively access Bukkit's CommandMap
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            // Define and register the /chatbot command with inline executor and tab completer
            Command chatbotCommand = new Command("chatbot") {
                private final ChatBotCommand handler = new ChatBotCommand();
                private final ChatBotTabCompleter completer = new ChatBotTabCompleter();

                /**
                 * Executes the /chatbot command.
                 *
                 * @param sender  The command sender.
                 * @param label   The alias used.
                 * @param args    The command arguments.
                 * @return true if handled, false otherwise.
                 */
                @Override
                public boolean execute(CommandSender sender, String label, String[] args) {
                    return handler.onCommand(sender, this, label, args);
                }

                /**
                 * Provides tab completion for the /chatbot command.
                 *
                 * @param sender  The command sender.
                 * @param alias   The alias used.
                 * @param args    The current arguments typed.
                 * @return A list of tab-completion suggestions.
                 */
                @Override
                public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                    return completer.onTabComplete(sender, this, alias, args);
                }
            };

            chatbotCommand.setDescription("Interact with the chatbot AI.");
            chatbotCommand.setUsage("/chatbot {platform} {model} {message...}");

            // Register the command dynamically
            commandMap.register(plugin.getName().toLowerCase(), chatbotCommand);

            MCEngineArtificialIntelligenceApi.getApi().checkUpdate("github","MCEngine-AddOn","artificialintelligence-chat-bot",plugin.getConfig().getString("github.token", "null"));

            logger.info("Enabled successfully.");
        } catch (Exception e) {
            logger.warning("Failed to initialize ChatBot AddOn: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
