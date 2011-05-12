/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2011  International Agency for Research on Cancer
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

/**
 *
 * @author ervikm
 */
public class ServerPermission extends java.security.BasicPermission {

  /**
   * Creates a permission with a name.
   * @param name 
   */
  public ServerPermission(String name) {
    super(name);
  }

  /**
   * Creates a permission with a name and an action string.
   * The action string is not used, but this constructor must exist
   * so that the policy file parser works.
   * @param name 
   * @param actions
   */
  public ServerPermission(String name, String actions) {
    super(name, actions);
  }
}
