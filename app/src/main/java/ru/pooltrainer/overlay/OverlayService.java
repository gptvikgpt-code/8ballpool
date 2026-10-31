package ru.pooltrainer.overlay;
import android.app.*;import android.content.*;import android.graphics.*;import android.os.*;import android.view.*;import android.widget.*;
public class OverlayService extends Service {
 WindowManager wm;View overlay;public android.os.IBinder onBind(Intent i){return null;}
 public int onStartCommand(Intent i,int flags,int id){if(overlay!=null)return START_STICKY;wm=(WindowManager)getSystemService(WINDOW_SERVICE);overlay=new AimView(this);WindowManager.LayoutParams p=new WindowManager.LayoutParams(-1,-1,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);wm.addView(overlay,p);return START_STICKY;}
 public void onDestroy(){if(overlay!=null){wm.removeView(overlay);overlay=null;}super.onDestroy();}
 class AimView extends View {Paint pen=new Paint(3);float x1=260,y1=700,x2=650,y2=450;int active=0;boolean edit=true;RectF toggle=new RectF();
 AimView(Context c){super(c);}
 protected void onDraw(Canvas c){super.onDraw(c);float d=getResources().getDisplayMetrics().density;toggle.set(12*d,35*d,155*d,82*d);pen.setColor(0xdd152a33);pen.setStyle(Paint.Style.FILL);c.drawRoundRect(toggle,15*d,15*d,pen);pen.setColor(Color.WHITE);pen.setTextSize(16*d);c.drawText(edit?"🔒 Зафиксировать":"✎ Настроить",22*d,65*d,pen);
 if(!edit)return;pen.setStrokeWidth(3*d);pen.setColor(0xff50f2d0);c.drawLine(x1,y1,x2,y2,pen);float dx=x2-x1,dy=y2-y1,len=(float)Math.hypot(dx,dy);if(len>1){pen.setColor(0x99ffffff);c.drawLine(x2,y2,x2+dx/len*220*d,y2+dy/len*220*d,pen);}
 pen.setStyle(Paint.Style.STROKE);pen.setStrokeWidth(3*d);pen.setColor(0xfff5c451);c.drawCircle(x1,y1,17*d,pen);pen.setColor(0xff50f2d0);c.drawCircle(x2,y2,17*d,pen);pen.setStyle(Paint.Style.FILL);
 }
 public boolean onTouchEvent(android.view.MotionEvent e){float x=e.getX(),y=e.getY();int a=e.getActionMasked();if(a==MotionEvent.ACTION_DOWN){if(toggle.contains(x,y)){edit=!edit;invalidate();return true;}if(!edit)return false;active=Math.hypot(x-x1,y-y1)<90?1:Math.hypot(x-x2,y-y2)<90?2:0;return active!=0;}if(a==MotionEvent.ACTION_MOVE&&active!=0){if(active==1){x1=x;y1=y;}else{x2=x;y2=y;}invalidate();return true;}if(a==MotionEvent.ACTION_UP){active=0;return true;}return false;}
 }
}
