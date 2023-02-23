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
 * @author Andy Cooke
 */
package canreg.common.qualitycontrol;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.PersonSearchVariable;
import canreg.common.Soundex;
import canreg.common.database.Patient;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
//import org.apache.commons.codec.language.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The default person search module
 * @author ervikm
 * based on code from DEPedits by Andy Cooke 2008
 *
 */
public class DefaultPersonSearch implements PersonSearcher, Serializable {

    private static final Logger LOGGER = Logger.getLogger(PersonSearcher.class.getName());
    private String[] variableNames;
    static final int missing = -1;  // flag to say variable value missing
    private float[] discPower;
    private float[] reliability;
    private float[] presence;
    private float threshold = 50;
    private float maximumTotalScore;
    private float[] variableWeights;
    private boolean[] lockeds;
    Map<String, DatabaseVariablesListElement> variablesInDBMap;
    private PersonSearchVariable[] searchVariables;
    // private Soundex soundex = new Soundex();
    // private DoubleMetaphone doubleMetaphone = new DoubleMetaphone();
    // private Caverphone caverphone = new Caverphone();

    /**
     *
     * @param variablesInDB
     */
    public DefaultPersonSearch(DatabaseVariablesListElement[] variablesInDB) {
        variablesInDBMap = new LinkedHashMap<String, DatabaseVariablesListElement>();
        for (DatabaseVariablesListElement dbvle : variablesInDB) {
            variablesInDBMap.put(dbvle.getShortName(), dbvle);
        }
    }

    /**
     *
     * @return
     */
    public synchronized PersonSearchVariable[] getPersonSearchVariables() {
        return searchVariables;
    }

    /**
     *
     * @param personSearchVariables
     */
    @Override
    public synchronized void setSearchVariables(PersonSearchVariable[] personSearchVariables) {
        int i = 0;
        searchVariables = personSearchVariables;
        variableNames = new String[personSearchVariables.length];
        variableWeights = new float[personSearchVariables.length];
        discPower = new float[variableNames.length];
        reliability = new float[variableNames.length];
        presence = new float[variableNames.length];
        lockeds = new boolean[variableNames.length];
        for (PersonSearchVariable psv : personSearchVariables) {
            variableNames[i] = psv.getName();
            variableWeights[i] = psv.getWeight();
            discPower[i] = psv.getDiscPower();
            reliability[i] = psv.getReliability();
            presence[i] = psv.getPresence();
            lockeds[i] = psv.isBlock();
            i++;
        }

        maximumTotalScore = 0;

        for (int varb = 0; varb < variableNames.length; ++varb) {

            float dis = discPower[varb];
            float rel = reliability[varb];
            float pres = presence[varb];
            float weigth = variableWeights[varb];


            int sim = 100;
            float maxscore = scoreFunction(dis, rel, pres, sim, weigth);
            maximumTotalScore += maxscore;
        }
    }

    /**
     * get all the variables of a patient available in the database and return only the variables selected beforehand
     * plus the RecordID of the patient
     *
     * @param patient contains all the variables available in the database for a patient
     * @param patientRecordIDvariableName string =  "patientRecordIDvariableName". That name allow to get the recordId
     * of the patient
     * @return a list that contain the selected variable plus the recordID of the patient
     */
    public Object[] getPatientVariables(Patient patient, String patientRecordIDvariableName) {
        if (variableNames == null) {
            throw (new NullPointerException());
        }
        Object[] result = new Object[variableNames.length + 1];
        for (int link = 0; link < variableNames.length; ++link) {
            result[link] = patient.getVariable(variableNames[link]);
        }
        // Add the patient record id at the end of the list
        result[variableNames.length] = patient.getVariable(patientRecordIDvariableName);
        return result;
    }

