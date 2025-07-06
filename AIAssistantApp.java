import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * AI Assistant Application with GPT-4o Integration
 * <p>
 * Demonstrates structured OOP design, strict input validation,
 * and dynamic responses via OpenAI's GPT-4o API using Java HttpClient.
 */
public class AIAssistantApp {
    // Replace with your actual OpenAI API key before running
    private static final String OPENAI_API_KEY = "  ";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    // Supported user commands
    private static final String[] COMMANDS = {"play music", "workout plan", "schedule study"};

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // --- User Profile Setup with Validation ---
        String name = promptNonEmpty(scanner, "Enter your name: ");
        int age = promptAge(scanner);
        boolean isPremium = promptBoolean(scanner);
        UserProfile user = new UserProfile(name, age, isPremium);

        System.out.println("\nHello, " + user.getName() + "! How can assistance begin today?");

        // --- Main Interaction Loop ---
        while (true) {
            // Display available commands each iteration
            System.out.println("\nAvailable commands:");
            for (String cmd : COMMANDS) {
                System.out.println(" - " + cmd);
            }
            System.out.println("(Type exactly as shown or 'exit' to quit)");
            System.out.print("Enter command: ");

            String command = scanner.nextLine().trim().toLowerCase();
            if (command.equals("exit")) {
                System.out.println("\nSession ended.");
                break;
            }

            // Handle known commands or fallback to GPT
            AIAssistant assistant;
            switch (command) {
                case "play music" -> {
                    String mood = promptNonEmpty(scanner, "Enter your music mood/preference: ");
                    user.getPreferences().put("mood", mood);
                    assistant = new MusicAssistant(user);
                }
                case "workout plan" -> {
                    String goal = promptNonEmpty(scanner, "Enter your fitness goal: ");
                    user.getPreferences().put("fitness_goal", goal);
                    assistant = new FitnessAssistant(user);
                }
                case "schedule study" -> {
                    String topic = promptNonEmpty(scanner, "Enter your study topic: ");
                    user.getPreferences().put("study_topic", topic);
                    assistant = new StudyAssistant(user);
                }
                default -> {
                    // Fallback for unrecognized commands
                    String prompt = promptNonEmpty(scanner, "Unknown command. Enter your request for AI: ");
                    System.out.println(sendToGpt(prompt));
                    System.out.println("\nIs there anything else I can assist you with, " + user.getName() + "?");
                    continue;
                }
            }

            // Create request object and get response from structured assistant
            Request req = new Request(command, LocalDateTime.now(), command);
            Response res = assistant.handleRequest(req);
            System.out.println("User request: '" + command + "'");
            System.out.println(res.getMessage());
            System.out.println("\nIs there anything else I can assist you with, " + user.getName() + "?");
        }
        scanner.close();
    }

    /**
     * Sends a prompt to GPT-4o and returns the generated reply.
     */
    private static String sendToGpt(String prompt) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // Build system and user messages array
            ArrayNode messages = mapper.createArrayNode();
            ObjectNode sys = mapper.createObjectNode();
            sys.put("role", "system");
            sys.put("content", "You are a helpful assistant.");
            messages.add(sys);
            ObjectNode usr = mapper.createObjectNode();
            usr.put("role", "user");
            usr.put("content", prompt);
            messages.add(usr);

            // Build request payload
            ObjectNode payload = mapper.createObjectNode();
            payload.put("model", "gpt-4o");
            payload.set("messages", messages);
            payload.put("temperature", 0.7);

            // Construct HTTP request
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_API_URL))
                    .header("Authorization", "Bearer " + OPENAI_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload)))
                    .build();

            // Send request and parse response
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(httpResponse.body());
            return root.path("choices").get(0).path("message").path("content").asText().trim();
        } catch (Exception e) {
            return "[Error with GPT-4o] " + e.getMessage();
        }
    }

    /**
     * Prompt user until non-empty input is provided.
     */
    private static String promptNonEmpty(Scanner scanner, String prompt) {
        String input;
        do {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty. Please try again.");
            }
        } while (input.isEmpty());
        return input;
    }

    /**
     * Prompt user until a valid non-negative integer age is entered.
     */
    private static int promptAge(Scanner scanner) {
        while (true) {
            System.out.print("Enter your age: ");
            String val = scanner.nextLine().trim();
            if (val.matches("(0|[1-9]\\d*)")) {
                return Integer.parseInt(val);
            }
            System.out.println("Invalid input. Enter a non-negative integer for age.");
        }
    }

    /**
     * Prompt user until 'true' or 'false' is entered.
     */
    private static boolean promptBoolean(Scanner scanner) {
        while (true) {
            System.out.print("Are you a premium user? (true/false): ");
            String val = scanner.nextLine().trim().toLowerCase();
            if (val.equals("true") || val.equals("false")) {
                return Boolean.parseBoolean(val);
            }
            System.out.println("Please enter 'true' or 'false'.");
        }
    }

    // ===============================
    // Data Layer Classes
    // ===============================

    static class UserProfile {
        private final String name;
        private final int age;
        private final boolean isPremium;
        private final Map<String, String> preferences;

        public UserProfile(String name, int age, boolean isPremium) {
            this.name = name;
            this.age = age;
            this.isPremium = isPremium;
            this.preferences = new HashMap<>();
        }

        public String getName() { return name; }
        public int getAge() { return age; }
        public boolean isPremium() { return isPremium; }
        public Map<String, String> getPreferences() { return preferences; }
    }

    static class Request {
        private final String text;
        private final LocalDateTime timestamp;
        private final String command;

        public Request(String text, LocalDateTime timestamp, String command) {
            this.text = text;
            this.timestamp = timestamp;
            this.command = command;
        }

        public String getText() { return text; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getCommand() { return command; }
    }

    static class Response {
        private final String message;

        public Response(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
    }

    // ===============================
    // Assistant Behavior Layer
    // ===============================

    interface AIAssistant {
        Response handleRequest(Request request);
    }

    static class MusicAssistant implements AIAssistant {
        private final UserProfile user;
        public MusicAssistant(UserProfile user) { this.user = user; }
        @Override
        public Response handleRequest(Request request) {
            String mood = user.getPreferences().getOrDefault("mood", "neutral");
            String prompt = String.format("Suggest 2 songs for a mood of '%s'.", mood);
            return new Response(sendToGpt(prompt));
        }
    }

    static class FitnessAssistant implements AIAssistant {
        private final UserProfile user;
        public FitnessAssistant(UserProfile user) { this.user = user; }
        @Override
        public Response handleRequest(Request request) {
            String goal = user.getPreferences().getOrDefault("fitness_goal", "general fitness");
            String prompt = String.format("Create a 30-minute workout plan focused on '%s'.", goal);
            return new Response(sendToGpt(prompt));
        }
    }

    static class StudyAssistant implements AIAssistant {
        private final UserProfile user;
        public StudyAssistant(UserProfile user) { this.user = user; }
        @Override
        public Response handleRequest(Request request) {
            String topic = user.getPreferences().getOrDefault("study_topic", "general topics");
            String nowStr = request.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            String prompt = String.format("Plan a study session on '%s' starting at %s.", topic, nowStr);
            return new Response(sendToGpt(prompt));
        }
    }
}
