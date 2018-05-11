#include <Wire.h>
#include <Servo.h>
/*SET-UP
 * For IMU; Vcc=3.3, GND = ground, SDA = pin4, SCL = pin 5 (Analog)
 * For WaterSensor; Vcc=5, GND=ground, S=A0; >20uF cap and 20k ohm resistor between S and ground
 * For Motors; All are digital 'D'. Each pin should be a PWM with a "~" next to it
 * -Front Left = pin 3
 * -Front Right = pin 5
 * -Back Left = pin 6
 * -Back Right = pin 9
 * -Side Left = pin 10
 * -Side Right = pin 11
 * 
 * Free Pins:
 * D:2,4,7,8,12,13r
 * A:2,3
 */     
 
//***** IMU Vars************
//For IMU, Vcc=3.3, GND = ground, SDA = pin4, SCL = pin 5 (Analog)
const int8_t MPU_addr=0x68;  // I2C address of the MPU-6050
int16_t AcX,AcY,AcZ,Tmp,GyX,GyY,GyZ;//vars used for IMU 
int16_t XA=0, YA=0, GZ=0;//Return data
const int8_t  dak = 88;//const for fix()
//**************************

//******* Other Constant Values**********
const short TOPMAX = 1900;//absolute max and min allowed. too high or low could danmage servos
const short BOTMIN = 1100;//these can be adjusted, just be carefull
//const int8_t ledPin = 4;
const int8_t VoltageSensorPin = 1;
//***************************************

//******* Data Values***********
int8_t waterSensorPin = 0;//water sensor - used for detecting leaks
int depth = 0;//depth of sub in 'units'
int8_t incomingByte = 0;   // for incoming serial data
//*****************************

//******* Motor Values**********
int8_t motor_pin[] = {3,5,6,9,10,11};//FLM,FRM,BLM,BRM,LM,RM
const int16_t MOTORSTOP = 1500;
int motors[] = {MOTORSTOP,MOTORSTOP,MOTORSTOP,MOTORSTOP,MOTORSTOP,MOTORSTOP};
Servo s0, s1, s2, s3, s4, s5;//Initilize servo for all 6 motors
Servo servos[] = {s0,s1,s2,s3,s4,s5};
//******************************

//Madgwick filter; re-add?
void setup() {
  Serial.begin(9600);     // opens serial port, sets data rate

  Wire.begin();    //IMU init
  Wire.beginTransmission(MPU_addr);
  Wire.write(0x6B);  // PWR_MGMT_1 register
  Wire.write(0);     // set to zero (wakes up the MPU-6050)
  Wire.endTransmission(true);
  for(int i = 0; i<6; i++){//initiat the motor controlers and set speed to 0
      servos[i].attach(motor_pin[i]);
      servos[i].writeMicroseconds(MOTORSTOP);
    }
  delay(1000); // delay to allow the ESC to recognize the stopped signal
}

void loop() {
  //if(true){//might impliment shut down
    wait_for_start();//wait for [ over serial
  int mode = is_mode();//get mode from first char
  if(mode == 1){//runs self test; returns passed in value
    selfTest();
  }else if(mode == 2){//normal mode to set moders and return sensor data
    getnums();//gets motor values
    sendInfo();//sends sensor info
    setMotors();//sets motor values
    //if you're wondering why I wait to set motor values after I send data, there is a reason
  }else if(mode == 3){//just an info dump
    sendInfo();
  }else if(mode == 4){//shutdown
    for(int i = 0; i<6; i++){//initiat the motor controlers and set speed to 0
      motors[i] = MOTORSTOP;
    }
    setMotors();
  }else{//simple debug mode
    debug();
  }
  incomingByte = 0;//resets byte
  //}
}

void sendInfo(){//updates sensor values, then 
  Wire.beginTransmission(MPU_addr);
  Wire.write(0x3B);  // starting with register 0x3B (ACCEL_XOUT_H)
  Wire.endTransmission(false);
  Wire.requestFrom(MPU_addr,14,true);  // request a total of 14 registers
  AcX=Wire.read()<<8|Wire.read();  // 0x3B (ACCEL_XOUT_H) & 0x3C (ACCEL_XOUT_L)     
  AcY=Wire.read()<<8|Wire.read();  // 0x3D (ACCEL_YOUT_H) & 0x3E (ACCEL_YOUT_L)
  AcZ=Wire.read()<<8|Wire.read();  // 0x3F (ACCEL_ZOUT_H) & 0x40 (ACCEL_ZOUT_L)
  Tmp=Wire.read()<<8|Wire.read();  // 0x41 (TEMP_OUT_H) & 0x42 (TEMP_OUT_L)
  GyX=Wire.read()<<8|Wire.read();  // 0x43 (GYRO_XOUT_H) & 0x44 (GYRO_XOUT_L)
  GyY=Wire.read()<<8|Wire.read();  // 0x45 (GYRO_YOUT_H) & 0x46 (GYRO_YOUT_L)
  GyZ=Wire.read()<<8|Wire.read();  // 0x47 (GYRO_ZOUT_H) & 0x48 (GYRO_ZOUT_L)
  
  fix();
  Serial.print(AcX/3);//pitch
  Serial.print(",");
  Serial.print(AcY/3);//roll
  Serial.print(",");
  Serial.print(GZ/100);//heading
  Serial.print(",");
  Serial.print(depth);
  Serial.print(",");
  Serial.println(getWater());
  Serial.flush();
  //Serial.print(",");
  //Serial.println(getVoltage());
  
}
void fix(){
  AcX = AcX / dak;
  AcY = AcY / dak;
  XA = XA + AcX;
  XA = XA/2;
  AcX = XA;
  YA = YA + AcY;
  YA = YA/2;
  AcY = YA;
  
  GZ = (GZ + ((GyZ + 59)/(12)%360));
  //GZ = (GZ + ((GyZ + 1)/(12)%360));
}


