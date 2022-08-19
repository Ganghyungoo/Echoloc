package com.example.echoloc;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.echoloc.adapter.MemberAdapter;
import com.example.echoloc.database.Pref;
import com.example.echoloc.model.Usermodel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MemberList_activity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
     FirebaseDatabase firebaseDatabase;
     DatabaseReference databaseReference;
     ArrayList<Usermodel> list;
     Pref pref;
    MemberAdapter memberAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_list);
        recyclerView=findViewById(R.id.member_adapter);
        firebaseDatabase=FirebaseDatabase.getInstance();
        pref = new Pref(getApplicationContext());
        list=new ArrayList<>();
        memberAdapter=new MemberAdapter(list);
        recyclerView.setAdapter(memberAdapter);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        String isfrom_public=getIntent().getStringExtra("isfrom_public");
        if(isfrom_public=="1")
        {
            databaseReference = firebaseDatabase.getReference("Echoloc").child("public").child(getIntent().getStringExtra("group_id")).child("members");

        }else{
            databaseReference = firebaseDatabase.getReference("Echoloc").child("private").child(getIntent().getExtras().getString("group_id")).child("members");

        }
        getData();
    }
    private void getData()
    {

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();

                for(DataSnapshot dsnapshot:snapshot.getChildren())
                {
                    Usermodel usermodel=dsnapshot.getValue(Usermodel.class);
                       list.add(usermodel);
                       System.out.println(1);
                }
                memberAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("에러");
            }
        });
    }
}