package com.yupi.mianshiya.utils;

import com.baomidou.mybatisplus.core.toolkit.Sequence;
import com.yupi.mianshiya.constant.Constants;

/**
 * 雪花ID生成器工具类
 *
 * @author 陈志仁(adpost)
 * @version 1.0.0
 * @date 2023/10/16
 */
public class SnowFlakeUtil {
    private static final Sequence sequence;

    static {
        sequence = new Sequence(Constants.LOCALHOST_IP);
    }

    public static long nextId() {
        return sequence.nextId();
    }
}