package sf.download.value;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by feng on 8/21/14.
 */
public class TextValue implements Value {
    @Override
    public Object extract(Object e) {
        if (e instanceof Elements) {
            if (((Elements) e).size() > 1) {
                List<String> r = new ArrayList<>();
                for (Element element : (Elements) e) {
                    r.add(element.text());
                }
                return r;
            } else if (((Elements) e).size() == 1) {
                return ((Elements) e).get(0).text();
            }
        } else if (e instanceof Element) {
            return ((Element) e).text();
        }
        return null;
    }
}
