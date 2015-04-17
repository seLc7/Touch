package cheng.touch;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by Cheng on 2015/4/13.
 */
public class MyService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("out", "Service onCreate");

        Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/20150402_093240.png").copy(Bitmap.Config.ARGB_8888, true);

        Paint paint = new Paint();
        paint.setColor(Color.RED);// 设置红色

        Canvas canvas = new Canvas(bitmap);
        canvas.drawCircle(820, 720, 25, paint);
        canvas.save(Canvas.ALL_SAVE_FLAG );
        canvas.restore();

        File f = new File("/sdcard/0.png");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            Log.i("out", "done");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
