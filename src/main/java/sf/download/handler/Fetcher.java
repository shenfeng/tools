package sf.download.handler;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sf.ProxyManager;
import sf.Utils;
import sf.download.UrlWorker;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by feng on 1/14/15.
 */
public class Fetcher {
    private final ProxyManager manager;
    private final int threads;
    private final PQueue queue;

    public Fetcher(ProxyManager manager, int threads, PQueue queue) {
        this.manager = manager;
        this.threads = threads;
        this.queue = queue;
    }

    private final CountDownLatch latch = new CountDownLatch(1);

    class Job implements UrlWorker.Job {
        private final FetchTask task;
        private final Config cfg;
        private final Handler h;

        public Job(FetchTask task) {
            this.task = task;
            this.cfg = queue.get(task.config);
            this.h = new Handler(this.cfg);
        }

        @Override
        public String getUrl() {
            return task.getUrl();
        }

        @Override
        public boolean isOk(CloseableHttpResponse resp, long start) throws IOException {
            task.status = resp.getStatusLine().getStatusCode();

            if (task.status != 200) {
                if (task.status == 301 || task.status == 302) {
                    String r = Utils.getLocation(resp);
                    switch (cfg.check.isUrlOk(r)) {
                        case OK:
                            task.followUrl = r;
                            return false; // retry
                        case ERROR:
                            return true; // no more
                        case PROXY:
                            cfg.proxy = true;
                            return false; // retry
                        case NOT_FOLLOW:
                            return true; // no retry, no more
                    }
                }
                return false;
            } else {
                task.html = Utils.toString(resp);
                switch (cfg.check.isHtmlOk(task.html)) {
                    case OK:
                        return true;
                    case PROXY:
                        cfg.proxy = true;
                        return false; // retry
                    default:
                        return false; // retry
                }
            }
        }

        @Override
        public void done(CloseableHttpResponse resp) {
            queue.markDone(task.url);
            if (task.listpage) {
                ListPage d = this.h.OnListPage(task.url, task.html);
                int added = 0, list = 0, dedup = 0, ignore = 0;
                for (String page : d.pages) { // follow list page
                    if (cfg.check.isUrlOk(page) == Config.Flag.NOT_FOLLOW) {
                        continue;
                    }

                    if (queue.queueTask(new FetchTask(page, true, task.config, task.extras))) {
                        added += 1;
                        list += 1;
                    } else {
                        dedup += 1;
                    }
                }

                if (!cfg.ignoredetail) {
                    for (Map<String, Object> detail : d.details) {
                        String u = detail.get(Handler.URL).toString();
                        if (cfg.check.isUrlOk(u) == Config.Flag.NOT_FOLLOW) {
                            continue;
                        }

                        if (queue.queueTask(new FetchTask(u, task))) {
                            added += 1;
                        } else {
                            dedup += 1;
                        }

                        if (detail.size() > 1) {
                            queue.appendResult(detail);
                        }
                    }
                }

                if (added > 0) {
                    while (liveThread.get() < threads) {
                        startWorkerThread();
                    }
                }
                logger.info("done ignore/list/detail/dedup {}/{}/{}/{}, {}", ignore, added, list, d.details.size(), dedup, task.url);
            } else {
                Map<String, Object> r = h.OnDetailPage(task.url, task.html);
                if (r != null) {
                    if (task.followUrl != null) {
                        r.put("real_url", task.followUrl);
                    }
                    if (task.extras != null) {
                        r.putAll(task.extras);
                    }
                    queue.appendResult(r);
                }
                logger.info("detail done: {} {}", task.url, task.html.length());
            }
        }

        @Override
        public HttpUriRequest request(HttpHost proxy) {
            return Utils.get(task.getUrl(), proxy, task.getReferer());
        }
    }

    private void startWorkerThread() {
        liveThread.incrementAndGet();
        Thread t = new Thread(new UrlWorker(this.manager, new UrlWorker.JobManager() {
            @Override
            public UrlWorker.Job poll() {
                FetchTask t = queue.next();
                if (t != null) {
                    return new Job(t);
                }
                if (liveThread.decrementAndGet() == 0) {
                    latch.countDown();
                }
                return null;
            }
        }, 5, 8));
        t.setName("w-" + threadId.incrementAndGet());
        t.start();
    }

    public void doIt() {
        for (int i = 0; i < threads; i++) {
            startWorkerThread();
        }
        try {
            latch.await(); // wait for all tasks done
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private final AtomicInteger liveThread = new AtomicInteger(0);
    private final AtomicInteger threadId = new AtomicInteger(0);
    private static final Logger logger = LoggerFactory.getLogger(Fetcher.class);
}
