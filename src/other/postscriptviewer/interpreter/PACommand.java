/*
 * Copyright 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

package other.postscriptviewer.interpreter;

public interface PACommand {

    static public final String RCSID = "$Id: PACommand.java,v 1.1 1997/12/17 23:37:50 uweh Exp $";
    
    public void execute(PAContext context) throws other.postscriptviewer.interpreter.PainterException;
    
}
