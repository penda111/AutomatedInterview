from concurrent.futures import ThreadPoolExecutor
import logging, socket, sys
from Chatbot import chatbot_start 

HOST, PORT = "192.168.50.91", 258
# HOST, PORT = "localhost", 5000

def chatbot_conversation_server(host, port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as ss:
        ss.bind((host, port))
        ss.listen()
        logging.info(f"Server started, listening on {(host, port)}")
        with ThreadPoolExecutor() as pool:
            while True:
                cs, addr = ss.accept()
                pool.submit(chatbot_start, cs, addr)

if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    host = sys.argv[1] if len(sys.argv) > 1 else HOST
    port = int(sys.argv[2]) if len(sys.argv) > 2 else PORT
    chatbot_conversation_server(host, port)
