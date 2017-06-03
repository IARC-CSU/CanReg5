//<ictl.co>
package canreg.common;

import java.util.Locale;

/**
 * Created by amin on 8/10/2015.
 */
public class LocalizationHelper {

    LocalizationHelper() {
    }

    public static Boolean isGregorianLocale() {
        return "en".equals(Locale.getDefault().getLanguage());
    }

    public static Boolean isPersianLocale() {
        return "fa".equals(Locale.getDefault().getLanguage());
    }

    public static Boolean isRtlLanguageActive() {
        return isPersianLocale();
    }
}

//<ictl.co>