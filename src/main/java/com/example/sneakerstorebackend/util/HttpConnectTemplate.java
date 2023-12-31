package com.example.sneakerstorebackend.util;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
public class HttpConnectTemplate {
    public static HttpResponse<?> connectToGHN(String url, String body, String TOKEN, String SHOP_ID) throws InterruptedException, IOException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(ConstantsConfig.GHN_URL + url) ;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Token", TOKEN)
                .header("ShopId", SHOP_ID)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<?> connectToGHNAddress(String url, String body, String TOKEN, String SHOP_ID) throws InterruptedException, IOException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(ConstantsConfig.GHN_URL_ADDRESS + url) ;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Token", TOKEN)
                .header("ShopId", SHOP_ID)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
