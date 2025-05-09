package com.code.mydiary;

import android.app.AlertDialog;
import android.widget.ImageButton;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.code.mydiary.util.GenderResourceUtil;
import com.code.mydiary.util.ToastUtil;
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

        // 返回按钮逻辑
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed()); // 让btn_back等效于系统返回键

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
            //   ACTION_DIAL 打开系统的拨号盘界面，并将指定的号码预先填入
            //    Uri.parse("tel:" + phone) 创建一个表示电话号码的 URI
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

        initSexChange();
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
                    // 检查重复
                    boolean duplicate = false;
                    for (String[] arr : contactRawList) {
                        if (arr[0].equals(name)) {
                            ToastUtil.showMsg(SOSActivity.this, "该姓名已存在");
                            duplicate = true;
                            break;
                        }
                        if (arr[1].equals(phone)) {
                            ToastUtil.showMsg(SOSActivity.this, "该号码已存在");
                            duplicate = true;
                            break;
                        }
                    }
                    if (duplicate) return;

                    userCRUD.addContact(userId, name, phone);
                    loadContacts();
                    adapter.notifyDataSetChanged();
                } else {
                    ToastUtil.showMsg(SOSActivity.this, "姓名和号码不能为空");
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

    private void initSexChange() {
        // 动态设置顶部背景
        ImageView imageView = findViewById(R.id.imageView);
        if (imageView != null) {
            imageView.setImageResource(
                    GenderResourceUtil.getMenuBackgroundRes(this)
            );
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        userCRUD.close();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SOSActivity.this, Menu.class);
        intent.putExtra("user_id", userId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}