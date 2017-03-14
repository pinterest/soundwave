package com.pinterest.cmp.cmdb.aws;

import com.pinterest.cmp.cmdb.bean.EsInstance;

import java.util.function.BiConsumer;

public interface Ec2NotificationHandler {

  MessageProcessingResult processEvent(NotificationEvent event) throws Exception;

  void registerOnCreationSuccessEventHandler(
      BiConsumer<EsInstance, MessageProcessingResult> instance);
}
