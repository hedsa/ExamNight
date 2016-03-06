package ir.mohandesplus.examnight.modules;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.List;

public class Question extends SugarRecord implements ParentListItem {

    public int field, code, type, universityCode;
    public String content, shortAnswer, longAnswer, dateInformation;

    // Required Empty Constructor for SugarORM
    public Question() {}

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }

    @Override
    public List<?> getChildItemList() {
        List<Question> list = new ArrayList<>();
        list.add(this);
        return list;
    }

    public boolean isSaved() {
        return Question.findById(Question.class, getId()) != null;
    }

}
