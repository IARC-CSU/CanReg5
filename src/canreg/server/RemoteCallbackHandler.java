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
 * @author Morten Johannes Ervik, CIN/IARC, ervikm@iarc.fr
 */
package canreg.server;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;

/**
 *
 * @author ervikm
 */
class RemoteCallbackHandler implements CallbackHandler {

    private String username;
    private char[] password;

    RemoteCallbackHandler(String username, char[] password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void handle(Callback[] cb) {
        for (int i = 0; i < cb.length; i++) {
            if (cb[i] instanceof NameCallback) {
                NameCallback nc = (NameCallback) cb[i];
                nc.setName(username);
            } else if (cb[i] instanceof PasswordCallback) {
                PasswordCallback pc = (PasswordCallback) cb[i];
                pc.setPassword(password);
                password = null;
            }
        }
    }
}
