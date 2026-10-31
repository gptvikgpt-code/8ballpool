package ru.pooltrainer.overlay;
import android.app.*;import android.os.*;import android.content.*;import android.graphics.Color;import android.provider.Settings;import android.net.Uri;import android.view.*;import android.widget.*;
public class MainActivity extends Activity {
 public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(0xff10131a);getWindow().setNavigationBarColor(0xff10131a);LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(40,55,40,25);root.setBackgroundColor(0xff10131a);setContentView(root);
 TextView title=new TextView(this);title.setText("🎱 POOL TRAINER RU");title.setTextColor(Color.WHITE);title.setTextSize(26);root.addView(title);
 TextView info=new TextView(this);info.setText("\nРучные линии прицеливания поверх игры. Перетаскивай точки для настройки траектории.\n");info.setTextColor(0xffb8c5d6);info.setTextSize(16);root.addView(info);
 Button start=button("▶ Включить поверх игры");root.addView(start);start.setOnClickListener(v->{if(!Settings.canDrawOverlays(this)){Intent i=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName()));startActivity(i);}else{startService(new Intent(this,OverlayService.class));Toast.makeText(this,"Панель включена. Открой 8 Ball Pool",Toast.LENGTH_LONG).show();}});
 Button stop=button("■ Выключить панель");root.addView(stop);stop.setOnClickListener(v->stopService(new Intent(this,OverlayService.class)));
 TextView note=new TextView(this);note.setText("\nТёмная тема включена. Это ручной тренажёр, без автоматического распознавания шаров.");note.setTextColor(0xff94a3b8);root.addView(note);
 }
 Button button(String s){Button b=new Button(this);b.setText(s);b.setTextColor(Color.WHITE);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff176b65));return b;}
}
