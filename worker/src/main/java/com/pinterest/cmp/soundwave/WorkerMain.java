package com.pinterest.cmp.soundwave;


import com.pinterest.cmp.JobManager;
import com.pinterest.cmp.cmdb.aws.Ec2InstanceStore;
import com.pinterest.cmp.cmdb.pinterest.EsInstanceStore;
import com.pinterest.cmp.config.Configuration;
import com.pinterest.cmp.zookeeper.ZkJobInfoStore;
import com.pinterest.commons.ostrich.OstrichAdminService;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main method that initializes logger, service framework proxy, finagle server and ostrich
 */
public class WorkerMain {

  private static final Logger LOG =
      LoggerFactory.getLogger(WorkerMain.class);
  private static final Logger FAILURE_LOG = LoggerFactory.getLogger("failure");
  private static final Logger SLOW_LOG = LoggerFactory.getLogger("slow");

  private static final String MAX_CONNECTION_IDLE_CONFIG_KEY = "max_connection_idle_in_minute";
  private static final String MAX_CONCURRENT_REQUESTS_KEY = "max_concurrent_requests";
  private static final String OSTRICH_PORT_KEY = "ostrich_port";
  private static final String SERVICE_NAME_KEY = "service_name";
  private static final String CLUSTER_NAME_KEY = "cluster_name";
  private static final String THRIFT_PORT_KEY = "thrift_server_port";
  private static final String SERVER_SET_PATH_KEY = "server_set_path";
  private static final String SLOW_REQUEST_THRESHOLD_MILLIS = "slow_request_threshold_millis";

  public static void main(String[] args) {
    try {
      if (System.getProperty("config.file") == null) {
        System.setProperty("config.file", "cmdb.opensource.properties");
      }
      //Start the Job Manager
      JobManager
          manager =
          new JobManager(new ZkJobInfoStore(), new Ec2InstanceStore(), new EsInstanceStore());
      manager.start();

      final PropertiesConfiguration configuration = Configuration.getProperties();

      // Start ostrich admin service and initialize stats receiver
      int ostrichPort = configuration.getInt(OSTRICH_PORT_KEY);
      new OstrichAdminService(ostrichPort).start();
      while (true) {
        Thread.sleep(1000);
      }

      //CHECKSTYLE_OFF: IllegalCatch
    } catch (Exception e) {
      LOG.error("Cannot start the service properly", e);
      System.exit(1);
    }
    //CHECKSTYLE_ON: IllegalCatch
  }
}
