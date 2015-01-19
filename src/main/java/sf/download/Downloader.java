package sf.download;

import com.google.gson.Gson;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sf.MainBase;
import sf.ProxyManager;
import sf.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by feng on 1/5/15.
 */
public class Downloader extends MainBase {
    @Option(name = "-threads", usage = "How many threads")
    protected int threads = 10;

//    protected final int JOB_PER_PROXY = 3;

    @Option(name = "-in", usage = "Urls to download")
    protected String in = "/tmp/dajie_urls";

    @Option(name = "-dir", usage = "Destination dir")
    protected String dir = "/tmp/dajie_out";

    @Option(name = "-proxies", usage = "proxies file")
    protected String proxy = "http://66.175.220.99/api/proxies?limit=5500";

    @Option(name = "-check", usage = "Validation check word")
    protected String check = "大街网";

    @Option(name = "-error", usage = "Validation check word")
    protected String error = "";

    @Option(name = "-remove", usage = "Selectors to remove")
    protected String remove = "style";

    @Option(name = "-refer", usage = "Refer")
    protected String refer = "http://www.baid.com";

    private static final Logger logger = LoggerFactory.getLogger(Downloader.class);
    private FileOutputStream doneFile;
    private FileOutputStream datas;

    private static final AtomicInteger DONE = new AtomicInteger(0);

    public static void main(String[] args) throws IOException, InterruptedException {
        Downloader main = new Downloader();
        main.parseArgsAndRun(args);
    }

    private final LinkedList<String> candidates = new LinkedList<>();

    class Job implements UrlWorker.Job {
        public final String url;
        public String html;
        public int status;

        protected Job(String url) {
            this.url = url;
        }

        @Override
        public String getUrl() {
            return this.url;
        }

        @Override
        public boolean isOk(CloseableHttpResponse resp, long start) throws IOException {
            this.status = resp.getStatusLine().getStatusCode();
            if (status == 200) {
                String body = Utils.toString(resp);
                if (check.length() > 0 && !body.contains(check)) {
                    logger.info("check failed, {}, {} chars", url, check);
                    return false;
                }
                Header[] t = resp.getHeaders("Content-Type");
                if (t.length > 0 && t[0].getValue().toLowerCase().contains("html") && remove.length() > 1) {
                    Document d = Jsoup.parse(body, url);
                    d.select(remove).remove();
                    this.html = d.toString();
                } else {
                    this.html = body;
                }

                logger.info("done {}, {}/{} chars, takes {}ms, proxy: {}",
                        url, body.length(), html.length(), System.currentTimeMillis() - start, proxy);
                return true;
            } else {
                Header[] location = resp.getHeaders("Location");
                String l = location.length > 0 ? location[0].getValue() : "";

                logger.warn("status: {}, url: {} => {}", status, url, l);
                if (!error.isEmpty() && l.contains("verify.baidu")) {
                    return false;
                }

                // redirect, should follow
                return status >= 300 && status <= 400;
            }
        }

        @Override
        public void done(CloseableHttpResponse resp) {
            synchronized (Downloader.this) {
                try {
                    datas.write((new Gson().toJson(this) + "\n").getBytes(StandardCharsets.UTF_8));
                    doneFile.write((url + "\n").getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    logger.error(url, e);
                }
            }
        }

        @Override
        public HttpUriRequest request(HttpHost proxy) {
            return Utils.get(this.url, proxy, refer);
        }
    }

    public void run() throws Exception {
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
                candidates.add(s);
        }

        final ProxyManager proxies = new ProxyManager(this.proxy);
        this.doneFile = new FileOutputStream(dir + "/done", true);
        this.datas = new FileOutputStream(dir + "/datas", true);

        logger.info("load {} proxies, has {}/{} remaining job", proxies.size(), done.size(), candidates.size());

