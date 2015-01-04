package me.shenfeng;

import me.shenfeng.db.DBApi;
import me.shenfeng.db.Proxy;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by feng on 11/8/14.
 */
public class ProxyCheck {
    @Option(name = "-h", usage = "Print help and exits")
    private boolean help = false;

    @Option(name = "-threads", usage = "How many threads")
    private int threads = 100;

    @Option(name = "-timeout", usage = "Timeout in seconds")
    private int timeout = 13;

    @Option(name = "-db", usage = "Kanzhun database url")
    protected String db = "root@|jdbc:mysql://192.168.1.251:3306/tools";

    private static final Logger logger = LoggerFactory.getLogger(ProxyCheck.class);

    public static void main(String[] args) throws InterruptedException, FileNotFoundException, SQLException {
        ProxyCheck main = new ProxyCheck();
        CmdLineParser parser = new CmdLineParser(main);
        parser.setUsageWidth(120);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            e.printStackTrace();
        }

        if (main.help) {
            System.err.println("java {{cp}} " + ProxyCheck.class.getCanonicalName() + " [options...] arguments...");
            parser.printUsage(System.err);
            System.exit(1);
        } else {
            main.run();
        }
    }

    private static final int OK = 1;
    private static final int FAIL = 0;

    private void run() throws FileNotFoundException, InterruptedException, SQLException {
        final DataSource ds = Utils.getDataSource(this.db);
        // last 8 hours
        final List<Proxy> proxies = DBApi.loadAllProxies(ds, System.currentTimeMillis() / 1000 - 3600 * 8);
        logger.info("load {} proxies, timeout: {}s, thread: {}", proxies.size(), timeout, threads);
        ExecutorService service = Executors.newFixedThreadPool(threads);

        final AtomicInteger success = new AtomicInteger(0);
        final AtomicInteger checked = new AtomicInteger(0);
        final AtomicInteger error = new AtomicInteger(0);
        final AtomicInteger fail = new AtomicInteger(0);

        for (final Proxy p : proxies) {
            service.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        int now = (int) (System.currentTimeMillis() / 1000);
                        long start = System.currentTimeMillis();

                        try (CloseableHttpClient client = HttpClients.custom().setMaxConnPerRoute(4).build()) {
                            HttpHost proxy = new HttpHost(p.host, p.port);

                            HttpGet get = httpGet("http://baidu.com", proxy);
                            CloseableHttpResponse resp = client.execute(get);
                            int code = resp.getStatusLine().getStatusCode();

                            byte[] bytes = EntityUtils.toByteArray(resp.getEntity());
                            String html = new String(bytes, 0, bytes.length, StandardCharsets.UTF_8);

                            long ms = System.currentTimeMillis() - start;

                            if (code == 200 && html.contains("refresh") && html.contains("baidu")) {
                                success.incrementAndGet();

                                logger.info("ok/error/fail/check/total: {}/{}/{}/{}/{}, {} ok, latency: {}ms",
                                        success.get(), error.get(), fail.get(), checked.get(), proxies.size(),
                                        proxy, ms);
                                DBApi.updateProxy(ds, p.id, now, OK, p.okCnt + 1, p.failCnt, ms);
                            } else {
                                fail.incrementAndGet();
                                DBApi.updateProxy(ds, p.id, now, FAIL, p.okCnt, p.failCnt + 1, ms);
                            }
                        } catch (IOException e) {
                            error.incrementAndGet();
                            DBApi.updateProxy(ds, p.id, now, FAIL, p.okCnt, p.failCnt + 1, System.currentTimeMillis() - start);
                        } finally {
                            checked.incrementAndGet();

                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        service.shutdown();
        service.awaitTermination(1, TimeUnit.HOURS);
        logger.info("ok/error/fail/check/total: {}/{}/{}/{}/{}",
                success.get(), error.get(), fail.get(), checked.get(), proxies.size()
        );
    }

    public HttpGet httpGet(String url, HttpHost proxy) {
        HttpGet get = new HttpGet(url);
        RequestConfig.Builder config = RequestConfig.custom().setSocketTimeout(timeout * 1000).
                setConnectTimeout(timeout * 1000).setRedirectsEnabled(false);

        config.setProxy(proxy);
        get.setConfig(config.build());
        get.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        get.setHeader("User-Agent", USER_AGENT);
        return get;
    }

    public final static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36";
}
