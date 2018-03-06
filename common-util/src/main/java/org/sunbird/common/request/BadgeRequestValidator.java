package org.sunbird.common.request;

import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.ProjectUtil;
import org.sunbird.common.responsecode.ResponseCode;

/**
 * Created by arvind on 2/3/18.
 */
public class BadgeRequestValidator {

  private static final int ERROR_CODE = ResponseCode.CLIENT_ERROR.getResponseCode();
  private static final String COMMA_SEPERATOR = ",";

  /**
   * Method to validate noteId
   *
   * @param request
   */
  public static void validateCreateBadgeIssuer(Request request) {
    StringBuilder builder = new StringBuilder("Required fields are - ");
    Boolean flag = false;
    if (ProjectUtil.isStringNullOREmpty((String)request.getRequest().get(JsonKey.NAME))) {
      builder = builder.append(JsonKey.NAME+COMMA_SEPERATOR);
      flag = true;
    }
    if (ProjectUtil.isStringNullOREmpty((String)request.getRequest().get(JsonKey.DESCRIPTION))) {
      builder = builder.append(JsonKey.DESCRIPTION + COMMA_SEPERATOR);
      flag = true;
    }
    if (ProjectUtil.isStringNullOREmpty((String)request.getRequest().get(JsonKey.URL))) {
      builder = builder.append(JsonKey.URL + COMMA_SEPERATOR);
      flag = true;
    }
    if (ProjectUtil.isStringNullOREmpty((String)request.getRequest().get(JsonKey.EMAIL))) {
      builder = builder.append(JsonKey.EMAIL + COMMA_SEPERATOR);
      flag = true;
    }

    if(flag){
      builder.deleteCharAt(builder.length()-1);
      throw createExceptionInstance(ResponseCode.invalidDataForCreateBadgeIssuer.getErrorCode(),builder.toString() );
    }
  }

  private static ProjectCommonException createExceptionInstance(String errorCode,
      String errMsg) {
    return new ProjectCommonException(ResponseCode.getResponse(errorCode).getErrorCode(),
        errMsg, ERROR_CODE);
  }

}
