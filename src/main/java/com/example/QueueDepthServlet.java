package com.example;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.PrintWriter;

public class QueueDepthServlet extends HttpServlet {

    static {
        // Load CCDT file
        System.setProperty("MQCHLLIB", "/home/adminuser/MQbinding");
        System.setProperty("MQCHLTAB", "AMQCLCHL.TAB");
        System.out.println("CCDT Loaded Successfully");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("text/plain");

        try {
            PrintWriter out = resp.getWriter();

            // Load JNDI context (from bindings)
            InitialContext ctx = new InitialContext();

            QueueConnectionFactory qcf =
                    (QueueConnectionFactory) ctx.lookup("MYQCF");

            Queue queue =
                    (Queue) ctx.lookup("TESTING.QUEUE");

            QueueConnection conn = qcf.createQueueConnection();
            QueueSession session =
                    conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

            // Use QueueBrowser to check depth
            QueueBrowser browser = session.createBrowser(queue);
            int depth = 0;

            java.util.Enumeration<?> msgs = browser.getEnumeration();
            while (msgs.hasMoreElements()) {
                msgs.nextElement();
                depth++;
            }

            conn.close();

            out.println("Queue Depth: " + depth);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
