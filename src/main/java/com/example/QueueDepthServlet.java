package com.example;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.mq.jms.MQQueue;
import com.ibm.msg.client.wmq.common.CommonConstants;

import javax.jms.*;
import java.util.Enumeration;

public class QueueDepthServlet {

    public static void main(String[] args) {

        try {
            // 1️⃣ Load CCDT file
            System.setProperty("MQCHLLIB", "/home/adminuser/MQbinding");          // Directory containing TAB file
            System.setProperty("MQCHLTAB", "AMQCLCHL.TAB");       // TAB file name

            System.out.println("Using CCDT: " +
                    System.getProperty("MQCHLLIB") + "/" +
                    System.getProperty("MQCHLTAB"));

            // 2️⃣ Create QCF (client mode) – CCDT will provide host/channel info
            MQQueueConnectionFactory qcf = new MQQueueConnectionFactory();
            qcf.setIntProperty(CommonConstants.WMQ_CONNECTION_MODE,
                    CommonConstants.WMQ_CM_CLIENT);

            // 3️⃣ Force CCDT loading (CRITICAL)
            qcf.setStringProperty(
                    CommonConstants.WMQ_CCDTURL,
                    "file:/opt/ccdt/AMQCLCHL.TAB"
            );

            // 4️⃣ MQ queue name (update if needed)
            MQQueue queue = new MQQueue("TESTING.QUEUE");

            // 5️⃣ Create connection/session
            QueueConnection conn = qcf.createQueueConnection();
            QueueSession session = conn.createQueueSession(false,
                    Session.AUTO_ACKNOWLEDGE);

            // 6️⃣ Queue depth using QueueBrowser
            QueueBrowser browser = session.createBrowser(queue);

            int depth = 0;
            Enumeration<?> msgs = browser.getEnumeration();
            while (msgs.hasMoreElements()) {
                msgs.nextElement();
                depth++;
            }

            conn.close();

            // 7️⃣ Print depth
            System.out.println("Queue Depth = " + depth);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
