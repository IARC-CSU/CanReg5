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

package canreg.client.analysis;

import java.util.LinkedList;

/**
 *
 * @author ervikm
 */
public class TableBuilderListElement {

    private String name;
    private String description;
    private String engineName;
    private String[] engineParameters;
    private String previewImageFilename;
    private String configFileName;
    private LinkedList<ConfigFields> configFields;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    public void setConfigFields(LinkedList<ConfigFields> configFields) {
        this.configFields = configFields;
    }

    public LinkedList<ConfigFields> getConfigFields() {
        return configFields;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the engineName
     */
    public String getEngineName() {
        return engineName;
    }

    /**
     * @param engineName the engineName to set
     */
    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    /**
     * @return the previewImageFilename
     */
    public String getPreviewImageFilename() {
        return previewImageFilename;
    }

    /**
     * @param previewImageFilename the previewImageFilename to set
     */
    public void setPreviewImageFilename(String previewImageFilename) {
        this.previewImageFilename = previewImageFilename;
    }

    /**
     * @return the configFileName
     */
    public String getConfigFileName() {
        return configFileName;
    }

    /**
     * @param configFileName the configFileName to set
     */
    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    /**
     * @return the engineParameter
     */
    public String[] getEngineParameters() {
        return engineParameters;
    }

    public void setEngineParameters(String[] engineParameters) {
        this.engineParameters = engineParameters;
    }

    @Override
    public String toString() {
        return name;
    }
}
