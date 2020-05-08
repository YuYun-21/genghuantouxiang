package com.example.yuanjiaotouxiang;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{

    private ImageView iv_icon;
    
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private LayoutInflater inflater;
    private View layout;
    private TextView takePhoto;
    private TextView choosePhoto;
    private TextView cancel;
    private static String path = "/sdcard/DemoHead/";//sd路径
    private Bitmap head;//头像Bitmap

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        intView();
    }
    
    
  private void intView() {
		// TODO Auto-generated method stub
	  iv_icon = (ImageView) findViewById(R.id.iv_head);

	  Bitmap bt = BitmapFactory.decodeFile(path + "head.jpg");//从Sd中找头像，转换成Bitmap
	  if(bt!=null){
		@SuppressWarnings("deprecation")
		Drawable drawable = new BitmapDrawable(bt);//转换成drawable
		iv_icon.setImageDrawable(drawable);
	  }else{
		/**
		*	如果SD里面没有则需要从服务器取头像，取回来的头像再保存在SD中
		* 
		*/
	  }
      iv_icon.setOnClickListener(new OnClickListener() {

          @Override
          public void onClick(View v) {
          	viewInit();
          }
      });
	}

  	//初始化控件方法
    public void viewInit() {
    builder = new AlertDialog.Builder(this);//创建对话框
    inflater = getLayoutInflater();
    layout = inflater.inflate(R.layout.activity_dialog_select, null);//获取自定义布局
    builder.setView(layout);//设置对话框的布局
    dialog = builder.create();//生成最终的对话框
    
    dialog.setCanceledOnTouchOutside(true);//设置点击Dialog外部任意区域关闭
    dialog.show();//显示对话框
    
    takePhoto = (TextView) layout.findViewById(R.id.photograph);
    choosePhoto = (TextView) layout.findViewById(R.id.photo);
    cancel = (TextView) layout.findViewById(R.id.cancel);
    //设置监听
    takePhoto.setOnClickListener(this);
    choosePhoto.setOnClickListener(this);
    cancel.setOnClickListener(this);
}
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.photograph://调用相机拍照
            	//最好用try/catch包裹一下，防止因为用户未给应用程序开启相机权限，而使程序崩溃
                try {
                    Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//开启相机应用程序获取并返回图片（capture：俘获）
                    intent2.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                            "head.jpg")));//指明存储图片或视频的地址URI
                    startActivityForResult(intent2, 2);//采用ForResult打开
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "相机无法启动，请先开启相机权限", Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
                break;
            case R.id.photo://从相册里面取照片
                Intent intent1 = new Intent(Intent.ACTION_PICK, null);//返回被选中项的URI
                intent1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");//得到所有图片的URI
                startActivityForResult(intent1, 1);
                dialog.dismiss();
                break;
            case R.id.cancel:
                dialog.dismiss();//关闭对话框
                break;
            default:break;
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            //从相册里面取相片的返回结果
            case 1:
                if (resultCode == RESULT_OK) {
                    cropPhoto(data.getData());//裁剪图片
                }
 
                break;
            //相机拍照后的返回结果
            case 2:
                if (resultCode == RESULT_OK) {
                    File temp = new File(Environment.getExternalStorageDirectory()
                            + "/head.jpg");
                    cropPhoto(Uri.fromFile(temp));//裁剪图片
                }
 
                break;
            //调用系统裁剪图片后
            case 3:
                if (data != null) {
                    Bundle extras = data.getExtras();
                    head = extras.getParcelable("data");
                    if (head != null) {
                        /**
                         	* 上传服务器代码
                         */
                        setPicToView(head);//保存在SD卡中
                        iv_icon.setImageBitmap(head);//用ImageView显示出来
                    }
                }
                break;
            default:
                break;
 
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
 
    ;
 
    /**
                * 调用系统的裁剪
     *
     * @param uri
     */
    public void cropPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        //找到指定URI对应的资源图片
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // aspectX aspectY 是裁剪框宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);// 输出图片大小
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        //进入系统裁剪图片的界面
        startActivityForResult(intent, 3);
        
    }
 
    private void setPicToView(Bitmap mBitmap) {
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd卡是否可用
            return;
        }
        FileOutputStream b = null;
        File file = new File(path);
        file.mkdirs();// 创建以此File对象为名（path）的文件夹
        String fileName = path + "head.jpg";//图片名字
        try {
            b = new FileOutputStream(fileName);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件（compress：压缩）
 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭流
                b.flush();
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
 
        }
    }
}