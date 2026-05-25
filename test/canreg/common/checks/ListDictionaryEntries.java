package canreg.common.checks;

import canreg.common.database.DictionaryEntry;

import java.util.Map;

class ListDictionaryEntries {
    private Map<String, DictionaryEntry> dictionaryEntries;

    public ListDictionaryEntries() {
    }

    /**
     * Getter dictionaryEntries.
     *
     * @return dictionaryEntries dictionaryEntries.
     */
    public Map<String, DictionaryEntry> getDictionaryEntries() {
        return dictionaryEntries;
    }

    /**
     * Setter dictionaryEntries.
     *
     * @param dictionaryEntries dictionaryEntries.
     */
    public void setDictionaryEntries(Map<String, DictionaryEntry> dictionaryEntries) {
        this.dictionaryEntries = dictionaryEntries;
    }
}
