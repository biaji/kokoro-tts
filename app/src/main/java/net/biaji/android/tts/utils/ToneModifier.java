package net.biaji.android.tts.utils;

import android.util.Pair;

import com.github.houbb.segment.api.ISegmentResult;
import com.github.houbb.segment.util.SegmentHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * 音调处理。此部分逻辑源自
 * https://github.com/PaddlePaddle/PaddleSpeech/blob/develop/paddlespeech/t2s/frontend/tone_sandhi.py
 */
public class ToneModifier {
    private static final String BU = "不";
    private static final String YI = "一";
    private static final String ER = "儿";
    private static final Set<String> X_ENG = new HashSet<>(Arrays.asList("x", "eng", "un"));
    private static final String PUNC = "、：，；。？！“”‘’':,;.?!";

    private final Set<String> mustNeuralToneWords;
    private final Set<String> mustNotNeuralToneWords;

    public ToneModifier() {
        mustNeuralToneWords = new HashSet<>(Arrays.asList(
                "麻烦", "麻利", "鸳鸯", "高粱", "骨头", "骆驼", "马虎", "首饰", "馒头", "馄饨", "风筝",
                "难为", "队伍", "阔气", "闺女", "门道", "锄头", "铺盖", "铃铛", "铁匠", "钥匙", "里脊",
                "里头", "部分", "那么", "道士", "造化", "迷糊", "连累", "这么", "这个", "运气", "过去",
                "软和", "转悠", "踏实", "跳蚤", "跟头", "趔趄", "财主", "豆腐", "讲究", "记性", "记号",
                "认识", "规矩", "见识", "裁缝", "补丁", "衣裳", "衣服", "衙门", "街坊", "行李", "行当",
                "蛤蟆", "蘑菇", "薄荷", "葫芦", "葡萄", "萝卜", "荸荠", "苗条", "苗头", "苍蝇", "芝麻",
                "舒服", "舒坦", "舌头", "自在", "膏药", "脾气", "脑袋", "脊梁", "能耐", "胳膊", "胭脂",
                "胡萝", "胡琴", "胡同", "聪明", "耽误", "耽搁", "耷拉", "耳朵", "老爷", "老实", "老婆",
                "戏弄", "将军", "翻腾", "罗嗦", "罐头", "编辑", "结实", "红火", "累赘", "糨糊", "糊涂",
                "精神", "粮食", "簸箕", "篱笆", "算计", "算盘", "答应", "笤帚", "笑语", "笑话", "窟窿",
                "窝囊", "窗户", "稳当", "稀罕", "称呼", "秧歌", "秀气", "秀才", "福气", "祖宗", "砚台",
                "码头", "石榴", "石头", "石匠", "知识", "眼睛", "眯缝", "眨巴", "眉毛", "相声", "盘算",
                "白净", "痢疾", "痛快", "疟疾", "疙瘩", "疏忽", "畜生", "生意", "甘蔗", "琵琶", "琢磨",
                "琉璃", "玻璃", "玫瑰", "玄乎", "狐狸", "状元", "特务", "牲口", "牙碜", "牌楼", "爽快",
                "爱人", "热闹", "烧饼", "烟筒", "烂糊", "点心", "炊帚", "灯笼", "火候", "漂亮", "滑溜",
                "溜达", "温和", "清楚", "消息", "浪头", "活泼", "比方", "正经", "欺负", "模糊", "槟榔",
                "棺材", "棒槌", "棉花", "核桃", "栅栏", "柴火", "架势", "枕头", "枇杷", "机灵", "本事",
                "木头", "木匠", "朋友", "月饼", "月亮", "暖和", "明白", "时候", "新鲜", "故事", "收拾",
                "收成", "提防", "挖苦", "挑剔", "指甲", "指头", "拾掇", "拳头", "拨弄", "招牌", "招呼",
                "抬举", "护士", "折腾", "扫帚", "打量", "打算", "打扮", "打听", "打发", "扎实", "扁担",
                "戒指", "懒得", "意识", "意思", "悟性", "怪物", "思量", "怎么", "念头", "念叨", "别人",
                "快活", "忙活", "志气", "心思", "得罪", "张罗", "弟兄", "开通", "应酬", "庄稼", "干事",
                "帮手", "帐篷", "希罕", "师父", "师傅", "巴结", "巴掌", "差事", "工夫", "岁数", "屁股",
                "尾巴", "少爷", "小气", "小伙", "将就", "对头", "对付", "寡妇", "家伙", "客气", "实在",
                "官司", "学问", "字号", "嫁妆", "媳妇", "媒人", "婆家", "娘家", "委屈", "姑娘", "姐夫",
                "妯娌", "妥当", "妖精", "奴才", "女婿", "头发", "太阳", "大爷", "大方", "大意", "大夫",
                "多少", "多么", "外甥", "壮实", "地道", "地方", "在乎", "困难", "嘴巴", "嘱咐", "嘟囔",
                "嘀咕", "喜欢", "喇嘛", "喇叭", "商量", "唾沫", "哑巴", "哈欠", "哆嗦", "咳嗽", "和尚",
                "告诉", "告示", "含糊", "吓唬", "后头", "名字", "名堂", "合同", "吆喝", "叫唤", "口袋",
                "厚道", "厉害", "千斤", "包袱", "包涵", "匀称", "勤快", "动静", "动弹", "功夫", "力气",
                "前头", "刺猬", "刺激", "别扭", "利落", "利索", "利害", "分析", "出息", "凑合", "凉快",
                "冷战", "冤枉", "冒失", "养活", "关系", "先生", "兄弟", "便宜", "使唤", "佩服", "作坊",
                "体面", "位置", "似的", "伙计", "休息", "什么", "人家", "亲戚", "亲家", "交情", "云彩",
                "事情", "买卖", "主意", "丫头", "丧气", "两口", "东西", "东家", "世故", "不由", "下水",
                "下巴", "上头", "上司", "丈夫", "丈人", "一辈", "那个", "菩萨", "父亲", "母亲", "咕噜",
                "邋遢", "费用", "冤家", "甜头", "介绍", "荒唐", "大人", "泥鳅", "幸福", "熟悉", "计划",
                "扑腾", "蜡烛", "姥爷", "照顾", "喉咙", "吉他", "弄堂", "蚂蚱", "凤凰", "拖沓", "寒碜",
                "糟蹋", "倒腾", "报复", "逻辑", "盘缠", "喽啰", "牢骚", "咖喱", "扫把", "惦记"
        ));
        mustNotNeuralToneWords = new HashSet<>(Arrays.asList(
                "男子", "女子", "分子", "原子", "量子", "莲子", "石子", "瓜子", "电子", "人人", "虎虎",
                "幺幺", "干嘛", "学子", "哈哈", "数数", "袅袅", "局地", "以下", "娃哈哈", "花花草草", "留得",
                "耕地", "想想", "熙熙", "攘攘", "卵子", "死死", "冉冉", "恳恳", "佼佼", "吵吵", "打打",
                "考考", "整整", "莘莘", "落地", "算子", "家家户户", "青青", "飘飘", "点点"
        ));
    }

