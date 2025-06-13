# 基于Kokoro的Android TTS引擎

## 存在问题

 - 还是慢
 - 暂时只支持中文
 - 汉字转拼音没有处理好多音字

## 特性

没啥特性

## 参考项目
 - https://github.com/thewh1teagle/kokoro-onnx
 - https://github.com/puff-dayo/Kokoro-82M-Android
 - https://k2-fsa.github.io/sherpa
 - https://huggingface.co/hexgrad/Kokoro-82M
 - https://github.com/hexgrad/misaki

## 组件来源
 - [kokoro-int8-multi-lang-v1_1.tar.bz2](https://k2-fsa.github.io/sherpa/onnx/tts/pretrained_models/kokoro.html#kokoro-multi-lang-v1-1-chinese-english-103-speakers)
 - 分词：https://github.com/houbb/segment
 - [中文转拼音](https://github.com/houbb/pinyin)
