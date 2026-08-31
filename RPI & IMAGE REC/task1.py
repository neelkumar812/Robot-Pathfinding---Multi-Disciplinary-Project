from email import message
import enum
import multiprocessing as mp
import time

from bluetoothFunctions import BluetoothCommuncation
from serialFunctions import SerialCommunication
from imageDetection import imageDetection
from messageStructure import MessageStructure

class Multiprocessor:

    def __init__(self):
        self.bluetooth = BluetoothCommuncation()
        self.serial = SerialCommunication()
        self.imageDetection = imageDetection()
        self.write_queue = mp.Queue()

    def connectDevices(self):
        self.serial.connect()
        self.bluetooth.connect()

    def readBluetooth(self):
        obstacleList = []
        while True:
            msg = self.bluetooth.read()
            # self.write_queue.put(MessageStructure("B", b"[RPI] Message from tablet received"))
            msg = msg.decode('utf-8')
            if "OBSTACLE:" in msg:
                obstacleList.append(msg.split()[-1])
                while msg != "STARTRUN":
                    msg = self.bluetooth.read()
                    msg = msg.decode('utf-8')
                    if "OBSTACLECHANGE:" in msg:
                        #OBSTACLECHANGE:obstacleNo (x,y,dir,obstacleNo)
                        obstacleToChange = int(msg.split(':')[1].split()[0]) - 1
                        newPosition = msg.split(':')[1].split()[1]
                        obstacleList[obstacleToChange] = newPosition
                        print("[RPI] Updated ObstacleList: ", obstacleList)
                    elif "OBSTACLEREMOVED:" in msg:
                        #OBSTACLEREMOVE:obstacleNo
                        obstacleToRemove = int(msg.split(':')[1]) - 1
                        obstacleList.pop(obstacleToRemove)
                        for i in range(obstacleToRemove, len(obstacleList)):
                            temp = obstacleList[i].split(',')
                            temp[3] = str(i+1)+')'
                            obstacleList[i] = ','.join(temp)
                    elif "OBSTACLECLEAR" in msg:
                        obstacleList = []
                        print("[RPI] Cleared all obstacles!")
                    elif "OBSTACLE:" in msg:
                        obstacleList.append(msg.split()[-1])
                    print("[RPI] ObstacleList: ", obstacleList)
                self.write_queue.put(MessageStructure("O", (' '.join(obstacleList)).encode('utf-8')))

            # else:
            #     self.write_queue.put(MessageStructure("I", msg))
            #     # instructions = msg.split()
            #     # for i in instructions:
            #     #     self.write_queue.put(MessageStructure("I", i))

    def captureImage(self):
        msg = self.imageDetection.image()
        return msg

    def writeBluetooth(self, msg):
        self.bluetooth.write(msg)


    def readSerial(self):
        msg = self.serial.read()
        return msg
    
    def writeSerial(self, msg):
        self.serial.sendMessage(msg)
        ack = None
        while ack != b'k':
            ack = self.readSerial()
            continue
        print("STM has completed instruction")
        time.sleep(0.5)

    def runAlgo(self, msg):
        if msg == b"ENDRUN":
            self.imageDetection.algo(msg)
        else:
            instructions = self.imageDetection.algo(msg).split('\n')[-2].split(',')
            if instructions != None:
                # instructions.split(',')
                order = [*instructions[-1]]
                instructions = instructions[:-1]
                self.write_queue.put(MessageStructure("B", "STATUS LOOKINGFOR " + order[0]))
                for i, instruction in enumerate(instructions):
                    self.write_queue.put(MessageStructure("I", instruction.encode('utf-8')))
                return order
            else:
                print("[readAlgo] instructions is None")

        return None
        
    
    def writeMessage(self):
        # write to bluetooth, stm, algo
        obstacleOrder = []
        obstacleTracker = 0
        while True:
            try:
                if self.write_queue.empty():
                    continue
                else:
                    message = self.write_queue.get()
                    header = message.getHeader()
                    msg = message.getMessage()

                    if header == "B": # Send to tablet
                        self.writeBluetooth(msg)
                        continue

                    elif header == "I": # Send to STM
                        if msg.decode('utf-8') == "c001":
                            label = self.captureImage()
                            # label = '3'
                            print("[RPI] Captured label: ", label)
                            if "No detections" not in label:
                                label = label.split()
                                if label[0] != "10":
                                    updateAndroidObstacle = "ROBOT," + obstacleOrder[obstacleTracker] + "," + label[0]
                                    self.writeBluetooth(updateAndroidObstacle)
                                    if label[1] != "N":
                                        self.writeSerial(label[1].encode('utf-8'))
                                time.sleep(1)
                            if obstacleTracker == len(obstacleOrder) - 1:
                                self.writeBluetooth("ENDRUN")
                                self.runAlgo(b"ENDRUN")
                            else:
                                updateAndroidStatus = "STATUS LOOKINGFOR " + obstacleOrder[obstacleTracker + 1]
                                self.writeBluetooth(updateAndroidStatus)
                            
                            obstacleTracker += 1
                        else:
                            self.writeBluetooth(msg)
                            self.writeSerial(msg)
                        continue

                    elif header == "O": # Send to Algo
                        obstacleOrder = self.runAlgo(msg)
                        obstacleTracker = 0
                        continue
                    
                    else:
                        print("[RPI] Invalid header values when writing")
                
            except Exception as exception:
                print("[RPI] Error occurred in write: " + str(exception))
                time.sleep(1)

    def bullseyeCorrection(self):
        self.serial.connect()
        message = self.captureImage()
        while message == "10":
            self.writeSerial(b"e090")
            message = self.captureImage()

    def statusUpdate(self, status):
        self.write_queue.put(MessageStructure("Status", status.encode('utf-8')))

    def updateRobotPosition(self, x, y, direction):
        position = "ROBOT," + x + "," + y + "," + direction
        self.write_queue.put(MessageStructure("B", position.encode('utf-8')))

    def testSTM(self):
        self.serial.connect()
        while True:
            message = input()
            self.writeSerial(message.encode('utf-8'))

    
            


if __name__ == "__main__":
    m = Multiprocessor()

    m.connectDevices()
    time.sleep(2)
    bluetoothProc = mp.Process(target=m.readBluetooth)
    writeProc = mp.Process(target=m.writeMessage)
    writeProc.start()
    bluetoothProc.start()

    