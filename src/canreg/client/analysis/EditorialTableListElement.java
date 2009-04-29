package canreg.client.analysis;

/**
 *
 * @author ervikm
 */
public class EditorialTableListElement {
    private String name;
    private String description;
    private String engineName;
    private String[] engineParameters;
    private String previewImageFilename;
    private String configFileName;

    /**
     * @return the name
     */
    public String getName() {
        return name;
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
    public String toString(){
        return name;
    }
}
