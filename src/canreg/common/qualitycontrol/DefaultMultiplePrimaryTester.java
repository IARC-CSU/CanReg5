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

package canreg.common.qualitycontrol;

/**
 *
 * @author ervikm
 */
public class DefaultMultiplePrimaryTester implements MultiplePrimaryTesterInterface {

    public int multiplePrimaryTest(String topographyOrig, String morphologyOrig,
            String topographySim, String morphologySim) {

        int result = 0;
        boolean decided = false;

        int morphGroupOrig, morphGroupSim, topogGrpOrig, topogGrpSim;

        //=======================< morph test >===========================
        //    get morph group numbers, test whether same || different etc
        //    if result is still undecided, will have to perform topog test

        // See to that morph has max 4 digits
        morphologyOrig = morphologyOrig.substring(0,Math.min(4,morphologyOrig.length()));
        morphologySim = morphologySim.substring(0,Math.min(4,morphologySim.length()));

        morphGroupOrig = getHistologyGroup(morphologyOrig);
        morphGroupSim = getHistologyGroup(morphologySim);

        //--------------< either is invalid ? >-----------
        if (morphGroupOrig == 0 || morphGroupSim == 0) {
            return mptInvalid;        //--------------< either is unspecified ? >-----------
        }
        if (morphGroupOrig == 17 || morphGroupSim == 17) {
            decided = false;
        } else {
            //----------< both carcinoma, one carcinoma NOS ? >-------
            if ((morphGroupOrig == 5 && morphGroupSim <= 5) || (morphGroupSim == 5 && morphGroupOrig <= 5)) {
                decided = false;    // need to test topog
            } //--------< both Hematop/Lymphoid (8-13), but one unspec (14)
            else if ((morphGroupOrig >= 8 && morphGroupOrig <= 13 && morphGroupSim == 14) || (morphGroupSim >= 8 && morphGroupSim <= 13 && morphGroupOrig == 14)) {
                decided = true;
                result = mptDuplicate;
            } else {
                //----------< two morph groups equal?
                if (morphGroupOrig == morphGroupSim) {
                    //--------< "Systemic" - only one allowed
                    if (morphGroupOrig >= 7 && morphGroupOrig <= 15) {
                        decided = true;
                        result = mptDuplicate;
                    } else {
                        decided = false;    // same morph group - need to test topog
                    }
                } else {
                    decided = true;
                    result = mptMultPrim;    // morph groups are different
                }
            }
        }

        if (decided) {
            return result;
        //==================== topog test =====================
        // morphs are same; || unspecified.......
        //{
        //    String m = (String)topog_orig + "," + (String)morph_orig + ", " +(String)topog_sim + "," + (String)morph_sim;
        //    MessageForm->ShowMessage (VerboseStyle, "Topog test:", m);
        //}
        }
        topogGrpOrig = getTopographyGroup(topographyOrig);
        topogGrpSim = getTopographyGroup(topographySim);

        if (topogGrpOrig == 80 && topogGrpSim == 80) // both are unspecified
        {
            result = mptDuplicate;
        } else if (topogGrpOrig == 80 || topogGrpSim == 80) // one is unspecified
        {
            result = mptUnkTopog;
        } else {
            if (topogGrpOrig == topogGrpSim) {
                result = mptDuplicate;    // two topog groups equal
            } else {
                result = mptMultPrim;        // two topog groups different
            }
        }

        //    MessageForm->ShowMessage (VerboseStyle, "MPtest result:", (String)(int)result);

        return result;
    }

