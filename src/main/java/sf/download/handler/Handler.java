package sf.download.handler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import sf.Utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Handler {
    // special key
    public static final String URL = "url";
    public static final String TITLE = "title";
    public static final String TIMESTAMP = "timestamp";

    public final Config cfg;

    public Handler(Config cfg) {
        this.cfg = cfg;
    }

    protected String Resolve(String base, String href) {
        if (!Utils.isEmpty(href) && !href.contains("javascript")) {
            String v = URI.create(base).resolve(href).toString();
            int idx = v.indexOf('#');
            if (idx > 0) {
                v = v.substring(0, idx);
            }
            return v;
        } else {
            return null;
        }
    }

    public ListData OnListPage(String url, String html) {
        Document doc = Jsoup.parse(html, url);
        ListData result = new ListData();

        int tooOld = 0;


        if (cfg.list.selectors != null) {
            // 列表页一般会列出一些meta信息，对应为 config里面的list 配置项
            // 子类有特殊需要，可以override
            for (String selector : cfg.list.selectors) {
                for (Element e : doc.select(selector)) {
                    Map<String, Object> m = new HashMap<>();
                    for (Field f : cfg.list.data) {
                        Object value = f.get(e);
                        if (value != null) {
                            if (f.name.equalsIgnoreCase(URL)) {
                                // convert to absolute path
                                value = Resolve(url, value.toString());
                            }
                            if (value != null)
                                m.put(f.name, value);
                        }
                    }

                    // url is required
                    if (!m.containsKey(URL)) {
                        continue;
                    }

                    // title is too short
                    if (m.containsKey(TITLE) && m.get(TITLE).toString().length() < 2) {
                        continue;
                    }

                    //  timestamp 是个特殊字段  ignore too old item
                    if (m.containsKey(TIMESTAMP)) {
                        if (cfg.isTooOld(m.get(TIMESTAMP).toString())) {
                            tooOld += 1;
                            continue;
                        }
                    }

                    result.details.add(m);
                }
            }
        }

        // TODO make max too old configure
        // if has any tool old items, do not follow pagination
        if (tooOld == 0) {
            for (String p : cfg.pager) {
                for (Element e : doc.select(p)) {
                    String href = Resolve(url, e.attr("href"));
                    if (href != null)
                        result.pages.add(href);
                }
            }
        }

        return result;
    }

    public Map<String, Object> OnDetailPage(String url, String html) {
        if (cfg.ignoredetail) {
            return null;
        }
        Document doc = Jsoup.parse(html, url);
        // 重新定义root节点
        List<Element> roots = new ArrayList<>();
        if (cfg.detail.selectors != null && !cfg.detail.selectors.isEmpty()) {
            for (String s : cfg.detail.selectors) {
                for (Element e : doc.select(s)) {
                    roots.add(e);
                }
            }
        } else {
            roots.add(doc);
        }

        // 抽取各个字段
        Map<String, Object> m = new HashMap<>();
        for (Element e : roots) {
            for (Field field : cfg.detail.data) {
                Object v = field.get(e);
                if (v != null && field.name.contains(URL)) {
                    v = Resolve(url, v.toString());
                }
                if (v != null) {
                    m.put(field.name, v);
                }
            }
        }
        m.put(Handler.URL, url);
        return m;
    }
}
