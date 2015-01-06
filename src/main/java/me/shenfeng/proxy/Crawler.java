package me.shenfeng.proxy;

import gen.db.DBApi;
import me.shenfeng.MainBase;
import me.shenfeng.Utils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by feng on 1/5/15.
 */
public class Crawler extends MainBase {
    public void run() {
        final DataSource toolsDb = Utils.getDataSource(this.db);

        ArrayList<String> seeds = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            seeds.add(String.format("http://www.proxy.com.ru/list_%d.html", i + 1));
        }

        int totalSaved = 0;

        for (String seed : seeds) {
            int saved = 0, duplicate = 0;
            try {
                String html = fetchPage(seed);
                if (html == null || html.length() == 0) {
                    continue;
                }
                List<FetchedProxy> proxies = Parser.p(seed, html);
                if (proxies == null) {
                    continue;
                }

                for (FetchedProxy p : proxies) {
                    if (DBApi.findProxy(toolsDb, p.host, p.port) != null) {
                        duplicate += 1;
                        continue;
                    }
                    DBApi.saveProxy(toolsDb, p.host, p.port, p.type, p.source, p.domain);
                    saved += 1;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            totalSaved += saved;
            logger.info("saved: {}, duplicate: {}, total: {}, {}", saved, duplicate, totalSaved, seed);
        }
    }

    public static void main(String[] args) {
        Crawler main = new Crawler();
        main.parseArgsAndRun(args);
    }

    private static final Logger logger = LoggerFactory.getLogger(Crawler.class);


    public static String fetchPage(String url) throws IOException {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            CloseableHttpResponse resp = client.execute(Utils.get(url, null, null));
            if (resp.getStatusLine().getStatusCode() == 200) {
                return Utils.toString(resp);
            } else {
                resp.close();
                logger.info("{}, {}", resp.getStatusLine().getStatusCode(), url);
                return null;
            }
        }
    }
}



