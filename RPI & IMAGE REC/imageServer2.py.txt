import os
import socket
import time
import os.path
from os import path
from PIL import Image

host = '192.168.21.30'
port = 54321


def setupServer():
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    print("[PC] Server launched")
    print("[PC] Waiting for RPI...")
    try:
        server.bind((host, port))
    except socket.error as msg:
        print(msg)
    print("[PC] Socket binded")
    server.listen(2)
    counter = 1

    while True:
        clientSocket, clientAddress = server.accept()
        print('[PC] RPI connected')
        filePath = r"/Users/dhruv/Documents/GitHub/yolov5/data/images/image" + str(counter) + ".jpg"

        file = open(filePath, 'wb')

        imageChunk = clientSocket.recv(2048)
        while imageChunk:
            file.write(imageChunk)
            if not imageChunk:
                break
            else:
                imageChunk = clientSocket.recv(2048)
        print('[PC] Image received from RPI')
        file.close()
        
        original_img = Image.open(filePath)
 
        # Rotate Image By 180 Degree
        rotated_img = original_img.rotate(180)
                                    
        # Save updated image
        rotated_img = rotated_img.save(filePath)

        os.chdir("/Users/dhruv/Documents/GitHub/yolov5")  
        
        os.system(
            'python detect.py --source data/images/image' + str(
                counter) + '.jpg --weights task2.pt --img 640 ' +
            '--save-conf --hide-conf --conf 0.3 --name results --exist-ok --device 0 --save-txt')
        clientSocket, clientAddress = server.accept()
        labelText = "/Users/dhruv/Documents/GitHub/yolov5/runs/detect/results/labels/image" + str(
            counter) + ".txt"

        if path.exists(labelText) is True:
            
            detections = []
            f = open(labelText, 'r')  
            for line in f.readlines():  
                detection = line.split(" ")
                length = max(float(detection[3]), float(detection[4]))
                detection.append(length)

                detections.append(detection)

            detections.sort(key=lambda x:x[5], reverse=True)
            
            # Remove -1
            new_detections = []

            for detection in detections:
                if detection[0] != 0:
                    new_detections.append(detection)
                    
            # If no labels left:
            if len(new_detections) == 0:
                message = "[PC] No detections"
                clientSocket.send(message.encode('utf-8'))
                print("[PC] Error message sent")
                
            else:
                label = int(new_detections[0][0])
                message = str(label)

                print('[PC] Sending: ' + message)
                clientSocket.send(message.encode('utf-8'))
                print("[PC] Message sent")
                
            time.sleep(1)
        else:
            message = "[PC] No detections"
            clientSocket.send(message.encode('utf-8'))
            print("[PC] Error message sent")
            time.sleep(1)
        counter += 1

if __name__ == '__main__':
    setupServer()
