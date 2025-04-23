package com.code.mydiary;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.code.mydiary.util.UserCRUD;
import java.util.ArrayList;

public class SOSActivity extends AppCompatActivity {

    private ListView listView;
    private Button btnAdd;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> contactDisplayList = new ArrayList<>();
    private ArrayList<String[]> contactRawList = new ArrayList<>();
    private long userId;
    private UserCRUD userCRUD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sos_activity);

        userId = getIntent().getLongExtra("user_id", -1);

        listView = findViewById(R.id.list_contacts);
        btnAdd = findViewById(R.id.btn_add_contact);

        userCRUD = new UserCRUD(this);
        userCRUD.open();

        loadContacts();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactDisplayList);
        listView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> showAddDialog());

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String phone = contactRawList.get(position)[1];
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
            startActivity(intent);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            String[] contact = contactRawList.get(position);
            new AlertDialog.Builder(this)
                .setTitle("删除联系人")
                .setMessage("确定要删除该联系人吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    userCRUD.deleteContact(userId, contact[0], contact[1]);
                    loadContacts();
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("取消", null)
                .show();
            return true;
        });
    }

    private void showAddDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText etName = new EditText(this);
        etName.setHint("联系人姓名");
        layout.addView(etName);

        final EditText etPhone = new EditText(this);
        etPhone.setHint("电话号码");
        etPhone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        layout.addView(etPhone);

        new AlertDialog.Builder(this)
            .setTitle("添加紧急联系人")
            .setView(layout)
            .setPositiveButton("添加", (dialog, which) -> {
                String name = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                if (!name.isEmpty() && !phone.isEmpty()) {
                    userCRUD.addContact(userId, name, phone);
                    loadContacts();
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "姓名和号码不能为空", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void loadContacts() {
        contactDisplayList.clear();
        contactRawList.clear();
        ArrayList<String[]> list = userCRUD.getContactList(userId);
        for (String[] arr : list) {
            contactDisplayList.add(arr[0] + " (" + arr[1] + ")");
            contactRawList.add(arr);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userCRUD.close();
    }
}