package com.endyary.function.handler;

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
import java.util.HashMap;
import java.util.Map;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.http.HttpStatusCode;

public class RegisterUserHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private final UserService userService;
  private static final Gson GSON = new GsonBuilder().create();

  public RegisterUserHandler() {
    this.userService = new UserService();
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(
      APIGatewayProxyRequestEvent event, Context context) {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
        .withHeaders(headers);

    String requestBody = event.getBody();
    LambdaLogger logger = context.getLogger();
    logger.log("Request body: " + requestBody);

    JsonObject user;
    try (JsonReader reader = new JsonReader(new StringReader(requestBody))) {
      user = JsonParser.parseReader(reader).getAsJsonObject();
      JsonObject createUserResult = userService.register(user);
      return new APIGatewayProxyResponseEvent()
          .withHeaders(headers)
          .withStatusCode(HttpStatusCode.OK)
          .withBody(GSON.toJson(createUserResult, JsonObject.class));
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
