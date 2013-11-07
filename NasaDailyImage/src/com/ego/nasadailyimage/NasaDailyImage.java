package com.ego.nasadailyimage;

import java.io.IOException;

import com.ego.nasadailyimage.exceptions.NasaDailyImageException;
import com.ego.nasadailyimage.handlers.IotdHandler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class NasaDailyImage extends Activity {

   private Handler handler;
private Bitmap bitmap;
   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_nasa_daily_image);
      handler = new Handler();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.nasa_daily_image, menu);
      return true;
   }

   private void resetDisplay(String title, String date, Bitmap image, String description) {
      TextView tvTitle = (TextView) findViewById(R.id.daily_image_title);
      tvTitle.setText(title);

      TextView tvDate = (TextView) findViewById(R.id.daily_image_updated_date);
      tvDate.setText(date);

      ImageView imageView = (ImageView) findViewById(R.id.daily_image_picture);
      imageView.setImageBitmap(image);

      TextView tvDescription = (TextView) findViewById(R.id.daily_image_description);
      tvDescription.setText(description);
   }

   public void refreshContent(View view) {
      final ProgressDialog progressDialog = ProgressDialog.show(this, "Loading", "Loading the RSS feed");
      Thread t = new Thread() {
         public void run() {
            try {
               final IotdHandler iotdHandler = new IotdHandler();
               iotdHandler.processFeed();
               bitmap = iotdHandler.getImage();
               handler.post(new Runnable() {
                  public void run() {
                     resetDisplay(iotdHandler.getTitle(), iotdHandler.getDate(), iotdHandler.getImage(),
                           iotdHandler.getDescription());
                     progressDialog.dismiss();
                  }
               });
            } catch (NasaDailyImageException e1) {
               e1.printStackTrace();
               try {
                  Thread.sleep(3000);
               } catch (InterruptedException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
               handler.post(new Runnable() {
                  public void run() {
                     resetDisplay("Some title", "What a date", null, "Oh my! Now a description too?");
                     progressDialog.dismiss();
                  }
               });
            }
         }
      };
      t.start();
   }

   public void setWallpaper(View view) {
      WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
      try {
         if(bitmap == null) {
            bitmap = BitmapFactory.decodeResource(this.getResources(),
                  R.drawable.test_image);
         }
         wallpaperManager.setBitmap(bitmap);
         Toast.makeText(this, "Wallpaper Set", Toast.LENGTH_SHORT).show();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         Toast.makeText(this, "Wallpaper Set", Toast.LENGTH_SHORT).show();
      }
   }
}
