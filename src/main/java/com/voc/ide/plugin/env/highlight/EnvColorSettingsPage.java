package com.voc.ide.plugin.env.highlight;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.openapi.util.NlsContexts;
import com.voc.ide.plugin.DevToolsIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

/**
 * @author WuYujie
 * @email coffee377@dingtalk.com
 * @time 2022/09/04 10:49
 */
public class EnvColorSettingsPage implements ColorSettingsPage {

    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Key", EnvSyntaxHighlighter.KEY),
            new AttributesDescriptor("Separator", EnvSyntaxHighlighter.SEPARATOR),
            new AttributesDescriptor("Value", EnvSyntaxHighlighter.VALUE),
            new AttributesDescriptor("Bad value", EnvSyntaxHighlighter.BAD_CHARACTER)
    };

    @Override
    public @Nullable Icon getIcon() {
        return DevToolsIcons.ENV_FILE;
    }

    @Override
    public @NotNull SyntaxHighlighter getHighlighter() {
        return new EnvSyntaxHighlighter();
    }

    @Override
    public @NonNls @NotNull String getDemoText() {
        return "# You are reading the \".properties\" entry.\n" +
                "! The exclamation mark can also mark text as comments.\n" +
                "website = https://en.wikipedia.org/\n" +
                "language = English\n" +
                "# The backslash below tells the application to continue reading\n" +
                "# the value onto the next line.\n" +
                "message = Welcome to \\\n" +
                "          Wikipedia!\n" +
                "# Add spaces to the key\n" +
                "key\\ with\\ spaces = This is the value that could be looked up with the key \"key with spaces\".\n" +
                "# Unicode\n" +
                "tab : \\u0009";
    }

    @Override
    public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @Override
    public @NotNull @NlsContexts.ConfigurableName String getDisplayName() {
        return "Env";
    }
}
