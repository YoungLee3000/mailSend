package com.ly.mailsend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ly.mailsend.util.Constants;
import com.ly.mailsend.util.PostUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerifyActivity extends BaseActivity implements View.OnClickListener{


    private Button mTakePhoto, mGenCert;
    private TextView mTVInfo;
    private ImageView mPicture;
    private static final String PERMISSION_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final int REQUEST_PERMISSION_CODE = 267;
    private static final int TAKE_PHOTO = 189;
    private static final int CHOOSE_PHOTO = 385;
    private static final int REQ_CROP = 873;
    private static final String FILE_PROVIDER_AUTHORITY = "com.ly.mailsend.provider";
    private Uri mImageUri, mImageUriFromFile;
    private Uri mSmallUri;
    private File imageFile;

    //从主界面传来的信息
    private String mTextHead;
    private String mTextContain;
    private String mUserId;

    //上传信息
    private String dataUrl = "http://www.gutejersy.com/android/getFile.php?userid=";

    private boolean gIfImage = false;
    private String mFilestr = "";

    //底部弹出框按钮
    private View bInflate;
    private TextView bChoosePhoto;
    private TextView bTakePhoto;
    private TextView bCancel;
    private Dialog bDialog;

    //进度条
    private ProgressDialog mDialog;


    //静态Handler
    private static final int CHANGE_SUCCESS = 1;
    private static final int CHANGE_PROCESS = 2;
    private MyHandler myHandler = new MyHandler(this);

    /**
     * 静态Handler
     */
    static class MyHandler extends Handler {

        private SoftReference<VerifyActivity> mySoftReference;

        public MyHandler(VerifyActivity verifyActivity) {
            this.mySoftReference = new SoftReference<>(verifyActivity);
        }

        @Override
        public void handleMessage(Message msg){
            final VerifyActivity verifyActivity = mySoftReference.get();
            switch (msg.what) {
                case CHANGE_SUCCESS:
                    verifyActivity.cancelDialog();
                    break;
                case CHANGE_PROCESS:
                    verifyActivity.showLoadingWindow("数据查询中");
                    break;
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        setTitle("信息确认");

        mTextHead = getIntent().getStringExtra("text head");
        mTextContain = getIntent().getStringExtra("text contain");
        mUserId = getIntent().getStringExtra("user id");

        /*申请读取存储的权限*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(Constants.TAG,"version larger than 23");
            if (checkSelfPermission(PERMISSION_WRITE_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.d(Constants.TAG,"no granted!");
                requestPermissions(new String[]{PERMISSION_WRITE_STORAGE}, REQUEST_PERMISSION_CODE);
            }
        }

        mPicture = (ImageView) findViewById(R.id.iv_picture);
        mTakePhoto = (Button) findViewById(R.id.bt_take_photo);
        mGenCert = (Button) findViewById(R.id.bt_gen_cert);
        mTVInfo  = (TextView) findViewById(R.id.text_info);

        String textTotal = mTextHead + "\n" + mTextContain;
        mTVInfo.setText(textTotal);

        mTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomDialog();
            }
        });

        mGenCert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadingWindow("上传数据中");
                new Thread()
                {
                    @Override
                    public void run()
                    {

                        //上传照片
                        String[] strArray = mFilestr.split("/");
                        String fileName = strArray[strArray.length -1];


                        Log.d(Constants.TAG,"if set image " + gIfImage);
                        String response = gIfImage
                                ? PostUtil.upload(dataUrl+mUserId,mFilestr)
//                                ? PostUtil.httpPostFile(VerifyActivity.this,
//                                        dataUrl+mUserId,mFilestr,fileName)
                                : PostUtil.sendPost(dataUrl+mUserId,
                                    new HashMap<String, String>(),"utf-8");
                        Log.d(Constants.TAG,response);

                        myHandler.sendEmptyMessage(CHANGE_SUCCESS);

                    }

                }.start();
            }
        });

    }



    /**
     * 显示进度条
     * @param message
     */
    protected void showLoadingWindow(String message)
    {


        if(mDialog != null && mDialog.isShowing())
            return ;

        mDialog = new ProgressDialog(VerifyActivity.this) ;
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条的形式为圆形转动的进度条
        mDialog.setCancelable(true);// 设置是否可以通过点击Back键取消
        mDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        // 设置提示的title的图标，默认是没有的，如果没有设置title的话只设置Icon是不会显示图标的
        mDialog.setMessage(message);
        mDialog.show();
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
     * 取消对话框
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (bDialog !=null){
            bDialog.cancel();
        }

    }

    /**
     * 显示底部对话框
     */
    public void showBottomDialog(){
        bDialog = new Dialog(this,R.style.ActionSheetDialogStyle);
        //填充对话框的布局
        bInflate = LayoutInflater.from(this).inflate(R.layout.dialog_bottom, null);
        //初始化控件
        bChoosePhoto = (TextView) bInflate.findViewById(R.id.choosePhoto);
        bTakePhoto = (TextView) bInflate.findViewById(R.id.takePhoto);
        bCancel = (TextView) bInflate.findViewById(R.id.cancel);
        bChoosePhoto.setOnClickListener(this);
        bTakePhoto.setOnClickListener(this);
        bCancel.setOnClickListener(this);
        //将布局设置给Dialog
        bDialog.setContentView(bInflate);
        //获取当前Activity所在的窗体
        Window dialogWindow = bDialog.getWindow();
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity( Gravity.BOTTOM);
        //设置Diglog宽度匹配屏幕
        dialogWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.y = 20;//设置Dialog距离底部的距离
//       将属性设置给窗体
        dialogWindow.setAttributes(lp);
        bDialog.show();//显示对话框
    }


    /**
     * 设置监听事件
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.takePhoto:
                takePhoto();
                break;
            case R.id.choosePhoto:
                openAlbum();
                break;
            case R.id.cancel:
                bDialog.cancel();
                break;
        }

    }

    /**
     * 打开相册
     */
    private void openAlbum() {
        Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
//        openAlbumIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
//                                 Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        openAlbumIntent.setType("image/*");
        startActivityForResult(openAlbumIntent, CHOOSE_PHOTO);//打开相册
    }

    /**
     * 拍照
     */
    private void takePhoto() {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//打开相机的Intent
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {//这句作用是如果没有相机则该应用不会闪退，要是不加这句则当系统没有相机应用的时候该应用会闪退
            imageFile = createImageFile();//创建用来保存照片的文件
            mImageUriFromFile = Uri.fromFile(imageFile);
            Log.i(Constants.TAG, "takePhoto: uriFromFile " + mImageUriFromFile);
            if (imageFile != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    /*7.0以上要通过FileProvider将File转化为Uri*/
                    mImageUri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, imageFile);
                } else {
                    /*7.0以下则直接使用Uri的fromFile方法将File转化为Uri*/
                    mImageUri = Uri.fromFile(imageFile);
                }
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);//将用于输出的文件Uri传递给相机
//                takePhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
//                                         Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(takePhotoIntent, TAKE_PHOTO);//打开相机
            }
        }
    }


    /**
     * 裁剪图片
     */
    private void crop() {

        /*新建用于存剪裁后图片的文件，并转化为Uri*/
        File tempImageFile = createImageFile();
        mFilestr = tempImageFile.getPath();

        if (tempImageFile != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                /*7.0以上要通过FileProvider将File转化为Uri*/
                mSmallUri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, tempImageFile);
            } else {
                /*7.0以下则直接使用Uri的fromFile方法将File转化为Uri*/
                mSmallUri = Uri.fromFile(tempImageFile);
            }
        }
        Log.d(Constants.TAG, "mSmallUri: " + mSmallUri.toString() );
        Log.d(Constants.TAG, "mFileStr: " + mFilestr);

        /*File image = new File(getExternalCacheDir() + "/demo.jpg");
        Log.i(TAG, "crop: path " + image.getAbsolutePath());
        mSmallUri = Uri.fromFile(image);*/

        Intent intent = new Intent("com.android.camera.action.CROP");
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
//                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(mImageUri, "image/*");
        intent.putExtra("aspectX", 768);
        intent.putExtra("aspectY", 1024);
        intent.putExtra("outputX", 768);
        intent.putExtra("outputY", 1024);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);//设置为不返回缩略图
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mSmallUri);//设置大图保存到文件
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());//保存的图片格式
        intent.putExtra("noFaceDetection", false);

        //给相关的包赋予uri的读写权限
        grantPermissions(this,intent,mSmallUri,true);
        startActivityForResult(intent, REQ_CROP);
    }


    /**
     * @param context
     * @param intent
     * @param uri
     * @param writeAble 是否可读
     */
    private void grantPermissions(Context context, Intent intent, Uri uri, boolean writeAble) {
        int flag = Intent.FLAG_GRANT_READ_URI_PERMISSION;
        if (writeAble) {
            flag |= Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        }
        intent.addFlags(flag);
        List<ResolveInfo> resInfoList = context.getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, uri, flag);
        }
    }


    /*缩略图*/
    private void cropAndThumbnail() {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(mImageUri, "image/*");//设置要缩放的图片Uri和类型
        intent.putExtra("aspectX", 768);//宽度比
        intent.putExtra("aspectY", 1024);//高度比
        intent.putExtra("outputX", 768);//输出图片的宽度
        intent.putExtra("outputY", 1024);//输出图片的高度
        intent.putExtra("scale", true);//缩放
        intent.putExtra("return-data", false);//当为true的时候就返回缩略图，false就不返回，需要通过Uri
        intent.putExtra("noFaceDetection", false);//前置摄像头

        startActivityForResult(intent, REQ_CROP);
    }

    /**
     * 创建用来存储图片的文件，以时间来命名就不会产生命名冲突
     *
     * @return 创建的图片文件
     */
    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = null;
        try {
            imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFile;
    }

    /*申请权限的回调*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i(Constants.TAG, "onRequestPermissionsResult: permission granted");
        } else {
            Log.i(Constants.TAG, "onRequestPermissionsResult: permission denied");
            Toast.makeText(this, "You Denied Permission", Toast.LENGTH_SHORT).show();
        }
    }

    /*相机或者相册返回来的数据*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode != RESULT_OK) return;
//                if (resultCode == RESULT_OK) {
//                    try {
//                        /*如果拍照成功，将Uri用BitmapFactory的decodeStream方法转为Bitmap*/
//                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(mImageUri));
//                        Log.i(TAG, "onActivityResult: imageUri " + mImageUri);
//                        galleryAddPic(mImageUriFromFile);
//                        mPicture.setImageBitmap(bitmap);//显示到ImageView上
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                }

                crop();
                break;
            case CHOOSE_PHOTO:
                if (data == null) {//如果没有拍照或没有选取照片，则直接返回
                    return;
                }
                Log.i(Constants.TAG, "onActivityResult: ImageUriFromAlbum: " + data.getData());
                if (resultCode == RESULT_OK) {

                    File tempFile = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ?
                            new File(handleImageOnKitKat(data.getData())) : new File(handleImageBeforeKitKat(data.getData()));

                    mImageUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                            FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, tempFile) :
                            Uri.fromFile(tempFile);

