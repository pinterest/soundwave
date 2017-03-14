package com.pinterest.cmp.cmdb.config;

import com.pinterest.cmp.config.Configuration;

import org.junit.Assert;


public class ConfigurationTest {

  @org.junit.Test
  public void getInstance() throws Exception {
    Assert.assertNotNull(Configuration.getProperties());
    Object val = Configuration.getProperties().getProperty("update_queue");
    Assert.assertNotNull(val);
  }

}
