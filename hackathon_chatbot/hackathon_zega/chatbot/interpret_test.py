from main import interpret_message

test_messages = [
    "Sure, why not?",
    "No thanks, not interested.",
    "Absolutely, go ahead!",
    "I don't think I need help now.",
    "I am not sure.",
    "Nope"
]

for message in test_messages:
    print(f"Message: {message} -> Interpretation: {interpret_message(message)}")
