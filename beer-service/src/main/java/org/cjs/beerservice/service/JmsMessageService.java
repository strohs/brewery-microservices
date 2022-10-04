package org.cjs.beerservice.service;

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
     * a general purpose "sender" for sending messages on a queue
     * @param destination - destination name
     * @param object - the object to be serialized as a TextMessage
     * @param typeForMessage - the simple class name of the object being sent
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
