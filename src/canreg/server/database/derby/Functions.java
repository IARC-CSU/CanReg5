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
 * @author Mahdi Asgarinia , CRC-IRAN, ICTLco, mahdi.asgari@gmail.com
 */
//<ictl.co>
package canreg.server.database.derby;

import canreg.common.Globals;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.ULocale;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by amin on 9/7/2016.
 */
public class Functions {

    public static String toDate(String dte, String pattern, String locale){
        SimpleDateFormat srcFormat = new SimpleDateFormat(Globals.DATE_FORMAT_STRING,new ULocale("en"));
        SimpleDateFormat trgFormat = new SimpleDateFormat(pattern,new ULocale(locale));
        try {
            Date date = srcFormat.parse(dte);
            return trgFormat.format(date);
        } catch (ParseException e) {
            return "NAN";
        }
    }
}

//</ictl.co>