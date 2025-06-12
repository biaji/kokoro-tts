package net.biaji.android.tts.utils;

import android.util.Log;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.ArrayList;
import java.util.List;

public class PinyinUtils {
    /**
     * 获取词的 finals tone3（带调拼音）
     */
    public static List<String> getFinalsTone3(String word) {
        List<String> result = new ArrayList<>();
        for (char c : word.toCharArray()) {
            String[] pinyinResult = PinyinHelper.toHanyuPinyinStringArray(c);
            if (pinyinResult != null && pinyinResult.length > 0) {
                String pinyin = PinyinHelper.toHanyuPinyinStringArray(c)[0]; // 取第一个拼音
                result.add(pinyin);
            } else {
                Log.d("Pinyin", "跳过：\"" + word + "\"");
            }
        }
        return result;
    }

    /**
     * 伪实现：提取拼音的 finals（带音调）
     */
    private static String extractFinalTone(String pinyin) {
        // 假设 "shi3" → 返回 "i3"；真实提取逻辑需根据拼音库实现
        // 可使用正则或 PinyinHelper 拆分音节结构
        return pinyin; // 简化处理，实际应分析韵母
    }
}
