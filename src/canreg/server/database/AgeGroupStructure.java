/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.server.database;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * The structure of the age groups used in the population datasets are governed by 4 numbers
 * - the regular size of the groups
 * - the highest age
 * - the size of the lowest age group
 * - the cut of age
 * 
 * @author ervikm
 */
public class AgeGroupStructure implements Serializable {

    private int sizeOfGroups;
    private int maxAge;
    private int sizeOfFirstGroup;
    private int cutOfAge;
    private String[] ageGroupNames;
    private String constructor;

    /**
     * 
     * @param sizeOfGroups
     * @param maxAge
     */
    public AgeGroupStructure(int sizeOfGroups, int maxAge) {
        this(sizeOfGroups, maxAge, sizeOfGroups, Integer.MAX_VALUE);
        this.constructor = sizeOfGroups + "," + maxAge;
    }

    /**
     * 
     * @param sizeOfGroups
     * @param maxAge
     * @param sizeOfFirstGroup
     */
    public AgeGroupStructure(int sizeOfGroups, int maxAge, int sizeOfFirstGroup) {
        this(sizeOfGroups, maxAge, sizeOfFirstGroup, Integer.MAX_VALUE);
        this.constructor = sizeOfGroups + "," + maxAge + "," + sizeOfFirstGroup;
    }

    /**
     * 
     * @param sizeOfGroups
     * @param maxAge
     * @param sizeOfFirstGroup
     * @param cutOfAge
     */
    public AgeGroupStructure(int sizeOfGroups, int maxAge, int sizeOfFirstGroup, int cutOfAge) {
        this.cutOfAge = cutOfAge;
        this.sizeOfFirstGroup = sizeOfFirstGroup;
        this.sizeOfGroups = sizeOfGroups;
        this.maxAge = maxAge;
        this.ageGroupNames = generateAgeGroupNames();
        this.constructor = sizeOfGroups + "," + maxAge + "," + sizeOfFirstGroup + "," + cutOfAge;
    }

    public AgeGroupStructure(String constructor) {
        this.constructor = constructor;

        String[] arguments = constructor.split(",");

        // Set defaults
        this.sizeOfGroups = 5;
        this.maxAge = 85;
        this.sizeOfFirstGroup = 5;
        this.cutOfAge = Integer.MAX_VALUE;

        // Overwrite defaults depending on input
        if (arguments.length > 1) {
            this.sizeOfGroups = Integer.parseInt(arguments[0].trim());
            this.maxAge = Integer.parseInt(arguments[1].trim());
            if (arguments.length > 2) {
                this.sizeOfFirstGroup = Integer.parseInt(arguments[2].trim());
                if (arguments.length > 3) {
                    this.cutOfAge = Integer.parseInt(arguments[3].trim());
                }
            }
        }

    }
    
    public String getConstructor(){
        return constructor;
    }

    /**
     * 
     * @return
     */
    public String[] getAgeGroupNames() {
        return ageGroupNames;
    }

    public int getNumberOfAgeGroups() {
        return ageGroupNames.length;
    }

    /**
     * 
     * @param age
     * @return
     */
    public int whatAgeGroupIsThisAge(int age) {
        int group = 0;
        if (age < sizeOfFirstGroup) {
            group = 0;
        } else if (age >= cutOfAge) {
            group = -1;
        } else {
            if (age >= maxAge) {
                age = maxAge;
            }
            if (sizeOfFirstGroup == 1) {
                group = age / sizeOfGroups + 1;
            } else {
                group = (age - sizeOfFirstGroup) / sizeOfGroups + 1;
            }
        }
        return group;
    }

    private String[] generateAgeGroupNames() {
        LinkedList<String> strings = new LinkedList<String>();
        int year = 0;
        if (sizeOfFirstGroup == 1) {
            strings.add("0");
            strings.add("1-4");
            year = 5;
        } else {
            String string = year + "-";
            year = sizeOfFirstGroup;
            string += (year - 1);
            strings.add(string);
        }
        while (year < maxAge) {
            String string = year + "-";
            year += sizeOfGroups;
            string += (year - 1);
            strings.add(string);
        }
        if (year < cutOfAge) {
            if (cutOfAge == Integer.MAX_VALUE) {
                strings.add(year + "+");
            } else {
                strings.add(year + "-" + (cutOfAge - 1));
            }
        }
        return strings.toArray(new String[0]);

    }

    @Override
    public String toString() {
        String string = new String();
        int year = 0;
        if (sizeOfFirstGroup == 1) {
            string += "0, 1-4";
            year = 5;
        } else {
            string += year + "-";
            year = sizeOfFirstGroup;
            string += (year - 1);
        }
        int loop = 0;
        while (year < maxAge && loop < 3) {
            string += ", " + year + "-";
            year += sizeOfGroups;
            string += (year - 1);
            loop++;
        }
        if (loop >= 3) {
            string += ", ...";
            year = maxAge;
        }
        if (year < cutOfAge) {
            if (cutOfAge == Integer.MAX_VALUE) {
                string += ", " + year + "+";
            } else {
                string += ", " + year + "-" + (cutOfAge - 1);
            }
        }
        return string;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AgeGroupStructure) {
            AgeGroupStructure ags = (AgeGroupStructure) o;
            return (sizeOfGroups == ags.sizeOfGroups &&
                    sizeOfFirstGroup == ags.sizeOfFirstGroup &&
                    maxAge == ags.maxAge &&
                    cutOfAge == ags.cutOfAge);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + this.sizeOfGroups;
        hash = 17 * hash + this.maxAge;
        hash = 17 * hash + this.sizeOfFirstGroup;
        hash = 17 * hash + this.cutOfAge;
        return hash;
    }
}
