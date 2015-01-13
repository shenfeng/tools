package sf.download.value;

import org.jsoup.select.Elements;

/**
 * Created by feng on 8/22/14.
 */
public class AttrValue implements Value {

    private final String attr;

    public AttrValue(String attr) {
        this.attr = attr;
    }

    @Override
    public String extract(Object e) {
        if (e instanceof Elements && ((Elements) e).size() > 0) {
            return ((Elements) e).get(0).attr(attr);
        }
        return null;
    }
}
