package net.biaji.android.tts.utils;

import java.util.ArrayList;

public class Tokenizer {

    /**
     * 音素字符串转为模型输入token
     *
     * @param phonemes
     * @return
     */
    public static long[] tokenize(String phonemes) {
        ArrayList<Long> resultList = new ArrayList<>();
        for (char single : phonemes.toCharArray()) {
            Integer singleN = DefaultConfig.vocab.get(single);
            if (singleN != null) {
                resultList.add(singleN.longValue());
            }

        }
        return resultList.stream().mapToLong(l -> l).toArray();
    }
}
