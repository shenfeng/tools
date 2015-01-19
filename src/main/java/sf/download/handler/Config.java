package sf.download.handler;

import sf.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by feng on 1/13/15.
 */
public class Config {

    public String seed;
    public Map<String, Map<String, String>> params;


    public Cfg list;
    public Cfg detail;

    public List<String> pager;

    public Check check = new Check(); // make sure not null

//    // 必须包含 check，并且必须不包含 errorcheck。有errorcheck，启用proxy
//    public String check;
//    public String errorcheck;
//    public List<String> notfollow;

    // 不再跟详情页
    public boolean ignoredetail = false;
    public boolean proxy = true; // default to true
    // 如果有 timestamp，多久以后的会被抛弃掉
    public int maxdays = 0;

    public Set<FetchTask> getSeeds() {
        Set<FetchTask> seeds = new HashSet<>();
        if (params == null) {
            seeds.add(new FetchTask(seed, true, "", new HashMap<String, String>()));
        } else {
            collect(new LinkedList<>(params.entrySet()), seed, seeds, new HashMap<String, String>());
        }
        return seeds;
    }

    public static class Check {
        public String must;
        public List<String> error;

        public List<String> notfollow;
        public List<String> errorurl;

        public Flag isHtmlOk(String html) {
            if (Utils.isEmpty(html)) {
                return Flag.ERROR;
            }

            if (!Utils.isEmpty(must) && !html.contains(must)) {
                return Flag.ERROR;
            }

            if (error != null) {
                for (String s : error) {
                    s = s.trim();
                    if (s.length() > 0 && html.contains(s)) {
                        return Flag.PROXY;
                    }
                }
            }
            return Flag.OK;
        }

        public Flag isUrlOk(String url) {
            if (Utils.isEmpty(url)) {
                return Flag.ERROR;
            }
            if (notfollow != null) {
                for (String s : notfollow) {
                    if (Pattern.matches(s, url)) {
                        return Flag.NOT_FOLLOW;
                    }
                }
            }
            if (errorurl != null) {
                for (String s : errorurl) {
                    if (Pattern.matches(s, url)) {
                        return Flag.PROXY;
                    }
                }
            }
            return Flag.OK;
        }
    }

    public static enum Flag {
        OK,
        ERROR,
        PROXY,
        NOT_FOLLOW
    }

    public static class Cfg {
        public List<String> selectors;
        public List<Field> data;
    }


    private void collect(List<Map.Entry<String, Map<String, String>>> ps,
                         String templates, Set<FetchTask> result, Map<String, String> d) {
        if (ps.isEmpty()) {
            return;
        }

        boolean collect = ps.size() == 1;
        Iterator<Map.Entry<String, Map<String, String>>> it = ps.iterator();
        Map.Entry<String, Map<String, String>> item = it.next();
        it.remove();
        String r = "${" + item.getKey() + "}";

        for (String s : item.getValue().keySet()) {
            HashMap<String, String> data = new HashMap<>(d);
            data.put(item.getKey(), item.getValue().get(s));

            try {
                String tmpl = templates.replace(r, URLEncoder.encode(s, "utf8"));
                if (collect) {
                    result.add(new FetchTask(tmpl, true, "", data));
                } else {
                    collect(new LinkedList<>(ps), tmpl, result, data); // recursive
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public String minDate;

    public boolean isTooOld(String date) {
        // 日期的格式为 2014-08-11

        if (minDate == null) { // cache
            long l = System.currentTimeMillis() -
                    (maxdays == 0 ? 30 : maxdays) * 24L * 3600 * 1000;
            Date d = new Date(l);
            minDate = new SimpleDateFormat("YYYY-MM-dd").format(d);
        }

        // 2014-08-11 > 2014-08-10
        return minDate.compareTo(date) > 0;
    }
}
