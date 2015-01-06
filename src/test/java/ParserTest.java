import me.shenfeng.Utils;
import me.shenfeng.db.Proxy;
import me.shenfeng.proxy.Parser;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by feng on 1/5/15.
 */
public class ParserTest {

    @Test
    public void testParseProxyComRu() {
        String url = "http://www.proxy.com.ru/list_1.html";
        String html = Utils.getResource("proxies/proxycomcn.html");
        List<Proxy> r = Parser.p(url, html);
        Assert.assertEquals(50, r.size());
    }
}
