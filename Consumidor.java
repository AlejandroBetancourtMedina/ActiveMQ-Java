package org.example;


import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

public class Consumidor {
    private static final String URL = "tcp://localhost:61616";
    private static final String USER = ActiveMQConnection.DEFAULT_USER;
    private static final String PASSWORD = ActiveMQConnection.DEFAULT_PASSWORD;
    private static final String DESTINATION_QUEUE = "APLICATION1.QUEUE";
    private static final boolean TRANSACTED_SESSION = false;
    private static final int TIMEOUT = 1000;

    private final Map<String, Integer> consumedMessageTypes;
    private int totalConsumedMessages = 0;

    public Consumidor() {
        this.consumedMessageTypes = new HashMap<>();
    }

    public void processMessages() throws JMSException {
        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(USER, PASSWORD, URL);
        final Connection connection = connectionFactory.createConnection();
        connection.start();

        final Session session = connection.createSession(TRANSACTED_SESSION, Session.AUTO_ACKNOWLEDGE);
        final Destination destination = session.createQueue(DESTINATION_QUEUE);
        final MessageConsumer consumer = session.createConsumer(destination);

        processAllMessagesInQueue(consumer);

        consumer.close();
        session.close();
        connection.close();

        showProcessedResults();
    }

    private void processAllMessagesInQueue(MessageConsumer consumer) throws JMSException {
        Message message;
        while ((message = consumer.receive(TIMEOUT)) != null) {
            processMessage(message);
        }
    }

    private void processMessage(Message message) throws JMSException {
        if (message instanceof TextMessage) {
            final TextMessage textMessage = (TextMessage) message;
            final String text = textMessage.getText();
            incrementMessageType(text);
            totalConsumedMessages++;
        }
    }

    private void incrementMessageType(String message) {
        consumedMessageTypes.merge(message, 1, Integer::sum);
    }

    private void showProcessedResults() {
        System.out.println("Procesados un total de " + totalConsumedMessages + " mensajes");
        for (Map.Entry<String, Integer> entry : consumedMessageTypes.entrySet()) {
            String messageType = entry.getKey();
            int numberOfTypeMessages = entry.getValue();
            System.out.println("Tipo " + messageType + " Procesados " + numberOfTypeMessages + " (" +
                    (numberOfTypeMessages * 100 / totalConsumedMessages) + "%)");
        }
    }

    public static void main(String[] args) throws JMSException {
        final Consumidor userActionConsumer = new Consumidor();
        userActionConsumer.processMessages();
    }
}
