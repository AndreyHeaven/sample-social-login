package com.company.demo.service;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.haulmont.bali.util.URLEncodeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Optional;

public abstract class AbstractOAuthServiceBean implements SocialLoginService {
    String getEncodedUrl(String appUrl) {
        return URLEncodeUtils.encodeUtf8(appUrl);
    }

    String requestUserData(HttpRequestBase request) {
        HttpClientConnectionManager cm = new BasicHttpClientConnectionManager();
        HttpClient httpClient = HttpClientBuilder.create().setConnectionManager(cm).build();

        try {
            HttpResponse httpResponse = httpClient.execute(request);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Unable to access Facebook API. Response HTTP status: "
                        + httpResponse.getStatusLine().getStatusCode());
            }
            return EntityUtils.toString(httpResponse.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            request.releaseConnection();
        }
    }

    public SocialLoginService.UserData getUserData(String appUrl, String code) {
        JsonObject accessToken = getAccessToken(code, appUrl);
        String userDataJson = getUserDataAsJson(accessToken.get("access_token").getAsString());

        JsonParser parser = new JsonParser();

        JsonObject jsonObject = parser.parse(userDataJson).getAsJsonObject();
        String id = Optional.ofNullable(jsonObject.get("id")).map(JsonElement::getAsString).orElse(null);
        String name = Optional.ofNullable(jsonObject.get("name")).map(JsonElement::getAsString).orElse(null);

        String email = Optional.ofNullable(accessToken.get("email")).map(JsonElement::getAsString)
                .orElse(Optional.ofNullable(jsonObject.get("email")).map(JsonElement::getAsString).orElse(null));

        return new SocialLoginService.UserData(id, name, email);
    }

    protected String getUserDataAsJson(String accessToken) {
        HttpRequestBase request = getUserDataRequest(accessToken);
        return requestUserData(request);
    }

    protected abstract HttpRequestBase getUserDataRequest(String accessToken);

    protected abstract HttpRequestBase getAccessTokenRequest(String code, String appUrl);

    protected JsonObject getAccessToken(String code, String appUrl) {
        HttpRequestBase accessTokenPath = getAccessTokenRequest(code, appUrl);
        String response = requestAccessToken(accessTokenPath);
        JsonParser parser = new JsonParser();
        JsonObject asJsonObject = parser.parse(response).getAsJsonObject();
        return asJsonObject;
    }

    protected String requestAccessToken(HttpRequestBase request) {
        HttpClientConnectionManager cm = new BasicHttpClientConnectionManager();
        HttpClient httpClient = HttpClientBuilder.create().setConnectionManager(cm).build();

        try {
            HttpResponse httpResponse = httpClient.execute(request);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Unable to access API. Response HTTP status: "
                        + httpResponse.getStatusLine().getStatusCode());
            }
            return EntityUtils.toString(httpResponse.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            request.releaseConnection();
        }
    }
}
