package com.pinterest.cmp.cmdb.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class StringListOrElementDeserializer extends JsonDeserializer {


  public StringListOrElementDeserializer() {
    super();
  }


  @Override
  public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException, JsonProcessingException {
    ObjectCodec oc = jsonParser.getCodec();
    JsonNode node = oc.readTree(jsonParser);
    if (node instanceof ArrayNode) {
      ArrayNode arrayNode = (ArrayNode) node;
      ArrayList<String> ret = new ArrayList<>();
      for (int i = 0; i < arrayNode.size(); i++) {
        ret.add(arrayNode.get(i).textValue());
      }
      return ret;
    } else {
      return Arrays.asList(node.textValue());
    }
  }
}
