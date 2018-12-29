package com.icecold.sleepbandtest.utils;

/**
 *
 * Created by icecold_laptop_2 on 2018/8/3.
 */

public class Utils {

    public static Utils getInstance(){

        return UtilsHolder.instance;
    }

    private Utils() {

    }

    private static class UtilsHolder {
        private static Utils instance = new Utils();
    }

    /**
     *
     * @param overNumber 数组低位在前
     * @return 转化后的int值
     */
    public int byteToInt(byte[] overNumber) {
        int number = 0;

        for(int i = 0; i < overNumber.length; ++i) {
            int y = (int)Math.pow(256.0D, (double)i);
            number += (overNumber[i] & 255) * y;
        }

        return number;
    }

    /**
     *
     * @param bb 三个字节的byte数组
     * @param index 第几位开始
     * @param flag 高低位顺序,true 高位在前,false 低位在前
     * @return
     */
    public int threeByteToInt(byte[] bb, int index, boolean flag){
        if (flag) {
            return (int) ((((bb[index + 0] & 0xff) << 16)
                    | ((bb[index + 1] & 0xff) << 8)
                    | ((bb[index + 2] & 0xff) << 0)));
        } else {
            return (int) ((((bb[index + 2] & 0xff) << 16)
                    | ((bb[index + 1] & 0xff) << 8)
                    | ((bb[index + 0] & 0xff) << 0)));
        }
    }
}
