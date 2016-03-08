package ir.mohandesplus.examnight.modules;

import android.text.TextUtils;

import com.orm.SugarRecord;

import java.io.Serializable;

public class Package extends SugarRecord implements Serializable {

    int saveMode;
    public int price;
    public String title, description, questions;

    // Required Empty Constructor for SugarORM
    public Package() {}

    public int[] getQuestionIds() {
        if (TextUtils.isEmpty(questions)) return new int[0];
        else {
            String[] stringQuestionIds = this.questions.split(",");
            int[] questionIds = new int[stringQuestionIds.length];
            for (int i=0; i<questionIds.length; i++) {
                questionIds[i] = Integer.valueOf(stringQuestionIds[i]);
            }
            return questionIds;
        }
    }

    public void setSaveMode(int saveMode) {
        this.saveMode = saveMode;
    }

    public Package getSavedPack() {
        return Package.findById(Package.class, getId());
    }

    public int getSaveMode() {
        return this.saveMode;
    }

    public boolean isSaved() {
        return Package.findById(Package.class, this.getId()) != null;
    }

}