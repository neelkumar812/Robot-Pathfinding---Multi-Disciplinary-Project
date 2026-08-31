from distutils.dir_util import copy_tree
import os

def deleteOldLabels():
    filelist = [ f for f in os.listdir("/Users/Dion/Documents/GitHub/MDP/yolov5/runs/detect/results/labels") if f.endswith(".txt") ]
    for f in filelist:
        os.remove(os.path.join("/Users/Dion/Documents/GitHub/MDP/yolov5/runs/detect/results/labels/", f))

def deleteOldImages():
    filelist = [ f for f in os.listdir("/Users/Dion/Documents/GitHub/MDP/yolov5/runs/detect/results") if f.startswith("image") ]
    for f in filelist:
        os.remove(os.path.join("/Users/Dion/Documents/GitHub/MDP/yolov5/runs/detect/results/", f))

def stitchImage():
    os.system("python3 imageStitch.py")


if __name__ == "__main__":
    stitchImage()
    deleteOldImages()
    deleteOldLabels()