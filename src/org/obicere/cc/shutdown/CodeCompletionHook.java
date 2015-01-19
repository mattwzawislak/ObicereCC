package org.obicere.cc.shutdown;

import org.obicere.cc.gui.settings.BooleanSetting;

/**
 * @author Obicere
 */
public class CodeCompletionHook extends SettingsShutDownHook {

    private static final String GROUP_NAME = "Code Completion";

    public static final String NAME = "code.completion";

    @HookValue("true")
    public static final String PARENTHESES_COMPLETION  = "code.completion.parenthesis";
    public static final String PARENTHESES_DESCRIPTION = "Add closing parentheses.";

    @HookValue("true")
    public static final String CURLY_BRACKET_COMPLETION  = "code.completion.curlybracket";
    public static final String CURLY_BRACKET_DESCRIPTION = "Add closing curly brackets.";

    @HookValue("true")
    public static final String SQUARE_BRACKET_COMPLETION  = "code.completion.squarebracket";
    public static final String SQUARE_BRACKET_DESCRIPTION = "Add closing brackets.";

    public CodeCompletionHook() {
        super(GROUP_NAME, NAME, PRIORITY_WINDOW_CLOSING);

        providePanel(PARENTHESES_COMPLETION, new BooleanSetting(this, PARENTHESES_COMPLETION, PARENTHESES_DESCRIPTION));
        providePanel(CURLY_BRACKET_COMPLETION, new BooleanSetting(this, CURLY_BRACKET_COMPLETION, CURLY_BRACKET_DESCRIPTION));
        providePanel(SQUARE_BRACKET_COMPLETION, new BooleanSetting(this, SQUARE_BRACKET_COMPLETION, SQUARE_BRACKET_DESCRIPTION));
    }

}
