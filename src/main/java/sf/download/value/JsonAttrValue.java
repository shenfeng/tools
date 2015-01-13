package sf.download.value;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Created by feng on 8/22/14.
 */
public class JsonAttrValue implements Value {
    private final String attr;

    public JsonAttrValue(String attr) {
        this.attr = attr;
    }

    @Override
    public String extract(Object e) {
        if (e instanceof JsonObject) {
            JsonElement je = ((JsonObject) e).get(attr);
            if (je != null) {
                return je.getAsString();
            }
        }
        return null;
    }
}
