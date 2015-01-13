package sf.download.handler;


import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by feng on 8/21/14.
 */
public class SiteConfig {

    // conf id (in db)
    public int id;

    // "阿里巴巴", 猎聘. if official == true: companyname = provider
    public String provider;

    // config filename: baidu.json, etc
    public String name;

    // 是否是json，是否是html
    public String type;

    // 是否来自官网
    public boolean official;

    // 不再跟详情页
    public boolean ignoredetail = false;

    // is job hunting website: like 51job, zhilian, liepin
    public boolean isHuntingSite;

    // 种子
    public String seed;

    // pager selector
    public String pager;

    public String detailselector;

    public String listselector;

    public List<Field> list;
    public List<Field> detail;


    // use proxy
    public boolean proxy = true; // default to true

    // 关键字，确认是否 需要的网页.
    // 如抓取百度时， 用“百度”可以 可以判断返回的html真是百度的内容 (proxy is not reliable)
    public String validcheck;

    // 黑名单
    public String errorcheck;

    // 如果有时间，多久以后的job会被抛弃掉
    public int maxDays;

    public String minDate;
    // rand sleep max ms before next run

    private int rsleep;

    public static SiteConfig parse(String cfgName, String cfg) {
        Gson gson = new Gson();
        SiteConfig sc = gson.fromJson(cfg, SiteConfig.class);
        sc.name = cfgName;
        return sc;
    }

    public boolean isTooOld(String date) {
        // 日期的格式为 2014-08-11

        if (minDate == null) { // cache
            long l = System.currentTimeMillis() -
                    (maxDays == 0 ? 30 : maxDays) * 24L * 3600 * 1000;
            Date d = new Date(l);
            minDate = new SimpleDateFormat("YYYY-MM-dd").format(d);
        }

        // 2014-08-11 > 2014-08-10
        return minDate.compareTo(date) > 0;
    }

    public int getRsleep() {
        // default 500ms
        return rsleep == 0 ? 500 : rsleep;
    }

    public void addGenericAttr(Map<String, Object> m) {
        m.put(BaseHandler.NAME, this.name);
        m.put(BaseHandler.CONF_ID, this.id);
        m.put(BaseHandler.PROVIDER, this.provider);
        if (this.official) {
            m.put(BaseHandler.COMPANY, this.provider);
        }
    }

    public static SiteConfig load(String f) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(f));
        String json = new String(bytes);
        SiteConfig cfg = new Gson().fromJson(json, SiteConfig.class);
        cfg.name = new File(f).getName();
        return cfg;
    }

    public static SiteConfig load(String content, String name) {
        SiteConfig cfg = new Gson().fromJson(content, SiteConfig.class);
        cfg.name = name;
        return cfg;
    }


    public static JsonObject parse(SiteConf conf) {
        Gson g = new Gson();
        String json = conf.config;
        if (Utils.isEmpty(json)) {
            json = "{}";
        }

        JsonObject obj = g.fromJson(json, JsonObject.class);
        if (!Utils.isEmpty(conf.seed)) {
            obj.addProperty("seed", conf.seed);
        }

        obj.addProperty("official", conf.official);

        if (!Utils.isEmpty(conf.errorcheck)) {
            obj.addProperty("errorcheck", conf.errorcheck);
        }

        if (!Utils.isEmpty(conf.validcheck)) {
            obj.addProperty("validcheck", conf.validcheck);
        }

        return obj;
    }
}
