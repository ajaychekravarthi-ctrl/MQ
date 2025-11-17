package com.example;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.mq.jms.MQQueue;
import com.ibm.msg.client.wmq.common.CommonConstants;

import javax.jms.*;
import javax.servlet.http.*;
import java.io.PrintWriter;

public class QueueDepthServlet extends HttpServlet {

    static {
        System.setProperty("MQCHLLIB", "/home/adminuser/MQbinding");
        System.setProperty("MQCHLTAB", "AMQCLCHL.TAB");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("text/plain");

        try {
            PrintWriter out = resp.getWriter();

            // Build QCF that depends on CCDT
            MQQueueConnectionFactory qcf = new MQQueueConnectionFactory();
            qcf.setIntProperty(CommonConstants.WMQ_CONNECTION_MODE,
                    CommonConstants.WMQ_CM_CLIENT);

            // Queue name (MQ real queue name)
            Queue queue = new MQQueue("TESTING.QUEUE");

            QueueConnection conn = qcf.createQueueConnection();
            QueueSession session = conn.createQueueSession(false,
                    Session.AUTO_ACKNOWLEDGE);

            QueueBrowser browser = session.createBrowser(queue);

            int depth = 0;
            java.util.Enumeration<?> messages = browser.getEnumeration();
            while (messages.hasMoreElements()) {
                messages.nextElement();
                depth++;
            }

            conn.close();
            out.println("Queue Depth: " + depth);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
