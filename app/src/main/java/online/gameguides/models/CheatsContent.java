package online.gameguides.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheatsContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<CheatItem> ITEMS = new ArrayList<CheatItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<Integer, CheatItem> ITEM_MAP = new HashMap<Integer, CheatItem>();

    public static void addItem(CheatItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static void clearItems () {
        ITEMS.clear();
        ITEM_MAP.clear();
    }


    /**
     * A dummy item representing a piece of content.
     */
    public static class CheatItem {
        public final int id;
        public String platform;
        public String link;

        public CheatItem(int id, String platform, String link) {
            this.id = id;
            this.platform = platform;
            this.link = link;
        }
    }
}
