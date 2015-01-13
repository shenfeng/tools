package sf.download.handler;

import cn.techwolf.util.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by feng on 8/22/14.
 */
public class JsonHandler extends BaseHandler {

    public JsonHandler(SiteConfig cfg) {
        super(cfg);
    }

    public static void main(String[] args) throws IOException {
        Gson gson = new Gson();
        byte[] bytes = Files.readAllBytes(Paths.get("/tmp/doList.json?pageSize=30&pageIndex=1"));
        JsonElement j = gson.fromJson(new String(bytes), JsonElement.class);
//        System.out.println(j);

        System.out.println(new Date(1424361600000L));
    }

    @Override
    public ListData OnListPage(String url, String html) {
        // html is json
        Gson gson = new Gson();
        JsonElement root = gson.fromJson(html, JsonObject.class);

        if (!Utils.isEmpty(cfg.listselector)) {
            String[] parts = cfg.listselector.split(",");
            for (String part : parts) {
                if (root != null && root instanceof JsonObject) {
                    root = ((JsonObject) root).get(part);
                } else {
                    break;
                }
            }
        }

        ListData d = new ListData();

        if (root != null && root.isJsonArray()) {
            JsonArray arr = (JsonArray) root;
            for (JsonElement element : arr) {
                Map<String, Object> m = new HashMap<>();
                for (Field f : cfg.list) {
                    Object value = f.get(element);
                    if (value != null) {
                        m.put(f.name, value);
                    }
                }

                if (m.containsKey(URL)) {
                    d.details.add(m);
                }
            }
        }
        return d;
    }
}
