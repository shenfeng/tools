package sf.download.value;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by feng on 8/22/14.
 */
public class AttrValue implements Value {

    private final String attr;

    public AttrValue(String attr) {
        this.attr = attr;
    }

    @Override
    public Object extract(Object e) {
        if (e instanceof Elements) {
            if (((Elements) e).size() > 1) {
                List<String> r = new ArrayList<>();
                for (Element element : (Elements) e) {
                    r.add(element.attr(attr));
                }
                return r;
            } else if (((Elements) e).size() == 1) {
                return ((Elements) e).get(0).attr(attr);
            }
        } else if (e instanceof Element) {
            return ((Element) e).attr(attr);
        }
        return null;
    }
}
