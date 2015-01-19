package sf.download.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sf.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by feng on 1/14/15.
 */
public class PQueue {
    public final String dir;

    static final String PENDING_FILE = "__tasks.json";
    static final String DONE_FILE = "__done.json";
    static final String TMP_RESULT_FILE = "__tmp_results.json";
    static final String RESULT_FILE = "__results.json";

    private final Gson gson = new Gson();
    private static final Logger logger = LoggerFactory.getLogger(PQueue.class);

    private final AtomicInteger dedup = new AtomicInteger(0);

    //    private final FileOutputStream doneFos;
    private final FileOutputStream pendingFos;
    private final FileOutputStream tmpResultFos;

    private final LinkedList<FetchTask> queue = new LinkedList<>();
    private final HashSet<String> queued = new HashSet<>();
    private final Map<String, Config> configs;
    private final FileOutputStream doneFos;

    public Config get(String key) {
        return configs.get(key);
    }

    public PQueue(String dir, Map<String, Config> configs) throws IOException {
        this.dir = dir;
        this.configs = configs;

        File d = new File(dir);
        boolean resume = d.exists() && d.isDirectory() && join(PENDING_FILE).length() > 0;
        if (!resume && !d.isDirectory()) {
            if (!d.mkdir()) {
                throw new RuntimeException("can not make dir " + dir);
            }
        }

        this.doneFos = new FileOutputStream(join(DONE_FILE), true);
        this.pendingFos = new FileOutputStream(join(PENDING_FILE), true);
        this.tmpResultFos = new FileOutputStream(join(TMP_RESULT_FILE), true);

        if (resume) {
            String line;
            try (BufferedReader br = new BufferedReader(new FileReader(join(DONE_FILE)))) {
                while ((line = br.readLine()) != null) {
                    doneUrls.add(line.trim());
                }
            }

            try (BufferedReader br = new BufferedReader(new FileReader(join(PENDING_FILE)))) {
                while ((line = br.readLine()) != null) {
                    queueTask(gson.fromJson(line, FetchTask.class), false);
                }
            }
        } else {
            for (Map.Entry<String, Config> entry : configs.entrySet()) {
                Set<FetchTask> tasks = entry.getValue().getSeeds();
                for (FetchTask task : tasks) {
                    task.config = entry.getKey();
                    queueTask(task);
                }
            }
        }
        Collections.shuffle(queue);
        logger.info("resume: {}, done: {}, dedup: {}, queue: {}", resume, doneUrls.size(), dedup.get(), queue.size());
    }

    int n = 0;

    public boolean queueTask(FetchTask task) {
        return queueTask(task, true);
    }

    public synchronized boolean queueTask(FetchTask task, boolean disk) {
        if (isDone(task.url) || queued.contains(task.getUrl())) {
            dedup.incrementAndGet();
            return false;
        } else {
            queue.add(task);
            queued.add(task.getUrl());
            if (++n % 5000 == 0) {
                Collections.shuffle(queue);
            }
            if (disk) {
                append(this.pendingFos, task);
            }
            return true;
        }
    }


    public void markDone(String url) {
        synchronized (doneUrls) {
            doneUrls.add(url);
            try {
                this.doneFos.write((url + "\n").getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public synchronized void appendResult(Object o) {
        append(this.tmpResultFos, o);
    }

    public synchronized FetchTask next() {
        // double check, since doneUrls is not
        FetchTask task = queue.poll();
        while (task != null) {
            if (!isDone(task.url)) {
                break;
            }
//            job.incCounter(task.configName, Job.DEDUP);
            dedup.incrementAndGet();
            task = queue.poll();
        }
        if (task != null)
            queued.remove(task.getUrl());
        return task;
    }


    private File join(String f) {
        return new File(dir, f);
    }

    private void append(final FileOutputStream fs, final Object obj) {
        String str = gson.toJson(obj) + "\n";
        byte[] bytes = str.getBytes(Utils.UTF_8);
        try {
            synchronized (fs) {
                fs.write(bytes);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }


    //    private static final String HTTP = "http://";
    private final HashSet<String> doneUrls = new HashSet<>();

//    private String cleanUpUrl(String url) {
//        if (url.startsWith(HTTP)) {
//            url = url.substring(HTTP.length());
//        }
//        return url;
//    }

    private boolean isDone(String url) {
        synchronized (doneUrls) {
            return doneUrls.contains(url);
        }
    }

}
