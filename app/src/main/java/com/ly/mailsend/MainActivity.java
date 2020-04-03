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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ly.mailsend.util.Constants;
import com.ly.mailsend.util.PostUtil;

import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity {

    private static final int CHANGE_SUCCESS = 1;

    private static final int CHANGE_PROCESS = 2;

    private static final int CHANGE_FAILURE = 3;


    private EditText mEditUser;

    private Button mBtnQuery,mBtnClear;

    private RecyclerView mRVAddress;

    private MailInfoAdapter mailInfoAdapter;

    private List<MailInfo> mailInfoList = new ArrayList<>();


    private boolean mIfJson = true;

//    private String dataUrl = "http://www.gutejersy.com/android/getAddress.php";

    private String dataUrl = "http://www.nlsmall.com/emsExpress/support.do?sendQuery";

//    private String dataUrl = "http://192.168.74.131:8080/emsExpress/support.do?sendQuery";
    private MyHandler myHandler = new MyHandler(this);



    private ProgressDialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditUser = (EditText) findViewById(R.id.edit_userID);

        mBtnQuery = (Button) findViewById(R.id.btn_query);

        mBtnClear = (Button) findViewById(R.id.btn_clear);

        mRVAddress = (RecyclerView) findViewById(R.id.rv_address);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        mailInfoAdapter = new MailInfoAdapter(this,mailInfoList);


        mailInfoAdapter.setOnItemClickListener(new MailInfoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                MailInfo mailInfo = mailInfoList.get(position);



                Intent intent = new Intent(MainActivity.this,EditInfoActivity.class);

                intent.putExtra(Constants.SE_NAME,mailInfo.getSenderName());
                intent.putExtra(Constants.SE_PHONE,mailInfo.getSenderPhone());
                intent.putExtra(Constants.SE_ADDRESS,mailInfo.getSenderAddress());
                intent.putExtra(Constants.RE_NAME,mailInfo.getReceiverName());
                intent.putExtra(Constants.RE_PHONE,mailInfo.getReceiverPhone());
                intent.putExtra(Constants.RE_ADDRESS,mailInfo.getReceiverAddress());
                intent.putExtra(Constants.SE_TYPE,mailInfo.getSendType());
                intent.putExtra(Constants.SE_WEIGHT,mailInfo.getSendWeight());
                intent.putExtra(Constants.SE_CODE,mailInfo.getSendCode());

                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);

            }
        });

        mRVAddress.setAdapter(mailInfoAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
//        linearLayoutManager.setStackFromEnd(true);
        mRVAddress.setLayoutManager(linearLayoutManager);

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
                        Date d1 = new Date(System.currentTimeMillis());
                        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                        map.put("barcodedata",para);
                        map.put("optime",df.format(d1));

//                        myHandler.sendEmptyMessage(CHANGE_SUCCESS);

                        String response = PostUtil.sendPost(
                                dataUrl,map,"utf-8",mIfJson);
                        Log.d(Constants.TAG,response);
                        mailInfoList.clear();
                        mailInfoList.addAll(PostUtil.parseJson(response));

                        myHandler.sendEmptyMessage(mailInfoList.size() > 0 ?
                                CHANGE_SUCCESS : CHANGE_FAILURE );

                    }

                }.start();

            }
        });

        mBtnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditUser.setText("");
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
                    mainActivity.mailInfoAdapter.notifyDataSetChanged();
                    break;
                case CHANGE_PROCESS:
                    mainActivity.showLoadingWindow("数据查询中");
                    break;
                case CHANGE_FAILURE:
                    Toast.makeText(mainActivity,"查询失败",Toast.LENGTH_SHORT).show();
                    mainActivity.cancelDialog();
                    break;
            }

        }
    }





}
