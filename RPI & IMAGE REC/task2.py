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
        while True:
            msg = self.bluetooth.read()
            msg = msg.decode('utf-8')
            if "STARTRUN" in msg:
                self.write_queue.put(MessageStructure("I", 'y000'.encode('utf-8')))

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
        print("STM in position, taking photo")
        self.write_queue.put(MessageStructure("I", "c001".encode('utf-8')))
        time.sleep(0.5)


        return None
        
    
    def writeMessage(self):
        # write to bluetooth, stm, algo
        adjustmentCount = 0
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
                        if msg.decode('utf-8') != 'c001':
                            self.writeSerial(msg)
                            continue

                        elif obstacleTracker >= 2:
                            print("ENDRUN") # Run ended, call image stitch
                                # self.stitchImage()

                        elif msg.decode('utf-8') == "c001" and obstacleTracker < 2:
                            label = self.captureImage()

                            while "No detections" in label and adjustmentCount < 4:
                                print("No detections obtained...")
                                self.writeSerial("s005".encode('utf-8')) # adjustment
                                adjustmentCount += 1
                                label = self.captureImage()

                            if adjustmentCount != 0: # return to original position before adjustments if any
                                distanceMoved = str(adjustmentCount * 5)
                                if len(distanceMoved) == 1:
                                    distanceMoved = "0" + distanceMoved
                                self.writeSerial(("w0" + distanceMoved).encode('utf-8'))
                                adjustmentCount = 0
                                

                            if label == '0' and obstacleTracker == 0: # first obstacle, right sequence
                                # self.write_queue.put(MessageStructure("I", "u000"))
                                self.write_queue.put(MessageStructure("I", "u000".encode('utf-8')))

                            elif label == '1' and obstacleTracker == 0: # first obstacle, left sequence
                                # self.write_queue.put(MessageStructure("I", "t000"))
                                self.write_queue.put(MessageStructure("I", "t000".encode('utf-8')))

                            elif label == '0' and obstacleTracker == 1: # second obstacle, right sequence
                                # self.write_queue.put(MessageStructure("I", "h000"))
                                self.write_queue.put(MessageStructure("I", "h000".encode('utf-8')))

                            elif label == '1' and obstacleTracker == 1: # second obstacle, left sequence
                                # self.write_queue.put(MessageStructure("I", "g000"))
                                self.write_queue.put(MessageStructure("I", "g000".encode('utf-8')))

                            # default cases:
                            elif "No detections" in label and obstacleTracker == 1:
                                # self.write_queue.put(MessageStructure("I", "u000"))
                                self.write_queue.put(MessageStructure("I", "u000".encode('utf-8')))

                            elif "No detections" in label and obstacleTracker == 2:
                                # self.write_queue.put(MessageStructure("I", "g000"))
                                self.write_queue.put(MessageStructure("I", "g000".encode('utf-8')))

                        

                            obstacleTracker += 1
                             
                        else:
                            self.writeBluetooth(msg)
                            self.writeSerial(msg)
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

    