    /**
     *
     * @param patient1
     * @param patient2
     * @return
     */
    @Override
    public synchronized float compare(Patient patient1, Patient patient2) {
        if (variableNames == null) {
            throw (new NullPointerException());
        }
        float similarity = comparePatients(patient1, patient2);
        float perCent = 100 * similarity / maximumTotalScore; // scale 0 to max
        //DEPeditsInst.Warning ("PerCent:"+DEPeditsInst.FloatToStr(PerCent,1));
        return perCent;
    }

    private float comparePatients(Patient patient1, Patient patient2) {
        int similarity = 0;
        float TotalScore = 0;
        String patient1data;
        String patient2data;
        DatabaseVariablesListElement dbvle;
        CompareAlgorithms compareAlgorithm;
        boolean locked;

        for (int link = 0; link < variableNames.length; ++link) {
            dbvle = variablesInDBMap.get(variableNames[link]);
            String unknownCode = null;
            if (dbvle.getUnknownCode() != null) {
                unknownCode = dbvle.getUnknownCode().toString();
            }

            Object patient1dataObject = patient1.getVariable(variableNames[link]);
            Object patient2dataObject = patient2.getVariable(variableNames[link]);

            if (patient1dataObject != null && patient2dataObject != null) {

                patient1data = patient1dataObject.toString();
                patient2data = patient2dataObject.toString();

                compareAlgorithm = searchVariables[link].getCompareAlgorithm();
                locked = lockeds[link];

                if (patient1data.trim().length() == 0 || patient2data.trim().length() == 0) {
                    similarity = missing;
                } else if (patient1data.equals(unknownCode)) {
                    similarity = missing;
                } else if (patient2data.equals(unknownCode)) {
                    similarity = missing;
                } else if (locked) {
                    similarity = (compareAlgorithm.equals(CompareAlgorithms.date)) ? compareExact(patient1data.substring(0, 4), patient2data.substring(0, 4)) : compareExact(patient1data, patient2data);
                    if (similarity < -100){
                        return 0;
                    }
                } else if (compareAlgorithm.equals(CompareAlgorithms.code)) {
                    similarity = compareCodes(patient1data, patient2data);
                } else if (compareAlgorithm.equals(CompareAlgorithms.alpha)) {
                    similarity = compareText(patient1data, patient2data);
                } else if (compareAlgorithm.equals(CompareAlgorithms.date)) {
                    similarity = compareDate(patient1data, patient2data);
                } else if (compareAlgorithm.equals(CompareAlgorithms.number)) {
                    similarity = compareNumber(patient1data, patient2data);
                } else if (compareAlgorithm.equals(CompareAlgorithms.soundex)) {
                    similarity = compareSoundex(patient1data, patient2data);
                } else {
                    similarity = compareText(patient1data, patient2data);
                }

                float score;
                if (similarity == missing) {
                    score = 0;
                } else {
                    float dis = discPower[link];
                    float rel = reliability[link];
                    float pres = presence[link];
                    float weigth = variableWeights[link];
                    score = scoreFunction(dis, rel, pres, similarity, weigth);
                }
                // String s = LinkField1[link]+", "+LinkField2[link]+"  Sim:"+Integer.toString(Similarity)+"  Score:"+undup.FloatToStr(Score, 1);
                // DEPeditsInst.Warning (s);
                // similDisp[link] = Similarity;
                // scoreDisp[link] = score;
                TotalScore += score;
            }
        }

        return TotalScore;
    }

    /**
     * Compare the two patients data to generate a score of similarity
     *
     * @param patient1 data of the patient1
     * @param patient2 data of the patient2
     * @return perCent similarity score
     */
    public synchronized float compareDataOnly(Object[] patient1, Object[] patient2) {
        if (variableNames == null) {
            throw (new NullPointerException());
        }
        float similarity = comparePatientsDataOnly(patient1, patient2);
        float perCent = 100 * similarity / maximumTotalScore; // scale 0 to max
        return perCent;
    }


