package me.shenfeng.api;

import me.shenfeng.db.DBApi;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by feng on 1/4/15.
 */
public class ApiHandler implements IHandler {

    private final BasicDataSource db;

    public ApiHandler(BasicDataSource db) {
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
            List<me.shenfeng.db.Proxy> proxies = DBApi.loadValidProxies(this.db, limit <= 0 ? 100 : limit);

            for (me.shenfeng.db.Proxy proxy : proxies) {
                ret.add(new Proxy(proxy.host, proxy.port, proxy.proxyType, proxy.latency));
            }


        } catch (SQLException e) {
            e.printStackTrace();

        }

        return ret;
    }
}
