package com.food.ordering.system.kafka.config.data;

import lombok.Data;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Configuration
@ConfigurationProperties(prefix = "kafka-config")
@Validated
public class KafkaConfigData {
    @NotNull
    private String bootstrapServers;
    @NotNull
    private String schemaRegistryUrlKey;
    @NotNull
    private String schemaRegistryUrl;
    @NotNull
    private Integer numOfPartitions;
    @NotNull
    private Short replicationFactor;
}

/*
 * application.ymlの設定
 kafka-config:
  bootstarp-servers: localhost:19092, localhost:29092, localhost:39092
  schema-regstry-url-key: schema.registry.url
  schema-regstry-url: http://localhost:8081
  num-of-partitions: 3
  replication-factor: 3
 */