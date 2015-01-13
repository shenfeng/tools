package sf.download.value;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by feng on 8/22/14.
 */
public class DateTsValue implements Value {

    private SimpleDateFormat format;

    public DateTsValue(String format) {
        this.format = new SimpleDateFormat(format);
    }

    @Override
    public String extract(Object e) {
        long l = Long.parseLong(e.toString());
        Date d = new Date(l);
        return this.format.format(d);
    }
}
