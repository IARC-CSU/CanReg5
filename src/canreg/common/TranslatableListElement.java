/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2013  International Agency for Research on Cancer
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
 * @author Morten Johannes Ervik, CIN/IARC, ervikm@iarc.fr
 */


package canreg.common;

/**
 *
 * @author ervikm
 * A simple way to translate list elements
 */
public class TranslatableListElement {
    String translatedName;
    String originalName;

    public TranslatableListElement(String originalName, String translatedName) {
        this.originalName = originalName;
        this.translatedName = translatedName;
    }

    @Override
    public String toString() {
        return translatedName;
    }

    @Override
    public boolean equals(Object o) {
        boolean equal = false;
        if (o instanceof TranslatableListElement) {
            equal = this.hashCode() == o.hashCode();
        } else {
            super.equals(o);
        }
        return equal;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.originalName != null ? this.originalName.hashCode() : 0);
        return hash;
    }

    public String getOriginalName() {
        return originalName;
    }
}
