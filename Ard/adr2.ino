#include <Wire.h>
#include <Servo.h>
#include <MadgwickAHRS.h>
//for IMU, Vcc=3.3, GND = ground, SDA = pin4, SCL = pin 5
const int MPU_addr=0x68;  // I2C address of the MPU-6050
Servo servo;
int waterlvl;//water sensor 
int16_t AcX,AcY,AcZ,Tmp,GyX,GyY,GyZ;
int16_t XA, YA, GZ;
const int16_t  dak = 88;
int incomingByte = 0;   // for incoming serial data
int ledPin = 4;
int a = 0;
int b = 0;
int motor_pin[] = {3,5,6,9,10,11};
int motors[] = {0,0,0,0,0,0};
Servo s0;
Servo s1;
Servo s2;
Servo s3;
Servo s4;
Servo s5;
Servo servos[] = {s0,s1,s2,s3,s4,s5};
int count = 0;
Madgwick filter;
void setup() {
  Serial.begin(4800);     // opens serial port, sets data rate
    //IMU init
  Wire.begin();
  Wire.beginTransmission(MPU_addr);
  Wire.write(0x6B);  // PWR_MGMT_1 register
  Wire.write(0);     // set to zero (wakes up the MPU-6050)
  Wire.endTransmission(true);
  filter.begin(10);
  XA=0;
  YA = 0;
  GZ = 0;
  for(int i = 0; i<6; i++){
      servos[i].attach(motor_pin[i]);
      servos[i].writeMicroseconds(1500);
    }
  delay(1000); // delay to allow the ESC to recognize the stopped signal
}

void loop() {
 
  //count = count+1;
  if(true){
    wait_for_start();
  int mode = is_mode();
  if(mode == 1){
    selfTest();
  }else if(mode == 2){
    getnums();
    sendInfo();
    setMotors();
  }else if(mode == 3){
    sendInfo();
  }else{
    debug();
  }

  incomingByte = 0;
  }
}

void sendInfo(){
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
  
  filter.updateIMU(GyX, GyY, GyZ, AcX, AcY, AcZ);
  fix();
  Serial.print(AcX);//pitch
  Serial.print(",");
  Serial.print(AcY);//roll
  Serial.print(",");
  Serial.print(GZ/100);//heading
  Serial.println(",3,0");//TODO water level and depth
  
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
  
  GZ = GZ + ((GyZ + 69)/(10 * 5));
}


void setMotors(){
  for(int i = 0; i<6; i++){
   servos[i].writeMicroseconds(motors[i]); // Send signal to ESC. 
  }
}
void debug(){
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
    done = 1;
    }
  }
 
}
void selfTest(){
  Serial.print( "Running self test: ");
  Serial.println(getnum());
}
int is_mode(){
  incomingByte = 0;
  while (incomingByte == 0) {
    if (Serial.available() > 0) {
      incomingByte = Serial.read();
    }
  }
  if(incomingByte == 116){//t
    return 1;
  }else if(incomingByte == 110){//n
    return 2;
  }else if(incomingByte == 118){//v
    return 3;
  }else{
    return -1;
  }
}
void wait_for_start() {
  //Serial.write("started");
  while (incomingByte != 91) {
    incomingByte = Serial.read();
  }
}
void getnums(){
  for(int i = 0; i <6;i++){
      motors[i] = getnum();
      incomingByte = 0;
    }
}
int getnum() {
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
