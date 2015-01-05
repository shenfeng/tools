package me.shenfeng.download;

import com.google.gson.Gson;
import me.shenfeng.Utils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by feng on 1/5/15.
 */
public class Downloader {
    @Option(name = "-h", usage = "Print help and exits")
    private boolean help = false;

    @Option(name = "-threads", usage = "How many threads")
    private int threads = 10;

    private final int JOB_PER_PROXY = 3;

    @Option(name = "-in", usage = "Urls to download")
    private String in = "/tmp/dajie_urls";

    @Option(name = "-dir", usage = "Destination dir")
    private String dir = "/tmp/dajie_out";

    @Option(name = "-proxies", usage = "proxies file")
    private String proxyFile = "proxies";

    @Option(name = "-check", usage = "Validation check word")
    private String check = "大街网";

    private static final Logger logger = LoggerFactory.getLogger(Downloader.class);
    private FileOutputStream doneFile;
    private FileOutputStream datas;
    private ConcurrentLinkedQueue<HttpHost> proxies;

    private static final AtomicInteger DONE = new AtomicInteger(0);


    public static void main(String[] args) throws IOException, InterruptedException {
        Downloader main = new Downloader();
        CmdLineParser parser = new CmdLineParser(main);
        parser.setUsageWidth(120);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            e.printStackTrace();
        }

        if (main.help) {
            System.err.println("java {{cp}} " + Downloader.class.getCanonicalName() + " [options...] arguments...");
            parser.printUsage(System.err);
            System.exit(1);
        } else {
            main.run();
        }
    }

    private ConcurrentLinkedQueue<Job> candidates = new ConcurrentLinkedQueue<>();

    private void run() throws IOException, InterruptedException {
        if (!new File(in).exists()) {
            throw new RuntimeException(in + " not exits");
        }

        HashSet<String> done = new HashSet<>();
        if (new File(dir + "/done").exists()) {
            done.addAll(Utils.readLines(dir + "/done"));
        }

        if (!new File(dir).isDirectory()) {
            if (!new File(dir).mkdirs()) {
                throw new RuntimeException("can not make dir: " + dir);
            }
        }

        for (String s : Utils.readLines(in)) {
            if (!done.contains(s))
                candidates.add(new Job(s));
        }

        this.proxies = Utils.loadProxies(this.proxyFile);
        this.doneFile = new FileOutputStream(dir + "/done", true);
        this.datas = new FileOutputStream(dir + "/datas", true);

        logger.info("load {} proxies, has {}/{} remaining job", proxies.size(), done.size(), candidates.size());

        ExecutorService service = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            service.submit(new URLWorker());
        }
        service.shutdown();
        service.awaitTermination(10, TimeUnit.DAYS);
    }

    class Job {
        final String url;
        String html;
        int status;

        public Job(String url) {
            this.url = url;
        }

        public boolean fetch(CloseableHttpClient client, HttpHost proxy) {
            long start = System.currentTimeMillis();
            HttpGet get = new HttpGet(url);
            RequestConfig.Builder config = RequestConfig.custom().setSocketTimeout(80000).
                    setConnectTimeout(15000).setRedirectsEnabled(false);
            if (proxy != null) {
                config.setProxy(proxy);
            }
            get.setConfig(config.build());
            get.setHeader("Accept", "textml,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            get.setHeader("User-Agent", USER_AGENT);
            get.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
            get.setHeader("Connection", "keep-alive");
            get.setHeader("Accept-Encoding", "gzip");
            get.setHeader("Proxy-Connection", "keep-alive");
            get.setHeader("Referer", "http://www.baidu.com/");

            try {
                CloseableHttpResponse resp = client.execute(get);
                this.status = resp.getStatusLine().getStatusCode();
                if (status == 200) {
                    String body = Utils.toString(resp);
                    if (check.length() > 0 && !body.contains(check)) {
                        logger.info("check failed, {}, {} chars", url, check);
                        return false;
                    }
                    Document d = Jsoup.parse(body, url);
                    d.select("script, style, link").remove();
                    this.html = d.toString();

                    logger.info("done {}, {}/{} chars, takes {}ms, proxy: {}",
                            url, body.length(), html.length(), System.currentTimeMillis() - start, proxy);
                    return true;
                } else {
                    resp.close();
                    logger.warn("status: {}, url: {}", status, url);
                    // redirect, should follow
                    return status >= 300 && status <= 400;
                }
            } catch (Exception e) {
                logger.warn("fetch {}, err: {}", url, e.getMessage());
                return false;
            }
        }

        public void markAsDone() {
            synchronized (Downloader.this) {
                try {
                    datas.write((new Gson().toJson(this) + "\n").getBytes(StandardCharsets.UTF_8));
                    doneFile.write((url + "\n").getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    logger.error(url, e);
                }
            }
        }
    }

    final static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36";

    class URLWorker implements Runnable {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpHost proxy = proxies.poll();
        int proxiesJobs = 0;

        @Override
        public void run() {
            while (true) {
                Job job = candidates.poll();
                if (job == null) {
                    logger.info("done");
                    return;
                }
                int retry = 0;

                while (true) {
                    boolean ok = job.fetch(client, proxy);
                    if (ok) {
                        job.markAsDone();
                        int done = DONE.incrementAndGet();
                        proxiesJobs += 1;
                        if (proxiesJobs > JOB_PER_PROXY) {
                            logger.info("switch proxy, remain job: {}/{}", candidates.size(), done);
                            reset();
                        }
                        break;
                    } else {
                        if (retry++ < 6)
                            reset();  // try a new proxy
                        else {
                            logger.info("retry 6 times, give up: {}", job.url);
                            break;
                        }
                    }
                }
            }
        }

        private void reset() {
            proxiesJobs = 0;
            proxies.offer(proxy);
            try {
                client.close();
            } catch (IOException ignore) {
            }
            client = HttpClientBuilder.create().build();
            proxy = proxies.poll();
        }
    }
}
