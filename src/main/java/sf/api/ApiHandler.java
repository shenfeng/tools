package sf.api;


import gen.api.Context;
import gen.api.IHandler;
import gen.api.Proxy;
import gen.db.DBApi;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by feng on 1/4/15.
 */
public class ApiHandler implements IHandler {

    private final DataSource db;

    public ApiHandler(DataSource db) {
        this.db = db;
    }

    @Override
    public boolean before(Context context) {
        return true;
    }

    @Override
    public void after(Context context) {

    }

    @Override
    public List<Proxy> getProxies(Context context, int limit) {

        List<Proxy> ret = new ArrayList<>();

        try {
            List<gen.db.Proxy> proxies = DBApi.loadValidProxies(this.db, limit <= 0 ? 100 : limit);

            for (gen.db.Proxy proxy : proxies) {
                ret.add(new Proxy(proxy.host, proxy.port, proxy.proxyType, proxy.latency));
            }


        } catch (SQLException e) {
            e.printStackTrace();

        }

        return ret;
    }
}
