package cheng.touch;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
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
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends Activity implements Runnable {

    private TextView touchView;
    // oldX,oldY:点下去获取的坐标；newX,newY：抬起获取的坐标；touch：移动中的坐标；mX,mY：公共存储坐标，存储运动轨迹的起始坐标。
    private int oldX, oldY, newX, newY, touchX, touchY, mX, mY;
    private static Handler handler;
    private Canvas canvas = new Canvas();
    private Bitmap bitmap = null;
    private Paint paint = new Paint();
    private Path path = new Path();

    private static final int DONE = 1;

    private ArrayList pointRGBList = new ArrayList(); // 点颜色列表
    private ArrayList lineRGBList = new ArrayList(); // 轨迹颜色列表
    private int count = 0; // 自增量，用于获取colorlist里的颜色
    private static final int step = 5 * 60; // 颜色的渐变点个数；5min,每秒分配一个颜色
    private static final int[] beginPoint = {255, 0, 0};  // 起点颜色
    private static final int[] endPoint = {0, 255, 0}; // 终点颜色
    private static final int[] beginLine = {0, 255, 0};
    private static final int[] endLine = {0, 0, 255};
    private long beginTime, endTime, currentTime;
    Date date; // 获取系统时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        date = new Date(System.currentTimeMillis());
        beginTime = date.getTime();
        endTime = beginTime + step * 1000;

        bitmap = BitmapFactory.decodeFile("/sdcard/0.png").copy(Bitmap.Config.ARGB_8888, true);
        canvas = new Canvas(bitmap);

        touchView = (TextView) findViewById(R.id.touch_area);

        touchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case (MotionEvent.ACTION_DOWN):
                        display("ACTION_DOWN", event);
                        getCount();
                        touchDown(event);
                        break;
                    case (MotionEvent.ACTION_UP):
                        display("ACTION_UP", event);
                        touchUp(event);
                        Thread thread = new Thread(MainActivity.this);
                        thread.start();
                        break;
                    case (MotionEvent.ACTION_MOVE):
                        display("ACTION_MOVE", event);
                        getCount();
                        touchMove(event);
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

        pointRGBList = shadeColor(beginPoint, endPoint, step);// 获取颜色列表
        lineRGBList = shadeColor(beginLine, endLine, step);
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

    /*手指点下屏幕时调用*/
    private void touchDown(MotionEvent event) {
        path.reset();
        oldX = (int) event.getRawX();
        oldY = (int) event.getRawY();
        mX = oldX;
        mY = oldY;
        path.moveTo(oldX, oldY);
    }

    /*获取up触点坐标*/
    private void touchUp(MotionEvent event) {
        newX = (int) event.getRawX();
        newY = (int) event.getRawY();
    }

    /*手指在屏幕上滑动时调用*/
    private void touchMove(MotionEvent event) {
        touchX = (int) event.getRawX();
        touchY = (int) event.getRawY();

        final float previousX = mX;
        final float previousY = mY;

        final float dx = Math.abs(touchX - previousX);
        final float dy = Math.abs(touchY - previousY);

        int[] f = (int[]) pointRGBList.get(count);


        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(7);
        paint.setARGB(255, f[0], f[1], f[2]);

        //两点之间的距离大于等于3时，生成贝塞尔绘制曲线
        if (dx >= 3 || dy >= 3) {
            //设置贝塞尔曲线的操作点为起点和终点的一半
            float cX = (touchX + previousX) / 2;
            float cY = (touchY + previousY) / 2;

            //二次贝塞尔，实现平滑曲线；previousX, previousY为操作点，cX, cY为终点
            path.quadTo(previousX, previousY, cX, cY);

            canvas.drawPath(path, this.paint);  // 绘制线
            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
            //第二次执行时，第一次结束调用的坐标值将作为第二次调用的初始坐标值
            mX = touchX;
            mY = touchY;
        }
    }

    /*获取颜色点*/
    private void getCount() {
        date = new Date(System.currentTimeMillis());
        currentTime = date.getTime();
        if (currentTime > endTime) {
            beginTime = endTime;
        }
        count = (int) ((currentTime - beginTime) / 1000);
    }

    /*生成两个颜色之间step数量个渐变色*/
    private ArrayList shadeColor(int[] beginColor, int[] endColor, int step) {
        ArrayList<int[]> colorList = new ArrayList<>();

        for (int j = 0; j < step; j++) {
            int f[] = new int[3];
            for (int i = 0; i < 3; i++) {
                //这个就是算法，RGB三色都按同样的算法
                f[i] = beginColor[i] - (beginColor[i] - endColor[i]) * j / step;
            }
            colorList.add(f);
        }
        return colorList;
    }


    /*绘图线程*/
    @Override
    public void run() {

        if (newX == oldX && newY == oldY) {
            int[] f = (int[]) pointRGBList.get(count);
            Log.i("color", f[0] + " " + f[1] + " " + f[2]);

            paint.setStyle(Paint.Style.FILL);
            paint.setARGB(255, f[0], f[1], f[2]); // 设置颜色
            // paint.setColor(Color.RED);// 设置红色
            canvas.drawCircle(newX, newY, 20, paint); // 绘制点
        } else {
            int[] f = (int[]) pointRGBList.get(count);

            paint.setARGB(255, f[0], f[1], f[2]); // 设置颜色
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(newX, newY, 15, paint); // 绘制点
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
}
