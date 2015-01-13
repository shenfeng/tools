package sf.download.handler;


import org.jsoup.nodes.Element;
import sf.Utils;
import sf.download.value.*;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by feng on 8/21/14.
 */
public class Field {
    private static List<Class<? extends Value>> classes = Arrays.asList(
            AttrValue.class, ConcatValue.class, DateTsValue.class,
            HtmlValue.class, IdentityValue.class, JsonAttrValue.class,
            RegexValue.class, SplitValue.class, TextValue.class
    );
    // 名字，比如title， url， kind
    public String name;
    // jquery selector
    public String selector;
    // attr:href
    public String value;
    private Value[] vs;

    public Field(String name, String selector, String value) {
        this.name = name;
        this.selector = selector;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Field{" +
                "name='" + name + '\'' +
                ", selector='" + selector + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public Object get(Object e) {
        setIfNeeded();

//        Elements es = e.select(selector);
//        Object ret = es;
//

        if (e instanceof Element) {
            Object input = ((Element) e).select(selector);
            for (Value v : vs) {
                input = v.extract(input);
            }
            return input;
        } else {
            for (Value v : vs) {
                e = v.extract(e);
            }
            return e;
        }
    }

    private void setIfNeeded() {
        if (vs != null) {
            return;
        }

        // attr:href|regex:\d+
        String[] values = Utils.split(value, '|');
        List<Value> vList = new ArrayList<>(values.length);

        for (String v : values) {
            v = v.trim(); // trim space
            String[] inputArgs = Utils.split(v, ':');
            boolean find = false;

            for (Class<? extends Value> t : classes) {
                if (!t.getSimpleName().toLowerCase().startsWith(inputArgs[0])) {
                    continue;
                }

                find = true;
                try {
                    if (inputArgs.length == 1) {
                        vList.add(t.newInstance());
                    } else {
                        Class cTypes[] = new Class[inputArgs.length - 1];
                        for (int i = 0; i < cTypes.length; i++) {
                            cTypes[i] = String.class;
                        }
                        Constructor<? extends Value> c = t.getConstructor(cTypes);

                        String[] cArgs = new String[cTypes.length];
                        System.arraycopy(inputArgs, 1, cArgs, 0, inputArgs.length - 1);
                        vList.add(c.newInstance(cArgs));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            if (!find) {
                throw new RuntimeException(inputArgs[0] + " not found for " + value + " => " + v);
            }
        }

        if (vList.size() > 0)
            vs = vList.toArray(new Value[vList.size()]);

        if (vs == null) {
            throw new RuntimeException(value + " not understand");
        }
    }
}
