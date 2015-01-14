package sf.download.handler;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by feng on 1/13/15.
 */
public class Config {

    public String seed;
    public Map<String, Map<String, String>> params;


    public Cfg list;
    public Cfg detail;

    public List<String> pager;

    //
    public String check;
    // 不再跟详情页
    public boolean ignoredetail = false;
    public boolean proxy = true; // default to true
    // 如果有 timestamp，多久以后的会被抛弃掉
    public int maxdays = 0;


    public static class Cfg {
        public List<String> selectors;
        public List<Field> data;
    }

    public Set<String> getSeeds() {
        Set<String> seeds = new HashSet<>();
        if (params == null) {
            seeds.add(seed);
        } else {
            collect(new LinkedList<>(params.entrySet()), seed, seeds);
        }

        return seeds;
    }


    private void collect(LinkedList<Map.Entry<String, Map<String, String>>> ps, String templates, Set<String> c) {
        if (ps.isEmpty()) {
            return;
        }

        boolean collect = ps.size() == 1;
        Iterator<Map.Entry<String, Map<String, String>>> it = ps.iterator();
        Map.Entry<String, Map<String, String>> item = it.next();
        it.remove();
        String r = "${" + item.getKey() + "}";
        for (String s : item.getValue().keySet()) {
            String tmpl = templates.replace(r, s);
            if (collect) {
                c.add(tmpl);
            } else {
                collect(new LinkedList<>(ps), tmpl, c); // recursive
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
