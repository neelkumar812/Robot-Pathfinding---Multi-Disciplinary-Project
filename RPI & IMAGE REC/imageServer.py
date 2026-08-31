import os
import socket
import time
import os.path
from os import path

host = '192.168.21.29'
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
        filePath = r"/Users/Dion/Documents/GitHub/MDP/yolov5/data/images/image" + str(counter) + ".jpg"
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

        os.chdir("/Users/Dion/Documents/GitHub/MDP/yolov5")
        os.system(
            'python3 detect.py --source data/images/image' + str(
                counter) + '.jpg --weights aloy8.pt --img 640 ' +
            '--save-conf --hide-conf --conf 0.3 --name results --exist-ok --save-txt')
        clientSocket, clientAddress = server.accept()
        labelText = "/Users/Dion/Documents/GitHub/MDP/yolov5/runs/detect/results/labels/image" + str(
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
                label = int(new_detections[0][0]) + 10
                message = str(label)

                # Offset 1: 20cm
                offset = float(new_detections[0][1])
                if offset < 0.35: # Car too much right
                    error = 0.5 - offset
                    distance_move = str(int(20*error))
                    if len(distance_move) == 1:
                        distance_move = "0"+distance_move

                    message = message + " s0" + distance_move
                elif offset > 0.65: # Too much left
                    error = offset - 0.5
                    distance_move = str(int(20*error))
                    if len(distance_move) == 1:
                        distance_move = "0"+distance_move

                    message = message + " w0" + distance_move
                else:
                    message = message + " N"

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
