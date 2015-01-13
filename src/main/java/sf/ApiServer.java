package sf;

import gen.api.Dispatcher;
import sf.api.ApiHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.kohsuke.args4j.Option;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by feng on 1/4/15.
 */
public class ApiServer extends MainBase {

    @Option(name = "-port", usage = "Port to listen to")
    protected int port = 9091;


    public static void main(String[] args) throws Exception {
        new ApiServer().parseArgsAndRun(args);
    }

    @Override
    public void run() throws Exception {
        Server server = new Server(this.port);
        final ApiHandler h = new ApiHandler(Utils.getDataSource(this.db));

        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                if (Dispatcher.dispatch(request, response, h)) {
                    baseRequest.setHandled(true);
                }
            }
        });

        server.start();
        server.join();
    }
}
