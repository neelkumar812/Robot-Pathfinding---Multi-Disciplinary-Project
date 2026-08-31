import picamera
import time
import cv2 as cv
import numpy as np

frames = 40
def filenames():
    frame = 31
    while frame <= frames:
        yield '/home/dion/mdp/dataset/21/21_%02d.jpg' % frame
        frame += 1

with picamera.PiCamera() as camera:
    camera.resolution = (2592,1944)
    camera.vflip = True
    camera.hflip = True
    camera.brightness = 55
    camera.framerate = 15
    
    camera.start_preview()
    # Give the camera some warm-up time
    time.sleep(2)
    
    start = time.time()
    camera.capture_sequence(filenames(), use_video_port=True)
    finish = time.time()