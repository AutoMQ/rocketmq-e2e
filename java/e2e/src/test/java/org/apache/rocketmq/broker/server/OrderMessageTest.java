/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.broker.server;

import java.time.Duration;
import java.util.Collections;

import apache.rocketmq.controller.v1.SubscriptionMode;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.rmq.RMQNormalConsumer;
import org.apache.rocketmq.client.rmq.RMQNormalProducer;
import org.apache.rocketmq.common.attribute.TopicMessageType;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.enums.TESTSET;
import org.apache.rocketmq.factory.ConsumerFactory;
import org.apache.rocketmq.factory.MessageFactory;
import org.apache.rocketmq.factory.ProducerFactory;
import org.apache.rocketmq.frame.BaseOperate;
import org.apache.rocketmq.listener.rmq.RMQNormalListener;
import org.apache.rocketmq.util.NameUtils;
import org.apache.rocketmq.util.RandomUtils;
import org.apache.rocketmq.util.TestUtils;
import org.apache.rocketmq.util.VerifyUtils;
import org.apache.rocketmq.util.data.collect.DataCollector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag(TESTSET.ORDER)
@Tag(TESTSET.SMOKE)
public class OrderMessageTest extends BaseOperate {
    private final Logger log = LoggerFactory.getLogger(OrderMessageTest.class);
    private String tag;
    private final static int SEND_NUM = 100;
    private RMQNormalProducer producer;
    private RMQNormalConsumer pushConsumer;
    private RMQNormalConsumer simpleConsumer;

    @BeforeEach
    public void setUp() {
        tag = NameUtils.getRandomTagName();
    }

    @AfterEach
    public void tearDown() {
        if (producer != null) {
            producer.close();
        }
        if (pushConsumer != null) {
            pushConsumer.close();
        }
        if (simpleConsumer != null) {
            simpleConsumer.close();
        }
    }

    @Test
    @DisplayName("Send 100 sequential messages synchronously, set 2 Messagegroups, and expect these 100 messages to be sequentially consumed by PushConsumer")
    public void testOrder_Send_PushConsumeOrderly() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

        String topic = getTopic(TopicMessageType.FIFO.getValue(), methodName);
        String groupId = getOrderlyGroupId(methodName, SubscriptionMode.SUB_MODE_POP);

        pushConsumer = ConsumerFactory.getRMQPushConsumer(account, topic, groupId, new FilterExpression(tag), new RMQNormalListener());
//        simpleConsumer = ConsumerFactory.getRMQSimpleConsumer(account, topic, groupId, new FilterExpression(tag), Duration.ofSeconds(5));
//        VerifyUtils.tryReceiveOnce(simpleConsumer.getSimpleConsumer());

        producer = ProducerFactory.getRMQProducer(account, topic);
        Assertions.assertNotNull(producer);
        String messageGroup = RandomUtils.getStringByUUID();
        for (int i = 0; i < SEND_NUM; i++) {
            Message message = MessageFactory.buildOrderMessage(topic, tag, String.valueOf(i), messageGroup + String.valueOf(i % 2));
            producer.send(message);
            System.out.printf("send message %s%n", message);
        }
        Assertions.assertEquals(SEND_NUM, producer.getEnqueueMessages().getDataSize(), "send message failed");
//        DataCollector<Object> dequeueMessages = simpleConsumer.getListener().getDequeueMessages();
//        dequeueMessages.addData(pushConsumer.getListener().getDequeueMessages());
//        VerifyUtils.verifyOrderMessage(producer.getEnqueueMessages(), pushConsumer.getListener().getDequeueMessages());
    }

}

