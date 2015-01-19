package sf.download.handler;

import java.util.Map;

/**
 * Created by feng on 1/14/15.
 */
public class FetchTask {
    public final String url;
    public String followUrl;

    private String referer;
    public final boolean listpage;
    public String config;

    public final Map<String, String> extras;

    public String html = "";
    public int status;
    public String err;

    public String getUrl() {
        if (followUrl != null) {
            return followUrl;
        }
        return url;
    }

    public String getReferer() {
        if (referer != null) {
            return referer;
        }
        return url;
    }

    public FetchTask(String url, boolean listpage, String config, Map<String, String> extras) {
        this.url = url;
        this.listpage = listpage;
        this.config = config;
        this.extras = extras;
    }

    public FetchTask(String url, FetchTask task) {
        this.listpage = false;
        this.config = task.config;
        this.url = url;
        this.referer = task.getUrl();
        this.extras = task.extras;
    }
}