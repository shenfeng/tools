package sf.download.value;

import org.jsoup.select.Elements;

/**
 * Created by feng on 8/21/14.
 */
public class TextValue implements Value {
    @Override
    public String extract(Object e) {
        if (e instanceof Elements && ((Elements) e).size() > 0) {
            return ((Elements) e).get(0).text().trim();
        }
        return null;
    }
}
