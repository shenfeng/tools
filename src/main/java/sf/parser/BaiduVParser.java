package sf.parser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by feng on 1/10/15.
 */
public class BaiduVParser extends ParseBase {

    // //    java -cp "/home/ubuntu/tools/tools-1.0.jar:/home/ubuntu/tools/lib/*" BaiduVParser -in datas -out json

    public static class Domain {
        public String siteName;
        public String url;
        public String icpNum;
        public String guaUrl;
        public String isOfficialAuth;
    }

    private static Pattern NUMBER = Pattern.compile("(\\d+)");

    public static class BaiduV {
        public String com;
        public String wesitename;
        public String place;
        public String jinying;
        public String beian;
        public String type;
        public List<String> urls = new ArrayList<>();
        public List<String> tags = new ArrayList<>();
        public List<Domain> domains = new ArrayList<>();

        public static class Rate {
            public int count;
            public int p;
            public String url;
        }

        public Rate rate;
        public Map<String, String> kvs = new HashMap<>();
    }


    public BaiduV parse(String url, String html) {
        Document d = Jsoup.parse(html, url);


//        System.out.println(urls);
//
        Elements doc = d.select(".ecl-vmp-card");
        if (doc.size() < 1) {
            return null;
        }
        BaiduV bv = new BaiduV();
        bv.com = doc.select("h2").text();

        int start = html.indexOf("urls: ");
        if (start > 0) {
            int end = html.indexOf("});", start);
            String urls = html.substring(start + "urls: ".length(), end);
            if (urls.length() > 5) {

                bv.domains = new Gson().fromJson(urls,
                        new TypeToken<List<Domain>>() {
                        }.getType());
            }
        }


        for (Element section : d.select(".main-section tr")) {
            String th = section.select("th").text();
            String td = section.select("td").text();


            if (th.contains("网站名称")) {
                bv.wesitename = td;
            } else if (th.contains("工商地址")) {
                bv.place = td;
            } else if (th.contains("网站地址")) {
                bv.urls.add(section.select("td a").attr("href"));
            } else if (th.contains("网民印象")) {
                for (Element tag : section.select("td a")) {
                    bv.tags.add(tag.text());
                }
            } else if (th.contains("网民评分")) {
                BaiduV.Rate r = new BaiduV.Rate();
                for (Element a : section.select("td a")) {
                    if (a.text().contains("评论")) {
                        r.url = a.attr("href");
                        Matcher m = NUMBER.matcher(a.text());
                        if (m.find()) {
                            r.count = Integer.parseInt(m.group(1));
                        }
                    } else if (a.text().contains("好评")) {
                        Matcher m = NUMBER.matcher(a.text());
                        if (m.find()) {
                            r.p = Integer.parseInt(m.group(1));
                        }
                    }
                }
                bv.rate = r;
            } else if (th.contains("备案编号")) {
                bv.beian = td;
            } else if (th.contains("经营范围")) {
                bv.jinying = td;
            } else if (th.contains("商家类型")) {
                bv.type = td;
            } else {
                int idx = th.indexOf(':');
                if (idx > 0) {
                    th = th.substring(0, idx);
                }

                bv.kvs.put(th, td);
            }
        }

        return bv;
    }

    public static void main(String[] args) {
        new BaiduVParser().parseArgsAndRun(args);
    }
}
