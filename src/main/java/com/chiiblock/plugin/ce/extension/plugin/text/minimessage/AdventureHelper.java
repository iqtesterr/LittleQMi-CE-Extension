package com.chiiblock.plugin.ce.extension.plugin.text.minimessage;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

public class AdventureHelper {
    private static final AdventureHelper INSTANCE = new AdventureHelper();
    private final MiniMessage textInput;

    private AdventureHelper() {
        this.textInput = MiniMessage.builder().tags(TagResolver.builder()
                .resolver(StandardTags.color())
                .resolver(StandardTags.shadowColor())
                .resolver(StandardTags.decorations())
                .resolver(StandardTags.reset())
                .resolver(StandardTags.keybind())
                .resolver(StandardTags.translatable())
                .resolver(StandardTags.translatableFallback())
                .resolver(StandardTags.rainbow())
                .resolver(StandardTags.gradient())
                .resolver(StandardTags.transition())
                .resolver(StandardTags.selector())
                .build()
        ).build();
    }

    public MiniMessage textInput() {
        return this.textInput;
    }

    public static AdventureHelper instance() {
        return INSTANCE;
    }
}