        ExecutorService service = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            service.submit(new UrlWorker(proxies, new UrlWorker.JobManager() {
                @Override
                public UrlWorker.Job poll() {
                    synchronized (candidates) {
                        String url = candidates.poll();
                        if (url != null) {
                            return new Job(url);
                        }
                    }
                    return null;
                }
            }, 6, threads));
        }
        service.shutdown();
        service.awaitTermination(10, TimeUnit.DAYS);
    }

//    public class Job {
//        final String url;
//        String html;
//        int status;
//
//        public Job(String url) {
//            this.url = url;
//        }
//
//        public boolean fetch(CloseableHttpClient client, HttpHost proxy) {
//            long start = System.currentTimeMillis();
//            try {
//                CloseableHttpResponse resp = client.execute(Utils.get(url, proxy, refer));
//                this.status = resp.getStatusLine().getStatusCode();
//                if (status == 200) {
//                    String body = Utils.toString(resp);
//                    if (check.length() > 0 && !body.contains(check)) {
//                        logger.info("check failed, {}, {} chars", url, check);
//                        return false;
//                    }
//                    Header[] t = resp.getHeaders("Content-Type");
//                    if (t.length > 0 && t[0].getValue().toLowerCase().contains("html") && remove.length() > 1) {
//                        Document d = Jsoup.parse(body, url);
//                        d.select(remove).remove();
//                        this.html = d.toString();
//                    } else {
//                        this.html = body;
//                    }
//
//                    logger.info("done {}, {}/{} chars, takes {}ms, proxy: {}",
//                            url, body.length(), html.length(), System.currentTimeMillis() - start, proxy);
//                    return true;
//                } else {
//                    resp.close();
//                    Header[] location = resp.getHeaders("Location");
//                    String l = location.length > 0 ? location[0].getValue() : "";
//
//                    logger.warn("status: {}, url: {} => {}", status, url, l);
//                    if (!error.isEmpty() && l.contains("verify.baidu")) {
//                        return false;
//                    }
//
//                    // redirect, should follow
//                    return status >= 300 && status <= 400;
//                }
//            } catch (Exception e) {
//                logger.warn("fetch {}, err: {}", url, e.getMessage());
//                return false;
//            }
//        }
//
//        public void markAsDone() {
//            synchronized (Downloader.this) {
//                try {
//                    datas.write((new Gson().toJson(this) + "\n").getBytes(StandardCharsets.UTF_8));
//                    doneFile.write((url + "\n").getBytes(StandardCharsets.UTF_8));
//                } catch (IOException e) {
//                    logger.error(url, e);
//                }
//            }
//        }
//    }

//    class URLWorker implements Runnable {
//        CloseableHttpClient client = HttpClientBuilder.create().build();
//        HttpHost proxy = proxies.borrow();
//        int proxiesJobs = 0;
//
//        @Override
//        public void run() {
//            while (true) {
//                Job job;
//                synchronized (candidates) {
//                    job = candidates.poll();
//                }
//                if (job == null) {
//                    logger.info("done");
//                    return;
//                }
//                int retry = 0;
//
//                while (true) {
//                    boolean ok = job.fetch(client, proxy);
//                    if (ok) {
//                        job.markAsDone();
//                        int done = DONE.incrementAndGet();
//                        proxiesJobs += 1;
//                        if (proxiesJobs > JOB_PER_PROXY) {
//                            logger.info("switch proxy, remain job: {}/{}", candidates.size(), done);
//                            reset();
//                        }
//                        break;
//                    } else {
//                        if (retry++ < 6)
//                            reset();  // try a new proxy
//                        else {
//                            logger.info("retry 6 times, give up: {}", job.url);
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//
//        private void reset() {
//            proxiesJobs = 0;
//            proxies.offer(proxy);
//            try {
//                client.close();
//            } catch (IOException ignore) {
//            }
//            client = HttpClientBuilder.create().build();
//            proxy = proxies.borrow();
//        }
//    }
}