    /**
     *  Compare the two patientsonly with the variables, weights and algorithm selected beforehand
     * @param patient1 object containing the patient 1 data
     * @param patient2 object containing the patient 2 data
     * @return
     */
    private float comparePatientsDataOnly(Object[] patient1, Object[] patient2) {
        int similarity = 0;
        float totalScore = 0;
        String patient1data;
        String patient2data;
        DatabaseVariablesListElement dbvle;
        CompareAlgorithms compareAlgorithm;
        boolean locked;

        for (int link = 0; link < variableNames.length; ++link) {
            dbvle = variablesInDBMap.get(variableNames[link]);
            String unknownCode = null;
            if (dbvle.getUnknownCode() != null) {
                unknownCode = dbvle.getUnknownCode().toString();
            }

            Object patient1dataObject = patient1[link];
            Object patient2dataObject = patient2[link];

            if (patient1dataObject != null && patient2dataObject != null) {

                patient1data = patient1dataObject.toString();
                patient2data = patient2dataObject.toString();

                compareAlgorithm = searchVariables[link].getCompareAlgorithm();
                locked = lockeds[link];

                if (patient1data.trim().length() == 0 || patient2data.trim().length() == 0) {
                    similarity = missing;
                } else if (patient1data.equals(unknownCode)) {
                    similarity = missing;
                } else if (patient2data.equals(unknownCode)) {
                    similarity = missing;
                } else if (locked) {
                    similarity = (compareAlgorithm.equals(CompareAlgorithms.date)) ? compareExact(patient1data.substring(0, 4), patient2data.substring(0, 4)) : compareExact(patient1data, patient2data);
                } else if (compareAlgorithm.equals(CompareAlgorithms.code)) {
                    similarity = compareCodes(patient1data, patient2data);
                } else if (compareAlgorithm.equals(CompareAlgorithms.alpha)) {
                    similarity = compareText(patient1data, patient2data);
                } else if (compareAlgorithm.equals(CompareAlgorithms.date)) {
                    similarity = compareDate(patient1data, patient2data);
                } else if (compareAlgorithm.equals(CompareAlgorithms.number)) {
                    similarity = compareNumber(patient1data, patient2data);
                } else if (compareAlgorithm.equals(CompareAlgorithms.soundex)) {
                    similarity = compareSoundex(patient1data, patient2data);
                } else if (compareAlgorithm.equals(CompareAlgorithms.exact)) {
                    similarity = compareExact(patient1data, patient2data);
                } else {
                    similarity = compareText(patient1data, patient2data);
                }

                float score;
                if (similarity == missing) {
                    score = 0;
                } else {
                    float dis = discPower[link];
                    float rel = reliability[link];
                    float pres = presence[link];
                    float weigth = variableWeights[link];
                    score = scoreFunction(dis, rel, pres, similarity, weigth);
                }
                totalScore += score;
            }
        }
        return totalScore;
    }

    private int compareCodes(String s1, String s2) {
        // called from Compare
        // finds how many common chars in same positions in s1 and s2
        // similarity points added if same from beginning
        // eg 1234,1235 higher than 1234,2234
        // (considering hierarchical coding system / keying errors)
        //------------------------------------------
        int len1 = s1.length();
        int len2 = s2.length();
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int BigLen, SmallLen;
        if (len1 >= len2) {
            BigLen = len1;
            SmallLen = len2;
        } else {
            BigLen = len2;
            SmallLen = len1;
        }
        int score = 0;
        boolean fromstart = true;
        for (int i = 0; i < SmallLen; ++i) {
            if (s1.charAt(i) == s2.charAt(i)) {
                score += 2;
                if (fromstart) {
                    ++score;
                }
            } else {
                fromstart = false;
            }
        }
        score = (100 * score) / (3 * BigLen);
        return score;
    }
    //_______________________________________________________________

