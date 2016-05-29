package com.chinamobile.hejiaqin.business.logic.more;

import android.graphics.Bitmap;
import android.os.Message;

import com.chinamobile.hejiaqin.business.BussinessConstants;
import com.customer.framework.component.qrcode.QRCodeWriter;
import com.customer.framework.component.qrcode.lib.BarcodeFormat;
import com.customer.framework.component.qrcode.lib.EncodeHintType;
import com.customer.framework.component.qrcode.lib.WriterException;
import com.customer.framework.component.qrcode.lib.common.BitMatrix;
import com.customer.framework.logic.LogicImp;

import java.util.Hashtable;

/**
 * Created by eshaohu on 16/5/24.
 */
public class MoreLogic extends LogicImp implements IMoreLogic {
    @Override
    public Bitmap createQRImage(String url, int qrWidth, int qrHeight) {
        try
        {
            //判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1)
            {
                return null;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, qrWidth, qrHeight, hints);
            int[] pixels = new int[qrWidth * qrHeight];
            //下面这里按照二维码的算法，逐个生成二维码的图片，
            //两个for循环是图片横列扫描的结果
            for (int y = 0; y < qrHeight; y++)
            {
                for (int x = 0; x < qrWidth; x++)
                {
                    if (bitMatrix.get(x, y))
                    {
                        pixels[y * qrWidth + x] = 0xff000000;
                    }
                    else
                    {
                        pixels[y * qrWidth + x] = 0xffffffff;
                    }
                }
            }
            //生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(qrWidth, qrHeight, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, qrWidth, 0, 0, qrWidth, qrHeight);
           return  bitmap;
        }
        catch (WriterException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
