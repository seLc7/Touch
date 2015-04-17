package cheng.touch;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class MainActivity extends Activity implements Runnable {

    TextView touchView;
    int oldX, oldY, newX, newY;
    static Handler handler;

    private static final int DONE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        touchView = (TextView) findViewById(R.id.touch_area);

        touchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case (MotionEvent.ACTION_DOWN):
                        display("ACTION_DOWN", event);
                        setOldCoordinate(event);
                        break;
                    case (MotionEvent.ACTION_UP):
                        display("ACTION_UP", event);
                        setNewCoordinate(event);
                        Thread thread = new Thread(MainActivity.this);
                        thread.start();
                        break;
                    case (MotionEvent.ACTION_MOVE):
                        display("ACTION_MOVE", event);
                        break;
                }
                return true;
            }
        });

        handler = new Handler() {

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case DONE:
                        Toast.makeText(getApplicationContext(), "标记完成", Toast.LENGTH_SHORT).show();
                }
            }
        };

    }

    private void display(String eventType, MotionEvent event) {
        /*int x = (int) event.getX(); // 相对坐标
        int y = (int) event.getY();*/
        float pressure = event.getPressure();
        float size = event.getSize();
        int RawX = (int) event.getRawX(); // 绝对坐标
        int RawY = (int) event.getRawY();
        String msg = "";
        msg += "事件类型：" + eventType + "\n";
        // msg += "相对坐标："+String.valueOf(x)+","+String.valueOf(y)+"\n";
        msg += "绝对坐标：" + String.valueOf(RawX) + "," + String.valueOf(RawY) + "\n";
        msg += "触点压力：" + String.valueOf(pressure) + "， ";
        msg += "触点尺寸：" + String.valueOf(size) + "\n";
        touchView.setText(msg);
    }

    /*获取down触点坐标*/
    private void setOldCoordinate(MotionEvent event) {
        oldX = (int) event.getRawX();
        oldY = (int) event.getRawY();
    }

    /*获取up触点坐标*/
    private void setNewCoordinate(MotionEvent event) {
        newX = (int) event.getRawX();
        newY = (int) event.getRawY();

    }

    /*绘图进程*/
    @Override
    public void run() {
        Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/0.png").copy(Bitmap.Config.ARGB_8888, true);

        Paint paint = new Paint();
        paint.setColor(Color.RED);// 设置红色

        Canvas canvas = new Canvas(bitmap);

        if (newX == oldX && newY == oldY) {
            canvas.drawCircle(newX, newY, 25, paint); // 绘制点
        } else {
            paint.setStrokeWidth((float) 20.0);
            canvas.drawLine(oldX, oldY, newX, newY, paint);  // 绘制线
        }

        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();


        File f = new File("/sdcard/0.png");
        try {
            FileOutputStream fos = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            Log.i("out", "done");

            Message msg = new Message(); // handler 弹出toast
            msg.what = DONE;
            handler.sendMessage(msg);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /* private int processHistory(MotionEvent event)
    {
        int historySize = event.getHistorySize();
        for (int i = 0; i < historySize; i++) {
            long time = event.getHistoricalEventTime(i);
            float pressure = event.getHistoricalPressure(i);
            float x = event.getHistoricalX(i);
            float y = event.getHistoricalY(i);
            float size = event.getHistoricalSize(i);
            // 处理过程…
        }
        return historySize;
    }*/

}
