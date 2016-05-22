/*
 * Copyright (c) 2009-2015 ShenZhen Eternal Dynasty Technology Co., Ltd.
 * All rights reserved.
 *
 * This file contains valuable properties of  ShenZhen Eternal Dynasty
 * Technology Co., Ltd.,  embodying  substantial  creative efforts  and
 * confidential information, ideas and expressions.    No part of this
 * file may be reproduced or distributed in any form or by  any  means,
 * or stored in a data base or a retrieval system,  without  the prior
 * written permission  of  ShenZhen Eternal Dynasty Technology Co.,Ltd.
 */

/*
 * Copyright (c) 2015.  ShenZhen IncuBetter Information Technology Co.,Ltd.
 *  All rights reserved.
 *
 * This file contains valuable properties of  ShenZhen IncuBetter Information 
 * Technology Co.,Ltd.,  embodying  substantial  creative efforts  and 
 * confidential information, ideas and expressions.    No part of this 
 * file may be reproduced or distributed in any form or by  any  means,
 * or stored in a data base or a retrieval system,  without  the prior 
 * written permission of ShenZhen IncuBetter Information Technology Co.,Ltd.
 */

package com.chinamobile.hejiaqin.business.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;

import com.chinamobile.hejiaqin.business.BussinessConstants;
import com.chinamobile.hejiaqin.business.model.login.UserInfo;
import com.customer.framework.component.storage.StorageMgr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class CommonUtils {



    //检测SD card是否存在
    public static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检查是否有网络
     */
    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            return info.isAvailable();
        }
        return false;
    }

    /**
     * 检查是否是WIFI
     */
    public static boolean isWifi(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI)
                return true;
        }
        return false;
    }

    /**
     * 检查是否是移动网络
     */
    public static boolean isMobile(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE)
                return true;
        }
        return false;
    }

    private static NetworkInfo getNetworkInfo(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * 保存图片至本地
     *
     * @param fileName 保存路径
     * @param mBitmap  要保存的图片
     */
    public static void saveBitmap(String fileName, Bitmap mBitmap) {
        File f = new File(fileName);
        FileOutputStream fOut = null;
        try {
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }

            f.createNewFile();
            fOut = new FileOutputStream(f);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取手机内部剩余存储空间
     */
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    public static UserInfo getLocalUserInfo() {
        return (UserInfo) StorageMgr.getInstance().getMemStorage().getObject(BussinessConstants.Login.USER_INFO_KEY);
    }
}