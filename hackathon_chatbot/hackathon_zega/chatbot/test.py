import asyncio
import websockets
import json

async def test_websocket():
    uri = "ws://localhost:8000/ws/chat"  # Replace with your WebSocket URL

    try:
        async with websockets.connect(uri) as websocket:
            # Receive the initial connection message
            initial_message = await websocket.recv()
            print(f"Server: {initial_message}")

            # Prepare symptoms to send
            symptoms = "chest_pain"
            print(f"Sending symptoms: {symptoms}")
            await websocket.send(symptoms)  # Send symptoms to the backend

            # Wait for the diagnostic response
            response = await websocket.recv()
            print(f"Raw server response: {response}")

            # Parse and display the response
            diagnostic = json.loads(response)
            print("\nDiagnostic Details:")
            print(f"Conversation ID: {diagnostic['conversation_id']}")
            print(f"Prognosis: {diagnostic['prognosis']}")
            print(f"Description: {diagnostic['description']}")
            print(f"Precautions: {', '.join(diagnostic['precautions'])}")
            print(f"Complications: {', '.join(diagnostic['complications'])}")
            print(f"Severity: {diagnostic['severity']}")

    except websockets.exceptions.ConnectionClosedError as e:
        print(f"WebSocket connection closed unexpectedly: {e}")
    except Exception as e:
        print(f"An error occurred: {e}")

asyncio.run(test_websocket())
