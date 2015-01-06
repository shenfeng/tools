package me.shenfeng;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by feng on 1/6/15.
 */
public abstract class MainBase {

    protected static Logger LOGGER;

    @Option(name = "-h", usage = "Print help and exits")
    protected boolean help = false;

    @Option(name = "-db", usage = "Kanzhun database url")
    protected String db = "root@|jdbc:mysql://127.0.0.1:3306/tools";


    public abstract void run() throws Exception;

    public void parseArgsAndRun(String[] args) {
        LOGGER = LoggerFactory.getLogger(this.getClass());
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(120);

        try {
            parser.parseArgument(args);
            if (this.help) {
                System.err.println("java {{cp}} " + this.getClass().getCanonicalName() + " [options...] arguments...");
                parser.printUsage(System.err);
                System.exit(1);
            } else {
                this.run();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
