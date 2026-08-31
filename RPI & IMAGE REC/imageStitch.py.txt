import cv2
import numpy as np
from PIL import Image
from os import path

def imageStitch():
    counter = 2
    x = 1
    h_list1 = []
    h_list2 = []
    fontScale = 1
    color = (0, 0, 255)
    font = cv2.FONT_HERSHEY_SIMPLEX
    org = (50, 50)
    thickness = 2
    while counter < 10:
        result_path = "/Users/Dion/Documents/GitHub/MDP/yolov5/runs/detect/results/image" + str(
            counter - 1) + ".jpg"
        if path.exists(result_path) is True:
            if x <= 4:
                img = cv2.imread(result_path)
                img = cv2.resize(img, (750, 750))
                image = cv2.putText(img, 'Image ' + str(counter - 1), org, font, fontScale,
                                    color, thickness, cv2.LINE_AA)
                h_list1.append(image)
                x += 1
            else:
                img = cv2.imread(result_path)
                img = cv2.resize(img, (750, 750))
                image = cv2.putText(img, 'Image ' + str(counter - 1), org, font, fontScale,
                                    color, thickness, cv2.LINE_AA)
                h_list2.append(image)
        else:
            imgBlank = Image.new("RGB", (750, 750), (255, 255, 255))
            imgBlank.save(result_path)
            if x <= 4:
                img = cv2.imread(result_path)
                img = cv2.putText(img, 'Image ' + str(counter - 1), org, font, fontScale,
                                  color, thickness, cv2.LINE_AA)
                h_list1.append(img)
            else:
                img = cv2.imread(result_path)
                img = cv2.resize(img, (750, 750))
                image = cv2.putText(img, 'Image ' + str(counter - 1), org, font, fontScale,
                                    color, thickness, cv2.LINE_AA)
                h_list2.append(image)

        counter += 1

    h_stack1 = np.hstack(h_list1)
    h_stack2 = np.hstack(h_list2)
    v_stack = np.vstack([h_stack1, h_stack2])

    # Save the image collage
    outpath = "/Users/Dion/Documents/GitHub/MDP/yolov5/runs/detect/results/Collage.jpg"
    cv2.imwrite(outpath, v_stack)
    im = Image.open(outpath)
    im.show()
    exit(0)


if __name__ == "__main__":
    imageStitch()
