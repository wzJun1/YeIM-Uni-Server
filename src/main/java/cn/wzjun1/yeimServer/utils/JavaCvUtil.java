package cn.wzjun1.yeimServer.utils;

import net.coobird.thumbnailator.Thumbnails;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class JavaCvUtil {

    /**
     * 获取视频缩略图
     *
     * @param videoPath：视频路径
     * @param baseDir：保存目录
     * @throws Exception
     */
    public static String getVideoCover(String videoPath, String baseDir) throws Exception {

        FFmpegFrameGrabber ff = FFmpegFrameGrabber.createDefault(videoPath);
        ff.start();
        //判断是否是竖屏小视频
        String rotate = ff.getVideoMetadata("rotate");
        int ffLength = ff.getLengthInFrames();
        Frame f;
        int i = 0;
        int index = 3;//截取图片第几帧
        BufferedImage bufferedImage = null;
        while (i < ffLength) {
            f = ff.grabImage();
            if (i == index) {
                Java2DFrameConverter converter = new Java2DFrameConverter();
                bufferedImage = converter.getBufferedImage(f);
                if (null != rotate && rotate.length() > 1) {
                    Image image = (Image) bufferedImage;
                    bufferedImage = rotate(image, 90);//图片旋转90度
                }
                break;
            }
            i++;
        }
        ff.stop();
        if (bufferedImage == null) {
            throw new Exception("截取视频封面异常");
        }
        String thumbFileName = MD5Util.encode(videoPath) + "_video_thumb.jpg";
        File thumb = new File(baseDir + File.separator + thumbFileName);
        Thumbnails.of(bufferedImage)
                .scale(1f)
                .outputQuality(0.8f)
                .toFile(thumb);
        return thumbFileName;
    }

    /**
     * 图片旋转角度
     *
     * @param src   源图片
     * @param angel 角度
     * @return 目标图片
     */
    public static BufferedImage rotate(Image src, int angel) {
        int src_width = src.getWidth(null);
        int src_height = src.getHeight(null);
        // calculate the new image size
        Rectangle rect_des = CalcRotatedSize(new Rectangle(new Dimension(
                src_width, src_height)), angel);

        BufferedImage res = null;
        res = new BufferedImage(rect_des.width, rect_des.height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = res.createGraphics();
        // transform(这里先平移、再旋转比较方便处理；绘图时会采用这些变化，绘图默认从画布的左上顶点开始绘画，源图片的左上顶点与画布左上顶点对齐，然后开始绘画，修改坐标原点后，绘画对应的画布起始点改变，起到平移的效果；然后旋转图片即可)

        //平移（原理修改坐标系原点，绘图起点变了，起到了平移的效果，如果作用于旋转，则为旋转中心点）
        g2.translate((rect_des.width - src_width) / 2, (rect_des.height - src_height) / 2);

        //旋转（原理transalte(dx,dy)->rotate(radians)->transalte(-dx,-dy);修改坐标系原点后，旋转90度，然后再还原坐标系原点为(0,0),但是整个坐标系已经旋转了相应的度数 ）
        g2.rotate(Math.toRadians(angel), src_width / 2, src_height / 2);

        g2.drawImage(src, null, null);
        return res;
    }

    /**
     * 计算转换后目标矩形的宽高
     *
     * @param src   源矩形
     * @param angel 角度
     * @return 目标矩形
     */
    private static Rectangle CalcRotatedSize(Rectangle src, int angel) {
        double cos = Math.abs(Math.cos(Math.toRadians(angel)));
        double sin = Math.abs(Math.sin(Math.toRadians(angel)));
        int des_width = (int) (src.width * cos) + (int) (src.height * sin);
        int des_height = (int) (src.height * cos) + (int) (src.width * sin);
        return new java.awt.Rectangle(new Dimension(des_width, des_height));
    }

}