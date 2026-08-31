import bluetooth
import time


class BluetoothCommuncation:
    MAC_ADDRESS = 'B8:27:EB:A2:8C:B6'
    PORT_NUMBER = 1
    BUFFER_SIZE = 5096
    
    def __init__(self):
        self.client = None
        self.server = None
    
    def check_connection(self):
        return self.client is None or self.server is None
    
    def connect(self):
        while True:       
            try:
                print("[RPI - BT] Attempting to connect...")
                
                self.server = bluetooth.BluetoothSocket()
                self.server.bind((self.MAC_ADDRESS, self.PORT_NUMBER))
                self.server.listen(self.PORT_NUMBER)            
                
                self.client, client_address = self.server.accept()
            except Exception as exception:
                print("[RPI - BT] Connection failed: " + str(exception))
                
                time.sleep(1)
            else:
                print("[RPI - BT] Connected successfully")
                print("[RPI - BT] Client address: " + str(client_address))
                
                break
    
    def write(self, message):
        print("[RPI - BT] Attempting to send message:", message)
        # print(message)
        
        try:
            self.client.send(message)
        except Exception as exception:
            print("[RPI - BT] Failed to send: " + str(exception))
    
    def read(self):
        # print("")
        print("[BT - RPI] Attempting to read...")
        
        try:
            message = self.client.recv(self.BUFFER_SIZE)
        except Exception as exception:
            print("[BT - RPI] Failed to read: " + str(exception))
        else:
            if message is not None and len(message) > 0:
                print("[BT - RPI] Message read: ", message)
                # print(message)
            
                return message

    