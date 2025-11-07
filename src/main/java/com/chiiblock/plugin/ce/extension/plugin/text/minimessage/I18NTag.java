package com.chiiblock.plugin.ce.extension.plugin.text.minimessage;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class I18NTag implements TagResolver {
    private static final I18NTag INSTANCE = new I18NTag();

    private I18NTag() {}

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull net.kyori.adventure.text.minimessage.Context ctx) throws ParsingException {
        if (!this.has(name)) {
            return null;
        }
        String i18nKey = arguments.popOr("No argument i18n key provided").toString();
        String translation = TranslationManager.instance().miniMessageTranslation(i18nKey);
        return Tag.selfClosingInserting(MiniMessage.miniMessage().deserialize(translation, INSTANCE));
    }

    @Override
    public boolean has(@NotNull String name) {
        return "i18n".equals(name);
    }

    public static I18NTag instance() {
        return INSTANCE;
    }
}
