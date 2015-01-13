import sf.Utils;
import sf.parser.BaiduParser;
import sf.parser.BaiduVParser;
import sf.parser.DajieParser;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by feng on 1/10/15.
 */
public class ParsersTest {

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

    @Test
    public void testParseBaidu() {

        String url = "http://www.baidu.com/s?wd=www.tsglgt.cn%40v&_=MTQ0NQ==";
        String html = Utils.getResource("parser/baidu.html");

        BaiduParser.Result r = new BaiduParser().parse(url, html);
        Assert.assertEquals(r.items.size(), 8);

        Assert.assertEquals("www.tsglgt.cn@v", r.word);
        Assert.assertEquals(1445, r.id);
    }

    @Test
    public void testParseDajie() {

        String url = "http://www.dajie.com/corp/1000777";
        String html = Utils.getResource("parser/dajie.html");

        DajieParser.Dajie r = new DajieParser().parse(url, html);

        Assert.assertEquals("山东电力公司", r.com);
        Assert.assertEquals("http://2.f1.dajieimg.com/group1/M00/45/63/CgpAo1KigiWAb6x_AAAAoLwQons464b.jpg", r.logo);
        Assert.assertEquals(12246, r.follow);
        Assert.assertEquals("http://www.sepco3.com", r.web);
        Assert.assertEquals("1000人以上", r.size);
        Assert.assertEquals("青岛", r.place);
        Assert.assertEquals("建筑设计/规划, 土木工程, 原油/能源", r.ind);
        Assert.assertEquals(9, r.rate);
    }
}
