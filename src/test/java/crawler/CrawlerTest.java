package crawler;

import com.google.gson.Gson;
import junit.framework.Assert;
import org.junit.Test;
import sf.Utils;
import sf.download.handler.Handler;
import sf.download.handler.Config;
import sf.download.handler.ListPage;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by feng on 1/13/15.
 */
public class CrawlerTest {

    @Test
    public void testCrawlerLiepin() {
        Config cfg = new Gson().fromJson(Utils.getResource("crawler/liepin_com.json"), Config.class);
        Assert.assertEquals(40 * 52, cfg.getSeeds().size());

        Handler h = new Handler(cfg);
        ListPage l = h.OnListPage("http://company.liepin.com/so/?pagesize=20&keywords=&dq=010&industry=000&e_kind=000",
                Utils.getResource("crawler/liepin_com_list.html"));

        Map<String, Object> d = h.OnDetailPage("http://company.liepin.com/7920451",
                Utils.getResource("crawler/lipin_com_detail.html"));


        Assert.assertEquals("芳姿(北京)商贸有限公司", d.get("company").toString());
        Assert.assertEquals("北京市朝阳区工人体育场北路8号院 三里屯SOHO A座 1706室", d.get("address").toString());
        Assert.assertEquals("外商独资·外企办事处", d.get("nature").toString());
        Assert.assertEquals("北京", d.get("city").toString());
        Assert.assertEquals("100-499人", d.get("size").toString());
        Assert.assertEquals("http://gm0.lietou-static.com/user/pic_logo/big/1013/10129255.jpg", d.get("logo").toString());


        Assert.assertTrue(d.get("description").toString().contains("丰富护肤品牌管理经验"));

        d = h.OnDetailPage("http://company.liepin.com/1933452",
                Utils.getResource("crawler/liepin_com_detail2.html"));

        Assert.assertEquals(3, ((List) d.get("pics")).size());
        Assert.assertEquals(((List) d.get("tags")).size(), 13);
    }


    @Test
    public void testZhaopin() {
        Config cfg = new Gson().fromJson(Utils.getResource("crawler/zhaopin_com.json"), Config.class);
        Assert.assertEquals(52 * 36, cfg.getSeeds().size());
        Assert.assertEquals(cfg.getSeeds().size(), new HashSet<>(cfg.getSeeds()).size());

        Handler h = new Handler(cfg);
        ListPage l = h.OnListPage("http://sou.zhaopin.com/jobs/searchresult.ashx?jl=%E5%8C%97%E4%BA%AC&kw=%E7%9F%A5&kt=2&isadv=0&sg=49ac9abbd1704b67a6cb441bcded4620&p=3",
                Utils.getResource("crawler/zhaopin_list.html"));


        Map<String, Object> d = h.OnDetailPage("http://company.zhaopin.com/P2/CC1906/5941/CC190659416.htm",
                Utils.getResource("crawler/zhaopin_detail.html"));

        Assert.assertEquals("北京嘉和知远咨询有限公司", d.get("company"));
        Assert.assertTrue(d.get("properties").toString().contains("专业服务/咨询"));
        Assert.assertTrue(d.get("introduction").toString().contains("北京嘉和知远咨询"));
    }
}
