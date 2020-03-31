package com.ly.mailsend;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ly.mailsend.util.PostUtil;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity {

    private static final int CHANGE_SUCCESS = 1;

    private static final int CHANGE_PROCESS = 2;


    private EditText mEditUser;

    private Button mBtnQuery;

    private RecyclerView mRVAddress;

    private AddressAdapter addressAdapter;

    private List<Address> addressList = new ArrayList<>();

    private String dataUrl = "http://www.gutejersy.com/android/getAddress.php";

    private MyHandler myHandler = new MyHandler(this);

    private ProgressDialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditUser = (EditText) findViewById(R.id.edit_userID);

        mBtnQuery = (Button) findViewById(R.id.btn_query);

        mRVAddress = (RecyclerView) findViewById(R.id.rv_address);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        addressAdapter = new AddressAdapter(this,addressList);


        addressAdapter.setOnItemClickListener(new AddressAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Address address = addressList.get(position);

                String textHead = address.getName() + "  "  + address.getPhoneNumber();
                String textContain = address.getProvince() + "  " + address.getCity() + "  " +
                                     address.getCounty() + "  "  + address.getStreet() + "\n" +
                                     address.getDetail();


                Intent intent = new Intent(MainActivity.this,VerifyActivity.class);
                intent.putExtra("user id", mEditUser.getText().toString());
                intent.putExtra("text head",textHead);
                intent.putExtra("text contain", textContain);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);

            }
        });

        mRVAddress.setAdapter(addressAdapter);
        mRVAddress.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));


        mBtnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String para = mEditUser.getText().toString();
                showLoadingWindow("获取数据中");

                new Thread()
                {
                    @Override
                    public void run()
                    {

                        Map<String,String> map = new HashMap<>();
                        map.put("userid",para);

//                        myHandler.sendEmptyMessage(CHANGE_SUCCESS);

                        String response = PostUtil.sendPost(
                                dataUrl,map,"utf-8");
                        addressList.clear();
                        addressList.addAll(PostUtil.parseJson(response));



                        myHandler.sendEmptyMessage(CHANGE_SUCCESS);

                    }

                }.start();

            }
        });




    }


    /**
     * 关闭进度条
     */
    protected void  cancelDialog(){
        if (mDialog != null){
            mDialog.dismiss();
        }
    }


    /**
     * 显示进度条
     * @param message
     */
    protected void showLoadingWindow(String message)
    {


        if(mDialog != null && mDialog.isShowing())
            return ;

        mDialog = new ProgressDialog(MainActivity.this) ;
        mDialog.setProgressStyle(ProgressDialog.BUTTON_NEUTRAL);// 设置进度条的形式为圆形转动的进度条
        mDialog.setCancelable(true);// 设置是否可以通过点击Back键取消
        mDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        // 设置提示的title的图标，默认是没有的，如果没有设置title的话只设置Icon是不会显示图标的
        mDialog.setMessage(message);
        mDialog.show();
    }


    /**
     * 静态Handler
     */
    static class MyHandler extends Handler {

        private SoftReference<MainActivity> mySoftReference;

        public MyHandler(MainActivity mainActivity) {
            this.mySoftReference = new SoftReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg){
            final MainActivity mainActivity = mySoftReference.get();
            switch (msg.what) {
                case CHANGE_SUCCESS:
                    mainActivity.cancelDialog();
                    mainActivity.addressAdapter.notifyDataSetChanged();
                    break;
                case CHANGE_PROCESS:
                    mainActivity.showLoadingWindow("数据查询中");
                    break;
            }

        }
    }





}
