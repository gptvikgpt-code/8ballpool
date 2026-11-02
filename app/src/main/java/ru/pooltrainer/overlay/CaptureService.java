package ru.pooltrainer.overlay;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.hardware.display.*;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.*;
import android.os.*;
import android.util.DisplayMetrics;
import java.nio.ByteBuffer;
import java.util.List;

public class CaptureService extends Service {
 public static final String UPDATE="ru.pooltrainer.overlay.BALL_UPDATE";
 private MediaProjection projection;
 private VirtualDisplay display;
 private ImageReader reader;
 private HandlerThread thread;
 private Handler handler;
 private long lastFrame;
 @Override public IBinder onBind(Intent i){return null;}
 @Override public int onStartCommand(Intent i,int flags,int id){
  if(i==null||!i.hasExtra("resultCode")){stopSelf();return START_NOT_STICKY;}
  NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
  nm.createNotificationChannel(new NotificationChannel("capture","Распознавание шаров",NotificationManager.IMPORTANCE_LOW));
  Notification n=new Notification.Builder(this,"capture").setSmallIcon(android.R.drawable.ic_menu_camera)
   .setContentTitle("Pool Trainer RU").setContentText("Распознавание шаров на экране").build();
  startForeground(2,n);
  MediaProjectionManager manager=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
  projection=manager.getMediaProjection(i.getIntExtra("resultCode",0),(Intent)i.getParcelableExtra("resultData"));
  if(projection==null){stopSelf();return START_NOT_STICKY;}
  thread=new HandlerThread("PoolCapture");thread.start();handler=new Handler(thread.getLooper());
  projection.registerCallback(new MediaProjection.Callback(){@Override public void onStop(){stopSelf();}},handler);
  DisplayMetrics m=getResources().getDisplayMetrics();
  int w=m.widthPixels,h=m.heightPixels;
  reader=ImageReader.newInstance(w,h,android.graphics.PixelFormat.RGBA_8888,2);
  reader.setOnImageAvailableListener(r->{
   Image img=r.acquireLatestImage();if(img==null)return;
   try{
    long now=SystemClock.elapsedRealtime();if(now-lastFrame<350)return;lastFrame=now;
    Image.Plane plane=img.getPlanes()[0];ByteBuffer buffer=plane.getBuffer();
    int stride=plane.getRowStride(),pixel=plane.getPixelStride();
    Bitmap padded=Bitmap.createBitmap(stride/pixel,h,Bitmap.Config.ARGB_8888);
    padded.copyPixelsFromBuffer(buffer);
    Bitmap bmp=Bitmap.createBitmap(padded,0,0,w,h);padded.recycle();
    // The game table is approximately x=19%-81%, y=23%-91% in landscape.
    int l=(int)(w*.195),t=(int)(h*.23),rr=(int)(w*.805),b=(int)(h*.91);
    List<BallDetector.Ball> balls=BallDetector.detect(bmp,l,t,rr,b);
    bmp.recycle();
    Intent update=new Intent(UPDATE);update.setPackage(getPackageName());
    float[] coords=new float[balls.size()*3];int k=0;
    for(BallDetector.Ball ball:balls){coords[k++]=ball.x;coords[k++]=ball.y;coords[k++]=ball.cue?1:0;}
    update.putExtra("balls",coords);update.putExtra("width",w);update.putExtra("height",h);
    sendBroadcast(update);
   }catch(Exception ignored){}finally{img.close();}
  },handler);
  display=projection.createVirtualDisplay("PoolTrainer",w,h,m.densityDpi,
   DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,reader.getSurface(),null,handler);
  return START_NOT_STICKY;
 }
 @Override public void onDestroy(){
  if(display!=null)display.release();
  if(reader!=null)reader.close();
  if(projection!=null)projection.stop();
  if(thread!=null)thread.quitSafely();
  super.onDestroy();
 }
}