    private int compareText(String s1, String s2) {
        // called from Compare
        // finds two longest common strings in s1 and s2
        // similarity points awarded according to lengths of common strings
        // with penalties for break, wrong order
        //------------------------------------------

        int len1 = s1.length();
        int len2 = s2.length();
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        String BigStr, SmallStr;
        int BigLen, SmallLen;
        if (len1 >= len2) {
            BigStr = s1;
            SmallStr = s2;
            BigLen = len1;
            SmallLen = len2;
        } else {
            BigStr = s2;
            SmallStr = s1;
            BigLen = len2;
            SmallLen = len1;
        }
        int posB[] = new int[2];
        int posS[] = new int[2];
        int matchCount = 0;
        int matchLen = 0;
        for (int len = SmallLen, moves = 1; len >= 1; --len, ++moves) {
            for (int posSmall = 0; posSmall < moves; ++posSmall) {
                String sub = SmallStr.substring(posSmall, posSmall + len);
                int posBig = BigStr.indexOf(sub, 0);
                if (posBig > -1) {
                    String rep1 = "", rep2 = "";
                    for (int i = 0; i < sub.length(); ++i) {
                        rep1 += "~";
                        rep2 += "^";
                    }
                    try //  works on Regular Expression, not string - don't like ( ) etc
                    {
                        SmallStr = SmallStr.replaceAll(sub, rep1);
                        BigStr = BigStr.replaceAll(sub, rep2);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, null, e);
                    }
                    posB[matchCount] = posBig;
                    posS[matchCount] = posSmall;
                    ++matchCount;
                    matchLen += len;
                }
                if (matchCount == 2) {
                    break;
                }
            }
            if (matchCount == 2) {
                break;
            }
        }
        int Score = 200 * matchLen / (SmallLen + BigLen);
        //        if (posB[0] > 0)
        //            Score = (Score * 15)/16;  // penalty for not being beginnig of string
        //        if (posS[0] > 0)
        //            Score = (Score * 15)/16;  // penalty for not being beginnig of string

