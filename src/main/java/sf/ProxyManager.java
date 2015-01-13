package sf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import gen.api.Proxy;
import org.apache.http.HttpHost;
import sf.proxy.Crawler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by feng on 1/13/15.
 */
public class ProxyManager {
    private final ConcurrentLinkedQueue<HttpHost> proxies;

    public ProxyManager(String path) {
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
        this.proxies = r;
    }

    public HttpHost borrow() {
        return proxies.poll();
    }

    public void offer(HttpHost p) {
        proxies.offer(p);
    }

    public int size() {
        return proxies.size();
    }
}
