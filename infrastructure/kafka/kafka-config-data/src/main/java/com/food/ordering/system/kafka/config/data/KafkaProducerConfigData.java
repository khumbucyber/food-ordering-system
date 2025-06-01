package com.food.ordering.system.kafka.config.data;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "kafka-producer-config")
// バリデーションを有効にするためのアノテーション//
// このアノテーションを付けることで、プロパティのバリデーションが有効になる
// 例えば、@NotNullや@Minなどのアノテーションをプロパティに付けることで、値の検証が行われるようになる
// https://chatgpt.com/share/6839030a-21a4-800e-80a5-fcdf68825fa0
@Validated
public class KafkaProducerConfigData {
    @NotNull
    private String keySerializerClass;
    @NotNull
    private String valueSerializerClass;
    @NotNull
    private String compressionType;
    @NotNull
    private String acks;
    @NotNull
    private Integer batchSize;
    @NotNull
    private Integer batchSizeBootFactor;
    @NotNull
    private Integer lingerMs;
    @NotNull
    // リクエストのタイムアウト時間（ミリ秒単位）
    private Integer requestTimeoutMs;
    // リトライ回数は、Kafkaプロデューサーがメッセージの送信に失敗した場合に再試行する回数を指定します。
    @NotNull
    private Integer retryCount;
}

/*
 * application.ymlの設定 
 kafka-producer-config:
  key-serializer-class: org.apache.kafka.common.serialization.StringSerializer
  value-serializer-class: io.confluent.kafka.serializers.KafkaAvroSerializer
  compression-type: snappy
  acks: all
  batch-size: 16384
  batch-size-boot-factor: 100
  linger-ms: 5
  request-timeout-ms: 60000
  retry-count: 5
 */
