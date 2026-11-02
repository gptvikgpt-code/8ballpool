package ru.pooltrainer.overlay;
import android.app.Service;
import android.content.Intent;
import android.graphics.*;
import android.os.IBinder;
import android.view.*;
import android.widget.TextView;
import android.content.Context;

public class OverlayService extends Service {
 private WindowManager wm;
 private AimView aim;
 private TextView control;
 private WindowManager.LayoutParams aimParams;
 private boolean editing=false;
 private static final int BASE=WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
 @Override public IBinder onBind(Intent i){return null;}
 @Override public int onStartCommand(Intent intent,int flags,int startId){
  if(aim!=null)return START_STICKY;
  wm=(WindowManager)getSystemService(WINDOW_SERVICE);
  aim=new AimView(this);
  aimParams=new WindowManager.LayoutParams(-1,-1,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,BASE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,PixelFormat.TRANSLUCENT);
  wm.addView(aim,aimParams);
  control=new TextView(this);
  control.setText("🎱 Настроить");
  control.setTextSize(14);
  control.setTextColor(Color.WHITE);
  control.setGravity(Gravity.CENTER);
  control.setBackgroundColor(0xee173b42);
  int d=(int)getResources().getDisplayMetrics().density;
  WindowManager.LayoutParams buttonParams=new WindowManager.LayoutParams(132*d,48*d,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);
  buttonParams.gravity=Gravity.TOP|Gravity.START;
  buttonParams.x=12*d;buttonParams.y=100*d;
  wm.addView(control,buttonParams);
  control.setOnClickListener(v->{
   editing=!editing;
   aimParams.flags=BASE | (editing?0:WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
   wm.updateViewLayout(aim,aimParams);
   control.setText(editing?"✓ Играть":"🎱 Настроить");
   aim.invalidate();
  });
  return START_STICKY;
 }
 @Override public void onDestroy(){
  if(control!=null){wm.removeView(control);control=null;}
  if(aim!=null){wm.removeView(aim);aim=null;}
  super.onDestroy();
 }
 class AimView extends View {
  Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  float x1=260,y1=700,x2=650,y2=450;
  int selected=0;
  AimView(Context context){super(context);}
  @Override protected void onDraw(Canvas c){
   float d=getResources().getDisplayMetrics().density;
   p.setStrokeWidth(3*d);
   p.setColor(0xff50f2d0);
   c.drawLine(x1,y1,x2,y2,p);
   float dx=x2-x1,dy=y2-y1,length=(float)Math.hypot(dx,dy);
   if(length>1){p.setColor(0x99ffffff);c.drawLine(x2,y2,x2+dx/length*220*d,y2+dy/length*220*d,p);}
   if(editing){
    p.setStyle(Paint.Style.STROKE);
    p.setColor(0xfff5c451);c.drawCircle(x1,y1,18*d,p);
    p.setColor(0xff50f2d0);c.drawCircle(x2,y2,18*d,p);
    p.setStyle(Paint.Style.FILL);
   }
  }
  @Override public boolean onTouchEvent(MotionEvent event){
   if(!editing)return false;
   float x=event.getX(),y=event.getY();
   switch(event.getActionMasked()){
    case MotionEvent.ACTION_DOWN:
     selected=Math.hypot(x-x1,y-y1)<Math.hypot(x-x2,y-y2)?1:2;
     return true;
    case MotionEvent.ACTION_MOVE:
     if(selected==1){x1=x;y1=y;}else if(selected==2){x2=x;y2=y;}
     invalidate();return true;
    case MotionEvent.ACTION_UP:
    case MotionEvent.ACTION_CANCEL:selected=0;return true;
   }
   return true;
  }
 }
}
