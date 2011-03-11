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

import java.util.*;

public class PAEngine extends Object {

    static private final String RCSID = "$Id: PAEngine.java,v 1.1 1997/12/17 23:37:54 uweh Exp $";
    
    static public final int MODE_STACK = 0;
    static public final int MODE_PROCEDURE = 1;
    static public final int MODE_ARRAY = 2;
    
    protected PAContext context;
    protected int mode;
    protected Stack procedure;
    protected int innerProcedures;

    public PAEngine(PAContext context){
        super();
        this.context = context;
        this.mode = PAEngine.MODE_STACK;
    }

    public void startProcedure() throws PainterException {
        this.procedure = new Stack();
        this.mode = PAEngine.MODE_PROCEDURE;
        this.innerProcedures = 0;
    }

    public void endProcedure() throws PainterException {
        this.context.operands.push(new PAToken(this.procedure, PAToken.PROCEDURE));
        this.mode = PAEngine.MODE_STACK;
    }

    public void bindProcedure(PAToken patoken){
        Stack oldStack = (Stack) patoken.value;
        Stack newStack = new Stack();
        int i,n;
        n = oldStack.size();
        for(i = 0; i < n; i++){
            Object token = oldStack.elementAt(i);
            if((token instanceof PAToken) && ((PAToken) token).type == PAToken.IDENTIFIER){
                Object foundToken = this.context.findIdentifier(((PAToken) token).value);
                if(foundToken == null){
                    newStack.push(token);
                } else {
                    newStack.push(foundToken);
                }
            } else {
                newStack.push(token);
            }
        }
        patoken.value = newStack;
    }

    public void process(Object token) throws PainterException {
        if(token == null){
            throw new IllegalStateException("Null token encountered; last unknown identifier was " + this.context.getLastUnknownIdentifier());
        }
        if(token instanceof PAToken && ((PAToken) token).type == PAToken.IMMEDIATE){
            Object foundValue = this.context.findIdentifier(((PAToken) token).value);
            this.process(foundValue);
            return;
        }
        if(this.mode == MODE_STACK){
            if(token instanceof PACommand){
                ((PACommand) token).execute(this.context);
            } else if(token instanceof PAToken){
                PAToken patoken = (PAToken) token;
 
                switch(patoken.type){
                case PAToken.IDENTIFIER:
                    this.process(this.context.findIdentifier(patoken.value));
                    break;
                case PAToken.KEY:
                case PAToken.MARK:
                case PAToken.START_ARRAY:
                    this.context.operands.push(token);
                    break;
                case PAToken.PROCEDURE:
                    Enumeration enum2 = ((Vector) patoken.value).elements();
                    while(enum2.hasMoreElements()){
                        this.process(enum2.nextElement());
                    }
                    break;
                case PAToken.START_PROCEDURE:
                    this.startProcedure();
                    break;
                case PAToken.END_ARRAY:
                    this.context.collectArray();
                    break;
                default:
                    throw new IllegalStateException("Unknown token encountered" + token);
                }
            } else {
                this.context.operands.push(token);
            }
        } else if(this.mode == MODE_PROCEDURE){
            if(token instanceof PAToken){
                PAToken patoken = (PAToken) token;

                switch(patoken.type){
                case PAToken.START_PROCEDURE:
                    this.innerProcedures++;
                    this.procedure.push(token);
                    break;
                case PAToken.END_PROCEDURE:
                    if(this.innerProcedures > 0){
                        this.innerProcedures--;
                        this.procedure.push(token);
                    } else {
                        this.endProcedure();
                    }
                    break;
                default:
                    this.procedure.push(token);
                }
            } else {
                this.procedure.push(token);
            }
        }
    }
    
}