    // 你需要用实际的分词库替换这里
    private List<String> splitWord(String word) {
        // Segment分词库实现

        List<String> wordList = new ArrayList<>();
        List<ISegmentResult> resultList = SegmentHelper.segment(word);
        for (ISegmentResult token : resultList) {
            wordList.add(token.word());
        }
        // 假设 wordList 得到后已按长度排序
        String firstSubword = wordList.get(0);
        int firstBeginIdx = word.indexOf(firstSubword);
        List<String> newWordList = new ArrayList<>();
        if (firstBeginIdx == 0) {
            String secondSubword = word.substring(firstSubword.length());
            newWordList.add(firstSubword);
            newWordList.add(secondSubword);
        } else {
            String secondSubword = word.substring(0, word.length() - firstSubword.length());
            newWordList.add(secondSubword);
            newWordList.add(firstSubword);
        }
        return newWordList;
    }

    /**
     * 轻音字处理
     *
     * @param word   输入字符串，比如“家里”
     * @param pos    词性 比如“s” 表示处所词 遵循结巴分词的词性
     * @param finals 韵母列表，比如['ia1', 'i3']
     * @return
     */
    private List<String> neuralModify(String word, String pos, List<String> finals) {
        if (mustNotNeuralToneWords.contains(word)) {
            return finals;
        }
        // 重叠词变调 比如，奶奶、试试、旺旺
        for (int j = 0; j < word.length(); j++) {
            if (j - 1 >= 0 && word.charAt(j) == word.charAt(j - 1)
                    && (pos.startsWith("n") || pos.startsWith("v") || pos.startsWith("a"))) { // 名词、动词、形容词
                finals.set(j, finals.get(j).substring(0, finals.get(j).length() - 1) + "5");
            }
        }
        int geIdx = word.indexOf("个");
        String lastChar = "" + word.charAt(word.length() - 1);
        if (!word.isEmpty() && "吧呢啊呐噻嘛吖嗨呐哦哒滴哩哟喽啰耶喔诶".contains(lastChar)) {
            finals.set(finals.size() - 1, finals.get(finals.size() - 1).substring(0, finals.get(finals.size() - 1).length() - 1) + "5");
        } else if (!word.isEmpty() && "的地得".contains(lastChar)) {
            finals.set(finals.size() - 1, finals.get(finals.size() - 1).substring(0, finals.get(finals.size() - 1).length() - 1) + "5");
        } else if (word.length() == 1
                && "了着过".contains(word)
                && Arrays.asList("ul", "uz", "ug").contains(pos)) { //走了、看着、去过
            finals.set(finals.size() - 1, finals.get(finals.size() - 1).substring(0, finals.get(finals.size() - 1).length() - 1) + "5");
        } else if (word.length() > 1
                && "们子".contains(lastChar)
                && Arrays.asList("r", "n").contains(pos)) {
            finals.set(finals.size() - 1, finals.get(finals.size() - 1).substring(0, finals.get(finals.size() - 1).length() - 1) + "5");
        } else if (word.length() > 1
                && "上下".contains(lastChar)
                && Arrays.asList("s", "l", "f").contains(pos)) { // 桌上、地下
            finals.set(finals.size() - 1, finals.get(finals.size() - 1).substring(0, finals.get(finals.size() - 1).length() - 1) + "5");
        } else if (word.length() > 1
                && "来去".contains(lastChar)
                && "上下进出回过起开".contains("" + word.charAt(word.length() - 2))) { //上来、下去
            finals.set(finals.size() - 1, finals.get(finals.size() - 1).substring(0, finals.get(finals.size() - 1).length() - 1) + "5");
        } else if ((geIdx >= 1 &&
                (Character.isDigit(word.charAt(geIdx - 1)) || "几有两半多各整每做是".contains("" + word.charAt(geIdx - 1))))
                || word.equals("个")) {
            finals.set(geIdx, finals.get(geIdx).substring(0, finals.get(geIdx).length() - 1) + "5");
        } else {
            if (mustNeuralToneWords.contains(word) || (word.length() > 2 && mustNeuralToneWords.contains(word.substring(word.length() - 2)))) {
                finals.set(finals.size() - 1, finals.get(finals.size() - 1).substring(0, finals.get(finals.size() - 1).length() - 1) + "5");
            }
        }
        List<String> wordList = splitWord(word);
        List<List<String>> finalsList = new ArrayList<>();
        finalsList.add(new ArrayList<>(finals.subList(0, wordList.get(0).length())));
        finalsList.add(new ArrayList<>(finals.subList(wordList.get(0).length(), finals.size())));
        for (int i = 0; i < wordList.size(); i++) {
            String w = wordList.get(i);
            if (mustNeuralToneWords.contains(w)
                    || (w.length() > 2 && mustNeuralToneWords.contains(w.substring(w.length() - 2)))) {
                List<String> fs = finalsList.get(i);
                fs.set(fs.size() - 1, fs.get(fs.size() - 1).substring(0, fs.get(fs.size() - 1).length() - 1) + "5");
            }
        }
        // 合并 finalsList
        List<String> merged = new ArrayList<>();
        for (List<String> subList : finalsList) merged.addAll(subList);
        return merged;
    }

