package io.github.mcengine.addon.artificialintelligence.chatbot.api.functions.calling;

import java.util.List;

public interface IFunctionCallingLoader {
    List<FunctionRule> loadFunctionRules();
}
