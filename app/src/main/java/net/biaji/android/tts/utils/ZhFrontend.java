package net.biaji.android.tts.utils;

import static java.util.Map.entry;

import android.util.Pair;

import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum;
import com.github.houbb.pinyin.util.PinyinHelper;
import com.github.houbb.segment.api.ISegmentResult;
import com.github.houbb.segment.bs.SegmentBs;
import com.github.houbb.segment.support.tagging.pos.tag.impl.SegmentPosTaggings;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 模仿paddelspeech的中文处理
 */
public class ZhFrontend {
    /**
     * 标点符号不参与语音转换
     */
    private static final Set<String> PUNC = Set.of(";", ":", ",", ".",
            "!", "?", "—", "\"", "…", "“", "”", "(", ")",
            "；", "：", "，", "。", "！", "？"
    );

    /**
     * 声母表
     */
    private static final Set<String> initialList = Set.of(
            "b", "p", "m", "f", "d", "t", "n", "l", "g", "k", "h",
            "j", "q", "x", "zh", "ch", "sh", "r", "z", "c", "s");// "y", "w");


    private static final Map<String, String> ZH_MAP = Map.<String, String>ofEntries(entry("b", "ㄅ"),
            entry("p", "ㄆ"), entry("m", "ㄇ"), entry("f", "ㄈ"),
            entry("d", "ㄉ"), entry("t", "ㄊ"), entry("n", "ㄋ"),
            entry("l", "ㄌ"), entry("g", "ㄍ"), entry("k", "ㄎ"),
            entry("h", "ㄏ"), entry("j", "ㄐ"), entry("q", "ㄑ"),
            entry("x", "ㄒ"), entry("zh", "ㄓ"), entry("ch", "ㄔ"),
            entry("sh", "ㄕ"), entry("r", "ㄖ"), entry("z", "ㄗ"),
            entry("c", "ㄘ"), entry("s", "ㄙ"), entry("a", "ㄚ"),
            entry("o", "ㄛ"), entry("e", "ㄜ"), entry("ie", "ㄝ"),
            entry("ai", "ㄞ"), entry("ei", "ㄟ"), entry("ao", "ㄠ"),
            entry("ou", "ㄡ"), entry("an", "ㄢ"), entry("en", "ㄣ"),
            entry("ang", "ㄤ"), entry("eng", "ㄥ"), entry("er", "ㄦ"),
            entry("i", "ㄧ"), entry("u", "ㄨ"), entry("v", "ㄩ"),
            entry("ii", "ㄭ"), entry("iii", "十"), entry("ve", "月"),
            entry("ia", "压"), entry("ian", "言"), entry("iang", "阳"),
            entry("iao", "要"), entry("in", "阴"), entry("ing", "应"),
            entry("iong", "用"), entry("iou", "又"), entry("ong", "中"),
            entry("ua", "穵"), entry("uai", "外"), entry("uan", "万"),
            entry("uang", "王"), entry("uei", "为"), entry("uen", "文"),
            entry("ueng", "瓮"), entry("uo", "我"),
            entry("van", "元"), entry("vn", "云"),
            entry(";", ";"), entry(":", ":"),
            entry(",", ","), entry(".", "."),
            entry("!", "!"), entry("?", "?"),
            entry("/", "/"), entry("—", "—"),
            entry("…", "…"), entry("\"", "\""),
            entry("(", "("), entry(")", ")"),
            entry("“", "“"), entry("”", "”"),
            entry(" ", " "), entry("1", "1"),
            entry("2", "2"), entry("3", "3"),
            entry("4", "4"), entry("5", "5"),
            entry("R", "R"),
            // 以下为拼音差异导致的补丁
            entry("wo", "我"), entry("yi", "ㄧ"), entry("yao", "要"),
            entry("ya", "压"), entry("yan", "言"), entry("yang", "阳"),
            entry("yin", "阴"), entry("ying", "应"), entry("yong", "用"),
            entry("you", "又"), entry("yue", "月"), entry("wa", "穵"), entry("wai", "外"),
            entry("wan", "万"), entry("wang", "王"), entry("wei", "为"),
            entry("wen", "文"), entry("un", "文"), entry("weng", "瓮"), entry("ui", "为"),
            entry("ue", "月"), entry("yun", "云"), entry("yuan", "元"),
            entry("iu", "又"), entry("yu", "ㄩ"), entry("ye", "ㄝ"), entry("wu", "ㄨ"),
            // 中文标点
            entry("；", ";"), entry("：", ":"),
            entry("，", ", "), entry("、", ", "), entry("。", ". "),
            entry("！", "! "), entry("？", "? ")
            // 拼音差异去除的映射

    );
    /**
     * 目前使用的pinyin4j不支持load自定义字典。搁置 TODO
     */
    private static final Map<String, List<String>> PHRASES_DICT = Map.ofEntries(
            entry("开户行", List.of())
    );

