package example.seele.com.rxstudy.entity;

import java.util.List;

/**
 * Created by SEELE on 2018/4/2.
 */

public class SimpleResponseEntity {


    /**
     * wordList : [{"offset":0,"pos":"x","length":4,"word":"text"}]
     * dataType : nlp.segment
     * appCode : bitspaceman
     * version : v0.0.3
     */

    private String dataType;
    private String appCode;
    private String version;
    private List<WordListBean> wordList;

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<WordListBean> getWordList() {
        return wordList;
    }

    public void setWordList(List<WordListBean> wordList) {
        this.wordList = wordList;
    }

    public static class WordListBean {
        /**
         * offset : 0
         * pos : x
         * length : 4
         * word : text
         */

        private int offset;
        private String pos;
        private int length;
        private String word;

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public String getPos() {
            return pos;
        }

        public void setPos(String pos) {
            this.pos = pos;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }


        @Override
        public String toString() {
            return "WordListBean{" +
                    "offset=" + offset +
                    ", pos='" + pos + '\'' +
                    ", length=" + length +
                    ", word='" + word + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "SimpleResponseEntity{" +
                "dataType='" + dataType + '\'' +
                ", appCode='" + appCode + '\'' +
                ", version='" + version + '\'' +
                ", wordList=" + wordList +
                '}';
    }
}
