/*
 * update.h
 *
 *  Created on: Oct 31, 2017
 *      Author: Dakota
 */

#ifndef UPDATE_H_
#define UPDATE_H_

class update {
 private:
  static int direction;
  static int depth;
  static int roll;
  static int pitch;

public:
	update();
	static int get_direction(){return direction;};
	static int get_depth(){return depth;};
	static int IMU_roll(){return roll;};
	static int IMU_pitch(){return pitch;};
	virtual ~update();
};

#endif /* UPDATE_H_ */
