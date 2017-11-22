/*
 * movable.h
 *
 *  Created on: Oct 31, 2017
 *      Author: Dakota
 */

#ifndef MOVABLE_H_
#define MOVABLE_H_
using namespace std;
#include "update.h"
#include "logger.h"
//#include <stdlib>
#include <string.h>
#include <cmath>
#include <iostream>
//#include <thread>
class movable {
public:
	movable();
	void stop();
	void surface();
	void face(int dir);
	void face_R(int dir);
	void moveInDir(int dir);
	void moveInDir_R(int dir);
	void set_stabilize(int boo);
	void setSpeed(int sp);
	void set_raw_input(int speed, int pos);
	void move();
	void norm();
	void stable_cal();
	int isFacing(int dir);
	int isCorrect();
	double * stable(double x,double y);
	virtual ~movable();



};

static int run = 0;//Initialize to false, must be set to true before running
	static int init = 0;
	static int stabilize = 0;//Whether or not to actively stabilize the sub
	//private static Logger logger = Logger.getLogger(movable.class.getCanonicalName());
	static double target_depth = 5;//in [no unit]
	static double target_direction = 0;//in degrees
	static int speed = 80;//not max speed, just normal seed
	static int mode = 2;// mode 0=don't move, 1=turn, 2=move and turn, 3
								// = move forward with no turning
	static double motors[6] = {0,0,0,0,0,0};//see basic for motor names

#endif /* MOVABLE_H_ */
