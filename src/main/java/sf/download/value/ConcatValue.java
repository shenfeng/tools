package sf.download.value;

/**
 * Created by feng on 8/22/14.
 */
public class ConcatValue implements Value {
    private final String template;

    public ConcatValue(String template) {
        this.template = template;
    }

    @Override
    public String extract(Object e) {
        return this.template.replace("$1", e.toString());
    }
}
