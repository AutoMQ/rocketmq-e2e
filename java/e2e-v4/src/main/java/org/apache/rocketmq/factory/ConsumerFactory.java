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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.rocketmq.factory;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely;
import org.apache.rocketmq.client.rmq.RMQNormalConsumer;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.utils.RandomUtils;

public class ConsumerFactory {

    private static Boolean aclEnable = Boolean.parseBoolean(System.getProperty("aclEnable"));

    public static RMQNormalConsumer getRMQNormalConsumer(String nsAddr, String consumerGroup) {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
        consumer.setInstanceName(RandomUtils.getStringByUUID());
        consumer.setNamesrvAddr(nsAddr);
        return new RMQNormalConsumer(consumer);
    }

    public static RMQNormalConsumer getRMQNormalConsumer(String nsAddr, String consumerGroup, RPCHook rpcHook) {
        DefaultMQPushConsumer consumer;
        if (aclEnable) {
            consumer = new DefaultMQPushConsumer(consumerGroup, rpcHook, new AllocateMessageQueueAveragely());
        } else {
            consumer = new DefaultMQPushConsumer(consumerGroup);
        }
        consumer.setInstanceName(RandomUtils.getStringByUUID());
        consumer.setNamesrvAddr(nsAddr);
        return new RMQNormalConsumer(consumer);
    }
}