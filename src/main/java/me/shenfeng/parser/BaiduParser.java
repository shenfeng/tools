package me.shenfeng.parser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.shenfeng.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by feng on 7/11/14.
 */


public class BaiduParser extends ParseBase {

    public class Item {
        public final String link;
        public final String title;
        public final String summary;
        public final String company;

        public final boolean official;

        public Item(String link, String title, String summary, String company, boolean official) {
            this.link = link;
            this.title = title;
            this.summary = summary;
            this.company = company;
            this.official = official;
        }

        public String getLinkDomain() {
            if (link != null && link.length() > 0) {
                int idx = link.indexOf('/');
                if (idx > 0) {
                    return link.substring(0, idx);
                }
            }

            return link;
        }


        @Override
        public String toString() {
            return "Item{" +
                    "link='" + link + '\'' +
                    ", title='" + title + '\'' +
                    ", summary='" + summary + '\'' +
                    ", company='" + company + '\'' +
                    ", official=" + official +
                    '}';
        }
    }

    public static class Result {
        public String word;
        public int id;
        public List<Item> items;
        public int num;

        public String getOffical() {
            for (Item item : items) {

                if (item.official) {
                    return item.link;
                }
            }
            return null;
        }

        public String getWeibo() {
            for (Item item : items) {
                if (item.title.contains("新浪微博")) {
                    return item.title;
                }
            }

            return null;
        }

        public String getCompany() {
            for (Item item : items) {
                if (item.company.length() > 0)
                    return item.company;
            }
            return null;
        }
    }


    private static Type MAP_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException, InterruptedException {
        new BaiduParser().parseArgsAndRun(args);
    }


    public Result parse(String url, String html) {
        Document doc = Jsoup.parse(html, url);
        String num = doc.select(".nums").text();
        List<Item> items = new ArrayList<>();


        if (num.indexOf('约') >= 0) {
            num = num.substring(num.indexOf('约') + 1, num.length() - 1);
            num = num.replaceAll(",", "");
        } else {
            num = "0";
        }

        for (Element result : doc.select("#content_left .result")) {
            Elements weibo = result.select(".c-gap-bottom-small");
            if (weibo.size() > 0) {
                Elements a = weibo.select("a");
                String href = a.attr("href");
                String title = a.text();
                items.add(new Item(href, title, "", "", false));
            } else {
                String link = result.select("span.g").text();
                boolean isOfficial = result.select(".OP_LOG_LINK").size() > 0;
                String title = result.select("h3").text();
                String summary = result.select(".c-abstract").text();


                String attr = result.select(".c-trust-as").attr("hint-data");
                String company = "";
                if (attr.length() > 0) {
                    Map<String, Object> a = gson.fromJson(attr, MAP_TYPE);
                    company = (String) a.get("label");
                }
                Item item = new Item(link, title, summary, company, isOfficial);
                items.add(item);
            }
        }

        Result r = new Result();
        r.items = items;
        try {
            r.num = Integer.parseInt(num);
        } catch (Exception e) {
        }


        Map<String, String> m = Utils.parseQueryString(URI.create(url).getQuery());
        if (m.containsKey("wd")) {
            r.word = m.get("wd");
        }

        try {
            if (m.containsKey("_")) {
                r.id = Integer.parseInt(new String(DatatypeConverter.parseBase64Binary(m.get("_"))));
            }
        } catch (Exception ignore) {
        }

        return r;
    }
}
