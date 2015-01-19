package sf.download;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sf.ProxyManager;
import sf.Utils;

import java.io.IOException;

/**
 * Created by feng on 1/14/15.
 */
public class UrlWorker implements Runnable {

    private final ProxyManager proxies;
    private final JobManager jobs;
    private final int maxRetry;
    private final int jobsPerProxy;

    public UrlWorker(ProxyManager proxies, JobManager jobs, int maxRetry, int jobsPerProxy) {
        this.proxies = proxies;
        this.jobs = jobs;
        this.maxRetry = maxRetry;
        this.jobsPerProxy = jobsPerProxy;

        this.proxy = this.proxies.borrow();
    }

    int proxiesJobs = 0;
    CloseableHttpClient client = HttpClientBuilder.create().build();
    HttpHost proxy;


    @Override
    public void run() {
        while (true) {
            Job job = jobs.poll();
            if (job == null) {
                break;
            }

            int retry = 0;
            long start = System.currentTimeMillis();
            while (retry < this.maxRetry) {
                try {
                    CloseableHttpResponse resp = client.execute(job.request(proxy));
                    if (job.isOk(resp, start)) {
                        job.done(resp);
                        if (proxiesJobs++ >= this.jobsPerProxy) {
                            reset();
                        }
                        break;
                    } else {
                        logger.info("fetch {}, proxy: {}, retry: {}", job.getUrl(), proxy, retry);
                        reset();
                    }
                    resp.close();
                } catch (IOException e) {
                    // retry
                    logger.warn("{}: {}/{}", job.getUrl(), e.getMessage(), retry);
                    reset();
                }
            }
        }
    }

    private void reset() {
        proxiesJobs = 0;
        proxies.offer(proxy);
        Utils.closeQuietly(client);
        // reopen a new one
        client = HttpClientBuilder.create().build();
        proxy = proxies.borrow();
    }


    public interface JobManager {
        Job poll();
    }

    public interface Job {
        String getUrl();

        boolean isOk(CloseableHttpResponse resp, long start) throws IOException;

        void done(CloseableHttpResponse resp);

        HttpUriRequest request(HttpHost proxy);
    }

    private static Logger logger = LoggerFactory.getLogger(UrlWorker.class);
}


