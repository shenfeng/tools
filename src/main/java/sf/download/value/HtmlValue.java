package sf.download.value;

import org.jsoup.select.Elements;

/**
 * Created by feng on 8/21/14.
 */
public class HtmlValue implements Value {
    @Override
    public String extract(Object e) {
        if (e instanceof Elements && ((Elements) e).size() > 0) {
            return ((Elements) e).html();
//            return Utils.compactHtml(((Elements) e).html(), "http://no.com/");
            // get all, not just the first one
        }
        return null;
    }
}
