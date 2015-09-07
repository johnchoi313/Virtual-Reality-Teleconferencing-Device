/* --- PRE SETUP --- */
import controlP5.*;
import oscP5.*;
import netP5.*;

boolean initialized = false;
int w = 1280, h = 480;

//Open Sound Control Variables 
OscP5 oscP5;
NetAddress oscReceiver;
boolean oscConnected;

//controlP5 variables
ControlP5 cp5;
Textfield ipField; 
Slider xSen, ySen, zSen;
int sliderSize = 200;
int itemHeight = 20;
int ipWidth = 100;
int fontSize = 16;
int margins = 20;

//rotation variables
float xRot, yRot, zRot = 90.0; //y = base, x = mid, z = top
float xSensitivity = .4; float ySensitivity = .4; float zSensitivity = .4;

/* ----- SETUP ----- */ 
void setup () {
  //set up screen
  w = displayWidth; h = displayHeight;
  size (w, h);
  
  //---create osc comms---\\
  oscP5 = new OscP5(this,4000);
 
  //---create GUI---\\
  cp5 = new ControlP5(this);
  //create IP input field
  ipField = cp5.addTextfield("IP Address");
  ipField.setPosition((w-ipWidth)/2, margins);
  ipField.setFont(createFont("arial",fontSize));
  ipField.setSize(ipWidth,itemHeight);
  ipField.setAutoClear(false);    
  //add sensitivity sliders
  xSen = cp5.addSlider("xSensitivity");
  xSen.setPosition(margins,margins);
  xSen.setSize(sliderSize,margins);
  xSen.setRange(0.0,1.0);
  ySen = cp5.addSlider("ySensitivity");
  ySen.setPosition(margins,margins*3);
  ySen.setSize(sliderSize,margins);
  ySen.setRange(0.0,1.0);
  zSen = cp5.addSlider("zSensitivity");
  zSen.setPosition(margins,margins*5);
  zSen.setSize(sliderSize,margins);
  zSen.setRange(0.0,1.0);
}

/* --- MAIN LOOP --- */
void draw () {  
  if(!initialized) {
    initialized = true;
    frame.setLocation(0,0);
  }
  background(128);
    
  //---update angles---\\  
  xRot += (mouseY - pmouseY)*xSensitivity;
  yRot += (mouseX - pmouseX)*ySensitivity;
  xRot = constrain(xRot,0.0,180.0);  
  yRot = constrain(yRot,0.0,180.0);
  zRot = constrain(zRot,0.0,180.0);

  //---send oscInformation---\\
  if(oscConnected) {
    OscMessage angles = new OscMessage("angles");
    angles.add(xRot);
    angles.add(yRot);
    angles.add(zRot);
    oscP5.send(angles, oscReceiver); 
  }
}

/* ---- HELPERS ---- */
void mouseWheel(MouseEvent e) {
  zRot += e.getAmount()*zSensitivity;
}
void controlEvent(ControlEvent event) { 
  if(event.isAssignableFrom(Textfield.class)) {
    String textField = event.getName();
    String ip = event.getStringValue();  
    if(textField.equals("IP Address")) {
      println("Osc Receiver connected at "+ip);  
      oscReceiver = new NetAddress(ip,5000);
      oscConnected = true;
    }          
  }
}
