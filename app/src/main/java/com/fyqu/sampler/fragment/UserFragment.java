package com.fyqu.sampler.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fyqu.sampler.Adapter;
import com.fyqu.sampler.Myutils;
import com.fyqu.sampler.R;
import com.fyqu.sampler.database.MyDatabaseHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserFragment extends Fragment {
    View root;
    ListView userList;
    MyDatabaseHelper dbHelper;
    Button btn_alter;
    Button btn_delete;
    TextView textPowername;
    TextView textUsername;
    EditText editPassword;

    Adapter adapter;

    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_user, container, false);
        dbHelper = new MyDatabaseHelper(getActivity());
        userList = (ListView) root.findViewById(R.id.user_list);
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("select * from User", null);//cursor里必须包含主键"_id"
        adapter = new Adapter(getActivity(), cursor, new Adapter.Callback() {
            @Override
            public void alterClick(View v, View itemV) {
                textPowername = (TextView) itemV.findViewById(R.id.text_powername);
                textUsername = (TextView) itemV.findViewById(R.id.text_username);
                editPassword = (EditText) itemV.findViewById(R.id.editText_password);
                dbHelper.getReadableDatabase().execSQL(
                        "update User set registerTime=?,password=? where userName=?", new Object[]{
                                Myutils.formatDateTime(System.currentTimeMillis()),
                                editPassword.getText().toString(),
                                textUsername.getText().toString()
                        });
                Toast.makeText(getActivity(), "密码更换成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void deleteClick(View v, View itemV) {
                textPowername = (TextView) itemV.findViewById(R.id.text_powername);
                textUsername = (TextView) itemV.findViewById(R.id.text_username);
                editPassword = (EditText) itemV.findViewById(R.id.editText_password);
                dbHelper.getReadableDatabase().execSQL(
                        "delete from User where userName=?", new Object[]{
                                textUsername.getText().toString()
                        });
                adapter.notifyDataSetChanged();
                adapter.getCursor().requery();
                userList.setAdapter(adapter);
            }
        });

        userList.setAdapter(adapter);
        //cursor.close();
        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}
