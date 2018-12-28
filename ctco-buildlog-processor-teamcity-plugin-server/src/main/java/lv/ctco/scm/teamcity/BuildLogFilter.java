package lv.ctco.scm.teamcity;

/*
 * @(#)BuildLogFilter.java
 *
 * Copyright C.T.Co Ltd, 33 Meistaru Street, Valdlauči, Ķekava district, LV-1076, Latvia. All rights reserved.
 */

import jetbrains.buildServer.messages.BuildMessage1;
import jetbrains.buildServer.messages.BuildMessagesTranslator;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import jetbrains.buildServer.messages.ErrorData;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.SRunningBuild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class BuildLogFilter implements BuildMessagesTranslator {
    private final Passwords myPasswords;

    public BuildLogFilter(@NotNull final Passwords passwords) {
        myPasswords = passwords;
    }

    @NotNull
    public List<BuildMessage1> translateMessages(@NotNull final SRunningBuild build, @NotNull final List<BuildMessage1> messages) {

        BuildMessagesTranslator value;

        final Collection<String> passwords = myPasswords.getPasswordsToReplace(build).values();

        final PasswordReplacer filter = createPasswordsFilter(passwords);
        value = filter == null ? emptyProcessor : new LogProcessor(filter);

        return value.translateMessages(build, messages);
    }

    private final BuildMessagesTranslator emptyProcessor = new BuildMessagesTranslator() {
        @NotNull
        public List<BuildMessage1> translateMessages(@NotNull final SRunningBuild build, @NotNull final List<BuildMessage1> message) {
            return message;
        }
    };

    private static final class LogProcessor implements BuildMessagesTranslator {
        private final PasswordReplacer passwordReplacer;

        private LogProcessor(@NotNull final PasswordReplacer replacer) {
            passwordReplacer = replacer;
        }

        @NotNull
        public List<BuildMessage1> translateMessages(@NotNull final SRunningBuild build, @NotNull final List<BuildMessage1> originalMessages) {
            List<BuildMessage1> translatedMessages = new ArrayList<BuildMessage1>();

            for (BuildMessage1 buildMessage1 : originalMessages) {
                final Object data = buildMessage1.getValue();

                if ( data == null || !(data instanceof String) && !(data instanceof ErrorData)) {
                    translatedMessages.add(buildMessage1);
                }
                else if(DefaultMessagesInfo.MSG_TEXT.equals(buildMessage1.getTypeId())) {
                    final String text = passwordReplacer.replacePasswords((String) data);
                    translatedMessages.add(DefaultMessagesInfo.createTextMessage(buildMessage1, text));
                }
                else if (DefaultMessagesInfo.MSG_ERROR.equals(buildMessage1.getTypeId())){
                    String localizedMessage = ((ErrorData) data).localizedMessage;
                    final String text = passwordReplacer.replacePasswords(localizedMessage);
                    ErrorData errorData = new ErrorData("", text, "");
                    translatedMessages.add(DefaultMessagesInfo.createError(Status.ERROR, errorData));
                }
                else{
                    translatedMessages.add(buildMessage1);
                }
            }
            return translatedMessages;
        }

    }

    @NotNull
    private static Pattern createPasswordsPattern(@NotNull Set<String> passwords) {
        final StringBuilder sb = new StringBuilder();
        for (String v : passwords) {
            if (sb.length() > 0) {
                sb.append('|');
            }
            sb.append('(').append(v).append(')');
        }
        return Pattern.compile(sb.toString());
    }

    @Nullable
    public static PasswordReplacer createPasswordsFilter(@NotNull final Collection<String> passwords) {
        if (passwords.isEmpty()) {
            return null;
        }
        final Pattern pt = createPasswordsPattern(new TreeSet<String>(passwords));
        return new PasswordReplacer() {
            @NotNull
            public String replacePasswords(@NotNull final String source) {
                return pt.matcher(source).replaceAll("*****");
            }
        };
    }

    private interface PasswordReplacer {
        @NotNull
        String replacePasswords(@NotNull String s);
    }
}