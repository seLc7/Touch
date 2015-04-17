package cheng.touch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity {

    TextView labelView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        labelView = (TextView)findViewById(R.id.event_label);
        TextView touchView = (TextView)findViewById(R.id.touch_area);
        final TextView historyView = (TextView)findViewById(R.id.history_label);

        touchView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case (MotionEvent.ACTION_DOWN):
                        display("ACTION_DOWN", event);
                        break;
                    case (MotionEvent.ACTION_UP):
                        int historySize = processHistory(event);
                        historyView.setText("历史数据量：" + historySize);
                        display("ACTION_UP", event);
                        break;
                    case (MotionEvent.ACTION_MOVE):
                        display("ACTION_MOVE", event);
                        break;
                }
                return true;
            }
        });

        Button btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MyService.class);
                startService(intent);
            }
        });
    }

    private void display(String eventType, MotionEvent event){
        int x = (int)event.getX();
        int y = (int)event.getY();
        float pressure = event.getPressure();
        float size = event.getSize();
        int RawX = (int)event.getRawX();
        int RawY = (int)event.getRawY();
        String msg = "";
        msg += "事件类型：" + eventType + "\n";
        msg += "相对坐标："+String.valueOf(x)+","+String.valueOf(y)+"\n";
        msg += "绝对坐标："+String.valueOf(RawX)+","+String.valueOf(RawY)+"\n";
        msg += "触点压力："+String.valueOf(pressure)+"， ";
        msg += "触点尺寸："+String.valueOf(size)+"\n";
        labelView.setText(msg);
    }
    private int processHistory(MotionEvent event)
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
    }

}
