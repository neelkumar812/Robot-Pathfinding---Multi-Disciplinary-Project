import socket
import picamera
import time
import sys

class imageDetection:
    HOST = '192.168.21.29'  # The server's hostname or IP address
    PORT = 54321  # The port used by the server
    PORT_ALGO = 54123
    READ_BUFFER_SIZE = 4096

    def __init__(self):
        self.client = None

    def captureImage(self):
        print('[CV] Initializing Camera.')
        camera = picamera.PiCamera()
        camera.resolution = (2592, 1944)
        camera.framerate = 30
        camera.vflip = True
        camera.hflip = True
        camera.brightness = 55
        camera.start_preview()

        print('[CV] Warming up camera...')
        print('[CV] Camera warmed up and ready')
    
        picName = 'image.jpg'
        picPath = "/home/dion/mdp/yolov5/data/images/"
        completePath = picPath + picName
        camera.capture(completePath)
        print("[CV] We have taken a picture.")
        camera.stop_preview()
        camera.close()
        return completePath

    def image(self):
        while True:
            try:
                print("[RPI - PC] Attempting to connect to PC to send...")
                self.client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                self.client.connect((self.HOST, self.PORT))
                print("[RPI - PC] Successfully connected to PC: " + str(self.HOST))

                path = self.captureImage()
                image = open(path, 'rb')
                imageData = image.read(2048)
                while imageData:
                    self.client.send(imageData)
                    imageData = image.read(2048)
                print('[RPI - PC] Image sent')
                image.close()
                # Closing the client
                self.client.close()
                print('[RPI - PC] Connection closed')
                time.sleep(1)
                
                print("[PC - RPI] Attempting to connect to PC to receive...")       
                # Reconnect the RPI client to the server
                self.client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                self.client.connect((self.HOST, self.PORT))
                print("[PC - RPI] Successfully connected to PC: " + str(self.HOST))
                message = self.read()
                self.client.close()
                print('[PC - RPI] Connection closed')           
                
                return message
            except Exception as exception:
                print("[RPI - PC] Sending image to the PC failed: " + str(exception))
                time.sleep(1)   

    def algo(self, obstacle):
        while True:
            try:
                print("[RPI - PC] Attempting to connect to Algo to send...")
                self.client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                self.client.connect((self.HOST, self.PORT_ALGO))
                print("[RPI - PC] Successfully connected to Algo: " + str(self.HOST))
                # obstacle = b"obstacles"
                self.client.send(obstacle)
                print('[RPI - PC] Obstacles sent')               
                self.client.close()
                # print('[RPI - PC] Connection closed')
                time.sleep(1)

                print("[PC - RPI] Attempting to connect to PC to receive...")       
                # Reconnect the RPI client to the server
                self.client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                self.client.connect((self.HOST, self.PORT_ALGO))
                print("[PC - RPI] Successfully connected to PC: " + str(self.HOST))
                message = self.read()
                self.client.close()
                print('[PC - RPI] Connection closed')           
                
                return message

            except Exception as exception:
                print("[RPI - PC] Algo failed: " + str(exception))
                time.sleep(1)  

    # Allow RPI to receive and read messages
    def read(self):
        print("[PC - RPI] Attempting to read from image server via Wi-Fi...")
        try:
            message = self.client.recv(self.READ_BUFFER_SIZE)
        except Exception as exception:
            print("[PC - RPI] Failed to read from image server via Wi-Fi: " + str(exception))
        else:
            if message is not None and len(message) > 0:
                print("[PC - RPI] Message read from image server via Wi-Fi:")
                message = message.decode()
                print('[PC - RPI] Received: ' + str(message))
                return message




if __name__ == "__main__":
    id = imageDetection()
    id.image()
    sys.exit(0)
