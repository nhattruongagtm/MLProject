from PIL import Image
import base64
import io
import cv2
import numpy as np
from matplotlib import pyplot as plt

def main(file):

    decoded_data = base64.b64decode(file)
    np_data = np.fromstring(decoded_data,np.uint8)
    image = cv2.imdecode(np_data,cv2.IMREAD_UNCHANGED)


    height, width, depth = image.shape

    # resizing the image to find spaces better
    image = cv2.resize(image, dsize=(width * 5, height * 4), interpolation=cv2.INTER_CUBIC)
    # grayscale
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)


    # binary
    ret, thresh = cv2.threshold(gray, 127, 255, cv2.THRESH_BINARY_INV)

    # dilation
    kernel = np.ones((5, 5), np.uint8)
    img_dilation = cv2.dilate(thresh, kernel, iterations=1)

    # adding GaussianBlur
    gsblur = cv2.GaussianBlur(img_dilation, (5, 5), 0)

    # find contours
    ctrs, hier = cv2.findContours(gsblur.copy(), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    # sort contours
    sorted_ctrs = sorted(ctrs, key=lambda ctr: cv2.boundingRect(ctr)[0])
    pchl = list()
    dp = image.copy()
    for i, ctr in enumerate(sorted_ctrs):
        # Get bounding box
        x, y, w, h = cv2.boundingRect(ctr)
        cv2.rectangle(dp, (x - 10, y - 10), (x + w + 10, y + h + 10), (13, 154, 0), 35)

    pil_im = Image.fromarray(dp)
    buff = io.BytesIO()
    pil_im.save(buff,format="PNG")
    img_str = base64.b64encode(buff.getvalue())
    return str(img_str,'utf-8')