package com.ccreanga.history.kafka;

import com.ccreanga.protocol.incoming.MatchMsg;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
@Slf4j
@EnableAsync
public class KafkaMessageProducer {

    private final KafkaTemplate<Long, byte[]> kafkaTemplate;

    public KafkaMessageProducer(KafkaTemplate<Long, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Async
    public void sendAsynchToKafka(String topic, MatchMsg message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        message.writeExternal(baos);
        ProducerRecord<Long, byte[]> record = new ProducerRecord<>(topic, message.getMatchId(), baos.toByteArray());
        ListenableFuture<SendResult<Long,byte[]>> future = kafkaTemplate.send(record);

        future.addCallback(new ListenableFutureCallback<>() {
            public void onSuccess(SendResult<Long, byte[]> result) {
                if (log.isTraceEnabled())
                    log.trace("Message sent succesfully to topic {}", topic);
                //todo - handle success
            }

            public void onFailure(Throwable ex) {
                if (log.isTraceEnabled())
                    log.trace("Message ent failure, exception {}, topic {}", ex.getMessage(), topic);
                //todo - handle failure
            }
        });
    }

}
