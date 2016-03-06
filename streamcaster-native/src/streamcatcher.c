#include <linux/videodev2.h>
#include <stdio.h>
#include <string.h>
#include <fcntl.h>
#include <assert.h>
#include <sys/ioctl.h>
#include <unistd.h>

int open_device(char* dev,int width,int height){
        struct v4l2_capability vid_caps;
        struct v4l2_format vid_format;
        int fdwr = open(dev, O_RDWR);
        int ret_code = ioctl(fdwr, VIDIOC_QUERYCAP, &vid_caps);
	if (ret_code < 0) {
		return ret_code;
	}
        memset(&vid_format, 0, sizeof(vid_format));
        vid_format.type = V4L2_BUF_TYPE_VIDEO_OUTPUT;
        vid_format.fmt.pix.width = width;
        vid_format.fmt.pix.height = height;
        vid_format.fmt.pix.pixelformat = V4L2_PIX_FMT_RGB24;
        vid_format.fmt.pix.bytesperline = width *3;
        vid_format.fmt.pix.sizeimage = width*height*3;
        vid_format.fmt.pix.field = V4L2_FIELD_NONE;
        vid_format.fmt.pix.priv = 0;
        vid_format.fmt.pix.colorspace = V4L2_COLORSPACE_JPEG;
        ret_code = ioctl(fdwr, VIDIOC_S_FMT, &vid_format);
	if (ret_code < 0) {
		return ret_code;
	}
        return fdwr;
}
int close_device(int devfd){
        close(devfd);
        return 0;
}
int writeData(int devfd,__u8 data[],int length){
        int retValue = write(devfd, data, length);
        return retValue;
}

