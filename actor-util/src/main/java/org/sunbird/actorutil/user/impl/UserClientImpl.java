package org.sunbird.actorutil.user.impl;

import akka.actor.ActorRef;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.sunbird.actorutil.InterServiceCommunication;
import org.sunbird.actorutil.InterServiceCommunicationFactory;
import org.sunbird.actorutil.user.UserClient;
import org.sunbird.common.ElasticSearchUtil;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.util.*;
import org.sunbird.common.request.Request;
import org.sunbird.common.responsecode.ResponseCode;
import org.sunbird.dto.SearchDTO;

public class UserClientImpl implements UserClient {

  private static InterServiceCommunication interServiceCommunication =
      InterServiceCommunicationFactory.getInstance();

  @Override
  public String createUser(ActorRef actorRef, Map<String, Object> userMap) {
    ProjectLogger.log("UserClientImpl: createUser called", LoggerEnum.INFO);
    return upsertUser(actorRef, userMap, ActorOperations.CREATE_USER.getValue());
  }

  @Override
  public void updateUser(ActorRef actorRef, Map<String, Object> userMap) {
    ProjectLogger.log("UserClientImpl: updateUser called", LoggerEnum.INFO);
    upsertUser(actorRef, userMap, ActorOperations.UPDATE_USER.getValue());
  }

  @Override
  public void esVerifyPhoneUniqueness() {
    esVerifyFieldUniqueness(JsonKey.ENC_PHONE, JsonKey.PHONE);
  }

  @Override
  public void esVerifyEmailUniqueness() {
    esVerifyFieldUniqueness(JsonKey.ENC_EMAIL, JsonKey.EMAIL);
  }

  private void esVerifyFieldUniqueness(String facetsKey, String objectType) {
    SearchDTO searchDto = null;
    searchDto = new SearchDTO();
    searchDto.setLimit(0);

    Map<String, String> facets = new HashMap<>();
    facets.put(facetsKey, null);
    List<Map<String, String>> list = new ArrayList<>();
    list.add(facets);
    searchDto.setFacets(list);

    Map<String, Object> esResponse =
        ElasticSearchUtil.complexSearch(
            searchDto,
            ProjectUtil.EsIndex.sunbird.getIndexName(),
            ProjectUtil.EsType.user.getTypeName());

    if (null != esResponse) {
      List<Map<String, Object>> facetsResponse =
          (List<Map<String, Object>>) esResponse.get(JsonKey.FACETS);

      if (CollectionUtils.isNotEmpty(facetsResponse)) {
        Map<String, Object> map = facetsResponse.get(0);
        List<Map<String, Object>> valueList = (List<Map<String, Object>>) map.get("values");

        for (Map<String, Object> value : valueList) {
          long count = (long) value.get(JsonKey.COUNT);
          if (count > 1) {
            throw new ProjectCommonException(
                ResponseCode.errorDuplicateEntries.getErrorCode(),
                MessageFormat.format(
                    ResponseCode.errorDuplicateEntries.getErrorMessage(), objectType),
                ResponseCode.CLIENT_ERROR.getResponseCode());
          }
        }
      }
    }
  }

  private String upsertUser(ActorRef actorRef, Map<String, Object> userMap, String operation) {
    String userId = null;

    Request request = new Request();
    request.setRequest(userMap);
    request.setOperation(operation);
    request.getContext().put(JsonKey.VERSION, JsonKey.VERSION_3);
    Object obj = interServiceCommunication.getResponse(actorRef, request);
    if (obj instanceof Response) {
      Response response = (Response) obj;
      userId = (String) response.get(JsonKey.USER_ID);
    } else if (obj instanceof ProjectCommonException) {
      throw (ProjectCommonException) obj;
    } else if (obj instanceof Exception) {
      throw new ProjectCommonException(
          ResponseCode.SERVER_ERROR.getErrorCode(),
          ResponseCode.SERVER_ERROR.getErrorMessage(),
          ResponseCode.SERVER_ERROR.getResponseCode());
    }

    return userId;
  }
}
