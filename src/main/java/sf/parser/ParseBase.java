package sf.parser;

import com.google.gson.Gson;
import sf.MainBase;
import org.kohsuke.args4j.Option;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;

/**
 * Created by feng on 1/12/15.
 */
public abstract class ParseBase extends MainBase {

    @Option(name = "-in", usage = "Input File")
    protected String in = "";

    @Option(name = "-out", usage = "Output File")
    protected String out = "";

    @Override
    public void run() throws Exception {
        Gson gson = new Gson();
        int total = 0, has = 0;
        if (this.in.length() > 0 && this.out.length() > 0) {
            FileOutputStream fos = new FileOutputStream(this.out);
            try (BufferedReader br = new BufferedReader(new FileReader(this.in))) {
                String line;
                while ((line = br.readLine()) != null) {
                    total += 1;
                    Job j = null;
                    try {
                        j = gson.fromJson(line, Job.class);
                        if (j.status != 200 || j.html == null || j.html.length() < 10) {
                            continue;
                        }

                        Object r = this.parse(j.url, j.html);
                        if (r == null) {
                            continue;
                        }
                        has += 1;
                        fos.write((new Gson().toJson(r) + "\n").getBytes(StandardCharsets.UTF_8));
                    } catch (Exception e) {
                        if (j != null)
                            LOGGER.error(j.url, e);
                    }
                }
            }
            fos.close();
            System.out.printf("%d/%d\n", has, total);
        }
    }

    public abstract Object parse(String url, String html);
}
