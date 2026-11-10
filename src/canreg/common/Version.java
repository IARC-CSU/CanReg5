/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2026  International Agency for Research on Cancer
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
 */
package canreg.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses and compares CanReg5 version strings.
 * Handles formats like 5.00.46, 5.00.46BETA1, 5.00.44m, v5.00.46, etc.
 * 
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.who.int
 */
public class Version implements Comparable<Version> {

    private final String original;
    private final int[] numbers;
    private final String suffix;
    private final boolean isPreRelease;

    private static final Pattern DIGIT_SPLIT = Pattern.compile("^([0-9]+)(.*)$");
    private static final Pattern SUFFIX_NUM_PATTERN = Pattern.compile("^(.*?)([0-9]+)$");

    public Version(String versionStr) {
        if (versionStr == null) {
            versionStr = "";
        }
        this.original = versionStr.trim();
        String v = this.original;
        if (v.startsWith("v") || v.startsWith("V")) {
            v = v.substring(1).trim();
        }

        String[] parts = v.split("\\.");
        int[] num = new int[parts.length];
        String suf = "";
        for (int i = 0; i < parts.length; i++) {
            Matcher m = DIGIT_SPLIT.matcher(parts[i]);
            if (m.matches()) {
                try {
                    num[i] = Integer.parseInt(m.group(1));
                } catch (NumberFormatException e) {
                    num[i] = 0;
                }
                if (m.group(2) != null && !m.group(2).isEmpty()) {
                    suf = m.group(2).trim();
                }
            } else {
                num[i] = 0;
                suf = parts[i].trim();
            }
        }
        this.numbers = num;
        this.suffix = suf;
        String upperSuffix = suf.toUpperCase();
        this.isPreRelease = upperSuffix.contains("BETA") || upperSuffix.contains("RC") || upperSuffix.contains("ALPHA");
    }

    public String getOriginal() {
        return original;
    }

    public String getSuffix() {
        return suffix;
    }

    public boolean isPreRelease() {
        return isPreRelease;
    }

    @Override
    public int compareTo(Version other) {
        if (other == null) {
            return 1;
        }
        int maxLen = Math.max(this.numbers.length, other.numbers.length);
        for (int i = 0; i < maxLen; i++) {
            int n1 = i < this.numbers.length ? this.numbers[i] : 0;
            int n2 = i < other.numbers.length ? other.numbers[i] : 0;
            if (n1 != n2) {
                return Integer.compare(n1, n2);
            }
        }

        // Numbers are identical. Compare suffixes.
        if (this.suffix.equalsIgnoreCase(other.suffix)) {
            return 0;
        }

        // Final release (no suffix) vs pre-release (BETA/RC/ALPHA):
        // Final release is newer than pre-release of the same version!
        if (this.suffix.isEmpty() && other.isPreRelease) {
            return 1;
        }
        if (this.isPreRelease && other.suffix.isEmpty()) {
            return -1;
        }

        // Patch letter (e.g. 5.00.44m vs 5.00.44 base):
        // A letter patch like 44m was released after base 44.
        if (this.suffix.isEmpty() && !other.isPreRelease && !other.suffix.isEmpty()) {
            return -1;
        }
        if (!this.suffix.isEmpty() && !this.isPreRelease && other.suffix.isEmpty()) {
            return 1;
        }

        // Pre-release type comparison: RC > BETA > ALPHA
        if (this.isPreRelease && other.isPreRelease) {
            int preType1 = getPreReleasePriority(this.suffix);
            int preType2 = getPreReleasePriority(other.suffix);
            if (preType1 != preType2) {
                return Integer.compare(preType1, preType2);
            }
        }

        // Compare suffixes numerically if they end in digits (e.g. BETA1 vs BETA3)
        return compareSuffixesWithNumbers(this.suffix, other.suffix);
    }

    private static int getPreReleasePriority(String s) {
        String u = s.toUpperCase();
        if (u.contains("RC")) {
            return 3;
        }
        if (u.contains("BETA")) {
            return 2;
        }
        if (u.contains("ALPHA")) {
            return 1;
        }
        return 0;
    }

    private static int compareSuffixesWithNumbers(String s1, String s2) {
        Matcher m1 = SUFFIX_NUM_PATTERN.matcher(s1);
        Matcher m2 = SUFFIX_NUM_PATTERN.matcher(s2);
        if (m1.matches() && m2.matches()) {
            int prefixCmp = m1.group(1).compareToIgnoreCase(m2.group(1));
            if (prefixCmp != 0) {
                return prefixCmp;
            }
            try {
                int n1 = Integer.parseInt(m1.group(2));
                int n2 = Integer.parseInt(m2.group(2));
                return Integer.compare(n1, n2);
            } catch (NumberFormatException ignored) {
            }
        }
        return s1.compareToIgnoreCase(s2);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return compareTo((Version) obj) == 0;
    }

    @Override
    public int hashCode() {
        return original.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return original;
    }
}

