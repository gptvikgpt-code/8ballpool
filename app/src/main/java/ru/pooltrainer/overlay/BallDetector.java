package ru.pooltrainer.overlay;

import android.graphics.Bitmap;
import android.graphics.Color;
import java.util.ArrayList;
import java.util.List;

/** Experimental pixel-based ball detector for 8 Ball Pool screenshots.
 * Works in screenshot pixel coordinates, not overlay coordinates.
 * Must be validated against captured game frames before use for automatic aiming.
 */
public final class BallDetector {
 public static final class Ball {
  public final float x,y,radius;
  public final boolean cue;
  Ball(float x,float y,float radius,boolean cue){
   this.x=x;this.y=y;this.radius=radius;this.cue=cue;
  }
 }
 private BallDetector(){}
 public static List<Ball> detect(Bitmap frame,int left,int top,int right,int bottom) {
  List<Ball> out=new ArrayList<>();
  if(frame==null)return out;
  int w=frame.getWidth(),h=frame.getHeight();
  left=Math.max(0,left);top=Math.max(0,top);
  right=Math.min(w,right);bottom=Math.min(h,bottom);
  if(right-left<100||bottom-top<100)return out;
  // Relative to the standard 1536px landscape game screenshot: ball diameter ~28px.
  float scale=(right-left)/950f;
  int r=Math.max(5,Math.round(13*scale));
  int step=Math.max(2,r/3);
  float[] hsv=new float[3];
  for(int y=top+r;y<bottom-r;y+=step){
   for(int x=left+r;x<right-r;x+=step){
    int color=frame.getPixel(x,y);
    Color.colorToHSV(color,hsv);
    boolean bright=hsv[1]<0.18f&&hsv[2]>0.76f;
    boolean colored=(hsv[1]>0.44f&&hsv[2]>0.36f)||
       (hsv[2]<0.22f&&hsv[1]<0.25f);
    if(!bright&&!colored)continue;
    // A ball has a strong circular edge; felt should not.
    int edge=0,background=0,inside=0;
    for(int k=0;k<12;k++){
     double a=k*Math.PI/6;
     int ix=x+(int)(r*0.48*Math.cos(a));
     int iy=y+(int)(r*0.48*Math.sin(a));
     int ox=x+(int)(r*1.36*Math.cos(a));
     int oy=y+(int)(r*1.36*Math.sin(a));
     if(ox<left||ox>=right||oy<top||oy>=bottom)continue;
     int ci=frame.getPixel(ix,iy),co=frame.getPixel(ox,oy);
     Color.colorToHSV(ci,hsv);
     if(hsv[1]<0.22f&&hsv[2]>0.72f || hsv[1]>0.4f&&hsv[2]>0.3f || hsv[2]<0.24f)inside++;
     Color.colorToHSV(co,hsv);
     if(hsv[1]>0.25f&&hsv[0]>175&&hsv[0]<225)background++;
     if(Math.abs(Color.red(ci)-Color.red(co))+
        Math.abs(Color.green(ci)-Color.green(co))+
        Math.abs(Color.blue(ci)-Color.blue(co))>115)edge++;
    }
    if(edge<7||background<6||inside<6)continue;
    boolean duplicate=false;
    for(Ball b:out)if(Math.hypot(b.x-x,b.y-y)<r*1.25){duplicate=true;break;}
    if(duplicate)continue;
    // Cue-ball identity is provisional: white stripes and highlights can resemble it.
    out.add(new Ball(x,y,r,bright));
   }
  }
  return out;
 }
}
