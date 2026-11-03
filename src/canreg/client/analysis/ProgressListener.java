/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2026 International Agency for Research on Cancer
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
 */
package canreg.client.analysis;

/**
 * Listener interface for receiving progress updates from table builders
 * and external processes (e.g. R scripts).
 * 
 * @author CanReg5
 */
@FunctionalInterface
public interface ProgressListener {

    /**
     * Called when progress is updated.
     * 
     * @param percentage completion percentage (0 - 100)
     * @param message description of the current step or task
     */
    void onProgress(int percentage, String message);
}

