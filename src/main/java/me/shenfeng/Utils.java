package me.shenfeng;

import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;

/**
 * Created by feng on 1/4/15.
 */
public class Utils {

    // user@password|jdbc_url
    public static DataSource getDataSource(String str) {
        String[] tmp = str.split("\\|");

        if (tmp.length != 2 || !tmp[0].contains("@")) {
            throw new RuntimeException("expect | @ seperated, but get " + str);
        }

        int idx = tmp[0].indexOf('@');
        BasicDataSource bs = new BasicDataSource();

        String url = tmp[1];
        // http://dev.mysql.com/doc/connector-j/en/connector-j-reference-configuration-properties.html
        if (!url.contains("zeroDateTimeBehavior")) {
            if (url.contains("?")) {
                url += "zeroDateTimeBehavior=convertToNull";
            } else {
                url += "?zeroDateTimeBehavior=convertToNull";
            }
        }

        bs.setUrl(url);
        bs.setUsername(tmp[0].substring(0, idx));
        bs.setPassword(tmp[0].substring(idx + 1));

        return bs;
    }


}
