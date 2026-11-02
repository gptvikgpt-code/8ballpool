package ru.pooltrainer.overlay;
import android.app.*;import android.os.*;import android.content.*;import android.graphics.Color;import android.provider.Settings;import android.net.Uri;import android.view.*;import android.widget.*;
public class MainActivity extends Activity {
 private static final int CAPTURE_REQUEST=101;
 public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(0xff10131a);getWindow().setNavigationBarColor(0xff10131a);LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(40,55,40,25);root.setBackgroundColor(0xff10131a);setContentView(root);
 TextView title=new TextView(this);title.setText("🎱 POOL TRAINER RU");title.setTextColor(Color.WHITE);title.setTextSize(26);root.addView(title);
 TextView info=new TextView(this);info.setText("\nАвтоматическое распознавание шаров через захват экрана. Линии прицеливания и отскоков поверх игры.\n");info.setTextColor(0xffb8c5d6);info.setTextSize(16);root.addView(info);
 Button start=button("▶ Включить поверх игры");root.addView(start);start.setOnClickListener(v->{if(!Settings.canDrawOverlays(this)){Intent i=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName()));startActivity(i);}else{startService(new Intent(this,OverlayService.class));Toast.makeText(this,"Панель включена. Открой 8 Ball Pool",Toast.LENGTH_LONG).show();}});
 Button auto=button("◎ Автораспознавание шаров");root.addView(auto);auto.setOnClickListener(v->{if(!Settings.canDrawOverlays(this)){startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));return;}android.media.projection.MediaProjectionManager mp=(android.media.projection.MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);startActivityForResult(mp.createScreenCaptureIntent(),CAPTURE_REQUEST);});
 Button stop=button("■ Выключить панель");root.addView(stop);stop.setOnClickListener(v->stopService(new Intent(this,OverlayService.class));stopService(new Intent(this,CaptureService.class)));
 TextView note=new TextView(this);note.setText("\nТёмная тема включена. Автораспознавание экспериментальное. Разрешение на захват экрана выдаётся Android отдельно.");note.setTextColor(0xff94a3b8);root.addView(note);
 }
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==CAPTURE_REQUEST&&result==RESULT_OK&&data!=null){startService(new Intent(this,OverlayService.class));Intent service=new Intent(this,CaptureService.class);service.putExtra("resultCode",result);service.putExtra("resultData",data);startForegroundService(service);Toast.makeText(this,"Захват включён — открой игру",Toast.LENGTH_LONG).show();}}
 Button button(String s){Button b=new Button(this);b.setText(s);b.setTextColor(Color.WHITE);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff176b65));return b;}
}
