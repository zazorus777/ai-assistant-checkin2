# ai_assistant_gpt4o.py

import openai
import datetime
from typing import Dict
from openai import OpenAI

# Initialize OpenAI client using GPT-4o model (SDK v1.0+)
client = OpenAI(api_key="  ")

# Supported command list
COMMANDS = ["play music", "workout plan", "schedule study"]

# ===============================
# Data Type Definitions
# ===============================

class UserProfile:
    """
    Represents a user's profile with validated attributes.
    """
    def __init__(self, name: str, age: int, is_premium: bool):
        if not name.strip():
            raise ValueError("Name cannot be empty.")
        if not isinstance(age, int) or age < 0:
            raise ValueError("Age must be a non-negative integer.")
        if not isinstance(is_premium, bool):
            raise ValueError("Premium status must be a boolean.")
        self.name = name.strip()
        self.age = age
        self.is_premium = is_premium
        self.preferences: Dict[str, str] = {}

class Request:
    """
    Encapsulates a user request: raw text, timestamp, and command.
    """
    def __init__(self, text: str, timestamp: datetime.datetime, command: str):
        self.text = text
        self.timestamp = timestamp
        self.command = command

class Response:
    """
    Wraps an assistant's response: message content.
    """
    def __init__(self, message: str):
        self.message = message

# ===============================
# GPT-4o Integration
# ===============================

def send_to_gpt(prompt: str) -> str:
    """
    Sends a prompt to GPT-4o and returns the assistant's reply.
    """
    try:
        result = client.chat.completions.create(
            model="gpt-4o",
            messages=[
                {"role": "system", "content": "You are a helpful assistant."},
                {"role": "user", "content": prompt}
            ]
        )
        return result.choices[0].message.content
    except Exception as e:
        return f"[Error with GPT-4o] {e}"

# ===============================
# Input Validation Helpers
# ===============================

def prompt_nonempty(prompt: str) -> str:
    """Prompts until non-empty input received."""
    while True:
        val = input(prompt).strip()
        if val:
            return val
        print("Input cannot be empty. Please try again.")

def prompt_age() -> int:
    """Prompts until valid non-negative integer is entered."""
    while True:
        val = input("Enter your age: ").strip()
        if val.isdigit():
            return int(val)
        print("Invalid input. Enter a non-negative integer for age.")

def prompt_boolean() -> bool:
    """Prompts until 'true' or 'false' is entered."""
    while True:
        val = input("Are you a premium user? (true/false): ").strip().lower()
        if val in ("true", "false"):
            return val == "true"
        print("Please enter 'true' or 'false'.")

# ===============================
# Structured Assistants
# ===============================

class MusicAssistant:
    """Recommends playlist based on user mood."""
    def __init__(self, user: UserProfile):
        self.user = user
    def handle_request(self, req: Request) -> Response:
        mood = self.user.preferences.get("mood", "neutral")
        prompt = f"Suggest 2 songs for a mood of '{mood}'."
        reply = send_to_gpt(prompt)
        return Response(reply)

class FitnessAssistant:
    """Suggests a workout plan based on fitness goal."""
    def __init__(self, user: UserProfile):
        self.user = user
    def handle_request(self, req: Request) -> Response:
        goal = self.user.preferences.get("fitness_goal", "general fitness")
        prompt = f"Create a 30-minute workout plan focused on '{goal}'."
        reply = send_to_gpt(prompt)
        return Response(reply)

class StudyAssistant:
    """Schedules and advises on study sessions for given topic."""
    def __init__(self, user: UserProfile):
        self.user = user
    def handle_request(self, req: Request) -> Response:
        topic = self.user.preferences.get("study_topic", "general topics")
        now = datetime.datetime.now().strftime("%Y-%m-%d %H:%M")
        prompt = f"Plan a study session on '{topic}' starting at {now}."
        reply = send_to_gpt(prompt)
        return Response(reply)

# ===============================
# Main Interaction Loop
# ===============================

def main():
    print("=== AI Assistant (GPT-4o Integrated) ===")

    # Collect user profile
    name = prompt_nonempty("Enter your name: ")
    age = prompt_age()
    is_premium = prompt_boolean()
    user = UserProfile(name, age, is_premium)

    print(f"\nHello, {user.name}! How can assistance begin today?")

    # Interaction loop
    while True:
        # Display commands
        print("\nAvailable commands:")
        for cmd in COMMANDS:
            print(f" - {cmd}")
        print("(Type exactly as shown or 'exit' to quit)")

        command = input("Enter command: ").strip().lower()
        if command == "exit":
            print("\nSession ended.")
            break

        # Route to appropriate assistant
        if command == "play music":
            mood = prompt_nonempty("Enter your music mood/preference: ")
            user.preferences["mood"] = mood
            assistant = MusicAssistant(user)

        elif command == "workout plan":
            goal = prompt_nonempty("Enter your fitness goal: ")
            user.preferences["fitness_goal"] = goal
            assistant = FitnessAssistant(user)

        elif command == "schedule study":
            topic = prompt_nonempty("Enter your study topic: ")
            user.preferences["study_topic"] = topic
            assistant = StudyAssistant(user)

        else:
            # Fallback to GPT for unknown commands
            prompt = input("Unknown command. Enter your request for AI: ")
            print(send_to_gpt(prompt))
            continue

        # Create request and get response
        req = Request(command, datetime.datetime.now(), command)
        res = assistant.handle_request(req)
        print(f"User request: '{command}'")
        print(res.message)

        print(f"\nIs there anything else I can assist you with, {user.name}?")

if __name__ == "__main__":
    main()
