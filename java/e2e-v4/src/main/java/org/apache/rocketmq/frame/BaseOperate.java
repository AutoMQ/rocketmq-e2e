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

package org.apache.rocketmq.frame;

import apache.rocketmq.controller.v1.AcceptTypes;
import apache.rocketmq.controller.v1.CreateGroupReply;
import apache.rocketmq.controller.v1.CreateGroupRequest;
import apache.rocketmq.controller.v1.CreateTopicRequest;
import apache.rocketmq.controller.v1.GroupType;
import apache.rocketmq.controller.v1.MessageType;
import apache.rocketmq.controller.v1.SubscriptionMode;
import com.automq.rocketmq.cli.CliClientConfig;
import com.automq.rocketmq.controller.client.GrpcControllerClient;
import org.apache.rocketmq.common.attribute.TopicMessageType;
import org.apache.rocketmq.utils.MQAdmin;
import org.apache.rocketmq.utils.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class BaseOperate extends ResourceInit {
    private static Logger logger = LoggerFactory.getLogger(BaseOperate.class);

    protected static GrpcControllerClient client;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                MQAdmin.mqAdminExt.shutdown();
                logger.info("Shutdown Hook is running !");
            }
        });
        client = new GrpcControllerClient(new CliClientConfig());
    }

    protected static String getTopic(String methodName) {
        return getTopic(TopicMessageType.NORMAL.getValue(), methodName);
    }

    protected static String getTopic(String messageType, String methodName) {
        String topic = String.format("topic_%s_%s", methodName, RandomUtils.getStringWithCharacter(6));
        logger.info("[Topic] topic:{}, methodName:{}", topic, methodName);
        try {
            CreateTopicRequest request = CreateTopicRequest.newBuilder()
                .setTopic(topic)
                .setCount(8)
                .setAcceptTypes(convertAcceptTypes(messageType))
                .build();
            Long topicId = client.createTopic(namesrvAddr, request).join();
            logger.info("create topic: {} , topicId:{}", topic, topicId);
            return topic;
        } catch (Exception e) {
            logger.error("create topic error", e);
        }
        return topic;
    }

    private static AcceptTypes convertAcceptTypes(String typeStr) {
        switch (typeStr) {
            case "NORMAL":
                return AcceptTypes.newBuilder().addTypes(MessageType.NORMAL).build();
            case "FIFO":
                return AcceptTypes.newBuilder().addTypes(MessageType.FIFO).build();
            case "DELAY":
                return AcceptTypes.newBuilder().addTypes(MessageType.DELAY).build();
            case "TRANSACTION":
                return AcceptTypes.newBuilder().addTypes(MessageType.TRANSACTION).build();
            default:
                return AcceptTypes.newBuilder().addTypes(MessageType.MESSAGE_TYPE_UNSPECIFIED).build();
        }
    }

    protected static String getGroupId(String methodName) {
        return getGroupId(methodName, SubscriptionMode.SUB_MODE_POP);
    }

    protected static String getGroupId(String methodName, SubscriptionMode mode) {
        String groupId = String.format("GID_%s_%s", methodName, RandomUtils.getStringWithCharacter(6));
        // prepare consumer group
        CreateGroupRequest request = CreateGroupRequest.newBuilder()
            .setName(groupId)
            .setMaxDeliveryAttempt(16)
            .setGroupType(GroupType.GROUP_TYPE_STANDARD)
            .setSubMode(mode)
            .build();
        CreateGroupReply reply = createConsumerGroup(request).join();
        logger.info("[ConsumerGroupId] groupId:{} , methodName:{} , mode: {} , reply:{}", groupId, methodName, mode, reply);
        return groupId;
    }

    private static CompletableFuture<CreateGroupReply> createConsumerGroup(CreateGroupRequest request) {
        try {
            CompletableFuture<CreateGroupReply> groupCf = client.createGroup(namesrvAddr, request);
            return groupCf.exceptionally(throwable -> {
                logger.error("Create group failed", throwable);
                throw new CompletionException(throwable);
            });
        } catch (Exception e) {
            logger.error("Create group failed", e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
