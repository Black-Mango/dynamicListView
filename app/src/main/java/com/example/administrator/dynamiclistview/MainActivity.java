package com.example.administrator.dynamiclistview;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private TextView add;
    private PickAdapter adapter;
    private   ArrayList<HashMap<String, Object>> dataList=new ArrayList<>();
    private Context context;
    private ListView pickList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        initViews();
    }

    protected void findViews() {
        context=MainActivity.this;
        pickList=(ListView)findViewById(R.id.pick_list);
        add=(TextView)findViewById(R.id.add);
    }

    protected void initViews() {
            HashMap<String, Object> map = new HashMap<>();
            map.put("name","");
            map.put("number","");
            dataList.add(map);
            adapter = new PickAdapter(context, dataList);
            pickList.setAdapter(adapter);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("name","");
                    map.put("number","");
                    dataList.add(map);
                    adapter.notifyDataSetChanged();
                    //刷新页面
                    setListViewHeightBasedOnChildren(pickList);
                }
            });
    }
    //构建adapter
    private class PickAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        Context context;
        String func;
        private PickAdapter(Context context, ArrayList<HashMap<String,Object>> dataList){
            inflater=LayoutInflater.from(context);
            this.context=context;
        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
                final PickAdapter.ViewHolder holder;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.item_pick, parent, false);
                    holder = new PickAdapter.ViewHolder();
                    holder.pick_name = (EditText) convertView.findViewById(R.id.name_input);
                    holder.pick_item = (EditText) convertView.findViewById(R.id.number_input);
                    holder.pick_number = (TextView) convertView.findViewById(R.id.pick_number);
                    holder.delete = (TextView) convertView.findViewById(R.id.delete);
                    convertView.setTag(holder);
                } else {
                    holder = (PickAdapter.ViewHolder) convertView.getTag();
                }
                final HashMap map = dataList.get(position);
                if (holder.pick_name.getTag() instanceof TextWatcher || holder.pick_name.getTag() instanceof TextWatcher) {
                    holder.pick_name.removeTextChangedListener((TextWatcher) (holder.pick_name.getTag()));
                    holder.pick_item.removeTextChangedListener((TextWatcher) (holder.pick_item.getTag()));
                }
                if (map.get("name").equals("")) {holder.pick_name.setText("");
                } else {
                    holder.pick_name.setText( map.get("name").toString());
                }
                if (map.get("number").equals("")) {
                    holder.pick_item.setText("");
                } else {
                    holder.pick_item.setText( map.get("number").toString());
                }
                //创建监听器绑定Name，变动时进行存储，Number同理，也可以在删除或者增加时先存储数据再进行增删操作。
                TextWatcher name = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        holder.pick_name.getText().toString();
                        if (holder.pick_name.getText().toString().equals("")) {
                            dataList.get(position).put("name", "");
                        } else
                            dataList.get(position).put("name", holder.pick_name.getText().toString());
                    }
                };
                TextWatcher number = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override
                    public void afterTextChanged(Editable s) {
                        holder.pick_item.getText().toString();
                        if (holder.pick_item.getText().toString().equals("")) {
                            dataList.get(position).put("number", "");
                        } else
                            dataList.get(position).put("number", holder.pick_item.getText().toString());
                    }
                };
                if (dataList.size() == 1) {
                    //只有一项不能删除
                    holder.delete.setVisibility(View.GONE);
                } else holder.delete.setVisibility(View.VISIBLE);
                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(context)
                                .setTitle("你确定要删除" + holder.pick_number.getText().toString() + "吗？")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dataList.remove(position);
                                        adapter.notifyDataSetChanged();
                                        setListViewHeightBasedOnChildren(pickList);
                                    }
                                })
                                .setNegativeButton("取消", null)
                                .show().setCanceledOnTouchOutside(false);
                    }
                });
                holder.pick_number.setText("物品明细(" + (position + 1) + ")");
                holder.pick_name.addTextChangedListener(name);
                holder.pick_item.addTextChangedListener(number);
                holder.pick_name.setTag(name);
                holder.pick_item.setTag(number);
            return convertView;
        }

        class ViewHolder {
            TextView pick_name;
            TextView pick_item;
            TextView pick_number;
            TextView delete;
        }
    }
    /**
     * 为了解决ListView在ScrollView中只能显示一行数据的问题
     *
     * @param listView
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) { // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);

            listItem.measure(0, 0); // 计算子项View 的宽高
            totalHeight += listItem.getMeasuredHeight(); // 统计所有子项的总高度
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // hisListView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }

    /**
     *隐藏软键盘
     * created at 2017/9/13 17:39
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public  boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = { 0, 0 };
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}
