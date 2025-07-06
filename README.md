# README.md

## AI Assistant Framework with GPT-4o Integration (Python & Java)

---

### How to Run the Program

#### Python Version
1. **Install Python 3.8 or later** from [python.org](https://www.python.org/).
2. **Install required packages using pip**:
   ```bash
   pip install openai
   ```
3. **Set your API Key** (replace `your-api-key` with actual key):
   - **Windows (CMD):**
     ```cmd
     set OPENAI_API_KEY=your-api-key
     ```
   - **macOS/Linux (Terminal):**
     ```bash
     export OPENAI_API_KEY=your-api-key
     ```
4. **Run the Python script**:
   ```bash
   python ai_assistant_gpt.py
   ```
5. **Follow the on-screen prompts**: Input name, age, premium status, and commands exactly as listed.

#### Java Version (using IntelliJ IDEA)
1. **Install JDK 17 or higher** from [Oracle](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) or use OpenJDK.
2. **Download Jackson JARs for JSON support**:
   - `jackson-core-2.17.1.jar`
   - `jackson-databind-2.17.1.jar`
   - `jackson-annotations-2.17.1.jar`
3. **Create a `libs/` folder in your project root**:
   - Right-click the project > New > Directory > Name it `libs`
   - Paste all three JAR files into this folder
4. **Add libraries to classpath in IntelliJ IDEA**:
   - Go to `File > Project Structure > Libraries > + Java`
   - Select the three `.jar` files inside `libs/`
   - Click `Apply` and `OK`
5. **API Key Integration**:
   - Inside the `AIAssistantApp.java`, replace the placeholder with your actual key:
     ```java
     private static final String API_KEY = "your-api-key";
     ```
6. **Ensure class name matches filename**:
   - Your public class must be named `AIAssistantApp.java`
7. **Run the program**:
   - Right-click `AIAssistantApp.java` > Run or use Shift+F10

---

### Overview of Assistant Functionality

This AI Assistant is a fully dynamic command-line application that integrates with OpenAI's GPT-4o to simulate a smart personal assistant.

**Supported Features**:
- Music recommendations based on mood
- Custom fitness workout plans
- Study session scheduling
- Fully dynamic GPT responses powered by natural language interpretation

**User Flow**:
- Asks user to input their profile (name, age, premium access)
- Displays a list of supported commands clearly
- On each command, dynamically asks for input parameters
- GPT-4o returns content-aware results tailored to the context
- Conversation continues until user types `exit`

**Example Commands**:
- `play music`
- `workout plan`
- `schedule study`

---

### Which Concepts Were Implemented Where

#### ✅ Data Modeling
- `UserProfile` stores user's name, age, premium status
- Robust input validation (non-negative age, boolean checks)

#### ✅ Object-Oriented Design
- `AssistantBase` (Python) / `BaseAssistant` (Java) defines common method: `handleRequest`
- Subclasses: `MusicAssistant`, `FitnessAssistant`, `StudyAssistant`
- Dynamic dispatch via command routing (polymorphism)
- Modular structure allows future extension (e.g., `weather`, `reminders`)

#### ✅ Input Handling and Error Checking
- Ensures name is not blank
- Validates age to be non-negative integer
- Validates premium field is `true` or `false`
- Enforces that command is typed exactly
- Repeats choices and errors clearly for user comprehension

#### ✅ Conversational Loop
- Maintains session loop until user types `exit`
- Repeats menu after every command
- GPT replies vary based on prior input dynamically

#### ✅ GPT-4o Integration
- Python: `openai.ChatCompletion.create()` (modern SDK)
- Java: Manual `HttpURLConnection` with JSON via Jackson
- Uses real-time prompt generation
- Responses are context-aware, personalized

---

### Limitations
- Does not persist user profile across sessions
- Does not handle fuzzy commands (must match exactly)
- Calendar scheduling is returned as instructions, not executed automatically

---

### Project Structure
```
ai-assistant-checkin2/
├── source_code/
│   ├── ai_assistant_gpt.py         # Python version
│   ├── AIAssistantApp.java         # Java version
│   
├── README.md                       # This file
├── reflection.pdf                  # Language comparison
└── libs/                           # JAR dependencies for Java
```

---

### Notes
- Fully compatible with terminal and IntelliJ IDEA
- Production-level input validation and flow
- GPT output is interpreted without hallucination logic

