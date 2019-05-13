package com.ccreanga.history.kafka;

import com.ccreanga.protocol.outgoing.server.DataMsg;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaMessageConsumer {

    private final KafkaTemplate<Long, byte[]> kafkaTemplate;

    private final ConsumerFactory<Long,byte[]> factory;

    public KafkaMessageConsumer(KafkaTemplate<Long, byte[]> kafkaTemplate,
        ConsumerFactory<Long, byte[]> factory) {
        this.kafkaTemplate = kafkaTemplate;
        this.factory = factory;
    }

    public void consume(String topic,long startTimestamp,long endTimestamp, OutputStream out){
        AtomicBoolean stop = new AtomicBoolean(false);
        try (Consumer<Long, byte[]> consumer = factory.createConsumer()) {
            SeekToTimeOnRebalance seekToTimeOnRebalance = new SeekToTimeOnRebalance(consumer, startTimestamp);
            consumer.subscribe(Collections.singletonList(topic), seekToTimeOnRebalance);

            while (!stop.get()) {
                ConsumerRecords<Long, byte[]> consumerRecords = consumer.poll(Duration.ofSeconds(1));

                consumerRecords.forEach(record -> {
                    try {
                        byte[] value = record.value();
                        DataMsg dataMsg = DataMsg.readExternal(new ByteArrayInputStream(value));
                        //System.out.println(dataMsg.getMessage().getTimestamp());
                        if (dataMsg.getTimestamp()>endTimestamp){
                            stop.set(true);
                            return;
                        }else {
                            log.trace("Writing message from topic {}",topic);
                            out.write(value);
                        }
                    } catch (IOException e) {
                        //todo
                    }

                });

                consumer.commitAsync();
                /**
                 org.apache.kafka.clients.consumer.CommitFailedException: Commit cannot be completed since the group has already rebalanced and assigned the partitions to another member. This means that the time between subsequent calls to poll() was longer than the configured max.poll.interval.ms, which typically implies that the poll loop is spending too much time message processing. You can address this either by increasing max.poll.interval.ms or by reducing the maximum size of batches returned in poll() with max.poll.records.
                 */
            }
        }
    }

    public static class SeekToTimeOnRebalance implements ConsumerRebalanceListener {
        private Consumer<?, ?> consumer;
        private final Long startTimestamp;

        public SeekToTimeOnRebalance(Consumer<?, ?> consumer, Long startTimestamp) {
            this.consumer = consumer;
            this.startTimestamp = startTimestamp;
        }

        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            Map<TopicPartition, Long> timestampsToSearch = new HashMap<>();
            for (TopicPartition partition : partitions) {
                timestampsToSearch.put(partition,  startTimestamp);
            }
            // for each assigned partition, find the earliest offset in that partition with a timestamp
            // greater than or equal to the input timestamp
            Map<TopicPartition, OffsetAndTimestamp> outOffsets = consumer.offsetsForTimes(timestampsToSearch);
            System.out.println(outOffsets);
            for (TopicPartition partition : partitions) {
                System.out.println(outOffsets.get(partition).timestamp());
                Long seekOffset = outOffsets.get(partition).offset();
                Long currentPosition = consumer.position(partition);
                // seek to the offset returned by the offsetsForTimes API
                // if it is beyond the current position
                if (seekOffset.compareTo(currentPosition) > 0) {
                    consumer.seek(partition, seekOffset);
                }
            }
        }

        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        }

    }

}