    /**
     * 处理诸如“看不懂”中“不”字的音调为轻音
     *
     * @param word   传入的字符串
     * @param finals 传入的拼音结果
     * @return 修改后的拼音结果
     */
    private List<String> buModify(String word, List<String> finals) {
        if (word.length() == 3 && word.charAt(1) == BU.charAt(0)) {
            finals.set(1, finals.get(1).substring(0, finals.get(1).length() - 1) + "5");
        } else {
            //规则 2: 处理 "不" 后面跟第四声字的情况 (例如: 不怕)
            //当 "不" (本来是第四声) 后面跟着另一个第四声的字时，
            //"不" 自身要变调，读作第二声 (bú)。
            for (int i = 0; i < word.length(); i++) {
                if (word.charAt(i) == BU.charAt(0) && i + 1 < word.length() && finals.get(i + 1).endsWith("4")) {
                    finals.set(i, finals.get(i).substring(0, finals.get(i).length() - 1) + "2");
                }
            }
        }
        return finals;
    }

    /**
     * 处理“一”字的读音
     *
     * @param word
     * @param finals
     * @return
     */
    private List<String> yiModify(String word, List<String> finals) {
        if (word.contains(YI) && word.chars().filter(c -> !("" + (char) c).equals(YI)).allMatch(Character::isDigit)) {
            return finals;
        } else if (word.length() == 3 && word.charAt(1) == YI.charAt(0) && word.charAt(0) == word.charAt(2)) {
            finals.set(1, finals.get(1).substring(0, finals.get(1).length() - 1) + "5");
        } else if (word.startsWith("第一")) {
            finals.set(1, finals.get(1).substring(0, finals.get(1).length() - 1) + "1");
        } else {
            for (int i = 0; i < word.length(); i++) {
                if (word.charAt(i) == YI.charAt(0) && i + 1 < word.length()) {
                    if (finals.get(i + 1).endsWith("4") || finals.get(i + 1).endsWith("5")) {
                        finals.set(i, finals.get(i).substring(0, finals.get(i).length() - 1) + "2");
                    } else {
                        if (!PUNC.contains("" + word.charAt(i + 1))) {
                            finals.set(i, finals.get(i).substring(0, finals.get(i).length() - 1) + "4");
                        }
                    }
                }
            }
        }
        return finals;
    }

