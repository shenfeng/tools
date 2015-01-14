package sf.download.handler;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sf.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;

/**
 * Created by feng on 1/14/15.
 */
public class PersistentQueue {
    public final String dir;

    static final String TASKS_FILE = "__tasks.json";
    static final String DONE_FILE = "__done.json";
    static final String TMP_RESULT_FILE = "__tmp_results.json";
    static final String RESULT_FILE = "__results.json";

    private final Gson gson = new Gson();
    private static final Logger logger = LoggerFactory.getLogger(PersistentQueue.class);

    private final FileOutputStream doneFs;


    public PersistentQueue(String dir) throws FileNotFoundException {
        this.dir = dir;

        File d = new File(dir);
        boolean resume = d.exists() && d.isDirectory() && join(TASKS_FILE).length() > 0;
        if (!resume && !d.isDirectory()) {
            if (!d.mkdir()) {
                throw new RuntimeException("can not make dir " + dir);
            }
        }

        this.doneFs = new FileOutputStream(join(DONE_FILE), true);
        this.pendingFs = new FileOutputStream(join(TASKS_FILE), true);
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

    private static final String HTTP = "http://";
    private final HashSet<String> doneUrls = new HashSet<>();

    private String cleanUpUrl(String url) {
        if (url.startsWith(HTTP)) {
            url = url.substring(HTTP.length());
        }
        return url;
    }

    private boolean isDone(String url) {
        synchronized (doneUrls) {
            return doneUrls.contains(cleanUpUrl(url));
        }
    }

    private void markDone(String url) {
        synchronized (doneUrls) {
            doneUrls.add(cleanUpUrl(url));
        }
    }

    public static class FetchTask {
        public final String url;
        public String followUrl;
        private String referer;

        public final boolean listpage;
        public final String config;


        public String html = "";
        public int status;
        public String err;

        public String getUrl() {
            if (followUrl != null) {
                return followUrl;
            }
            return url;
        }

        public String getReferer() {
            if (referer != null) {
                return referer;
            }
            return url;
        }

        public FetchTask(String url, boolean listpage, String config) {
            this.url = url;
            this.listpage = listpage;
            this.config = config;
        }

        public FetchTask(String url, FetchTask task) {
            this.listpage = false;
            this.config = task.config;
            this.url = url;
            this.referer = task.getUrl();
        }
    }

}
