package me.shenfeng.proxy;


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
    public abstract List<FetchedProxy> parse(String url, String html);

    public static List<FetchedProxy> p(String url, String html) {
        List<Parser> parsers = new ArrayList<>();
        parsers.add(new ProxyCom());
        for (Parser p : parsers) {
            List<FetchedProxy> r = p.parse(url, html);
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    public static class ProxyCom extends Parser {
        @Override
        public List<FetchedProxy> parse(String url, String html) {
            if (!url.contains("www.proxy.com.ru")) {
                return null;
            }

            List<FetchedProxy> proxies = new ArrayList<>();
            String domain = URI.create(url).getHost();

            Document d = Jsoup.parse(html, url);
            for (Element row : d.select("table tr")) {
                Elements tds = row.select("td");
                if (tds.size() == 5) {
                    String host = tds.get(1).text();
                    if (host.contains(".")) {
                        int port = Integer.parseInt(tds.get(2).text());
                        proxies.add(new FetchedProxy(host, port, "http", url, domain));
                    }
                }
            }
            return proxies;
        }
    }
}
