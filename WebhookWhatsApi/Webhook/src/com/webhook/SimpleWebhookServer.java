package com.webhook;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SimpleWebhookServer {

    private static final String VERIFY_TOKEN = "cloudapi@81502430";

    public static void main(String[] args) throws Exception {
        // Cria o servidor na porta 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/webhook", new WebhookHandler());
        server.setExecutor(null); // cria um executor padrão
        server.start();
        System.out.println("Servidor rodando na porta 8080...");
    }

    static class WebhookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response;

            // Verifica se a requisição é GET para a verificação do webhook
            if ("GET".equals(exchange.getRequestMethod())) {
                URI requestURI = exchange.getRequestURI();
                String query = requestURI.getQuery();
                Map<String, String> queryParams = parseQuery(query);

                String mode = queryParams.get("hub.mode");
                String token = queryParams.get("hub.verify_token");
                String challenge = queryParams.get("hub.challenge");

                if ("subscribe".equals(mode) && VERIFY_TOKEN.equals(token)) {
                    response = challenge;
                } else {
                    response = "Erro de verificação";
                }

                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }

            // Verifica se a requisição é POST para receber mensagens
            else if ("POST".equals(exchange.getRequestMethod())) {
                // Lê o payload da mensagem recebida
                byte[] bytes = exchange.getRequestBody().readAllBytes();
                String body = new String(bytes);

                System.out.println("Payload recebido: " + body);

                // Aqui você pode adicionar lógica para processar a mensagem

                response = "Mensagem recebida";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        private Map<String, String> parseQuery(String query) {
            Map<String, String> queryPairs = new HashMap<>();
            if (query == null) {
                return queryPairs;
            }

            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0) {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                    queryPairs.put(key, value);
                }
            }
            return queryPairs;
        }
    }
}

