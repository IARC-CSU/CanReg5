/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2012  International Agency for Research on Cancer
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
package canreg.client.analysis;

/**
 *
 * @author ErvikM
 */
public class CancerCasesCount implements Comparable {

        private String icd10;
        private String label;
        private Double count;
        private int index;

        public CancerCasesCount(String icd10, String label, Double count, int index) {
            this.label = label;
            this.count = count;
            this.icd10 = icd10;
            this.index = index;
        }

        @Override
        public int compareTo(Object o) {
            if (o instanceof CancerCasesCount) {
                CancerCasesCount other = (CancerCasesCount) o;
                return -getCount().compareTo(other.getCount());
            } else {
                return 0;
            }
        }

        /**
         * @return the icd10
         */
        public String getIcd10() {
            return icd10;
        }

        /**
         * @param icd10 the icd10 to set
         */
        public void setIcd10(String icd10) {
            this.icd10 = icd10;
        }

        /**
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * @param label the label to set
         */
        public void setLabel(String label) {
            this.label = label;
        }

        /**
         * @return the count
         */
        public Double getCount() {
            return count;
        }

        /**
         * @param count the count to set
         */
        public void setCount(Double count) {
            this.count = count;
        }

        @Override
        public String toString() {
            return label + " (" + icd10 + "): " + count.intValue();
        }

        /**
         * @return the index
         */
        public int getIndex() {
            return index;
        }

        /**
         * @param index the index to set
         */
        public void setIndex(int index) {
            this.index = index;
        }
    }
