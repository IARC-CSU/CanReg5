package canreg.client.gui.components;

/**
 *
 * @author ervikm
 */
public class TranslatedListElement {
    String translatedName;
    String originalName;

    public TranslatedListElement(String originalName, String translatedName){
        this.originalName = originalName;
        this.translatedName = translatedName;
    }

    @Override
    public String toString(){
        return translatedName;
    }

    public String getOriginalName(){
        return originalName;
    }
}
