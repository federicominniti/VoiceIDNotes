import jpysocket
import MFCCExtractor
import os

SIMPLE_PACKET_SIZE = 1024
FILE_PACKET_SIZE = 8 * 1024


def start_server():
    host = 'localhost'
    port = 5001
    s = jpysocket.jpysocket()
    s.bind((host, port))
    s.listen(5)
    print("Server started.. ")

    while True:
        connection, address = s.accept()
        msg_recv = connection.recv(SIMPLE_PACKET_SIZE)
        msg_recv = jpysocket.jpydecode(msg_recv)
        if msg_recv == "extract":
            print("\nRequest for extracting features")
            msg_recv = connection.recv(SIMPLE_PACKET_SIZE)
            size = int((int(jpysocket.jpydecode(msg_recv))) / FILE_PACKET_SIZE) + 1

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
    s.close()