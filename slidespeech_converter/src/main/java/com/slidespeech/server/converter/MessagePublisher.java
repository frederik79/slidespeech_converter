package com.slidespeech.server.converter;

import java.io.IOException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;

public class MessagePublisher {
	
	private static ObjectMapper mapper;
	private static final MessagePublisher _instance = new MessagePublisher();
	private MessageProducer processedProducer;
	private Connection connection;
	private Session session;
	
	private MessagePublisher() {
		//init mapper
		mapper = new ObjectMapper();
		//init messaging
		try {
	         //Perfom lookup on the queue
			 Queue processedQueue = HornetQJMSClient.createQueue("processedQueue");
			 
	         //Perform a lookup on the Connection Factory
	         TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName());

	         ConnectionFactory cf = (ConnectionFactory) HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, transportConfiguration);

	         //Create a JMS Connection
			 connection = cf.createConnection("slidespeech", "alabama");

	         //Create a JMS Session
	         session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

	         //Create a JMS Message Producer
	         processedProducer = session.createProducer(processedQueue);
	         
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	 
    public static MessagePublisher getInstance() {
        return _instance;
    }
    
    public synchronized void sendTextMessage(String messageText){
    	
    	try {
    		TextMessage message = session.createTextMessage(messageText);
    		processedProducer.send(message);
    	} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    
    public synchronized void sendMessage(Object message){
    	
    	try {
    		TextMessage msg = session.createTextMessage(mapper.writeValueAsString(message));
    		processedProducer.send(msg);
    	} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    
    /**
     * For synchronous requests, replys on tmp queue
     * @param messageText
     * @param dest
     */
    public synchronized void sendTextMessage(String messageText, Destination dest){
    	
    	try {
    		MessageProducer producer = session.createProducer(dest);
    		TextMessage message = session.createTextMessage(messageText);
    		producer.send(message);
    		producer.close();
    		
    	} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    
    /**
     * For synchronous requests, replys on tmp queue
     * @param message
     * @param dest
     */
    public synchronized void sendMessage(Object message, Destination dest){
    	
    	try {
    		MessageProducer producer = session.createProducer(dest);
    		TextMessage msg = session.createTextMessage(mapper.writeValueAsString(message));
    		producer.send(msg);
		 	producer.close();
    	
    	} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }  
    
    public void disconnect(){
    	try {
    		if(processedProducer != null){
    			processedProducer.close();
    		}
    		if(session != null){
    			session.close();
    		}
	    	if(connection != null)
	        {
				connection.close();
	        }
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
    }

}
