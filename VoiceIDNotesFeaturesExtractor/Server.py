import jpysocket
import MFCCExtractor
import os

SIMPLE_PACKET_SIZE = 1024
FILE_PACKET_SIZE = 8 * 1024


def start_server():
    host = 'localhost'
    port = 5001
    socket = jpysocket.jpysocket()
    socket.bind((host, port))
    socket.listen(5)
    print("Server started.. ")

    while True:
        connection, address = socket.accept()
        msg_encoded = connection.recv(SIMPLE_PACKET_SIZE)
        msg = jpysocket.jpydecode(msg_encoded)
        if msg == "extract":
            print("\nRequest for extracting features")
            msg_encoded = connection.recv(SIMPLE_PACKET_SIZE)
            size = int((int(jpysocket.jpydecode(msg_encoded))) / FILE_PACKET_SIZE) + 1

            # temporary .wav file, opened in binary
            f = open("temp.wav", 'wb')
            print("Downloading file..")
            while size > 0:
                packet = connection.recv(FILE_PACKET_SIZE)
                f.write(packet)
                size -= 1
            f.close()
            print("Extracting features..")
            result = MFCCExtractor.extract_mfcc("temp.wav")
            print("Features extracted: " + result)
            # client side needs to receive a complete line for that reason we insert "/r/n"
            connection.send(bytes(result + "\r\n", 'UTF-8'))
            os.remove("temp.wav")
        connection.close()
    socket.close()