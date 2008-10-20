package canreg.common.qualitycontrol;

import canreg.common.DatabaseVariablesListElement;
import canreg.server.database.Patient;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 *
 * @author ervikm
 */
public class PersonSearch implements PersonSearcher {

    private String[] variableNames;
    static final int missing = -1;  // flag to say variable value missing
    
    private float[] discPower;
    private float[] reliability;
    private float[] presence;
    
    private float maximumTotalScore;
    private float[] variableWeights;
    Map <String,DatabaseVariablesListElement> variablesInDBMap;

    public PersonSearch(DatabaseVariablesListElement[] variablesInDB) {
        variablesInDBMap = new LinkedHashMap<String,DatabaseVariablesListElement>();
        for (DatabaseVariablesListElement dbvle:variablesInDB){
            variablesInDBMap.put(dbvle.getShortName(), dbvle);
        }
    }

    public void setWeights(String[] variableNames, float[] variableWeigths) {
        this.variableNames = variableNames;
        this.variableWeights = variableWeigths;
        
        discPower = new float[variableNames.length];
        reliability = new float[variableNames.length];
        presence = new float[variableNames.length];
        
        // Temporarily we set all variables to 1.
        // TODO
        // Find a way to calculate this efficiently...
        for (int i = 0; i<variableNames.length; i++){
            discPower[i]=1;
            reliability[i]=1;
            presence[i]=1;
        }
        
        maximumTotalScore = 0;
  
        for (int varb = 0; varb < variableNames.length; ++varb) {

            float dis = discPower[varb];
            float rel = reliability[varb];
            float pres = presence[varb];
            
            int sim = 100;
            float maxscore = scoreFunction(dis, rel, pres, sim);
            maximumTotalScore += maxscore;
        }
    }

    public float compare(Patient patient1, Patient patient2) {
        if (variableNames==null){
            throw (new NullPointerException());
        }
        float similarity = comparePatients(patient1,patient2);
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
        String varibleType;
        
        for (int link = 0; link < variableNames.length; ++link) {
            dbvle = variablesInDBMap.get(variableNames[link]);
            String unknownCode = null;
            if (dbvle.getUnknownCode()!= null)
                unknownCode = dbvle.getUnknownCode().toString();

            patient1data = patient1.getVariable(variableNames[link]).toString();
            patient2data = patient2.getVariable(variableNames[link]).toString();

            varibleType = dbvle.getVariableType();
            
            if (patient1data.equals("") || patient2data.equals("")) {
                similarity = missing;
            } else if (patient1data.equals(unknownCode)) {
                similarity = missing;
            } else if (patient2data.equals(unknownCode)) {
                similarity = missing;
            } else if (varibleType.equalsIgnoreCase("Dict")){
                    similarity = CompareCodes(patient1data, patient2data);
            } else if (varibleType.equalsIgnoreCase("Alpha")){
                    similarity = CompareText(patient1data, patient2data);
            } else if (varibleType.equalsIgnoreCase("Date")){
                    similarity = CompareDate(patient1data, patient2data);
            } else if (varibleType.equalsIgnoreCase("Number")){
                    similarity = CompareNumber(patient1data, patient2data);
            }

            float score;
            if (similarity == missing) {
                score = 0;
            } else {
                float dis = discPower[link];
                float rel = reliability[link];
                float pres = presence[link];
                score = scoreFunction(dis, rel, pres, similarity);
            }
            //String s = LinkField1[link]+", "+LinkField2[link]+"  Sim:"+Integer.toString(Similarity)+"  Score:"+undup.FloatToStr(Score, 1);
            //DEPeditsInst.Warning (s);
            // similDisp[link] = Similarity;
            // scoreDisp[link] = score;
            TotalScore += score;
        }

        return TotalScore;
    }

    private int CompareCodes(String s1, String s2) {
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
    private int CompareText(String s1, String s2) {
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
                        // DEPeditsInst.Warning(SmallStr+"|"+sub+"|"+rep1+"|"+rep2);/////////////////
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
    private int CompareDate(String s1, String s2) {
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
    private int CompareNumber(String s1, String s2) {
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
            N1=Integer.parseInt(s1);
            N2=Integer.parseInt(s1);
        } catch (NumberFormatException nfe){
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

    private float scoreFunction(float dis, float rel, float pres, float sim) {
        float Score = (sim / 5) * (2 + 4 * rel + 3 * dis) - 60 * rel - 20; // 2007
        //float	Score = (sim / 6) * (2 +4*rel +3*dis + rel*dis) - 6*rel -2;	//	20/08/2003
        return Score;
    }
}
