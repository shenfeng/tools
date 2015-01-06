package me.shenfeng.proxy;

import me.shenfeng.db.Proxy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by feng on 1/5/15.
 */
public abstract class Parser {
    public abstract List<Proxy> parse(String url, String html);

    public static List<Proxy> p(String url, String html) {
        List<Parser> parsers = new ArrayList<>();
        parsers.add(new ProxyCom());
        for (Parser p : parsers) {
            List<Proxy> r = p.parse(url, html);
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    public static class ProxyCom extends Parser {

        @Override
        public List<Proxy> parse(String url, String html) {
            if (!url.contains("www.proxy.com.ru")) {
                return null;
            }

            List<Proxy> proxies = new ArrayList<>();

            String domain = URI.create(url).getHost();

            Document d = Jsoup.parse(html, url);
            for (Element row : d.select("table tr")) {
                Elements tds = row.select("td");
                if (tds.size() == 5) {
                    String host = tds.get(1).text();
                    if (host.contains(".")) {
                        int port = Integer.parseInt(tds.get(2).text());
                        proxies.add(new Proxy(0, host, port, "http", 0, 0, 0, 0, url, domain, null, null));
                    }
                }
            }
            return proxies;
        }
    }
}
