package sf.download.value;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by feng on 8/22/14.
 */
public class RegexValue implements Value {

    public final Pattern pattern;
    public final int group;

    /**
     * regex:.+-(.+):1 => 表示:正则表达式 .+-(.+), 要group 1. group 0 是全部
     * SD5-后台开发工程师（深圳） => 后台开发工程师（深圳）
     *
     * @param patten
     * @param group
     */
    public RegexValue(String patten, String group) {
        this.pattern = Pattern.compile(patten);
        this.group = Integer.parseInt(group);
    }

    @Override
    public String extract(Object e) {
        if (e instanceof String) {
            Matcher m = pattern.matcher(e.toString().trim());
            if (m.find()) {
                return m.group(this.group).trim();
            }
        }
        return null;
    }
}