        if (matchCount == 2) {
            Score = (Score * 15) / 16;  // penalty for being split
            //            if (posB[0] > 0 && posB[1] > 0)
            //                Score = (Score * 15)/16;  // penalty for not being beginnig of string
            //            if (posS[0] > 0 && posS[1] > 0)
            //                Score = (Score * 15)/16;  // penalty for not being beginnig of string
            if ((posB[0] < posB[1] && posS[0] > posS[1]) || (posB[0] > posB[1] && posS[0] < posS[1])) {
                Score = (Score * 15) / 16;  // penalty for wrong order
            }
        }
        // s+="  matchLen:"+Integer.toString (matchLen)+"  ";
        // s+="  Score:"+Integer.toString (Score)+"  ";
        // DEPeditsInst.Warning (s1+" ,"+s2+" :"+s);
        return Score;
    }
    //_______________________________________________________________

    private int compareDate(String s1, String s2) {
        // called from Compare
        // finds similarity between two dates
        // points awarded to same day, month or year
        // or swapped month/day, swapped digits
        //------------------------------------------
        int len1 = s1.length();
        int len2 = s2.length();
        if (len1 < 8 || len2 < 8) {
            return missing;
        }
        int Score = 0;
        //-----------------------------------< consider Year; max score 45
        String YrStr1 = s1.substring(0, 4);
        String YrStr2 = s2.substring(0, 4);
        if (YrStr1.equals(YrStr2)) {
            Score += 45;  // Years equal
        } else // Years not equal
        {
            int YrInt1, YrInt2;
            try {
                YrInt1 = Integer.parseInt(YrStr1);
                YrInt2 = Integer.parseInt(YrStr2);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, null, e);
                return 0;
            }
            if (YrInt1 == YrInt2 + 1 || YrInt1 == YrInt2 - 1) {
                Score += 25;  // Years one year difference
            } else // years not one year different
            {
                if (YrStr1.charAt(0) == YrStr2.charAt(0)) // same century!
                {
                    char y31 = YrStr1.charAt(2);
                    char y32 = YrStr2.charAt(2);
                    char y41 = YrStr1.charAt(3);
                    char y42 = YrStr2.charAt(3);
                    if (y31 == y42 && y32 == y41) {
                        Score += 30;  // year digits reversed
                    } else if (y31 == y32 || y41 == y42) {
                        Score += 15;  // one digit is same
                    }
                }
            }
        }
        //-----------------------------------< consider Month; max score 20
        boolean MonthSame = false;
        String MonStr1 = s1.substring(4, 6);
        String MonStr2 = s2.substring(4, 6);
        if (MonStr1.equals("00") || MonStr1.equals("99") || MonStr2.equals("00") || MonStr2.equals("99")) {
            return Score;
        }
        if (MonStr1.equals(MonStr2)) {
            MonthSame = true;
            Score += 20;  // Months equal
        }

        //-----------------------------------< consider Days; max score 35
        String DayStr1 = s1.substring(6);
        String DayStr2 = s2.substring(6);
        if (DayStr1.equals("00") || DayStr1.equals("99") || DayStr2.equals("00") || DayStr2.equals("99")) {
            return Score;
        }
        if (DayStr1.equals(DayStr2)) {
            Score += 35;  // Days equal
        } else // Days not equal
        {
            char d11 = DayStr1.charAt(0);
            char d12 = DayStr2.charAt(0);
            char d21 = DayStr1.charAt(1);
            char d22 = DayStr2.charAt(1);
            if (d11 == d22 && d12 == d21) {
                Score += 20;  // Day digits reversed
            } else if (d11 == d12) {
                Score += 5;  // just first digit is same (0,1,2,3)
            } else if (d21 == d22) {
                Score += 10;  // second digit is same
            }
            if (!MonthSame) //Day different; Month different - Swapped?
            {
                if (DayStr1.equals(MonStr2) && DayStr2.equals(MonStr1)) {
                    Score += 30;  // Day/Month swap
                }
            }
        }
        //DEPeditsInst.Warning (s1+" ,"+s2+"  : "+Integer.toString (Score));

        return Score;
    }
    //_______________________________________________________________

    private int compareNumber(String s1, String s2) {
        // called from Compare
        // finds similarity of two numbers
        // points awarded for proportional numeric closeness
        //------------------------------------------

        /*****  25% error returns 0

         eg  Age 62, 64
         Diff = 2 , Max = 64
         score = (1 - 4 * 2 / 64) * 100 = 87

         eg  Age 40, 35
         Diff = 5 , Max = 40
         score = (1 - 4 * 5 / 40) * 100 = 50
         ***************************/
        if (s1.equals(s2)) {
            return 100;
        }
        int N1;
        int N2;
        try {
            N1 = Integer.parseInt(s1);
            N2 = Integer.parseInt(s1);
        } catch (NumberFormatException nfe) {
            return 0;
        }
        int Diff, Max;
        if (N1 > N2) {
            Max = N1;
            Diff = N1 - N2;
        } else {
            Max = N2;
            Diff = N2 - N1;
        }
        float score = (1 - 4 * (float) Diff / Max) * 100;
        if (score < 0) {
            score = 0;
        }
        return (int) score;
    }

    private float scoreFunction(float dis, float rel, float pres, float sim, float weigth) {
        float Score = sim * weigth; // 2012
        return Score;
    }

    /**
     *
     * @return
     */
    @Override
    public float getThreshold() {
        return threshold;
    }

    /**
     *
     * @param threshold
     */
    @Override
    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    private int compareSoundex(String patient1data, String patient2data) {
        String soundex1 = Soundex.soundex(patient1data);
        String soundex2 = Soundex.soundex(patient2data);
        if (soundex1.equals(soundex2)) {
            return 100;
        } else {
            return 0;
        }
    }

    private int compareExact(String patient1data, String patient2data) {
        return (patient1data.equals(patient2data)) ? 100 : -1000;
    }

    @Override
    public PersonSearchVariable[] getSearchVariables() {
        return searchVariables;
    }
}
