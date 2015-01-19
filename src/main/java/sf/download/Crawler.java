package sf.download;

import com.google.gson.Gson;
import sf.MainBase;
import org.kohsuke.args4j.Option;
import sf.ProxyManager;
import sf.download.handler.Config;
import sf.download.handler.Fetcher;
import sf.download.handler.PQueue;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

/**
 * Created by feng on 1/13/15.
 */
public class Crawler extends MainBase {

    @Option(name = "-threads", usage = "How many threads")
    protected int threads = 10;

    @Option(name = "-conf", usage = "Json configuration")
    protected String conf = "/home/feng/workspace/tools/src/test/resources/crawler/zhaopin_com.json";

    @Option(name = "-dir", usage = "Destination dir")
    protected String dir = "/tmp/zhaopin_com";

    @Option(name = "-proxies", usage = "proxies file")
    protected String proxy = "http://66.175.220.99/api/proxies?limit=7500";

    @Override
    public void run() throws Exception {
        if (!this.conf.endsWith(".json") || !new File(this.conf).exists()) {
            LOGGER.error("can not load json config file: %s", this.conf);
            return;
        }

        ProxyManager manager = new ProxyManager(proxy);

        Config cfg = new Gson().fromJson(new FileReader(this.conf), Config.class);
        HashMap<String, Config> cfgs = new HashMap<>();
        cfgs.put("conf", cfg);

        LOGGER.info("conf: {}, dir: {}, threads: {}, proxies: {}", this.conf, this.dir, this.threads, manager.size());

        PQueue queue = new PQueue(this.dir, cfgs);
        Fetcher f = new Fetcher(manager, this.threads, queue);
        f.doIt();
    }

    public static void main(String[] args) {
        new Crawler().parseArgsAndRun(args);
    }
}
