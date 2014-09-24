package org.obicere.cc.shutdown;

import org.obicere.cc.gui.settings.BooleanSetting;

/**
 * @author Obicere
 */
public class CodeCompletionHook extends SettingsShutDownHook {

    private static final String GROUP_NAME = "Code Completion";

    public static final String NAME = "code.completion";

    @HookValue("true")
    public static final String NEW_LINE_COMPLETION  = "code.completion.newline";
    public static final String NEW_LINE_DESCRIPTION = "Add new estimated tab count.";

    @HookValue("true")
    public static final String PARENTHESES_COMPLETION  = "code.completion.parenthesis";
    public static final String PARENTHESES_DESCRIPTION = "Add closing parentheses.";

    @HookValue("true")
    public static final String CURLY_BRACKET_COMPLETION  = "code.completion.curlybracket";
    public static final String CURLY_BRACKET_DESCRIPTION = "Add closing curly brackets.";

    @HookValue("true")
    public static final String BRACKET_COMPLETION  = "code.completion.bracket";
    public static final String BRACKET_DESCRIPTION = "Add closing brackets.";

    public CodeCompletionHook() {
        super(GROUP_NAME, NAME, PRIORITY_WINDOW_CLOSING);

        providePanel(NEW_LINE_COMPLETION, new BooleanSetting(this, NEW_LINE_COMPLETION, NEW_LINE_DESCRIPTION));
        providePanel(PARENTHESES_COMPLETION, new BooleanSetting(this, PARENTHESES_COMPLETION, PARENTHESES_DESCRIPTION));
        providePanel(CURLY_BRACKET_COMPLETION, new BooleanSetting(this, CURLY_BRACKET_COMPLETION, CURLY_BRACKET_DESCRIPTION));
        providePanel(BRACKET_COMPLETION, new BooleanSetting(this, BRACKET_COMPLETION, BRACKET_DESCRIPTION));
    }

}
