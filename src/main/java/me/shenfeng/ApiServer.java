package me.shenfeng;

import me.shenfeng.api.ApiHandler;
import me.shenfeng.api.Dispatcher;
import org.apache.commons.dbcp.BasicDataSource;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by feng on 1/4/15.
 */
public class ApiServer {


    public static void main(String[] args) throws Exception {
        Server server = new Server(7000);


        BasicDataSource db = new BasicDataSource();
        db.setUrl("jdbc:mysql://192.168.1.251:3306/tools");
        db.setUsername("root");
        db.setPassword("");


        final ApiHandler h = new ApiHandler(db);

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
