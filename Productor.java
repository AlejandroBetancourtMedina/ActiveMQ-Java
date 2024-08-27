package org.example;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.Random;

public class Productor {

    // Enum para representar las acciones de usuario
    public enum UserAction {
        CONFIGURACION("IR A OPCIONES DE CONFIGURACION"),
        PORTADA("VER PORTADA"),
        LOGIN("ACCEDER A LA APLICACION"),
        SUGERENCIA("ENVIAR SUGERENCIA");

        private final String userAction;

        // Constructor del enum
        private UserAction(String userAction) {
            this.userAction = userAction;
        }

        // Método para obtener la acción como cadena
        public String getActionAsString() {
            return this.userAction;
        }
    }

    private static final Random RANDOM = new Random(System.currentTimeMillis());
    private static final String URL = "tcp://localhost:61616";
    private static final String USER = ActiveMQConnection.DEFAULT_USER;
    private static final String PASSWORD = ActiveMQConnection.DEFAULT_PASSWORD;
    private static final String DESTINATION_QUEUE = "APLICATION1.QUEUE";
    private static final boolean TRANSACTED_SESSION = true;
    private static final int MESSAGES_TO_SEND = 20;

    // Método principal para enviar mensajes
    public void sendMessages() throws JMSException {
        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(USER, PASSWORD, URL);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        final Session session = connection.createSession(TRANSACTED_SESSION, Session.AUTO_ACKNOWLEDGE);
        final Destination destination = session.createQueue(DESTINATION_QUEUE);

        final MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);

        sendMessages(session, producer);
        session.commit();

        session.close();
        connection.close();

        System.out.println("Mensajes enviados correctamente");
    }


    // Método para enviar mensajes
    private void sendMessages(Session session, MessageProducer producer) throws JMSException {
        for (int i = 1; i <= MESSAGES_TO_SEND; i++) {
            final UserAction userActionToSend = getRandomUserAction();
            sendMessage(userActionToSend.getActionAsString(), session, producer);
        }
    }

    // Método para enviar un mensaje individual
    private void sendMessage(String message, Session session, MessageProducer producer) throws JMSException {
        final TextMessage textMessage = session.createTextMessage(message);
        producer.send(textMessage);
    }

    // Método para obtener una acción de usuario aleatoria
    private static UserAction getRandomUserAction() {
        final int userActionNumber = (int) (RANDOM.nextFloat() * UserAction.values().length);
        return UserAction.values()[userActionNumber];
    }

    // Método principal de la aplicaciónn
    public static void main(String[] args) throws JMSException {
        final Productor productor = new Productor();
        productor.sendMessages();
    }
}
