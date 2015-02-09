package com.slidespeech.server.converter;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.jms.client.HornetQConnectionFactory;

public class ApplicationContext implements ServletContextListener{
	private Queue queue;
	private Connection connection = null;
	private Session session = null;
	private MessageConsumer messageConsumer = null;
	
	public void contextInitialized(ServletContextEvent contextEvent) {
		System.out.println("Context Created, attaching JMS listener");

        try {
			queue = HornetQJMSClient.createQueue("conversionQueue");
			
			//Step 3. Perform a lookup on the Connection Factory
	        TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName());
	
	        ConnectionFactory cf = (ConnectionFactory) HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, transportConfiguration);
	        //one conversion at a time, leave the rest in the queue
	        ((HornetQConnectionFactory) cf).setConsumerWindowSize(0);
	        
	        //Step 4.Create a JMS Connection
			connection = cf.createConnection("slidespeech", "alabama");

	        //Step 5. Create a JMS Session
	        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	        
	        
	        //Step 16. We create a JMS message consumer
	        messageConsumer = session.createConsumer(queue);

	        //attach Listener to receive the messages
			MessageListener listener = new ConversionListener();
			messageConsumer.setMessageListener(listener);
	         
	         //Step 17. We start the connection so we can receive messages
	         connection.start();
	        
        
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public void contextDestroyed(ServletContextEvent contextEvent) {
		//close publisher connection
		MessagePublisher.getInstance().disconnect();
		//close consumer connection
		try {
			if(messageConsumer != null){
				messageConsumer.close();
			}
			if(session != null){
				session.close();
			}
			if(connection != null){
				connection.close();
	        }
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		System.out.println("Context Destroyed");
	}
}