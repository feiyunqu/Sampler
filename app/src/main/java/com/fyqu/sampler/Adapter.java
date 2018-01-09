package com.fyqu.sampler;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.TextView;

public class Adapter extends CursorAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private Callback mCallback;

    public Adapter(Context context, Cursor cursor,Callback callback) {
        super(context, cursor,false);
        mContext = context;
        mCallback = callback;
        mInflater = LayoutInflater.from(context);
    }

    public interface Callback {
        void alterClick(View v,View itemV);
        void deleteClick(View v,View itemV);
    }

    @Override
    public void bindView(final View view, Context context, Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndex("name"));
        String userName = cursor.getString(cursor.getColumnIndex("userName"));
        String password = cursor.getString(cursor.getColumnIndex("password"));
        TextView text_powername = (TextView) view.findViewById(R.id.text_powername);
        TextView text_username = (TextView) view.findViewById(R.id.text_username);
        EditText editText_password = (EditText) view.findViewById(R.id.editText_password);
        text_powername.setText(name);
        text_username.setText(userName);
        editText_password.setText(password);
        Button btn_alter = (Button) view.findViewById(R.id.btn_alter);
        Button btn_delete = (Button) view.findViewById(R.id.btn_delete);
        btn_alter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.alterClick(v,view);
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.deleteClick(v,view);
            }
        });
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.userlist, parent, false);//一般都这样写，返回列表行元素，注意这里返回的就是bindView中的view
    }
}
