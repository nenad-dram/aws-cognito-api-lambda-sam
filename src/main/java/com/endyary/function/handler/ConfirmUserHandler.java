package com.endyary.function.handler;

import static com.endyary.function.service.Constants.EMAIL;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.endyary.function.service.UserService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

public class ConfirmUserHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private final UserService userService;
  private static final Gson GSON = new GsonBuilder().create();

  public ConfirmUserHandler() {
    this.userService = new UserService();
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(
      APIGatewayProxyRequestEvent event, Context context) {
    LambdaLogger logger = context.getLogger();

    JsonObject requestBody;
    try (JsonReader reader = new JsonReader(new StringReader(event.getBody()))) {
      requestBody = JsonParser.parseReader(reader).getAsJsonObject();
      String email = requestBody.get(EMAIL).getAsString();
      String confirmationCode = requestBody.get("code").getAsString();

      JsonObject confirmUserResult = userService.confirm(email, confirmationCode);
      return new APIGatewayProxyResponseEvent()
          .withStatusCode(200)
          .withBody(GSON.toJson(confirmUserResult, JsonObject.class));
    } catch (AwsServiceException ex) {
      return handleException(logger, ex.awsErrorDetails().errorMessage(),
          ex.awsErrorDetails().sdkHttpResponse().statusCode());
    } catch (Exception ex) {
      return handleException(logger, ex.getMessage(), 500);
    }
  }

  private APIGatewayProxyResponseEvent handleException(LambdaLogger logger, String errorMessage,
      int statusCode) {
    logger.log(errorMessage);
    return new APIGatewayProxyResponseEvent()
        .withBody(errorMessage)
        .withStatusCode(statusCode);
  }
}
