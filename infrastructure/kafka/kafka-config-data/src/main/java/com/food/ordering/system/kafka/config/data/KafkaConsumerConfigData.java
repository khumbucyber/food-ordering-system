package com.food.ordering.system.kafka.config.data;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "kafka-consumer-config")
@Validated
public class KafkaConsumerConfigData {
    @NotNull
    private String keyDeserializer;
    @NotNull
    private String valueDeserializer;
    @NotNull
    private String autoOffsetReset;
    @NotNull
    private String specificAvroReaderKey;
    @NotNull
    private String specificAvroReader;
    @NotNull
    private Boolean batchListner;
    @NotNull
    private Boolean autoStartup;
    @NotNull
    private Integer concurrencyLevel;
    @NotNull
    private Integer sessionTimeoutMs;
    @NotNull
    private Integer heartbeatIntervalMs;
    @NotNull
    private Integer maxPollIntervalMs;
    @NotNull
    private Long pollTimeoutMs;
    @NotNull
    private Integer maxPollRecords;
    @NotNull
    private Integer maxPartitionFetchBytesDefault;
    @NotNull
    private Integer maxPartitionFetchBytesBoostFactor;
}
