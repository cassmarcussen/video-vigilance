import sys
 
sys.path.insert(0, "home/sharondong/.local/lib/python2.7/site-packages/")
import cv2
 
 
def test():
    return "hi"
 
 
def extract():
    vidcap = cv2.VideoCapture("video.mp4")
    # Read video at 10 sec mark
    vidcap.set(cv2.CAP_PROP_POS_MSEC, 1000)
    success, image = vidcap.read()
    if success:
        cv2.imwrite("image.jpg", image)
        return "success"
    else:
        return "fail"
    