void setMotors(){//sets the value of each motor 
  for(int i = 0; i<6; i++){
   servos[i].writeMicroseconds(motors[i]); // Send signal to ESC. 
  }
}
void debug(){//sends info about current motor values
  int done = 0;
  while(done == 0){
    if(Serial.available() > 0){
    Serial.println("debugging!");
    Serial.print("Form is: ");
    for(int i = 0; i <6;i++){
      Serial.print(motors[i], DEC);
      Serial.print(", ");
    }
    Serial.println();
    Serial.flush();
    done = 1;
    }
  }
 
}
void selfTest(){//sends back "Running seld test: #" #=passed in number
  //.println("Self test: " + getnum());
  Serial.print("Running self test: ");
  Serial.print(getnum(), DEC);//change to print?
  Serial.flush();
}
int is_mode(){//gets mode based on what the next char on the line is
  incomingByte = 0;
  while (incomingByte == 0) {
    if (Serial.available() > 0) {
      incomingByte = Serial.read();
    }
  }
  if(incomingByte == 116){//t
    return 1;//self test
  }else if(incomingByte == 110){//n
    return 2;//normal mode, update motors, send data
  }else if(incomingByte == 118){//v
    return 3;//just send info without updating motors
  }else if(incomingByte == 115){//s
    return 4;//shutdown 
  }else{
    return -1;
  }
}
int getWater(){
  return ((int)(analogRead(waterSensorPin)/2));
}
void wait_for_start() {//waits for the '[' char
  //Serial.write("started");
  while (incomingByte != 91) {
    incomingByte = Serial.read();
  }
}
boolean started(){
  incomingByte = Serial.read();
  if(incomingByte == 91){
    return true;
  }
  return false;
}
void getnums(){//gets 6 number and makes sure they are within the correct range
  for(int i = 0; i <6;i++){
      int tmp = getnum();
      if(tmp > TOPMAX){
        tmp = TOPMAX;
      }
      if(tmp < BOTMIN){
        tmp = BOTMIN;
      }
      motors[i] = tmp;
      incomingByte = 0;
    }
}
int getnum() {//turns ascii chars into int, ignoring non-number chars and stops at first ','
  int k = 0;
  while (incomingByte != 44) {
    if (Serial.available() > 0) {
      incomingByte = Serial.read();
    }
    if (incomingByte > 47 and incomingByte < 58) {
      k = k * 10;
      k = k + incomingByte - 48;
      incomingByte = 0;
    }
    //Serial.println(k, DEC);
  }
  return k;
}
//Battery Voltage
//int getVoltage(){//TODO
//  return constant*((int)(analogRead(VoltageSensorPin)));
//}
float convertRawAcceleration(int aRaw) {
  // since we are using 2G range
  // -2g maps to a raw value of -32768
  // +2g maps to a raw value of 32767
  
  float a = (aRaw * 2.0) / 32768.0;
  return a;
}

float convertRawGyro(int gRaw) {
  // since we are using 250 degrees/seconds range
  // -250 maps to a raw value of -32768
  // +250 maps to a raw value of 32767
  
  float g = (gRaw * 250.0) / 32768.0;
  return g;
}

/************ New code, may replace existing loop()
 *  void loop() {
  if(started()){//might impliment shut down
  int mode = is_mode();//get mode from first char
  if(mode == 1){//runs self test; returns passed in value
    selfTest();
  }else if(mode == 2){//normal mode to set moders and return sensor data
    getnums();//gets motor values
    sendInfo();//sends sensor info
    setMotors();//sets motor values
    //if you're wondering why I wait to set motor values after I send data, there is a reason
  }else if(mode == 3){//just an info dump
    sendInfo();
  }else{//simple debug mode
    debug();
  }
  incomingByte = 0;//resets byte
  }else{
  counter ++;
  if(counter > 1200){
  stop();
  }else{
  delay(1);
  }
}
 */

//[ = 91
//] = 93
//, = 44
//t=116
//n=110
//num = num + 48 up to 57

/*
 * digitalWrite(ledPin, HIGH);   // sets the LED on
      delay(100);                  // waits for a second
      digitalWrite(ledPin, LOW);    // sets the LED off
      delay(100); 
              
      while(true){
    delay(100);
    Serial.print("Form is: ");
    for(int i = 0; i <6;i++){
      Serial.print(motors[i], DEC);
      Serial.print(", ");
    }
    Serial.println();
  }
 */
//http://bildr.org/2012/03/stable-orientation-digital-imu-6dof-arduino/
