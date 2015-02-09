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

package canreg.common;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ervikm
 */
class Translation {
    private Map<Object, Object> translationMap;
    private Object defaultValue;

    protected Translation() {
        translationMap = new HashMap<Object, Object>();
    }

    protected void setTranslation(Map<Object,Object> translationMap){
        this.translationMap = translationMap;
    }

    protected Object translate(Object originalValue){
        Object returnValue = translationMap.get(originalValue);
        if (returnValue==null){
            returnValue = defaultValue;
        }
        return returnValue;
    }

    /**
     * @return the defaultValue
     */
    protected Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    protected void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    void addTranslation(Object value, Object translatedValue) {
        translationMap.put(value, translatedValue);
    }
}