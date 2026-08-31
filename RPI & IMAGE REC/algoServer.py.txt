import os
import socket
import time
import os.path
from os import path
import os.path,subprocess
from subprocess import STDOUT,PIPE

def compile_java(java_file):
    subprocess.check_call(['javac', java_file])

def execute_java(java_file, inputString):
    java_class,ext = os.path.splitext(java_file)
    cmd = ['java', java_class, inputString]
    proc = subprocess.Popen(cmd, stdout=PIPE, stderr=STDOUT)
    stdout,stderr = proc.communicate(input=inputString)
    return stdout

host = '192.168.21.29'
port = 54123

def deleteOldLabels():
    filelist = [ f for f in os.listdir("/Users/Dion/Documents/GitHub/MDP/yolov5/runs/detect/results/labels") if f.endswith(".txt") ]
    for f in filelist:
        os.remove(os.path.join("/Users/Dion/Documents/GitHub/MDP/yolov5/runs/detect/results/labels/", f))

def deleteOldImages():
    filelist = [ f for f in os.listdir("/Users/Dion/Documents/GitHub/MDP/yolov5/runs/detect/results") if f.startswith("image") ]
    for f in filelist:
        os.remove(os.path.join("/Users/Dion/Documents/GitHub/MDP/yolov5/runs/detect/results/", f))

def setupAlgoServer():
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    print("[PC] Server launched")
    print("[PC] Waiting for RPI...")
    try:
        server.bind((host, port))
    except socket.error as msg:
        print(msg)
    print("[PC] Socket binded")
    server.listen(2)

    while True:
        clientSocket, clientAddress = server.accept()
        print('[PC] RPI connected')
        obstaclePath = clientSocket.recv(2048)
        if b"ENDRUN" in obstaclePath:
            os.system("python3 imageStitch.py")
            deleteOldLabels()


        else:
            print("[PC] Obstacles receieved from RPI: " + obstaclePath.decode('utf-8'))
            compile_java('Main.java')
            message = execute_java('Main.java', obstaclePath.decode('utf-8'))
            clientSocket, clientAddress = server.accept()
            time.sleep(1)
            clientSocket.send(message)

            if message:
                print("[PC] Sending result of algo: ", message.decode('utf-8'))
                clientSocket.send(message)
                time.sleep(1)
            else:
                print("[PC] No output from Algo!")


if __name__ == '__main__':
    setupAlgoServer()
    
