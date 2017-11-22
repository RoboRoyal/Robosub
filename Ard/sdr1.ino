//arduino 101
//intel
#include <CurieIMU.h>
#include <MadgwickAHRS.h>

Madgwick filter;
float accelScale, gyroScale;
int aix, aiy, aiz;
int gix, giy, giz;
float ax, ay, az;
float gx, gy, gz;
int roll, pitch, heading;

int incomingByte = 0;   // for incoming serial data
int ledPin = 4;
int a = 0;
int b = 0;
int motors[] = {0,0,0,0,0,0};
int motor_pin[] = {3,5,6,9,10,11};//FLM,FRM,BLM,BRM
int count = 0;

void setup() {
  Serial.begin(1200);     // opens serial port, sets data rate to 9600 bps
   pinMode(ledPin, OUTPUT);
    for(int i = 0; i<6; i++){
      pinMode(motor_pin[i], OUTPUT);
    }
  CurieIMU.begin();
  CurieIMU.setGyroRate(10);
  CurieIMU.setAccelerometerRate(10);
  filter.begin(10);

  // Set the accelerometer range to 2G
  CurieIMU.setAccelerometerRange(2);
  // Set the gyroscope range to 250 degrees/second
  CurieIMU.setGyroRange(100);
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
  CurieIMU.readMotionSensor(aix, aiy, aiz, gix, giy, giz);

    // convert from raw data to gravity and degrees/second units
    ax = convertRawAcceleration(aix);
    ay = convertRawAcceleration(aiy);
    az = convertRawAcceleration(aiz);
    gx = convertRawGyro(gix);
    gy = convertRawGyro(giy);
    gz = convertRawGyro(giz);

    // update the filter, which computes orientation
    filter.updateIMU(gx, gy, gz, ax, ay, az);

    // print the heading, pitch and roll
    roll = 0;
    pitch = 0;
    roll = (int)(1*filter.getRoll());
    pitch = (int)(1*filter.getPitch());
    heading = filter.getYaw();
  //Serial.println("hello!");
  //sent IMU info
  Serial.print(pitch);
  Serial.print(",");
  Serial.print(roll);
  Serial.println(",3,4");
}

void setMotors(){
  for(int i = 0; i<6; i++){
   analogWrite(motor_pin[i], motors[i]); 
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
  if(incomingByte == 116){
    return 1;
  }else if(incomingByte == 110){
    return 2;
  }else if(incomingByte == 118){
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
