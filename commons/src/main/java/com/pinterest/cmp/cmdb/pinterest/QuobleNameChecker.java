package com.pinterest.cmp.cmdb.pinterest;

import com.pinterest.cmp.cmdb.aws.MessageProcessingResult;
import com.pinterest.cmp.cmdb.bean.EsInstance;
import com.pinterest.cmp.cmdb.bean.PinterestEsInstance;

public final class QuobleNameChecker {

  public static void checkQuobleName(EsInstance instance, MessageProcessingResult result) {
    if (instance instanceof PinterestEsInstance) {
      PinterestEsInstance esInstance = (PinterestEsInstance) instance;
      if ("node".equals(esInstance.getDeployment())
          && esInstance.getTags() != null
          && esInstance.getTags().containsKey("Qubole")) {
        //True Qubole name tag is available later
        result.setError(MessageProcessingResult.MessageProcessingError.QUOBLE_NAME_NOT_AVAILABLE);
      }
    }
  }
}
