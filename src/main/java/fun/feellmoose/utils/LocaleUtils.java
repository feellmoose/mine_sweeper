package fun.feellmoose.utils;

import java.util.Locale;

public final class LocaleUtils {

    private LocaleUtils() {}

    static {
        Locale.setDefault(Locale.US);
    }

    public static String toString(Locale locale) {
        return locale.toString();
    }
    public static Locale fromString(String string) {
        String[] parts = string.split("_");
        try {
            if (parts.length == 1) return Locale.of(parts[0]);
            else if (parts.length == 2) return Locale.of(parts[0], parts[1]);
            else if (parts.length == 3) return Locale.of(parts[0], parts[1], parts[2]);
            return Locale.getDefault();
        } catch (Exception e) {
            return Locale.getDefault();
        }
    }
}