    private static final Set<String> MUST_ERHUA = Set.of(
            "小院儿", "胡同儿", "范儿", "老汉儿", "撒欢儿", "寻老礼儿", "妥妥儿", "媳妇儿"
    );
    private static final Set<String> NOT_ERHUA = Set.of(
            "虐儿", "为儿", "护儿", "瞒儿", "救儿", "替儿", "有儿", "一儿", "我儿", "俺儿", "妻儿",
            "拐儿", "聋儿", "乞儿", "患儿", "幼儿", "孤儿", "婴儿", "婴幼儿", "连体儿", "脑瘫儿",
            "流浪儿", "体弱儿", "混血儿", "蜜雪儿", "舫儿", "祖儿", "美儿", "应采儿", "可儿", "侄儿",
            "孙儿", "侄孙儿", "女儿", "男儿", "红孩儿", "花儿", "虫儿", "马儿", "鸟儿", "猪儿", "猫儿",
            "狗儿", "少儿"
    );
    private static final String UNK = "❓";

    private final ToneModifier toneModifier = new ToneModifier();

    public ZhFrontend() {
        // 如果需要初始化拼音库或分词库，在这里完成
    }

    /**
     * 处理输入的中文
     *
     * @param text
     * @param withErhua 是否处理儿化音
     * @return
     */
    public Pair<String, List<MToken>> process(String text, boolean withErhua) {
        List<MToken> tokens = new ArrayList<>();
        // 1. 分词与词性标注
        List<Pair<String, String>> segCut = segmentAndPosTag(text);
        segCut = toneModifier.preMergeForModify(segCut);

        for (int s = 0; s < segCut.size(); s++) {
            Pair<String, String> wp = segCut.get(s);
            String word = wp.first;
            String pos = wp.second;

            if (pos.equals("un")) {
                pos = "x";  // Segment的词性标注与jieba有所不同
                if (isNumeric(word) && s + 1 < segCut.size()) {
                    if (segCut.get(s + 1).first.startsWith("年")) { //数字特殊读法处理
                        word = handleNum(word, true);
                    } else {
                        word = handleNum(word, false);
                    }
                }
            }

            MToken token = new MToken(word, pos, "");

            // 某些字符词性改为X（中文范围内的不可测字符），英文标点符号词性定为x
            if (pos.equals("x") && word.codePoints().allMatch(c -> c >= 0x4E00 && c <= 0x9FFF)) {
                pos = "X";
            } else if (!pos.equals("x") && PUNC.contains(word)) {
                pos = "x";
            }

            if ("x".equals(pos) || "eng".equals(pos)) {
                if (!word.isBlank()) {
                    if ("x".equals(pos) && PUNC.contains(word)) {
                        word = ZH_MAP.getOrDefault(word, word); // 中文标点转英文标点
                        token.setPhonemes(word);
                    }
                    tokens.add(token);
                } else if (!tokens.isEmpty()) {
                    tokens.get(tokens.size() - 1).appendWhitespace(word);
                }
                continue;
            } else if (!tokens.isEmpty()
                    && !"x".equals(tokens.get(tokens.size() - 1).getTag())
                    && !tokens.get(tokens.size() - 1).getTag().startsWith("w") // segments分词词性以w开头标记标点符号。参考https://github.com/houbb/segment/blob/master/src/main/java/com/github/houbb/segment/constant/enums/SegmentPosEnum.java
                    && !"eng".equals(tokens.get(tokens.size() - 1).getTag())
                    && tokens.get(tokens.size() - 1).getWhitespace().isEmpty()) {
                tokens.get(tokens.size() - 1).setWhitespace("/");
            }

            // 2. 获取声母韵母
            Pair<List<String>, List<String>> iniFins = getInitialsFinals(word);
            List<String> subInitials = iniFins.first;
            List<String> subFinals = iniFins.second;

            // 处理ju/qu等发音
            for (int t = 0; t < subFinals.size(); t++) {
                if (List.of("j", "q", "x").contains(subInitials.get(t))
                        && subFinals.get(t).startsWith("u")
                ) {
                    String mSubFinal = subFinals.get(t).replace("u", "v");
                    subFinals.set(t, mSubFinal);
                }
            }

            // 3. 声调变换
            subFinals = toneModifier.modifiedTone(word, pos, subFinals);

            // 4. 儿化
            if (withErhua) {
                Pair<List<String>, List<String>> erhua = mergeErhua(subInitials, subFinals, word, pos);
                subInitials = erhua.first;
                subFinals = erhua.second;
            }

            // 5. 组装音素
            List<String> phones = new ArrayList<>();
            for (int i = 0; i < subInitials.size(); i++) {
                String ini = subInitials.get(i);
                String fin = subFinals.get(i);
                if (!ini.isEmpty()) phones.add(ini);
                if (fin != null && (!PUNC.contains(fin) || !fin.equals(ini))) phones.add(fin);
            }
            // 替换规则
            String phonesStr = String.join("_", phones)
                    .replace("_eR", "_er")
                    .replace("R", "_R");
            phonesStr = Pattern.compile("(?=\\d)")  //分隔音调
                    .matcher(phonesStr)
                    .replaceAll("_");
            List<String> phoneList = Arrays.asList(phonesStr.split("_"));
            token.setPhonemes(phoneList.stream()
                    .map(p -> ZH_MAP.getOrDefault(p, UNK))
                    .collect(Collectors.joining()));
            tokens.add(token);
        }

        String result = tokens.stream()
                .map(tk -> (tk.getPhonemes() == null ? UNK : tk.getPhonemes()) + tk.getWhitespace())
                .collect(Collectors.joining());

        return new Pair<>(result, tokens);
    }

