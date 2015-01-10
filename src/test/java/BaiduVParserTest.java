import me.shenfeng.Utils;
import me.shenfeng.parser.BaiduVParser;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by feng on 1/10/15.
 */
public class BaiduVParserTest {

    @Test
    public void testParseBaiduv() {
        String url = "http://www.baidu.com/s?wd=baidu.com%40v";
        String html = Utils.getResource("parser/baiduv.html");

        BaiduVParser.BaiduV r = new BaiduVParser().parse(url, html);
        Assert.assertEquals("北京百度网讯科技有限公司", r.com);
        Assert.assertTrue(r.urls.contains("http://baidu.com"));

        Assert.assertEquals("北京百度网讯科技有限公司", r.wesitename);
        Assert.assertEquals("北京市海淀区上地十街10号百度大厦2层", r.place);
        Assert.assertTrue(r.tags.contains("内容丰富 (308)"));

        Assert.assertTrue(r.rate.p == 81);
        Assert.assertTrue(r.domains.size() == 300);
   }
}
