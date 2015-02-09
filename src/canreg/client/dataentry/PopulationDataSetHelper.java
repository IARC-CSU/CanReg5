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


package canreg.client.dataentry;

import canreg.common.database.AgeGroupStructure;
import canreg.common.database.PopulationDataset;
import canreg.common.database.PopulationDatasetsEntry;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author ervikm
 */
public class PopulationDataSetHelper {

    public static PopulationDataset readCanReg4PDS(String fileName) throws FileNotFoundException, IOException {

        /* From the CanReg4 code
        TablesFileInst->WriteStringLine (Fptr, PDSTitle);

        TablesFileInst->WriteStringLine (Fptr, PDSFilter);

        //----------< date/source >-------------
        Fptr->WriteBuffer ((void*)PDSDate, 8);
        Fptr->WriteBuffer ((void*)"\r\n", 2);

        TablesFileInst->WriteStringLine (Fptr, PDSSource);

        //----------------< Standard Pop label
        TablesFileInst->WriteStringLine (Fptr, PDSStanPop);

        //----------< population details >----------
        Fptr->WriteBuffer ((void*)&NofAgeGroups, 1);

        for (char i=0 ; i<NofAgeGroups ; ++i)
        {
        Fptr->WriteBuffer ((void*)&AgeGroupDist[i], 1);	// min age
        Fptr->WriteBuffer ((void*)&Population [i][0], 4);	// male pop
        Fptr->WriteBuffer ((void*)&Population [i][1], 4);	// female pop
        Fptr->WriteBuffer ((void*)&StandPop[i], 2);	// world stan pop
        }

        Fptr->WriteBuffer ((void*)&UnkAgePop [0], 4);	// unknown age male pop
        Fptr->WriteBuffer ((void*)&UnkAgePop [1], 4);	// unknown age female pop
         */

        PopulationDataset pds = new PopulationDataset();

        // Open the file
        InputStream istream = new FileInputStream(fileName);
        // Decode using a DataInputStream - you can use this if the
        // bytes are written as IEEE format.
        DataInputStream dataStream = new DataInputStream(istream);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataStream));
        // First line is the Title
        String line = bufferedReader.readLine();
        pds.setPopulationDatasetName(line);
        // Second line is the filter
        line = bufferedReader.readLine();
        pds.setFilter(line);

        // Read and set the date
        line = bufferedReader.readLine();
        pds.setDate(line);

        // Third line is the source
        line = bufferedReader.readLine();
        pds.setSource(line);

        // Fourth line is the Standard population label
        line = bufferedReader.readLine();
        if (line.equalsIgnoreCase("World")) {
            pds.setWorldPopulationID(0);
        } else {
            pds.setWorldPopulationID(0); // Default
        }
        // Read the rest of the file as datastream
        bufferedReader.close();
        dataStream.close();
        istream.close();

        // reopen the file
        istream = new FileInputStream(fileName);
        dataStream = new DataInputStream(istream);

        // Skip the five first lines again...
        for (int i = 0; i < 5; i++) {
            dataStream.readLine();
        }
        int numberOfAgeGroups = readNumber(dataStream, 1);
        // System.out.println(numberOfAgeGroups);
        AgeGroupStructure ags = null;
        switch (numberOfAgeGroups) {
            case 18:
                ags = new AgeGroupStructure(5, 85);
                break;
            case 16:
                ags = new AgeGroupStructure(5, 75);
                break;
            case 14:
                ags = new AgeGroupStructure(5, 65);
                break;
            case 8:
                ags = new AgeGroupStructure(10, 75, 15);
                break;
            case 7:
                ags = new AgeGroupStructure(10, 65, 15);
                break;
            case 5:
                ags = new AgeGroupStructure(5, 15, 1, 15);
                break;
        }
        pds.setAgeGroupStructure(ags);
        // System.out.println(ags);
        for (int i=0 ; i<numberOfAgeGroups ; ++i) {
            int minage = readNumber(dataStream, 1);
            int malepop = readNumber(dataStream, 4);
            int fempop = readNumber(dataStream, 4);
            int worldpop = readNumber(dataStream, 2);
            pds.addAgeGroup(new PopulationDatasetsEntry(i, 1, malepop));
            pds.addAgeGroup(new PopulationDatasetsEntry(i, 2, fempop));
        }
        int unknownMalePopulation = readNumber(dataStream, 4);
        int unknownFemalePopulation = readNumber(dataStream, 4);
        pds.addUnkownAgeGroup(1, unknownMalePopulation);
        pds.addUnkownAgeGroup(2, unknownFemalePopulation);

        // Finally
        dataStream.close();
        istream.close();
        return pds;
    }

    private static String readBytes(DataInputStream dataStream, int numberOfBytes) throws IOException {
        String temp = "";
        for (int i = 0; i < numberOfBytes; i++) {
            char c = (char) dataStream.readByte();
            temp += c;
        }
        return temp;
    }

    private static String readText(DataInputStream dataStream) throws IOException {
        String temp = "";
        int b = dataStream.readByte();

        while (b != 0) {
            // debugOut(""+b);
            temp += (char) b;
            b = (char) dataStream.readByte();
        }
        return temp;
    }

    private static int readNumber(DataInputStream dataStream, int numberOfBytes) throws IOException {
        int value = 0;
        byte[] byteArray = new byte[numberOfBytes];
        for (int i = 0; i < numberOfBytes; i++) {
            byteArray[i] = dataStream.readByte();
        }
        value = byteArrayToIntLH(byteArray);
        return value;
    }

    // Convert a byte array with the most significant byte in the first position to integer
    /**
     *
     * @param b
     * @return
     */
    public static int byteArrayToIntHL(byte[] b) {
        int value = 0;
        for (int i = 0; i < b.length; i++) {
            if (i == (b.length - 1)) {
                value += (b[i] & 0xFF);
            } else if (i == 0) {
                value += b[i] << ((b.length - i) * 8);
            } else {
                value += (b[i] & 0xFF) << ((b.length - i) * 8);
            }
        }
        return value;
    }

    // Convert a byte array with the most significant byte in the last position to integer
    /**
     *
     * @param b
     * @return
     */
    public static int byteArrayToIntLH(byte[] b) {
        int value = 0;
        for (int i = 0; i < b.length; i++) {
            if (i == 0) {
                value += (b[i] & 0xFF);
            } else if (i == (b.length - 1)) {
                value += b[i] << (i * 8);
            } else {
                value += (b[i] & 0xFF) << (i * 8);
            }
        }
        return value;
    }
}