    /**
     * 拼音列表中是不是所有发音都为第三音
     *
     * @param finals 拼音列表
     * @return
     */
    private boolean allToneThree(List<String> finals) {
        if (finals.isEmpty()) {
            return false;
        }
        for (String x : finals) {
            if (!x.endsWith("3")) return false;
        }
        return true;
    }

    private List<String> threeModify(String word, List<String> finals) {
        if (word.length() == 2 && allToneThree(finals)) {
            finals.set(0, finals.get(0).substring(0, finals.get(0).length() - 1) + "2");
        } else if (word.length() == 3) {
            List<String> wordList = splitWord(word);
            if (allToneThree(finals)) {
                if (wordList.get(0).length() == 2) {
                    finals.set(0, finals.get(0).substring(0, finals.get(0).length() - 1) + "2");
                    finals.set(1, finals.get(1).substring(0, finals.get(1).length() - 1) + "2");
                } else if (wordList.get(0).length() == 1) {
                    finals.set(1, finals.get(1).substring(0, finals.get(1).length() - 1) + "2");
                }
            } else {
                // 省略其余细节实现，可参考 Python 代码
            }
        } else if (word.length() == 4) {
            List<List<String>> finalsList = new ArrayList<>();
            finalsList.add(new ArrayList<>(finals.subList(0, 2)));
            finalsList.add(new ArrayList<>(finals.subList(2, 4)));
            finals.clear();
            for (List<String> sub : finalsList) {
                if (allToneThree(sub)) {
                    sub.set(0, sub.get(0).substring(0, sub.get(0).length() - 1) + "2");
                }
                finals.addAll(sub);
            }
        }
        return finals;
    }

