package sf.parser;

import sf.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by feng on 1/12/15.
 */
public class DajieParser extends ParseBase {

    public static class Dajie {
        public String com;
        public String logo;
        public String web;
        public String ind;
        public String place;
        public String size;
        public int rate;
        public int follow;
    }


    @Override
    public Dajie parse(String url, String html) {
        Document d = Jsoup.parse(html, url);

        Dajie dj = new Dajie();

        Elements h1 = d.select("h1");
        h1.select("span").remove();
        dj.com = h1.text();

        String src = d.select(".cor-logo-img img").attr("src");
        if (!src.contains("100x100")) {
            dj.logo = src;
        }

        String follow = d.select("#J_followNum").text();
        dj.follow = Utils.getInt(follow);

        for (Element dd : d.select(".corp-card-mod .card-detail dd")) {
            Elements spans = dd.select("span");
            if (spans.size() > 1) {
                String label = spans.get(0).text();
                String value = spans.get(1).text();

                if (label.contains("公司规模")) {
                    dj.size = value;
                } else if (label.contains("所在地区")) {
                    dj.place = value;
                } else if (label.contains("所属行业")) {
                    dj.ind = value;
                } else if (label.contains("官方网址")) {
                    dj.web = dd.select("a").attr("href");
                } else if (label.contains("公司评分")) {
                    dj.rate = Utils.getInt(spans.get(2).text());
                }
            }
        }

        return dj;
    }

    public static void main(String[] args) {
        new DajieParser().parseArgsAndRun(args);
    }
}
