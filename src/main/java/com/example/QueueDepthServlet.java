package com.example;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.mq.jms.MQQueue;
import com.ibm.msg.client.wmq.common.CommonConstants;

import javax.jms.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.PrintWriter;
import java.util.Enumeration;

public class QueueDepthServlet extends HttpServlet {

    static {
        // Set CCDT file environment
        System.setProperty("MQCHLLIB", "/home/adminuser/MQbinding");
        System.setProperty("MQCHLTAB", "AMQCLCHL.TAB");

        // Log CCDT path
        System.out.println(">>> CCDT File: "
                + System.getProperty("MQCHLLIB") + "/"
                + System.getProperty("MQCHLTAB"));

        // MQ Trace (to confirm CCDT loading)
        System.setProperty("com.ibm.msg.client.commonservices.trace.status", "ON");
        System.setProperty("com.ibm.msg.client.commonservices.trace.level", "10");
        System.setProperty("com.ibm.msg.client.commonservices.files.name", "/home/adminuser/mqtrace");

        // Validate CCDT file presence
        File ccdtFile = new File("/home/adminuser/MQbinding/AMQCLCHL.TAB");
        System.out.println(">>> CCDT exists: " + ccdtFile.exists()
                + " size=" + ccdtFile.length());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

        resp.setContentType("text/plain");

        PrintWriter out;
        try {
            out = resp.getWriter();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        QueueConnection conn = null;

        try {
            // Create MQ Connection Factory
            MQQueueConnectionFactory qcf = new MQQueueConnectionFactory();

            // Use Client Mode
            qcf.setIntProperty(CommonConstants.WMQ_CONNECTION_MODE,
                    CommonConstants.WMQ_CM_CLIENT);

            // Force CCDT usage
            qcf.setStringProperty(CommonConstants.WMQ_CCDTURL,
                    "file:/home/adminuser/MQbinding/AMQCLCHL.TAB");

            // Disable reconnect behavior (avoid 2278 delays)
            qcf.setIntProperty(
                    CommonConstants.WMQ_CLIENT_RECONNECT_OPTIONS,
                    CommonConstants.WMQ_CLIENT_RECONNECT_DISABLED
            );

            // Target queue
            MQQueue queue = new MQQueue("TESTING.QUEUE");

            // Create connection
            conn = qcf.createQueueConnection();
            conn.start();

            // Create session
            QueueSession session = conn.createQueueSession(false,
                    Session.AUTO_ACKNOWLEDGE);

            // Browse queue depth
            QueueBrowser browser = session.createBrowser(queue);

            int depth = 0;
            Enumeration<?> msgs = browser.getEnumeration();

            while (msgs.hasMoreElements()) {
                msgs.nextElement();
                depth++;
            }

            // Output depth
            out.println("Queue Depth = " + depth);

        } catch (Exception e) {
            e.printStackTrace();
            out.println("ERROR: " + e.getMessage());
        } finally {
            // Cleanup
            if (conn != null) {
                try { conn.close(); } catch (Exception ignore) {}
            }
        }
    }
}
