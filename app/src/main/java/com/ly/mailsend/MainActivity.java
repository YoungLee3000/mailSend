package com.ly.mailsend;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ly.mailsend.util.PostUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity {


    private EditText mEditUser;

    private Button mBtnQuery;

    private RecyclerView mRVAddress;

    private AddressAdapter addressAdapter;

    private List<Address> addressList = new ArrayList<>();

    private String dataUrl = "http://www.gutejersy.com/android/getAddress.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditUser = (EditText) findViewById(R.id.edit_userID);

        mBtnQuery = (Button) findViewById(R.id.btn_query);

        mRVAddress = (RecyclerView) findViewById(R.id.rv_address);




        addressAdapter = new AddressAdapter(this,addressList);

        addressAdapter.setOnItemClickListener(new AddressAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Address address = addressList.get(position);

                String textHead = address.getName() + "  "  + address.getPhoneNumber();
                String textContain = address.getProvince() + " " + address.getCity() + " " +
                                     address.getCounty() + " "  + address.getStreet() + "\n" +
                                     address.getDetail();


                Intent intent = new Intent(MainActivity.this,VerifyActivity.class);
                intent.putExtra("text head",textHead);
                intent.putExtra("text contain", textContain);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);

            }
        });

        mRVAddress.setAdapter(addressAdapter);


        mBtnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread()
                {
                    @Override
                    public void run()
                    {

                        String response = PostUtil.sendPost(
                                dataUrl,new HashMap<String, String>(),"utf-8");
                        addressList.clear();
                        addressList.addAll(PostUtil.parseJson(response));
                        addressAdapter.notifyDataSetChanged();
                    }

                }.start();

            }
        });




    }





}
