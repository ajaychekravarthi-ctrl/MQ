package com.example;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

public class QueueDepthServlet extends javax.servlet.http.HttpServlet {

    @Override
    protected void doGet(javax.servlet.http.HttpServletRequest req,
                         javax.servlet.http.HttpServletResponse resp) {

        resp.setContentType("text/plain");

        PrintWriter out;
        try {
            out = resp.getWriter();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        QueueConnection conn = null;

        try {
            // Load JNDI context using .bindings
            Hashtable<String, Object> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY,
                    "com.sun.jndi.fscontext.RefFSContextFactory");
            env.put(Context.PROVIDER_URL,
                    "file:/home/adminuser/mqjndi");

            Context ctx = new InitialContext(env);

            // Lookup QCF & Queue from bindings
            QueueConnectionFactory qcf =
                    (QueueConnectionFactory) ctx.lookup("MyQCF");

            Queue queue = (Queue) ctx.lookup("MyQueue");

            // Create connection
            conn = qcf.createQueueConnection();
            conn.start();

            QueueSession session = conn.createQueueSession(false,
                    Session.AUTO_ACKNOWLEDGE);

            // Browse messages
            QueueBrowser browser = session.createBrowser(queue);
            Enumeration<?> msgs = browser.getEnumeration();

            int depth = 0;
            while (msgs.hasMoreElements()) {
                msgs.nextElement();
                depth++;
            }

            out.println("Queue Depth = " + depth);

            browser.close();
            session.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            out.println("ERROR: " + e.getMessage());
        }
    }
}
