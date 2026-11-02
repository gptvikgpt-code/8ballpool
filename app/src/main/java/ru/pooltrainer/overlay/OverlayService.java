package ru.pooltrainer.overlay;
import android.app.Service;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
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
 private boolean autoPlaying=false;
 private TextView autoControl;
 private long lastShot=0;
 private float[] previousBalls;
 private int stableFrames=0;
 private final BroadcastReceiver receiver=new BroadcastReceiver(){@Override public void onReceive(Context ctx,Intent i){if(aim!=null){aim.balls=i.getFloatArrayExtra("balls");aim.sourceW=i.getIntExtra("width",1);aim.sourceH=i.getIntExtra("height",1);aim.invalidate();if(autoPlaying)processAutoFrame(aim.balls,aim.sourceW,aim.sourceH);}}};
 private static final int BASE=WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
 @Override public IBinder onBind(Intent i){return null;}
 @Override public int onStartCommand(Intent intent,int flags,int startId){
  if(aim!=null)return START_STICKY;
  wm=(WindowManager)getSystemService(WINDOW_SERVICE);
  aim=new AimView(this);
  aimParams=new WindowManager.LayoutParams(-1,-1,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,BASE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,PixelFormat.TRANSLUCENT);
  wm.addView(aim,aimParams);
  registerReceiver(receiver,new IntentFilter(CaptureService.UPDATE),Context.RECEIVER_NOT_EXPORTED);
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
  autoControl=new TextView(this);
  autoControl.setText("▶ Автоигра");
  autoControl.setTextSize(14);autoControl.setTextColor(Color.WHITE);
  autoControl.setGravity(Gravity.CENTER);autoControl.setBackgroundColor(0xee176b65);
  WindowManager.LayoutParams ap=new WindowManager.LayoutParams(132*d,48*d,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);
  ap.gravity=Gravity.TOP|Gravity.START;ap.x=150*d;ap.y=100*d;
  wm.addView(autoControl,ap);
  autoControl.setOnClickListener(v->{
   if(!autoPlaying&&!AutoPlayService.available()){
    android.widget.Toast.makeText(this,"Включи Pool Trainer RU в настройках специальных возможностей",android.widget.Toast.LENGTH_LONG).show();
    Intent settings=new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
    settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(settings);return;
   }
   autoPlaying=!autoPlaying;stableFrames=0;previousBalls=null;
   autoControl.setText(autoPlaying?"■ Стоп":"▶ Автоигра");
   autoControl.setBackgroundColor(autoPlaying?0xeea33030:0xee176b65);
  });
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
  try{unregisterReceiver(receiver);}catch(Exception ignored){}
  if(autoControl!=null){wm.removeView(autoControl);autoControl=null;}
  if(control!=null){wm.removeView(control);control=null;}
  if(aim!=null){wm.removeView(aim);aim=null;}
  super.onDestroy();
 }
 private void processAutoFrame(float[] balls,int w,int h){
  if(!autoPlaying||balls==null||w<100||h<100||balls.length<6)return;
  long now=android.os.SystemClock.elapsedRealtime();
  if(now-lastShot<7000)return;
  if(previousBalls==null||previousBalls.length!=balls.length){previousBalls=balls.clone();stableFrames=0;return;}
  float delta=0;
  for(int i=0;i<balls.length;i+=3)delta+=Math.hypot(balls[i]-previousBalls[i],balls[i+1]-previousBalls[i+1]);
  previousBalls=balls.clone();
  if(delta>balls.length/3f*2.5f){stableFrames=0;return;}
  if(++stableFrames<6)return;
  stableFrames=0;
  int cue=-1;int count=0;
  for(int i=0;i<balls.length;i+=3)if(balls[i+2]>.5f){cue=i;count++;}
  // Do not fire if cue-ball identification is ambiguous.
  if(count!=1)return;
  float cx=balls[cue],cy=balls[cue+1];
  float best=Float.POSITIVE_INFINITY,tx=0,ty=0;
  for(int i=0;i<balls.length;i+=3){
   if(i==cue)continue;
   float dx=balls[i]-cx,dy=balls[i+1]-cy;
   float distance=(float)Math.hypot(dx,dy);
   if(distance<30||distance>best)continue;
   best=distance;tx=balls[i];ty=balls[i+1];
  }
  if(!Float.isFinite(best)||best==Float.POSITIVE_INFINITY)return;
  // Experimental direct shot: pull the cue back opposite the nearest target.
  float dx=(tx-cx)/best,dy=(ty-cy)/best;
  float startX=cx-dx*55,startY=cy-dy*55;
  float endX=cx-dx*155,endY=cy-dy*155;
  if(startX<0||startX>w||startY<0||startY>h||endX<0||endX>w||endY<0||endY>h)return;
  if(AutoPlayService.swipe(startX,startY,endX,endY,500))lastShot=now;
 }
 class AimView extends View {
  Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  float x1=260,y1=700,x2=650,y2=450;
  int selected=0;
  float[] balls;
  int sourceW=1,sourceH=1;
  float left=95,top=240,right=980,bottom=1600;
  AimView(Context context){super(context);}
  @Override protected void onDraw(Canvas c){
   float d=getResources().getDisplayMetrics().density;
   // The table bounds can be adjusted by moving its corner handles in edit mode.
   left=Math.max(8*d,Math.min(left,getWidth()-120*d));
   right=Math.max(left+100*d,Math.min(right,getWidth()-8*d));
   top=Math.max(100*d,Math.min(top,getHeight()-120*d));
   bottom=Math.max(top+100*d,Math.min(bottom,getHeight()-8*d));
   p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1*d);p.setColor(0x6680d9c7);
   c.drawRect(left,top,right,bottom,p);
   for(int i=1;i<6;i++){float gx=left+(right-left)*i/6f;c.drawLine(gx,top,gx,bottom,p);}
   for(int i=1;i<12;i++){float gy=top+(bottom-top)*i/12f;c.drawLine(left,gy,right,gy,p);}
   p.setStyle(Paint.Style.FILL);
   p.setStrokeWidth(3*d);p.setColor(0xff50f2d0);
   c.drawLine(x1,y1,x2,y2,p);
   float dx=x2-x1,dy=y2-y1,len=(float)Math.hypot(dx,dy);
   if(len>1){
    float vx=dx/len,vy=dy/len,px=x2,py=y2;
    p.setColor(0xfff5c451);p.setStrokeWidth(2*d);
    // Ray/rectangle intersections: reflect angle of incidence at each cushion.
    for(int bounce=0;bounce<5;bounce++){
     float tx=Float.POSITIVE_INFINITY,ty=Float.POSITIVE_INFINITY;
     if(vx>0.0001f)tx=(right-px)/vx;else if(vx< -0.0001f)tx=(left-px)/vx;
     if(vy>0.0001f)ty=(bottom-py)/vy;else if(vy< -0.0001f)ty=(top-py)/vy;
     if(tx<0)tx=Float.POSITIVE_INFINITY;if(ty<0)ty=Float.POSITIVE_INFINITY;
     float t=Math.min(tx,ty);
     if(!Float.isFinite(t))break;
     float nx=px+vx*t,ny=py+vy*t;
     c.drawLine(px,py,nx,ny,p);
     px=nx;py=ny;
     if(Math.abs(tx-ty)<0.01f){vx=-vx;vy=-vy;}
     else if(tx<ty)vx=-vx;else vy=-vy;
     px=Math.max(left+0.01f,Math.min(right-0.01f,px+vx*0.02f));
     py=Math.max(top+0.01f,Math.min(bottom-0.01f,py+vy*0.02f));
    }
   }
   if(balls!=null&&sourceW>0&&sourceH>0){
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2*d);p.setColor(0xffffd54f);
    float sx=getWidth()/(float)sourceW,sy=getHeight()/(float)sourceH;
    for(int i=0;i+2<balls.length;i+=3){
     float bx=balls[i]*sx,by=balls[i+1]*sy;
     c.drawCircle(bx,by,14*d,p);
     if(balls[i+2]>0.5f){p.setColor(0xffffffff);c.drawCircle(bx,by,18*d,p);p.setColor(0xffffd54f);}
    }
    p.setStyle(Paint.Style.FILL);
   }
   if(editing){
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3*d);
    p.setColor(0xfff5c451);c.drawCircle(x1,y1,18*d,p);
    p.setColor(0xff50f2d0);c.drawCircle(x2,y2,18*d,p);
    p.setColor(0xffffffff);c.drawCircle(left,top,15*d,p);c.drawCircle(right,bottom,15*d,p);
    p.setStyle(Paint.Style.FILL);
   }
  }
  @Override public boolean onTouchEvent(MotionEvent event){
   if(!editing)return false;
   float x=event.getX(),y=event.getY();
   switch(event.getActionMasked()){
    case MotionEvent.ACTION_DOWN:
     if(Math.hypot(x-left,y-top)<75)selected=3;
     else if(Math.hypot(x-right,y-bottom)<75)selected=4;
     else selected=Math.hypot(x-x1,y-y1)<Math.hypot(x-x2,y-y2)?1:2;
     return true;
    case MotionEvent.ACTION_MOVE:
     if(selected==1){x1=x;y1=y;}else if(selected==2){x2=x;y2=y;}else if(selected==3){left=x;top=y;}else if(selected==4){right=x;bottom=y;}
     invalidate();return true;
    case MotionEvent.ACTION_UP:
    case MotionEvent.ACTION_CANCEL:selected=0;return true;
   }
   return true;
  }
 }
}
