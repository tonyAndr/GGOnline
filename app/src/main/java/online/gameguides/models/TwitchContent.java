package online.gameguides.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TwitchContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<TwitchItem> ITEMS = new ArrayList<TwitchItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, TwitchItem> ITEM_MAP = new HashMap<String, TwitchItem>();

    public static void addItem(TwitchItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static void clearItems () {
        ITEMS.clear();
        ITEM_MAP.clear();
    }

//    private static TwitchItem createDummyItem(int position) {
//        return new TwitchItem(String.valueOf(position), "Item " + position, makeDetails(position));
//    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class TwitchItem {
        public final String id;
        public final String previewUrl;
        public final String status;
        public final String nick;
        public final String viewers;
        public final String lang;
        public final String url;

        public TwitchItem(String id, String previewUrl, String nick, String status, String viewers, String lang, String url) {
            this.id = id;
            this.previewUrl = previewUrl;
            this.nick = nick;
            this.status = status;
            this.viewers = viewers;
            this.lang = lang;
            this.url = url;
        }

    }
}
