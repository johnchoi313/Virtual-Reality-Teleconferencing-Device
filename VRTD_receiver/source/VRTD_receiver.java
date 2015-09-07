import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.video.*; 
import processing.serial.*; 
import cc.arduino.*; 
import controlP5.*; 
import oscP5.*; 
import netP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class VRTD_receiver extends PApplet {

/* --- PRE SETUP --- */







boolean initialized = false;
int w = 1280, h = 480;

//Open Sound Control Variables 
OscP5 oscP5;

//controlP5 variables
ControlP5 cp5;
DropdownList serialPortDL, webcamLDL, webcamRDL;
int serialPortWidth = 100;
int webcamWidth = 250;
int itemHeight = 20;
int elements = 20;
int margins = 20;

//Arduino variables
Arduino arduino;
String serialPorts[];
boolean arduinoConnected = false;
int xPin = 9; int yPin = 8; int zPin = 10;
float xRot, yRot, zRot = 90.0f; //y = base, x = mid, z = top

//Webcam variables
Capture webcamL;
Capture webcamR;
String camList[];
boolean webcamLConnected = false;
boolean webcamRConnected = false;

/* ----- SETUP ----- */ 
public void setup () {
  //set up screen
  w = displayWidth; h = displayHeight;
  size (w, h);
  imageMode(CENTER);
   
  //---create osc comms---\\
  oscP5 = new OscP5(this,5000);
 
  //---create GUI---\\
  cp5 = new ControlP5(this);
  //setup Arduino
  serialPorts = Arduino.list();
  serialPortDL = cp5.addDropdownList("Serial Port");
  serialPortDL.setPosition((w-serialPortWidth)/2, margins);
  serialPortDL.setItemHeight(itemHeight);
  serialPortDL.setSize(serialPortWidth,itemHeight*elements);
  for(int i=0; i < serialPorts.length; ++i) {
    serialPortDL.addItem(serialPorts[i], i);
  }
  //setup cameras
  camList = Capture.list();  
  webcamLDL = cp5.addDropdownList("Left Camera");
  webcamLDL.setPosition(margins,margins);
  webcamLDL.setItemHeight(itemHeight);
  webcamLDL.setSize(webcamWidth,itemHeight*elements);
  webcamRDL = cp5.addDropdownList("Right Camera");
  webcamRDL.setPosition(w-webcamWidth-margins, margins);
  webcamRDL.setItemHeight(itemHeight);
  webcamRDL.setSize(webcamWidth,itemHeight*elements);
  for(int i = 0; i < camList.length; ++i) {
    webcamLDL.addItem(camList[i],i);
    webcamRDL.addItem(camList[i],i);
  }
}

/* --- MAIN LOOP --- */
public void draw () {  
  if(!initialized) {
    initialized = true;
    frame.setLocation(0,0);
  }
  background(128);

  //---update webcam info---\\
  if(webcamLConnected) {
    if (webcamL.available()) { webcamL.read(); }
    image (webcamL, w*.25f, h/2, w/2, h);
  }
  if(webcamRConnected) {
    if (webcamR.available()) { webcamR.read(); }
    image (webcamR, w*.75f, h/2, w/2, h);
  }
  //---update servo info---\\
  if(arduinoConnected) {
    arduino.servoWrite(xPin,PApplet.parseInt(xRot));
    arduino.servoWrite(yPin,PApplet.parseInt(yRot));
    arduino.servoWrite(zPin,PApplet.parseInt(zRot));
  }
}

/* ---- HELPERS ---- */
public void oscEvent(OscMessage oscMessage) {  
  xRot = oscMessage.get(0).floatValue();
  yRot = oscMessage.get(1).floatValue();
  zRot = oscMessage.get(2).floatValue();
}
public void controlEvent(ControlEvent event) { 
  // check if the Event was triggered from a ControlGroup
  if (event.isGroup()) {
    println("event from group : "+event.getGroup().getValue()+" from "+event.getGroup());
  } else if (event.isController()) {
    int val = PApplet.parseInt(event.getController().getValue());
    String controller = event.getController().getName();
    //get the arduino
    if (controller.equals("Serial Port")) {
      arduino = new Arduino(this, serialPorts[val], 57600);
      println("Arduino connected at "+serialPorts[val]);
      arduino.pinMode(xPin, Arduino.SERVO);
      arduino.pinMode(yPin, Arduino.SERVO);
      arduino.pinMode(zPin, Arduino.SERVO);
      arduinoConnected = true;
    }
    //get the left webcam
    if (controller.equals("Left Camera")) {
      webcamL = new Capture(this, camList[val]);
      println("Left Webcam is "+camList[val]);
      webcamLConnected = true;
      webcamL.start();
    }
    //get the right webcam
    if (controller.equals("Right Camera")) {
      webcamR = new Capture(this, camList[val]);
      println("Right Webcam is "+camList[val]);
      webcamRConnected = true;
      webcamR.start();
    } 
  }
}


  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "VRTD_receiver" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
