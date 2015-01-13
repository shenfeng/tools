package sf.download;

import sf.MainBase;
import org.kohsuke.args4j.Option;

import java.io.File;

/**
 * Created by feng on 1/13/15.
 */
public class Crawler extends MainBase {

    @Option(name = "-conf", usage = "Json configration")
    protected String conf = "";


    @Option(name = "-dir", usage = "Destination dir")
    protected String dir = "/tmp/dajie_out";

    @Override
    public void run() throws Exception {
        if (!this.conf.endsWith(".json") || !new File(this.conf).exists()) {
            LOGGER.error("can not load json config file: %s", this.conf);
            return;
        }
    }

    public static void main(String[] args) {
        new Crawler().parseArgsAndRun(args);
    }
}
