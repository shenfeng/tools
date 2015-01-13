package sf.download.value;

/**
 * Created by feng on 8/22/14.
 */
public class IdentityValue implements Value {
    @Override
    public String extract(Object e) {
        return e.toString();
    }
}
