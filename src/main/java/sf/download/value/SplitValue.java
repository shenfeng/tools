package sf.download.value;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by feng on 8/23/14.
 */
public class SplitValue implements Value {
    private final String sep;
    private final int f;

    public SplitValue(String sep, String f) {
        this.sep = sep;
        this.f = Integer.parseInt(f);
    }

    /**
     * 职位月薪：面议
     * 工作地点：拉萨
     * 发布日期：2014-08-21
     *
     * @param e
     * @return
     */
    @Override
    public Object extract(Object e) {
        if (e instanceof Elements) {
            Elements es = (Elements) e;
            Map<String, String> m = new HashMap<>();
            for (Element ele : es) {
                String txt = ele.text();
                String[] parts = txt.split(sep);
                if (parts.length == 2) {
                    m.put(parts[0], parts[1]);
                }
            }
            if (m.size() > 0) {
                return m;
            }
        } else if (e instanceof String) {
            String[] parts = ((String) e).split(sep);
            if (parts.length > f) {
                return parts[f];
            }
        }

        return null;
    }
}
