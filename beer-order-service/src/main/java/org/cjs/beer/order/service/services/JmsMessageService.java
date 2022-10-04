package org.cjs.beer.order.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.TextMessage;

@Service
@RequiredArgsConstructor
public class JmsMessageService {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    /**
     * a general purpose "sender" for sending jms messages to a queue
     * @param destination - name of the messaging endpoint (usually a queue name)
     * @param object - the object to be serialized as a TextMessage
     * @param typeForMessage - the simple class name of the object being serialized, this name will be set
     *                       as a message property using the key "_type"
     */
    public void sendJmsMessage(String destination, Object object, String typeForMessage) {
        jmsTemplate.send(destination, session -> {
            try {
                TextMessage message = session.createTextMessage(objectMapper.writeValueAsString(object));
                message.setStringProperty("_type", typeForMessage);
                return message;
            } catch (JsonProcessingException e) {
                throw new JMSException("boom");
            }
        });
    }
}
