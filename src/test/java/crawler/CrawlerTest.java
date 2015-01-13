package crawler;

import com.google.gson.Gson;
import junit.framework.Assert;
import org.junit.Test;
import sf.Utils;
import sf.download.handler.BaseHandler;
import sf.download.handler.Config;
import sf.download.handler.ListData;

import java.util.List;
import java.util.Map;

/**
 * Created by feng on 1/13/15.
 */
public class CrawlerTest {

    @Test
    public void testCrawler58() {
        Config cfg = new Gson().fromJson(Utils.getResource("crawler/liepin_com.json"), Config.class);
        Assert.assertEquals(40 * 52, cfg.getSeeds().size());

        BaseHandler h = new BaseHandler(cfg);
        ListData l = h.OnListPage("http://company.liepin.com/so/?pagesize=20&keywords=&dq=010&industry=000&e_kind=000",
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
}