    /**
     * 合并以“不”开头的词，例如将 ["不", "喜欢"] 合并为 ["不喜欢"]。
     * 如果最后的“不”单独出现，则保留为 ("不", "d")
     *
     * @param seg 分词列表，每项是 Pair<词, 词性>
     * @return 合并后的词性列表
     */
    private List<Pair<String, String>> mergeBu(List<Pair<String, String>> seg) {
        List<Pair<String, String>> newSeg = new ArrayList<>();
        // misaki与原始paddlespeech处理逻辑不同，先使用原始版本。
        for (int i = 0; i < seg.size(); i++) {
            Pair<String, String> pair = seg.get(i);
            String word = pair.first;
            String pos = pair.second;
            String lastWord = null;
            String nextPos = null;
            if (!X_ENG.contains(pos)) {
                if (i > 0) {
                    lastWord = seg.get(i - 1).first;
                }
                // 如果上一词是“不”，将其与当前词合并
                if (lastWord != null && lastWord.equals(BU)) {
                    word = lastWord + word;
                }
            }

            if (i + 1 < seg.size()) {
                nextPos = seg.get(i + 1).second;
            }

            if (!word.equals(BU) || nextPos == null || X_ENG.contains(nextPos)) {
                newSeg.add(new Pair(word, pos));
            }

        }
/*
        for (Pair<String, String> pair : seg) {
            String word = pair.first;
            String pos = pair.second;

            // 如果上一词是“不”，将其与当前词合并
            if (BU.equals(lastWord)) {
                word = lastWord + word;
            }

            // 如果当前词不是“不”，加入新列表
            if (!BU.equals(word)) {
                newSeg.add(new Pair<>(word, pos));
            }

            // 保存当前词供下次使用（这里是已合并的 word）
            lastWord = word;
        }

        // 如果最后一个词是“不”，补一个 ("不", "d")
        if (BU.equals(lastWord)) {
            newSeg.add(new Pair<>(BU, "d"));
        }
*/
        return newSeg;
    }


    /**
     * 合并“一”和前后重复词或后接词，如 ["听", "一", "听"] -> ["听一听"]
     */
    private List<Pair<String, String>> mergeYi(List<Pair<String, String>> seg) {
        List<Pair<String, String>> newSeg = new ArrayList<>();
        boolean skipNext = false;

        // Function 1: merge "听", "一", "听" -> "听一听"
        for (int i = 0; i < seg.size(); i++) {
            if (skipNext) {
                skipNext = false;
                continue;
            }

            Pair<String, String> current = seg.get(i);

            if (i - 1 >= 0 && i + 1 < seg.size()) {
                String prevWord = seg.get(i - 1).first;
                String nextWord = seg.get(i + 1).first;
                String prevPos = seg.get(i - 1).second;

                if (YI.equals(current.first) && prevWord.equals(nextWord) && "v".equals(prevPos)) {
                    // 合并形如 "听一听"
                    Pair<String, String> merged = new Pair<>(prevWord + YI + nextWord, prevPos);
                    // 替换 newSeg 最后一个
                    newSeg.set(newSeg.size() - 1, merged);
                    skipNext = true;
                    continue;
                }
            }

            newSeg.add(current);
        }

        // Function 2: 合并孤立的“一”和后一个词，如 "一", "下" -> "一下"
        List<Pair<String, String>> finalSeg = new ArrayList<>();
        for (int i = 0; i < newSeg.size(); i++) {
            Pair<String, String> current = newSeg.get(i);

            if (!finalSeg.isEmpty() && YI.equals(finalSeg.get(finalSeg.size() - 1).first)) {
                Pair<String, String> last = finalSeg.remove(finalSeg.size() - 1);
                finalSeg.add(new Pair<>(last.first + current.first, last.second));
            } else {
                finalSeg.add(current);
            }
        }

        return finalSeg;
    }

