package sf.download.handler;

import cn.techwolf.util.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BaseHandler {
    // special key
    public static final String URL = "url";
    public static final String REAL_URL = "url";
    public static final String TITLE = "title";
    public static final String PUBLISH = "publish";
    public static final String NAME = "name";
    public static final String CONF_ID = "conf_id";
    public static final String PROVIDER = "provider";
    public static final String COMPANY = "company";
    private static Class<? extends BaseHandler>[] classes;

    public final SiteConfig cfg;

    protected BaseHandler(SiteConfig cfg) {
        this.cfg = cfg;
    }

    public static BaseHandler newInstance(SiteConfig cfg) {
        String name = cfg.name;
        if (name.endsWith(".json")) {
            name = name.substring(0, name.length() - ".json".length());
        }
        // alibaba_json.json will return JsonHandler
        String[] parts = name.split("_");
        if (parts.length > 1) {
            String p = parts[parts.length - 1].toLowerCase();
            loadClassedIfNeeded();

            for (Class<? extends BaseHandler> c : classes) {
                if (c.getSimpleName().toLowerCase().startsWith(p)) {
                    try {
                        Constructor<? extends BaseHandler> cto = c.getConstructor(SiteConfig.class);
                        return cto.newInstance(cfg);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        // return default one
        return new BaseHandler(cfg);
    }

    private static void loadClassedIfNeeded() {
        if (classes == null) {
            Reflections reflections = new Reflections("cn.techwolf");
            List<Class<? extends BaseHandler>> vs = new ArrayList<>(reflections.getSubTypesOf(BaseHandler.class));
            classes = vs.toArray(new Class[vs.size()]);
        }
    }

    // 列表页一般会列出一些meta信息，对应为 config里面的list 配置项
    // 子类有特殊需要，可以override
    protected Map<String, Object> OnListMeta(String url, Element e, List<Field> fs) {
        Map<String, Object> m = new HashMap<>();
        for (Field f : fs) {
            Object value = f.get(e);

            if (value != null) {
                if (f.name.equalsIgnoreCase(URL)) {
                    // convert to absolute path
                    value = Resolve(url, value.toString());
                }
                if (f.name.equalsIgnoreCase(PUBLISH)) {
                	value = Utils.normalizePublishDate(value.toString());
                }
                if (value != null)
                    m.put(f.name, value);
            }
        }
        return m;
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

    // 计算翻页的url
    protected String OnPageUrl(String base, Element e) {
        String href = e.attr("href");
        return Resolve(base, href);
    }

    public ListData OnListPage(String url, String html) {
        Document doc = Jsoup.parse(html, url);
        ListData result = new ListData();

        int tooOld = 0;

        if (!Utils.isEmpty(cfg.listselector)) {
            Elements elements = doc.select(cfg.listselector);
            for (Element element : elements) {
                Map<String, Object> m = OnListMeta(url, element, cfg.list);
                // url is required
                if (!m.containsKey(URL)) {
                    continue;
                }
                // title is too short
                if (m.containsKey(TITLE) && m.get(TITLE).toString().length() < 2) {
                    continue;
                }

                if (m.containsKey(PUBLISH)) {
//                     ignore too old item
                    if (cfg.isTooOld(m.get(PUBLISH).toString())) {
                        tooOld += 1;
                        continue;
                    }
                }
                cfg.addGenericAttr(m);
                result.details.add(m);
            }
        }

        // if has any tool old items, do not follow pagination
        if (!Utils.isEmpty(cfg.pager) && tooOld == 0) {
            for (Element e : doc.select(cfg.pager)) {
                String v = OnPageUrl(url, e);
                if (!Utils.isEmpty(v)) {
                    result.pages.add(v);
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
        Element root = doc;

        // 定义根节点
        if (!Utils.isEmpty(cfg.detailselector)) {
            Elements es = doc.select(cfg.detailselector);
            if (es.size() > 0) {
                root = es.get(0);
            }
        }
        Map<String, Object> m = new HashMap<>();

        for (Field f : cfg.detail) {
            Object value = f.get(root);
            if (value != null) {
                m.put(f.name, value);
            }
        }

        cfg.addGenericAttr(m);

        m.put(BaseHandler.URL, url);
        return m;
    }
}
