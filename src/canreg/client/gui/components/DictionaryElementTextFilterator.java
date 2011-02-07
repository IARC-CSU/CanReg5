package canreg.client.gui.components;

import java.util.List;
import ca.odell.glazedlists.TextFilterator;
import canreg.server.database.DictionaryEntry;

/**
 * Get the Strings to filter against for a given Issue.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class DictionaryElementTextFilterator implements TextFilterator<DictionaryEntry> {
    @Override
    public void getFilterStrings(List<String> baseList, DictionaryEntry dictionaryEntry) {
        baseList.add(dictionaryEntry.getCode());
        baseList.add(dictionaryEntry.getDescription());
    }
}
