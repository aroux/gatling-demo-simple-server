import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(5687), 1000);
        server.createContext("/", new DefaultHandler("GET"));
        server.createContext("/search", new SearchMyHandler());
        server.createContext("/items", new DefaultHandler("GET"));
        server.createContext("/item", new DefaultHandler("GET"));
        server.createContext("/create", new DefaultHandler("POST"));
        server.setExecutor(Executors.newFixedThreadPool(100)); // creates a default executor
        server.start();
    }




    static abstract class AbstractHandler implements HttpHandler {

        private Random random = new Random();

        private String printRequest(BufferedReader requestReader) throws IOException {
            System.out.println("Request body : ");
            StringBuilder stringBuilder = new StringBuilder();
            requestReader.lines().forEach(stringBuilder::append);
            System.out.println(stringBuilder.toString());
            return stringBuilder.toString();
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String response = "";
            int httpStatus = 200;

            System.out.println("----------------------------------------------------------------------------------------------------");
            System.out.println("Received request : " + httpExchange.getRequestMethod() + " on " + httpExchange.getRequestURI());
            if (!getSupportedMethod().equals(httpExchange.getRequestMethod())) {
                response = "Only " + getSupportedMethod() + " is supported for " + httpExchange.getRequestURI();
                httpStatus = 400;
            } else {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody()))) {

                    String requestBody = printRequest(reader);
                    response = handleRequest(requestBody);
                } catch (Exception e){
                    response = "Error during message processing. " + e.getClass().getSimpleName() + ": " + e.getMessage();
                    httpStatus = 500;
                }
            }

            try {
                int sleepTime = random.nextInt(30);
                Thread.sleep(sleepTime);
                System.out.println("Sleep for " + sleepTime + "ms before responding");
            } catch (InterruptedException e) {
                // Ignore
            }
            System.out.println("Return : " + response);
            httpExchange.sendResponseHeaders(httpStatus, response.length());

            try (OutputStream os = httpExchange.getResponseBody()){
                os.write(response.getBytes());
            }
            System.out.println("----------------------------------------------------------------------------------------------------");

        }

        protected abstract String getSupportedMethod();

        protected abstract String handleRequest(String body);
    }

    static class SearchMyHandler extends AbstractHandler {


        @Override
        protected String getSupportedMethod() {
            return "POST";
        }

        @Override
        protected String handleRequest(String body) {
            switch(body) {
                case "BMW" : return "118d,135i";
                case "VW" : return "Tiguan,Touran";
                case "AUDI" : return "Q5,Q7";
                default :
                    return "no match";
            }
        }
    }

    static class DefaultHandler extends AbstractHandler {

        private final String method;

        public DefaultHandler(String method) {
            this.method = method;
        }

        @Override
        protected String getSupportedMethod() {
            return method;
        }

        @Override
        protected String handleRequest(String body) {
            return "K";
        }
    }
}
