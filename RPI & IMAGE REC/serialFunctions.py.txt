from asyncore import write
import serial # pySerial module
import time



class SerialCommunication:
    PORT = '/dev/ttyUSB0' # Static serial port
    BAUD_RATE = 115200
    
    def __init__(self):
        self.connection = None
    
    def check_connection(self):
        return self.connection is None
    
    def connect(self):
        while True:
            try:
                print("[RPI - STM] Attempting connect to STM...")
                self.connection = serial.Serial(self.PORT, self.BAUD_RATE)

            except Exception as e:
                print("[RPI - STM] Connection failed: " + str(e))
                time.sleep(1)

            else:
                print("[RPI - STM] STM Connected successfully")
                break
    
    def sendMessage(self, message):
        print("[RPI - STM] Attempting to send message:" , message)
        # print(message)
        
        try:
            self.connection.write(message)
            print("[RPI - STM] Message Sent!")
        except Exception as e:
            print("[RPI - STM] Failed to send: " + str(e))
    
    def read(self):
        print("[STM - RPI] Attempting to read...")
    
        try:
            message = self.connection.read()
        except Exception as e:
            print("[STM - RPI] Failed to read: "  + str(e))
        else:
            if message is not None and len(message) > 0:
                print("[STM - RPI] Message read:")
                print(message)
                
                return message
            
            elif message is None:
                print("[STM - RPI] Message is None")