    /**
     * 处理叠词
     *
     * @return 处理后的队列
     */
    private List<Pair<String, String>> mergeReduplication(List<Pair<String, String>> seg) {
        List<Pair<String, String>> newSeg = new ArrayList<>();
        for (Pair<String, String> pair : seg) {
            String word = pair.first;
            String pos = pair.second;

            if (!newSeg.isEmpty()) {
                Pair<String, String> last = newSeg.get(newSeg.size() - 1);
                if (word.equals(last.first) && !X_ENG.contains(pos)) {
                    // 合并最后一个并重新添加
                    newSeg.remove(newSeg.size() - 1);
                    newSeg.add(new Pair<>(last.first + word, last.second));
                    continue;
                }
            }
            newSeg.add(new Pair<>(word, pos));
        }
        return newSeg;
    }

    /**
     * 是否为两字叠词
     *
     * @param word
     * @return
     */
    private boolean isReduplication(String word) {
        return word.length() == 2 && word.charAt(0) == word.charAt(1);
    }

    /**
     * 合并相邻的两个分词，如果它们各自都是“全部为三声”，并且合起来的长度不超过3（即常见的二字、三字短语）。
     * 合并后，有助于统一三声变调的处理，防止错误地将某些三声词单独处理。
     * <p>
     * “你好”（nǐ hǎo） → 实际发音接近 “ní hǎo”
     * <p>
     * “小马” → 发音近似 “xiáo mǎ”
     *
     * @param seg
     * @return
     */
    private List<Pair<String, String>> mergeContinuousThreeTones(List<Pair<String, String>> seg) {
        List<Pair<String, String>> newSeg = new ArrayList<>();
        List<List<String>> subFinalsList = new ArrayList<>();

        for (int i = 0; i < seg.size(); i++) {
            Pair<String, String> pair = seg.get(i);
            String word = pair.first;
            String pos = pair.second;
            if (X_ENG.contains(word)) {
                subFinalsList.add(List.of(word));
                continue;
            }
            List<String> finals = PinyinUtils.getFinalsTone3(word);
            // 特殊处理“嗯” -> "n2" 可能不需要
            for (int j = 0; j < word.length(); j++) {
                if (word.charAt(j) == '嗯') {
                    finals.set(j, "n2");
                }
            }
            subFinalsList.add(finals);
        }

        boolean[] mergeLast = new boolean[seg.size()];
        for (int i = 0; i < seg.size(); i++) {
            Pair<String, String> current = seg.get(i);
            String word = current.first;
            String pos = current.second;

            if (!X_ENG.contains(pos) && i - 1 > 0 &&
                    allToneThree(subFinalsList.get(i - 1)) &&
                    allToneThree(subFinalsList.get(i)) &&
                    !mergeLast[i - 1]) {

                Pair<String, String> prev = seg.get(i - 1);
                boolean isReduplication = isReduplication(prev.first);

                if (!isReduplication && (prev.first.length() + word.length() <= 3)) {
                    // 合并：更新 newSeg 最后一个元素
                    Pair<String, String> last = newSeg.get(newSeg.size() - 1);
                    newSeg.remove(newSeg.size() - 1);
                    newSeg.add(new Pair<>(last.first + word, last.second));
                    mergeLast[i] = true;
                    continue;
                }
            }

            newSeg.add(new Pair<>(word, pos));
        }
        // TODO 合入_merge_continuous_three_tones_2 逻辑
        return newSeg;

    }

    /**
     * 合并儿化音
     *
     * @param seg
     * @return
     */
    private List<Pair<String, String>> mergeEr(List<Pair<String, String>> seg) {
        return null;
    }

    /**
     * 预处理队列，合并处理不当分词
     *
     * @param segCut
     * @return
     */
    public List<Pair<String, String>> preMergeForModify(List<Pair<String, String>> segCut) {
        List<Pair<String, String>> content = segCut;
        content = mergeBu(content);
        content = mergeYi(content);
        content = mergeReduplication(content);
        content = mergeContinuousThreeTones(content);
        return content;
    }

    /**
     * 返回声调变调处理后的 finals。
     *
     * @param word   输入词
     * @param pos    词性
     * @param finals 带调韵母
     */
    public List<String> modifiedTone(String word, String pos, List<String> finals) {
        finals = buModify(word, finals);
        finals = yiModify(word, finals);
        finals = neuralModify(word, pos, finals);
        finals = threeModify(word, finals);
        return finals;
    }

}