    private List<Pair<String, String>> segmentAndPosTag(String text) {
        List<Pair<String, String>> result = new ArrayList<>();
        List<ISegmentResult> wordList = SegmentBs.newInstance().posTagging(SegmentPosTaggings.simple()).segment(text);
        for (ISegmentResult word : wordList
        ) {
            //TODO: 移除无必要字符
            result.add(new Pair<>(word.word(), word.pos()));
        }
        return result;
    }

    /**
     * 获取拼音声母与韵母
     *
     * @param word 输入文字
     * @return 输入文字对应的声母、韵母字符串队列
     */
    private Pair<List<String>, List<String>> getInitialsFinals(String word) {
        List<String> initials = new ArrayList<>();
        List<String> finals = new ArrayList<>();

        String pinyinStr = PinyinHelper.toPinyin(word, PinyinStyleEnum.NUM_LAST);
        String[] pinyinArr = pinyinStr.split(" ");

        for (String pinyin : pinyinArr) {

            // TODO 处理“嗯”特殊逻辑：必须有声母和韵母，且拼音用 n2

            // 提取声母
            String ini = "";
            String fin = pinyin;
            if (pinyin.length() > 2 && List.of("zh", "ch", "sh").contains(pinyin.substring(0, 2))) {
                ini = pinyin.substring(0, 2);
                fin = pinyin.substring(2);
            } else {
                for (String iniCand : initialList) {
                    if (pinyin.startsWith(iniCand)) {
                        ini = iniCand;
                        fin = pinyin.substring(ini.length());
                        break;
                    }
                }
            }

            // 适配原代码声母韵母特殊处理 (处理zi/ci/si、zhi/chi/shi/ri)
            if (fin.matches("^i\\d")) {
                if (ini.equals("z") || ini.equals("c") || ini.equals("s")) {
                    fin = fin.replaceFirst("i", "ii");
                } else if (ini.equals("zh") || ini.equals("ch") || ini.equals("sh") || ini.equals("r")) {
                    fin = fin.replaceFirst("i", "iii");
                } else if (ini.equals("x") && fin.equals("uan")) {
                    fin = fin.replaceFirst("u", "v"); // xuan -> xvan
                }
            }

            initials.add(ini);
            finals.add(fin);
        }

        return new Pair<>(initials, finals);
    }


    /**
     * 实现儿化处理
     *
     * @param initials 声母队列
     * @param finals   韵母队列
     * @param word     词
     * @param pos      词性
     * @return
     */
    private Pair<List<String>, List<String>> mergeErhua(List<String> initials, List<String> finals, String word, String pos) {
        // 修正错误拼音 er1
        for (int i = 0; i < finals.size(); i++) {
            if (i == finals.size() - 1 && word.charAt(i) == '儿' && finals.get(i).equals("er1")) {
                finals.set(i, "er2");
            }
        }

        // 发音
        if (!MUST_ERHUA.contains(word) && (NOT_ERHUA.contains(word) || Arrays.asList("a", "j", "nr").contains(pos))) {
            return new Pair<>(initials, finals);
        }

        // 特殊情况（"....."符号），直接返回
        if (finals.size() != word.length()) {
            return new Pair<>(initials, finals);
        }

        // 处理不发音情况
        List<String> newInitials = new ArrayList<>();
        List<String> newFinals = new ArrayList<>();
        for (int i = 0; i < finals.size(); i++) {
            if (i == finals.size() - 1 && word.charAt(i) == '儿' &&
                    (finals.get(i).equals("er2") || finals.get(i).equals("er5")) &&
                    !NOT_ERHUA.contains(word.substring(word.length() - 2)) && !newFinals.isEmpty()) {

                String lastFinal = newFinals.get(newFinals.size() - 1);
                newFinals.set(newFinals.size() - 1, lastFinal.substring(0, lastFinal.length() - 1) + "R" + lastFinal.charAt(lastFinal.length() - 1));
            } else {
                newInitials.add(initials.get(i));
                newFinals.add(finals.get(i));
            }
        }
        return new Pair<>(newInitials, newFinals);
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String handleNum(String word, boolean split) {
        if (split) {
            char[] numChars = word.toCharArray();
            StringBuilder sb = new StringBuilder();
            for (char c : numChars) {
                sb.append(NumUtils.toChineseLower(String.valueOf(c)));
            }
            word = sb.toString();
        } else {
            word = NumUtils.toChineseLower(word);
        }
        return word;
    }

}
