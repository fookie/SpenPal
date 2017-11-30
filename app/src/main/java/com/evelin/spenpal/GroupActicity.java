package com.evelin.spenpal;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GroupActicity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Map<String, Object> memberUnit = new HashMap<>();
        List<Map<String, Object>> memberList = new ArrayList<>();
        memberUnit.put("name", "test name");
        memberUnit.put("avatar", R.mipmap.ic_launcher);
        SimpleAdapter memberAdapter = new SimpleAdapter(this, memberList, R.layout.content_groupmember,new String[]{"avatar", "name"}, new int[]{R.id.groupMemberAvatar, R.id.groupMemberListName});
        ListView members = (ListView) findViewById(R.id.groupMemberList);
        members.setAdapter(memberAdapter);
    }
}