    private static int getHistologyGroup(String morphStr) {
        if (morphStr.trim().length() == 0) {
            return 0;
        }
        int morphology = Integer.parseInt(morphStr);
        if (morphology < 8000) // invalid
        {
            return 0;
        }
        int morphology_group = 0;

        //================================================< CARCINOMAS

        //---------------< Squamous & Transitional cell carcinomas

        if ((morphology >= 8051 & morphology <= 8084) ||
                (morphology >= 8120 & morphology <= 8131)) {
            morphology_group = 1;

        //-----------------------------------< Basal Cell carcinomas
        } else if (morphology >= 8090 & morphology <= 8110) {
            morphology_group = 2;

        //-----------------------------------------< Adenocarcinomas
        } else if ((morphology >= 8140 & morphology <= 8149) ||
                (morphology >= 8160 & morphology <= 8162) ||
                (morphology >= 8190 & morphology <= 8221) ||
                (morphology >= 8260 & morphology <= 8337) ||
                (morphology >= 8350 & morphology <= 8551) ||
                (morphology >= 8570 & morphology <= 8576) ||
                (morphology >= 8940 & morphology <= 8941)) {
            morphology_group = 3;

        //-------------------------------< Other specific Carcinomas
        } else if ((morphology >= 8030 & morphology <= 8046) ||
                (morphology >= 8150 & morphology <= 8157) ||
                (morphology >= 8170 & morphology <= 8180) ||
                (morphology >= 8230 & morphology <= 8255) ||
                (morphology >= 8340 & morphology <= 8347) ||
                (morphology >= 8560 & morphology <= 8562) ||
                (morphology >= 8580 & morphology <= 8671)) {
            morphology_group = 4;

        //----------------------------------< Unspecified Carcinomas
        } else if ((morphology >= 8010 & morphology <= 8015) ||
                (morphology >= 8020 & morphology <= 8022) ||
                (morphology == 8050)) {
            morphology_group = 5;

        //=========================< SARCOMAS & other soft tissue tumours
        } else if ((morphology >= 8680 & morphology <= 8713) ||
                (morphology >= 8800 & morphology <= 8921) ||
                (morphology >= 8990 & morphology <= 8991) ||
                (morphology >= 9040 & morphology <= 9044) ||
                (morphology >= 9120 & morphology <= 9125) ||
                (morphology >= 9130 & morphology <= 9136) ||
                (morphology >= 9141 & morphology <= 9252) ||
                (morphology >= 9370 & morphology <= 9373) ||
                (morphology >= 9540 & morphology <= 9582)) {
            morphology_group = 6;

        //=================================================< MESOTHELIOMAS
        } else if (morphology >= 9050 & morphology <= 9055) {
            morphology_group = 7;

        //================================< HEMATOPOIETIC, LYMPHOID TISSUE
        //------------------------------------------------< Myeloid
        } else if ((morphology == 9840) ||
                (morphology >= 9861 & morphology <= 9931) ||
                (morphology >= 9945 & morphology <= 9946) ||
                (morphology == 9950) ||
                (morphology >= 9961 & morphology <= 9964) ||
                (morphology >= 9980 & morphology <= 9987)) {
            morphology_group = 8;

        //-------------------------------------------------< B-Cell
        } else if ((morphology >= 9670 & morphology <= 9699) ||
                (morphology == 9728) ||
                (morphology >= 9731 & morphology <= 9734) ||
                (morphology >= 9761 & morphology <= 9767) ||
                (morphology == 9769) ||
                (morphology >= 9823 & morphology <= 9826) ||
                (morphology == 9833) ||
                (morphology == 9836) ||
                (morphology == 9940)) {
            morphology_group = 9;

        //-----------------------------------------< T-Cell, NK-Cell
        } else if ((morphology >= 9700 & morphology <= 9719) ||
                (morphology == 9729) ||
                (morphology == 9768) ||
                (morphology >= 9827 & morphology <= 9831) ||
                (morphology == 9834) ||
                (morphology == 9837) ||
                (morphology == 9948)) {
            morphology_group = 10;

        //----------------------------------------< Hodgkin Lymphoma
        } else if (morphology >= 9650 & morphology <= 9667) {
            morphology_group = 11;

        //-----------------------------------------------< Mast-cell
        } else if (morphology >= 9740 & morphology <= 9742) {
            morphology_group = 12;

        //-------------------< Histiocytes, Accessory Lymphoid cells
        } else if (morphology >= 9750 & morphology <= 9758) {
            morphology_group = 13;

        //---------------------------------------< Unspecified types
        } else if ((morphology >= 9590 & morphology <= 9591) ||
                (morphology == 9596) ||
                (morphology == 9727) ||
                (morphology == 9760) ||
                (morphology >= 9800 & morphology <= 9801) ||
                (morphology == 9805) ||
                (morphology == 9820) ||
                (morphology == 9832) ||
                (morphology == 9835) ||
                (morphology == 9860) ||
                (morphology == 9960) ||
                (morphology == 9970) ||
                (morphology == 9975) ||
                (morphology == 9989)) {
            morphology_group = 14;

        //==============================================< KAPOSI SARCOMA
        } else if (morphology == 9140) {
            morphology_group = 15;

        //=============================================< OTHER SPECIFIED
        } else if ((morphology >= 8720 & morphology <= 8790) ||
                (morphology >= 8930 & morphology <= 8936) ||
                (morphology >= 8950 & morphology <= 8983) ||
                (morphology >= 9000 & morphology <= 9030) ||
                (morphology >= 9060 & morphology <= 9110) ||
                (morphology >= 9260 & morphology <= 9365) ||
                (morphology >= 9380 & morphology <= 9539)) {
            morphology_group = 16;

        //=================================================< UNSPECIFIED
        } else if (morphology >= 8000 & morphology <= 8005) {
            morphology_group = 17;

        //---------------------------< not allocated, must be invalid
        } else {
            morphology_group = 0;
        }
        return morphology_group;
    }

    public static int getTopographyGroup(String topogStr) {

        String topogStrTemp = new String(topogStr);

        // As found in CanReg4
        if (topogStrTemp.trim().length() == 0) {
            return 0;
        }

        if (topogStrTemp.substring(0, 1).equalsIgnoreCase("C")) {
            topogStrTemp = topogStrTemp.substring(1);
        }

        int topog = 0;
        try {
            topog = Integer.parseInt(topogStrTemp) / 10;
        } catch (NumberFormatException nfe) {
            System.out.println(topogStrTemp + " does not start with a number or a C");
            return 0;
        }
        int topogGroup = 0;

        if (topog == 1 || topog == 2) {
            topogGroup = 1;
        } else if (topog == 0 || topog == 3 || topog == 4 || topog == 5 || topog == 6) {
            topogGroup = 3;
        } else if (topog == 9 || topog == 10 || topog == 12 || topog == 13 || topog == 14) {
            topogGroup = 9;
        } else if (topog == 19 || topog == 20) {
            topogGroup = 19;
        } else if (topog == 23 || topog == 24) {
            topogGroup = 23;
        } else if (topog == 33 || topog == 34) {
            topogGroup = 33;
        } else if (topog == 40 || topog == 41) {
            topogGroup = 40;
        } else if (topog == 65 || topog == 66 || topog == 67 || topog == 68) {
            topogGroup = 65;
        } else {
            topogGroup = topog;
        }
        return topogGroup;
    }
}
