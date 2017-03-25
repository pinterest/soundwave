/**
 * Copyright 2017 Pinterest, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
