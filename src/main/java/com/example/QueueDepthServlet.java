package com.example;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.mq.jms.MQQueue;
import com.ibm.msg.client.wmq.common.CommonConstants;

import javax.jms.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Enumeration;

public class QueueDepthServlet extends HttpServlet {

    static {
        // CCDT path for IBM MQ Client
        System.setProperty("MQCHLLIB", "/home/adminuser/MQbinding");
        System.setProperty("MQCHLTAB", "AMQCLCHL.TAB");

        // Debug: print CCDT path to Tomcat logs
        System.out.println(">>> CCDT File: " +
                System.getProperty("MQCHLLIB") + "/" +
                System.getProperty("MQCHLTAB"));
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
            // Create MQ QCF (client mode)
            MQQueueConnectionFactory qcf = new MQQueueConnectionFactory();
            qcf.setIntProperty(CommonConstants.WMQ_CONNECTION_MODE, CommonConstants.WMQ_CM_CLIENT);

            // Force CCDT loading (required)
            qcf.setStringProperty(
                    CommonConstants.WMQ_CCDTURL,
                    "file:/home/adminuser/MQbinding/AMQCLCHL.TAB"
            );

            // Create Queue reference
            MQQueue queue = new MQQueue("TESTING.QUEUE");

            // Create connection
            conn = qcf.createQueueConnection();
            conn.start();  // REQUIRED for browse

            QueueSession session = conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

            // Browse queue and count depth
            QueueBrowser browser = session.createBrowser(queue);

            int depth = 0;
            Enumeration<?> msgs = browser.getEnumeration();

            while (msgs.hasMoreElements()) {
                msgs.nextElement();
                depth++;
            }

            out.println("Queue Depth = " + depth);

        } catch (Exception e) {
            e.printStackTrace();
            out.println("ERROR: " + e.getMessage());
        } finally {
            // Cleanup MQ connection
            if (conn != null) {
                try { conn.close(); } catch (Exception ignore) {}
            }
        }
    }
}
