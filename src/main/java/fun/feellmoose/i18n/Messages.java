package fun.feellmoose.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

public class Messages {

    public static final Locale LOCALE_CN_CXG = new Locale.Builder()
            .setLanguage("zh")
            .setRegion("CN")
            .setVariant("CiXioGui")
            .build();

    public record MessageString(String template) {
        public String formatted(Object... args) {
            if (args.length == 0) return template;
            return String.format(this.template, args);
        }
    }

    public static MessageString load(String description, String language, String country) {
        return load(description, Locale.of(language, country));
    }

    public static MessageString load(String description, Locale locale) {
        return new MessageString(ResourceBundle.getBundle("messages", locale).getString(description));
    }

}
