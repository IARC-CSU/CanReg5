package canreg.common;

/**
 *
 * @author ervikm
 * A simple way to translate list elements
 */
public class TranslatableListElement {
    String translatedName;
    String originalName;

    public TranslatableListElement(String originalName, String translatedName){
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
