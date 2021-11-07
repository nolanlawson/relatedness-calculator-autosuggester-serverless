package autosuggester;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.nolanlawson.relatedness.autosuggest.RelationSuggester;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final int MAX_LENGTH = 500;
    private static final int MAX_NUM_SUGGESTIONS = 10;
    private static final RelationSuggester relationSuggester = new RelationSuggester();
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json;charset=utf-8");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);
        try {
            final String q = input.getQueryStringParameters().get("q");
            if (q.length() > MAX_LENGTH) {
                throw new RuntimeException("Length too large: " + q.length());
            }

            List<String> suggestions = relationSuggester.suggest(q, MAX_NUM_SUGGESTIONS);
            Map<String, List<String>> results = new HashMap<>();
            results.put("results", suggestions);
            String output = gson.toJson(results);
            headers.put("Cache-Control", "public, max-age=604800, s-max-age=604800");
            return response
                    .withStatusCode(200)
                    .withBody(output);
        } catch (Throwable t) {
            t.printStackTrace();
            return response
                    .withBody("{\"error\":true}")
                    .withStatusCode(500);
        }
    }
}
