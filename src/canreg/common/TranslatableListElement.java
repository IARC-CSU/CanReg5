package canreg.common;

/**
 *
 * @author ervikm
 * A simple way to translate list elements
 */
public class TranslatableListElement {
    String translatedName;
    String originalName;

    public TranslatableListElement(String originalName, String translatedName) {
        this.originalName = originalName;
        this.translatedName = translatedName;
    }

    @Override
    public String toString() {
        return translatedName;
    }

    @Override
    public boolean equals(Object o) {
        boolean equal = false;
        if (o instanceof TranslatableListElement) {
            equal = this.hashCode() == o.hashCode();
        } else {
            super.equals(o);
        }
        return equal;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.originalName != null ? this.originalName.hashCode() : 0);
        return hash;
    }

    public String getOriginalName() {
        return originalName;
    }
}