//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
////                        handleImageOnKitKat(data);//4.4之后图片解析
//                        mImageUri = Uri.fromFile(new File(handleImageOnKitKat(data)));
//                    } else {
////                        handleImageBeforeKitKat(data);//4.4之前图片解析
//                        mImageUri = Uri.fromFile(new File(handleImageBeforeKitKat(data)));
//                    }
                }
//
                crop();
                break;
            case REQ_CROP:
                try {
                    if (mSmallUri != null) {
                        gIfImage = true;
                        Bitmap bitmap = BitmapFactory.decodeStream(
                                getContentResolver().openInputStream(mSmallUri));
                        mPicture.setImageBitmap(bitmap);
                    } else {
                        Log.i(Constants.TAG, "onActivityResult: Uri is null");
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 4.4版本以下对返回的图片Uri的处理：
     * 就是从返回的Intent中取出图片Uri，直接显示就好
     * @param uri 调用系统相册之后返回的Uri
     */
    private String  handleImageBeforeKitKat(Uri uri) {
//        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
//        displayImage(imagePath);
        return imagePath;
    }

    /**
     * 4.4版本以上对返回的图片Uri的处理：
     * 返回的Uri是经过封装的，要进行处理才能得到真实路径
     * @param uri 调用系统相册之后返回的Uri
     */
    @TargetApi(19)
    private String handleImageOnKitKat(Uri uri ) {
        String imagePath = null;
//        Uri uri = data.getData();
        Log.d(Constants.TAG,"uri image : " + uri);
        if (DocumentsContract.isDocumentUri(this, uri)) {
            //如果是document类型的Uri，则提供document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];//解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如果是content类型的uri，则进行普通处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是file类型的uri，则直接获取路径
            imagePath = uri.getPath();
        }
//        displayImage(imagePath);
        return imagePath;
    }

    /**
     * 将imagePath指定的图片显示到ImageView上
     */
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            mPicture.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 将Uri转化为路径
     * @param uri 要转化的Uri
     * @param selection 4.4之后需要解析Uri，因此需要该参数
     * @return 转化之后的路径
     */
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    /**
     * 将拍的照片添加到相册
     *
     * @param uri 拍的照片的Uri
     */
    private void galleryAddPic(Uri uri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(uri);
        sendBroadcast(mediaScanIntent);
    }




}
