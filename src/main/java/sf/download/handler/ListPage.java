package sf.download.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by feng on 8/26/14.
 */
public class ListPage {
    // list翻页的url
    public final List<String> pages = new ArrayList<>();

    // 终端页面的信息，其中key为url的为必须字段, 为终端页面的url
    // 列表页一般会列出一些meta信息，对应为 config里面的list 配置项
    public final List<Map<String, Object>> details = new ArrayList<>();
}
