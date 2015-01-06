package me.shenfeng;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import gen.api.Proxy;
import me.shenfeng.proxy.Crawler;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.Math.min;


/**
 * Created by feng on 1/4/15.
 */
public class Utils {

    // user@password|jdbc_url
    public static DataSource getDataSource(String str) {
        String[] tmp = str.split("\\|");

        if (tmp.length != 2 || !tmp[0].contains("@")) {
            throw new RuntimeException("expect | @ seperated, but get " + str);
        }

        int idx = tmp[0].indexOf('@');
        BasicDataSource bs = new BasicDataSource();

        String url = tmp[1];
        // http://dev.mysql.com/doc/connector-j/en/connector-j-reference-configuration-properties.html
        if (!url.contains("zeroDateTimeBehavior")) {
            if (url.contains("?")) {
                url += "zeroDateTimeBehavior=convertToNull";
            } else {
                url += "?zeroDateTimeBehavior=convertToNull";
            }
        }

        bs.setUrl(url);
        bs.setUsername(tmp[0].substring(0, idx));
        bs.setPassword(tmp[0].substring(idx + 1));

        return bs;
    }

    public static void closeQuietly(Closeable c) {
        try {
            c.close();
        } catch (Exception igore) {

        }
    }

    public static final String CHARSET = "charset=";
    public static final Charset ASCII = Charset.forName("US-ASCII");
    public static final Charset UTF_8 = Charset.forName("utf8");

    public static Charset parseCharset(String type) {
        if (type != null) {
            try {
                type = type.toLowerCase();
                int i = type.indexOf(CHARSET);
                if (i != -1) {
                    String charset = type.substring(i + CHARSET.length()).trim();
                    return Charset.forName(fixCharset(charset));
                }
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    private final static String fixCharset(String s) {
        if (s.equalsIgnoreCase("gb2312")) {
            return "gbk";
        }
        return s;
    }

    private static Charset guess(String html, String patten) {
        int idx = html.indexOf(patten);
        if (idx != -1) {
            int start = idx + patten.length();
            int end = html.indexOf('"', start);
            if (end != -1) {
                try {
                    String charset = html.substring(start, end);
                    return Charset.forName(fixCharset(charset));
                } catch (Exception ignore) {
                }
            }
        }
        return null;
    }

    public static Charset detectCharset(HttpResponse resp, byte[] body) {
        Header header = resp.getFirstHeader("content-type");
        if (header != null) {
            Charset c = parseCharset(header.getValue());
            if (c != null) {
                return c;
            }
        }
        String s = new String(body, 0, min(512, body.length), ASCII);
        Charset c = guess(s, CHARSET);
        return c == null ? UTF_8 : c;
    }

    public static String toString(HttpResponse resp) throws IOException {
        byte[] bytes = EntityUtils.toByteArray(resp.getEntity());
        return new String(bytes, 0, bytes.length, detectCharset(resp, bytes));
    }

    public static ConcurrentLinkedQueue<HttpHost> loadProxies(String path) {
        Type type = new TypeToken<List<Proxy>>() {
        }.getType();

        List<Proxy> proxies = null;
        try {
            String json = Crawler.fetchPage(path);
            if (json != null) {
                proxies = new Gson().fromJson(json, type);
            }
        } catch (IOException ignore) {
        }

        if (proxies == null) {
            try (InputStream is = Utils.class.getClassLoader().getResourceAsStream(path)) {
                proxies = new Gson().fromJson(new InputStreamReader(is), type);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        Collections.shuffle(proxies);
        ConcurrentLinkedQueue<HttpHost> r = new ConcurrentLinkedQueue<>();
        for (Proxy p : proxies) {
            r.add(new HttpHost(p.host, p.port));
        }
        return r;
    }

    public static List<String> readLines(String f) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            List<String> list = new ArrayList<String>();
            String line = reader.readLine();
            while (line != null) {
                list.add(line);
                line = reader.readLine();
            }
            return list;
        }
    }

    public static String getResource(String r) {
        InputStream in = Utils.class.getClassLoader().getResourceAsStream(r);
        try {
            if (in != null) {
                byte[] buffer = new byte[8912];
                int read;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                while ((read = in.read(buffer)) >= 0) {
                    bos.write(buffer, 0, read);
                }
                in.close();
                return new String(bos.toByteArray(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        throw new RuntimeException("not found " + r);

    }

    public final static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36";

    public static HttpGet get(String url, HttpHost proxy, String refer) {
        HttpGet get = new HttpGet(url);
        RequestConfig.Builder config = RequestConfig.custom().setSocketTimeout(40000).
                setConnectTimeout(15000).setRedirectsEnabled(false);
        if (proxy != null) {
            config.setProxy(proxy);
        }
        get.setConfig(config.build());
        get.setHeader("Accept", "textml,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        get.setHeader("User-Agent", Utils.USER_AGENT);
        get.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
        get.setHeader("Connection", "keep-alive");
        get.setHeader("Accept-Encoding", "gzip");
        get.setHeader("Proxy-Connection", "keep-alive");
        if (refer == null)
            get.setHeader("Referer", "http://www.baidu.com/");
        else
            get.setHeader("Referer", refer);
        return get;
    }
}
