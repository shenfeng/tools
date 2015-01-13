package sf.proxy;

/**
 * Created by feng on 1/6/15.
 */
public class FetchedProxy {
    public final String host;
    public final int port;
    public final String type;
    public final String source;
    public final String domain;

    public FetchedProxy(String host, int port, String type, String source, String domain) {
        this.host = host;
        this.port = port;
        this.type = type;
        this.source = source;
        this.domain = domain;
    }
}
