/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2015  International Agency for Research on Cancer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */
package canreg.client.gui.tools;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.swing.UIManager;

public class UITools {

    private static final Set<Object> OVERRIDDEN_FONT_KEYS = new HashSet<Object>();

    public static void setUIFont(javax.swing.plaf.FontUIResource f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
                OVERRIDDEN_FONT_KEYS.add(key);
            }
        }
        UIManager.put("defaultFont", f);
        OVERRIDDEN_FONT_KEYS.add("defaultFont");
    }

    /**
     * Removes any font overrides previously applied via {@link #setUIFont} so
     * the active Look & Feel's default fonts take effect again.
     */
    public static void resetUIFont() {
        for (Object key : OVERRIDDEN_FONT_KEYS) {
            UIManager.put(key, null);
        }
        OVERRIDDEN_FONT_KEYS.clear();
    }
}
