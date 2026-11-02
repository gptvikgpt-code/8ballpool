package ru.pooltrainer.overlay;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

/**
 * Opt-in gesture controller. Intentionally does not send any gestures
 * until a validated table state and explicit local start action exist.
 */
public class AutoPlayService extends AccessibilityService {
 private static volatile AutoPlayService instance;
 private boolean busy;
 @Override protected void onServiceConnected(){super.onServiceConnected();instance=this;}
 @Override public void onAccessibilityEvent(AccessibilityEvent event){}
 @Override public void onInterrupt(){}
 @Override public void onDestroy(){instance=null;super.onDestroy();}
 public static boolean available(){return instance!=null;}
 /** Gesture primitive for future validated practice-mode shots. */
 public static boolean swipe(float fromX,float fromY,float toX,float toY,long durationMs){
  AutoPlayService service=instance;
  if(service==null||service.busy)return false;
  if(!Float.isFinite(fromX)||!Float.isFinite(fromY)||!Float.isFinite(toX)||!Float.isFinite(toY))return false;
  service.busy=true;
  Path path=new Path();path.moveTo(fromX,fromY);path.lineTo(toX,toY);
  GestureDescription.StrokeDescription stroke=new GestureDescription.StrokeDescription(path,0,Math.max(100,Math.min(durationMs,2000)));
  GestureDescription gesture=new GestureDescription.Builder().addStroke(stroke).build();
  boolean sent=service.dispatchGesture(gesture,new GestureResultCallback(){
   @Override public void onCompleted(GestureDescription g){service.busy=false;}
   @Override public void onCancelled(GestureDescription g){service.busy=false;}
  },null);
  if(!sent)service.busy=false;
  return sent;
 }
}