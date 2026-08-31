/* USER CODE BEGIN Header */
/**
  ******************************************************************************
  * @file           : main.c
  * @brief          : Main program body
  ******************************************************************************
  * @attention
  *
  * Copyright (c) 2022 STMicroelectronics.
  * All rights reserved.
  *
  * This software is licensed under terms that can be found in the LICENSE file
  * in the root directory of this software component.
  * If no LICENSE file comes with this software, it is provided AS-IS.
  *
  ******************************************************************************
  */
/* USER CODE END Header */
/* Includes ------------------------------------------------------------------*/
#include "main.h"
#include "cmsis_os.h"

/* Private includes ----------------------------------------------------------*/
/* USER CODE BEGIN Includes */
#include "oled.h"
#include "gyro.h"
#include "motor.h"
#include "servo.h"
#include "pid.h"
#include "ICM20948.h"
/* USER CODE END Includes */

/* Private typedef -----------------------------------------------------------*/
/* USER CODE BEGIN PTD */

/* USER CODE END PTD */

/* Private define ------------------------------------------------------------*/
/* USER CODE BEGIN PD */

/* USER CODE END PD */

/* Private macro -------------------------------------------------------------*/
/* USER CODE BEGIN PM */
I2C_HandleTypeDef hi2c1;

TIM_HandleTypeDef htim1;
TIM_HandleTypeDef htim2;
TIM_HandleTypeDef htim3;
TIM_HandleTypeDef htim8;

UART_HandleTypeDef huart3;
/* USER CODE END PM */

/* Private variables ---------------------------------------------------------*/
ADC_HandleTypeDef hadc1;
ADC_HandleTypeDef hadc2;
DMA_HandleTypeDef hdma_adc1;
DMA_HandleTypeDef hdma_adc2;

I2C_HandleTypeDef hi2c1;

TIM_HandleTypeDef htim1;
TIM_HandleTypeDef htim2;
TIM_HandleTypeDef htim3;
TIM_HandleTypeDef htim4;
TIM_HandleTypeDef htim8;

UART_HandleTypeDef huart3;

/* Definitions for defaultTask */
osThreadId_t defaultTaskHandle;
const osThreadAttr_t defaultTask_attributes = {
  .name = "defaultTask",
  .stack_size = 128 * 4,
  .priority = (osPriority_t) osPriorityNormal,
};
/* Definitions for EncoderTask */
osThreadId_t EncoderTaskHandle;
const osThreadAttr_t EncoderTask_attributes = {
  .name = "EncoderTask",
  .stack_size = 128 * 4,
  .priority = (osPriority_t) osPriorityLow,
};
/* Definitions for CorrectionTask */
osThreadId_t CorrectionTaskHandle;
const osThreadAttr_t CorrectionTask_attributes = {
  .name = "CorrectionTask",
  .stack_size = 128 * 4,
  .priority = (osPriority_t) osPriorityLow,
};
/* Definitions for DirectTask */
osThreadId_t DirectTaskHandle;
const osThreadAttr_t DirectTask_attributes = {
  .name = "DirectTask",
  .stack_size = 128 * 4,
  .priority = (osPriority_t) osPriorityLow,
};
/* Definitions for GyroTask */
osThreadId_t GyroTaskHandle;
const osThreadAttr_t GyroTask_attributes = {
  .name = "GyroTask",
  .stack_size = 128 * 4,
  .priority = (osPriority_t) osPriorityLow,
};
/* Definitions for irsense */
osThreadId_t irsenseHandle;
const osThreadAttr_t irsense_attributes = {
  .name = "irsense",
  .stack_size = 128 * 4,
  .priority = (osPriority_t) osPriorityLow,
};
/* USER CODE BEGIN PV */
static const uint8_t ICM_ADDR = 0x68 << 1;
/* USER CODE END PV */

/* Private function prototypes -----------------------------------------------*/
void SystemClock_Config(void);
static void MX_GPIO_Init(void);
static void MX_DMA_Init(void);
static void MX_TIM8_Init(void);
static void MX_TIM2_Init(void);
static void MX_TIM1_Init(void);
static void MX_TIM3_Init(void);
static void MX_I2C1_Init(void);
static void MX_USART3_UART_Init(void);
static void MX_ADC1_Init(void);
static void MX_ADC2_Init(void);
static void MX_TIM4_Init(void);
void StartDefaultTask(void *argument);
void encoder(void *argument);
void correction(void *argument);
void direction(void *argument);
void gyroIMU(void *argument);
void irTask(void *argument);

/* USER CODE BEGIN PFP */

/* USER CODE END PFP */

/* Private user code ---------------------------------------------------------*/
/* USER CODE BEGIN 0 */
uint8_t buff[20];
uint8_t aRxBuffer[20];
double total_angle=0, debugAng=0;
int flag = 0;
char dir;

int travelD=0;
float Kp = 0.02, Ki = 0;
double errA=0,errB = 0;
double cumErrA = 0,cumErrB = 0;
int newEncVal = 0;
int offset = 0;

double cumErrAng=0;

int pulseLA=0,pulseRB=0;
double pwmLA=0, pwmRB=0;
double distLA=0, distRB=0;
double speedLA=0, speedRB=0;
double tarEncA=0, tarEncB=0; // to print for debug, in correction
int16_t angular_speed = 0; // debug purposes
float Aint=0;

int magnitude=0;
uint8_t logText[20];

/*IR sensor */
uint16_t lengthFront[4096] = {0}; // DMA
uint16_t lengthSide[4096] = {0};
int32_t distanceFront = 0;
int32_t distanceSide = 0;
uint32_t IRdistL, IRdistR;
int32_t leftIR, rightIR;

/* ultrasonic */
uint32_t IC_Val1 = 0;
uint32_t IC_Val2 = 0;
uint32_t Difference = 0;
uint8_t Is_First_Captured = 0;  // is the first value captured ?
double Distance  = 0;

/*task 2*/
int vertDist=0;
ICM20948 imu;

int pidAng; // debug chicken

void writeByte(uint8_t addr, uint8_t data)
{
  buff[0] = addr;
  buff[1] = data;
  HAL_I2C_Master_Transmit(&hi2c1, ICM_ADDR, buff, 2, 20);
}

void readByte(uint8_t addr, uint8_t *data)
{
  buff[0] = addr;
  // Tell we want to read from the register
  HAL_I2C_Master_Transmit(&hi2c1, ICM_ADDR, buff, 1, 10);
  // Read 2 byte from z dir register
  HAL_I2C_Master_Receive(&hi2c1, ICM_ADDR, data, 2, 20);
}

void resetDisVal() {

	distLA=0, distRB=0; // rst traveled distance
	pwmLA = pwmRB = 0; // rst speed
	cumErrA=0; cumErrB=0; // rst back wheel Ki
	errA=0;errB=0;  // rst back wheel Kp
}

void chicken(double tarAng, int fb) {

	pidAng = (int)(148 + fb * ((total_angle - tarAng) * 3 + 0.016 * cumErrAng));

	// limit turning
	if (pidAng <= 95) pidAng = 95;
	if (pidAng >= 250) pidAng = 250;

	cumErrAng += total_angle - tarAng;

	htim1.Instance->CCR4 = pidAng;
}

void goose(double tarAng, int fb) {
	double error;

//	if (debugAng < 0) {
//		if (tarAng < debugAng) error = -(debugAng-tarAng);
//	}

	pidAng = (int)(150 + fb * ((debugAng - tarAng) * 1.5 + 0 * cumErrAng));

	  if (cumErrAng >= 65535) cumErrAng = 65535;

	  if(cumErrAng <= -65535) cumErrAng = -65535;

	// limit turning
	if (pidAng <= 95) pidAng = 95;
	if (pidAng >= 250) pidAng = 250;

	cumErrAng += debugAng - tarAng;

	htim1.Instance->CCR4 = pidAng;
}

void HCSR04_Read(void)
{
	HAL_GPIO_WritePin(GPIOD, GPIO_PIN_13, GPIO_PIN_SET);  // pull the TRIG pin HIGH
	osDelay(1);
	HAL_GPIO_WritePin(GPIOD, GPIO_PIN_13, GPIO_PIN_RESET);  // pull the TRIG pin low

	__HAL_TIM_ENABLE_IT(&htim4, TIM_IT_CC1);
}

void ogForward(int mag, double tarEnc) {
	servoCenter();
	toDriveFront();

	while (travelD * (21.60 / 1550.0) / 2.0 < mag) {

		cumErrAng=0;
		chicken(0,1);

		/*START sync back wheel*/
		errA = tarEnc - speedLA; // speed is cm/
		errB = tarEnc - speedRB;
		cumErrA += errA;
		cumErrB += errB;

		if(cumErrA > 7000) cumErrA = 7000;
		if(cumErrA < -7000) cumErrA = -7000;

		pwmLA +=  errA * Kp + Ki * cumErrA;
//		pwmLA =  10 * errA + 0.6 * cumErrA;

		if (pwmLA <= 0) pwmLA = 0;
		if (pwmLA >= 7000) pwmLA = 7000;

		if(cumErrB > 7000) cumErrB = 7000;
		if(cumErrB < -7000) cumErrB = -7000;

		pwmRB +=  errB * Kp + Ki * cumErrB;
//		pwmRB =  tarEnc * 65.768 + 4 * errB + 0.04 * cumErrB;

		if (pwmRB <= 0) pwmRB = 0;
		if (pwmRB >= 7000) pwmRB = 7000;
		/*END sync back wheel*/

		/*DRIVE with new pwm*/
		__HAL_TIM_SET_COMPARE(&htim8,ch1,(int)pwmLA);
		__HAL_TIM_SET_COMPARE(&htim8,ch2,(int)pwmRB);

		osDelayUntil(200);
	}

	// when u reach the distance
	// TODO: hard reverse
	moveStop();
	servoCenter();
}

void moveForward(int mag, double tarEnc) {
	servoCenter();
	toDriveFront();

	while (travelD * (21.60 / 1550.0) / 2.0 < mag) {
		if (Distance <= 25) break;

		cumErrAng=0;
		chicken(0,1);

		/*START sync back wheel*/
		errA = tarEnc - speedLA; // speed is cm/
		errB = tarEnc - speedRB;
		cumErrA += errA;
		cumErrB += errB;

		if(cumErrA > 7000) cumErrA = 7000;
		if(cumErrA < -7000) cumErrA = -7000;

		pwmLA +=  errA * Kp + Ki * cumErrA;
//		pwmLA =  10 * errA + 0.6 * cumErrA;

		if (pwmLA <= 0) pwmLA = 0;
		if (pwmLA >= 7000) pwmLA = 7000;

		if(cumErrB > 7000) cumErrB = 7000;
		if(cumErrB < -7000) cumErrB = -7000;

		pwmRB +=  errB * Kp + Ki * cumErrB;
//		pwmRB =  tarEnc * 65.768 + 4 * errB + 0.04 * cumErrB;

		if (pwmRB <= 0) pwmRB = 0;
		if (pwmRB >= 7000) pwmRB = 7000;
		/*END sync back wheel*/

		/*DRIVE with new pwm*/
		__HAL_TIM_SET_COMPARE(&htim8,ch1,(int)pwmLA);
		__HAL_TIM_SET_COMPARE(&htim8,ch2,(int)pwmRB);

		osDelayUntil(200);
	}

	// when u reach the distance
	// TODO: hard reverse
	moveStop();
	servoCenter();
}

void forward2(int mag,double tarEnc) {
	servoCenter();
	toDriveFront();

	while (travelD * (21.60 / 1550.0) / 2.0 < mag) {

		if (IRdistL > 50 && IRdistR > 50) break;

		cumErrAng=0;
		chicken(0, 1);

		/*START sync back wheel*/
		errA = tarEnc - speedLA; // speed is cm/
		errB = tarEnc - speedRB;
		cumErrA += errA;
		cumErrB += errB;

		if(cumErrA > 7000) cumErrA = 7000;
		if(cumErrA < -7000) cumErrA = -7000;

		pwmLA +=  errA * Kp + Ki * cumErrA;
//		pwmLA =  10 * errA + 0.6 * cumErrA;

		if (pwmLA <= 0) pwmLA = 0;
		if (pwmLA >= 7000) pwmLA = 7000;

		if(cumErrB > 7000) cumErrB = 7000;
		if(cumErrB < -7000) cumErrB = -7000;

		pwmRB +=  errB * Kp + Ki * cumErrB;
//		pwmRB =  tarEnc * 65.768 + 4 * errB + 0.04 * cumErrB;

		if (pwmRB <= 0) pwmRB = 0;
		if (pwmRB >= 7000) pwmRB = 7000;
		/*END sync back wheel*/

		/*DRIVE with new pwm*/
		__HAL_TIM_SET_COMPARE(&htim8,ch1,(int)pwmLA);
		__HAL_TIM_SET_COMPARE(&htim8,ch2,(int)pwmRB);

		osDelayUntil(200);
	}

	// when u reach the distance
	moveStop();
	servoCenter();
}

void forward3(int mag,double tarEnc, int flag) {
	servoCenter();
	toDriveFront();

	while (travelD * (21.60 / 1550.0) / 2.0 < mag) {

		if (flag == 1) {
			if (IRdistL < 60) break;
		}
		else {
			if (IRdistR < 60) break;
		}

		cumErrAng=0;
		chicken(0, 1);

		/*START sync back wheel*/
		errA = tarEnc - speedLA; // speed is cm/
		errB = tarEnc - speedRB;
		cumErrA += errA;
		cumErrB += errB;

		if(cumErrA > 7000) cumErrA = 7000;
		if(cumErrA < -7000) cumErrA = -7000;

		pwmLA +=  errA * Kp + Ki * cumErrA;
//		pwmLA =  10 * errA + 0.6 * cumErrA;

		if (pwmLA <= 0) pwmLA = 0;
		if (pwmLA >= 7000) pwmLA = 7000;

		if(cumErrB > 7000) cumErrB = 7000;
		if(cumErrB < -7000) cumErrB = -7000;

		pwmRB +=  errB * Kp + Ki * cumErrB;
//		pwmRB =  tarEnc * 65.768 + 4 * errB + 0.04 * cumErrB;

		if (pwmRB <= 0) pwmRB = 0;
		if (pwmRB >= 7000) pwmRB = 7000;
		/*END sync back wheel*/

		/*DRIVE with new pwm*/
		__HAL_TIM_SET_COMPARE(&htim8,ch1,(int)pwmLA);
		__HAL_TIM_SET_COMPARE(&htim8,ch2,(int)pwmRB);

		osDelayUntil(200);
	}

	// when u reach the distance
	moveStop();
	servoCenter();
}

void finalForward(int mag, double tarEnc) {
	servoCenter();
	toDriveFront();

	while (travelD * (21.60 / 1550.0) / 2.0 < mag) {
		if (Distance <= 10) break;

		cumErrAng=0;
		chicken(0,1);

		/*START sync back wheel*/
		errA = tarEnc - speedLA; // speed is cm/
		errB = tarEnc - speedRB;
		cumErrA += errA;
		cumErrB += errB;

		if(cumErrA > 7000) cumErrA = 7000;
		if(cumErrA < -7000) cumErrA = -7000;

		pwmLA +=  errA * Kp + Ki * cumErrA;
//		pwmLA =  10 * errA + 0.6 * cumErrA;

		if (pwmLA <= 0) pwmLA = 0;
		if (pwmLA >= 7000) pwmLA = 7000;

		if(cumErrB > 7000) cumErrB = 7000;
		if(cumErrB < -7000) cumErrB = -7000;

		pwmRB +=  errB * Kp + Ki * cumErrB;
//		pwmRB =  tarEnc * 65.768 + 4 * errB + 0.04 * cumErrB;

		if (pwmRB <= 0) pwmRB = 0;
		if (pwmRB >= 7000) pwmRB = 7000;
		/*END sync back wheel*/

		/*DRIVE with new pwm*/
		__HAL_TIM_SET_COMPARE(&htim8,ch1,(int)pwmLA);
		__HAL_TIM_SET_COMPARE(&htim8,ch2,(int)pwmRB);

		osDelayUntil(200);
	}

	// when u reach the distance
	// TODO: hard reverse
	moveStop();
	servoCenter();
}


void moveBackward(int mag, double tarEnc) {
	servoCenter();
	toDriveBack();

	while (travelD * (21.60 / 1550.0) / 2.0 < mag) {

		cumErrAng=0;
		chicken(0, -1);

		errA = tarEnc - speedLA; // speed is cm
		errB = tarEnc - speedRB;
		cumErrA += errA;
		cumErrB += errB;

		if(cumErrA > 7000) cumErrA = 7000;
		if(cumErrA < - 7000) cumErrA = -7000;

		pwmLA +=  errA * Kp + Ki * cumErrA;

		if (pwmLA <= 0) pwmLA = 0;
		if (pwmLA >= 7000) pwmLA = 7000;

		if(cumErrB > 7000) cumErrB = 7000;
		if(cumErrB < - 7000) cumErrB = -7000;

		pwmRB +=  errB * Kp + Ki * cumErrB;

		if (pwmRB <= 0) pwmRB = 0;
		if (pwmRB >= 7000) pwmRB = 7000;
		/*END sync back wheel*/

		/*DRIVE with new pwm*/
		__HAL_TIM_SET_COMPARE(&htim8,ch1,(int)pwmLA);
		__HAL_TIM_SET_COMPARE(&htim8,ch2,(int)pwmRB);

		osDelayUntil(500);
	}

	// when u reach the distance
	moveStop();
	servoCenter();

}

void corrByRev(double gap, double tarEnc) { // gap is the space from obstacle, 'y' = 15
	servoCenter();
	toDriveBack();

	while (Distance < gap) {
		cumErrAng=0;
		chicken(0, -1);

		errA = tarEnc - speedLA; // speed is cm
		errB = tarEnc - speedRB;
		cumErrA += errA;
		cumErrB += errB;

		if(cumErrA > 7000) cumErrA = 7000;
		if(cumErrA < - 7000) cumErrA = -7000;

		pwmLA +=  errA * Kp + Ki * cumErrA;

		if (pwmLA <= 0) pwmLA = 0;
		if (pwmLA >= 7000) pwmLA = 7000;

		if(cumErrB > 7000) cumErrB = 7000;
		if(cumErrB < - 7000) cumErrB = -7000;

		pwmRB +=  errB * Kp + Ki * cumErrB;

		if (pwmRB <= 0) pwmRB = 0;
		if (pwmRB >= 7000) pwmRB = 7000;
		/*END sync back wheel*/

		/*DRIVE with new pwm*/
		__HAL_TIM_SET_COMPARE(&htim8,ch1,(int)pwmLA);
		__HAL_TIM_SET_COMPARE(&htim8,ch2,(int)pwmRB);

		osDelayUntil(200);
	}

	// when u reach the distance
	moveStop();
	servoCenter();
}
void moveLeft(int mag, double tarEnc, int fb) {
	htim1.Instance->CCR4 = 95;
	// speed 23: -10
	// speed 50: -25
	// speed 28+: -12.566
	tarEncA = tarEnc - 10;
	tarEncB = tarEnc;

	if (fb == 1) toDriveFront();
	else if (fb == -1) toDriveBack();

	while (abs((int)total_angle) <= mag) {

		errA = tarEncA - speedLA; // speed is cm
		errB = tarEncB - speedRB;
		cumErrA += errA;
		cumErrB += errB;

		if(cumErrA > 7000) cumErrA = 7000;
		if(cumErrA < - 7000) cumErrA = -7000;

		pwmLA +=  errA * Kp + Ki * cumErrA;

		if (pwmLA <= 0) pwmLA = 0;
		if (pwmLA >= 7000) pwmLA = 7000;

		if(cumErrB > 7000) cumErrB = 7000;
		if(cumErrB < - 7000) cumErrB = -7000;

		pwmRB +=  errB * Kp + Ki * cumErrB;

		if (pwmRB <= 0) pwmRB = 0;
		if (pwmRB >= 7000) pwmRB = 7000;
		/*END sync back wheel*/

		/*DRIVE with new pwm*/
		__HAL_TIM_SET_COMPARE(&htim8,ch1,(int)pwmLA);
		__HAL_TIM_SET_COMPARE(&htim8,ch2,(int)pwmRB);

		osDelayUntil(500);
	}

	// when u reach target angle
	moveStop();

	/* overshoot */
	if (fb == 1) toDriveBack(); // care this part for backleft, backright
	else if (fb == -1) toDriveFront();

	while(abs((int)total_angle) > mag){
		pwmLA = 500 * (abs((int)total_angle) - mag) + 0.01 * Aint;
		pwmRB = pwmLA;

		Aint +=  (abs((int)total_angle) - mag);

		__HAL_TIM_SET_COMPARE(&htim8,ch1,(int)pwmLA);
		__HAL_TIM_SET_COMPARE(&htim8,ch2,(int)pwmRB);

		osDelayUntil(100);
	}
	/** End of overshoot */

	moveStop();
	servoCenter();
	Aint=0;
}


void compassTurn(int mag, double tarEnc) { //mag can be +ve or -ve
//	double angTH = debugAng + mag;

	toDriveFront();

	if (mag > 0) { // turn left
		htim1.Instance->CCR4 = 95;
//		while (angTH >= debugAng) {
		while (debugAng <= mag) {
			__HAL_TIM_SET_COMPARE(&htim8,ch1,(int)1000);
			__HAL_TIM_SET_COMPARE(&htim8,ch2,(int)3000);
			osDelayUntil(100);
		}

	} else { // turn right
		htim1.Instance->CCR4 = 250;
//		while (angTH <= debugAng) {
		while (debugAng >= mag) {
			__HAL_TIM_SET_COMPARE(&htim8,ch1,(int)3000);
			__HAL_TIM_SET_COMPARE(&htim8,ch2,(int)500);
			osDelayUntil(100);
		}
	}

	moveStop();
	servoCenter();
}

void left2(int mag, double tarEnc, int fb) {
	double AngTH = debugAng + mag;
	htim1.Instance->CCR4 = 95;
	// speed 23: -10
	// speed 50: -25
	// speed 28+: -12.566
	tarEncA = tarEnc - 10;
	tarEncB = tarEnc;

	if (fb == 1) toDriveFront();
	else if (fb == -1) toDriveBack();

	while (debugAng <= AngTH) {

		errA = tarEncA - speedLA; // speed is cm
		errB = tarEncB - speedRB;
		cumErrA += errA;
		cumErrB += errB;

		if(cumErrA > 7000) cumErrA = 7000;
		if(cumErrA < - 7000) cumErrA = -7000;

		pwmLA +=  errA * Kp + Ki * cumErrA;

		if (pwmLA <= 0) pwmLA = 0;
		if (pwmLA >= 7000) pwmLA = 7000;

		if(cumErrB > 7000) cumErrB = 7000;
		if(cumErrB < - 7000) cumErrB = -7000;

		pwmRB +=  errB * Kp + Ki * cumErrB;

		if (pwmRB <= 0) pwmRB = 0;
		if (pwmRB >= 7000) pwmRB = 7000;
		/*END sync back wheel*/

		/*DRIVE with new pwm*/
		__HAL_TIM_SET_COMPARE(&htim8,ch1,(int)pwmLA);
		__HAL_TIM_SET_COMPARE(&htim8,ch2,(int)pwmRB);

		osDelayUntil(500);
	}

	moveStop();
	servoCenter();
	Aint=0;
}

void moveRight(int mag, double tarEnc, int fb) {
	htim1.Instance->CCR4 = 250; // right
	tarEncA = tarEnc;
	tarEncB = tarEnc-10;

	if (fb == 1) toDriveFront(); // care this part for backleft, backright
	else if (fb == -1) toDriveBack();

	while (abs((int)total_angle) <= mag) {

		errA = tarEncA - speedLA; // speed is cm
		errB = tarEncB - speedRB;
		cumErrA += errA;
		cumErrB += errB;

		if(cumErrA > 7000) cumErrA = 7000;
		if(cumErrA < - 7000) cumErrA = -7000;

		pwmLA +=  errA * Kp + Ki * cumErrA;

		if (pwmLA <= 0) pwmLA = 0;
		if (pwmLA >= 7000) pwmLA = 7000;

		if(cumErrB > 7000) cumErrB = 7000;
		if(cumErrB < - 7000) cumErrB = -7000;

		pwmRB +=  errB * Kp + Ki * cumErrB;

		if (pwmRB <= 0) pwmRB = 0;
		if (pwmRB >= 7000) pwmRB = 7000;
		/*END sync back wheel*/

		/*DRIVE with new pwm*/
		__HAL_TIM_SET_COMPARE(&htim8,ch1,(int)pwmLA);
		__HAL_TIM_SET_COMPARE(&htim8,ch2,(int)pwmRB);

		osDelayUntil(500);
	}

	// when u reach target angle
	moveStop();

	/* overshoot */
	if (fb == 1) toDriveBack(); // care this part for backleft, backright
	else if (fb == -1) toDriveFront();

	while(abs((int)total_angle) > mag){
		pwmLA = 500 * (abs((int)total_angle) - mag) + 0.01 * Aint;
		pwmRB = pwmLA;

		Aint +=  (abs((int)total_angle) - mag);

		__HAL_TIM_SET_COMPARE(&htim8,ch1,(int)pwmLA);
		__HAL_TIM_SET_COMPARE(&htim8,ch2,(int)pwmRB);

		osDelayUntil(100);
	}
	/** End of overshoot */

	moveStop();
	servoCenter(); // straighten wheel
	Aint=0;
}

void right2(int mag, double tarEnc, int fb) {
	double AngTH = debugAng - mag;
	htim1.Instance->CCR4 = 250; // right
	tarEncA = tarEnc;
	tarEncB = tarEnc-10;

	if (fb == 1) toDriveFront(); // care this part for backleft, backright
	else if (fb == -1) toDriveBack();

	while (debugAng >= AngTH) {

		errA = tarEncA - speedLA; // speed is cm
		errB = tarEncB - speedRB;
		cumErrA += errA;
		cumErrB += errB;

		if(cumErrA > 7000) cumErrA = 7000;
		if(cumErrA < - 7000) cumErrA = -7000;

		pwmLA +=  errA * Kp + Ki * cumErrA;

		if (pwmLA <= 0) pwmLA = 0;
		if (pwmLA >= 7000) pwmLA = 7000;

		if(cumErrB > 7000) cumErrB = 7000;
		if(cumErrB < - 7000) cumErrB = -7000;

		pwmRB +=  errB * Kp + Ki * cumErrB;

		if (pwmRB <= 0) pwmRB = 0;
		if (pwmRB >= 7000) pwmRB = 7000;
		/*END sync back wheel*/

		/*DRIVE with new pwm*/
		__HAL_TIM_SET_COMPARE(&htim8,ch1,(int)pwmLA);
		__HAL_TIM_SET_COMPARE(&htim8,ch2,(int)pwmRB);

		osDelayUntil(500);
	}

	moveStop();
	servoCenter(); // straighten wheel
	Aint=0;
}

/* USER CODE END 0 */

/**
  * @brief  The application entry point.
  * @retval int
  */
int main(void)
{
  /* USER CODE BEGIN 1 */

  /* USER CODE END 1 */

  /* MCU Configuration--------------------------------------------------------*/

  /* Reset of all peripherals, Initializes the Flash interface and the Systick. */
  HAL_Init();

  /* USER CODE BEGIN Init */

  /* USER CODE END Init */

  /* Configure the system clock */
  SystemClock_Config();

  /* USER CODE BEGIN SysInit */

  /* USER CODE END SysInit */

  /* Initialize all configured peripherals */
  MX_GPIO_Init();
  MX_DMA_Init();
  MX_TIM8_Init();
  MX_TIM2_Init();
  MX_TIM1_Init();
  MX_TIM3_Init();
  MX_I2C1_Init();
  MX_USART3_UART_Init();
  MX_ADC1_Init();
  MX_ADC2_Init();
  MX_TIM4_Init();
  /* USER CODE BEGIN 2 */
  OLED_Init();

  HAL_TIM_Encoder_Start(&htim2,TIM_CHANNEL_ALL); //Start Pulsing for encoder
  HAL_TIM_Encoder_Start(&htim3,TIM_CHANNEL_ALL);

  HAL_TIM_PWM_Start(&htim8, TIM_CHANNEL_1); // dc motor back wheels
  HAL_TIM_PWM_Start(&htim8, TIM_CHANNEL_2);

  HAL_TIM_PWM_Start(&htim1,TIM_CHANNEL_4); // servo start
  HAL_TIM_Base_Start_IT(&htim4);
  HAL_TIM_IC_Start_IT(&htim4, TIM_CHANNEL_1);

  HAL_UART_Receive_IT(&huart3, (uint8_t *) aRxBuffer, 4);

  gyroInit();
  /* USER CODE END 2 */

  /* Init scheduler */
  osKernelInitialize();

  /* USER CODE BEGIN RTOS_MUTEX */
  /* add mutexes, ... */
  /* USER CODE END RTOS_MUTEX */

  /* USER CODE BEGIN RTOS_SEMAPHORES */
  /* add semaphores, ... */
  /* USER CODE END RTOS_SEMAPHORES */

  /* USER CODE BEGIN RTOS_TIMERS */
  /* start timers, add new ones, ... */
  /* USER CODE END RTOS_TIMERS */

  /* USER CODE BEGIN RTOS_QUEUES */
  /* add queues, ... */
  /* USER CODE END RTOS_QUEUES */

  /* Create the thread(s) */
  /* creation of defaultTask */
  defaultTaskHandle = osThreadNew(StartDefaultTask, NULL, &defaultTask_attributes);

  /* creation of EncoderTask */
  EncoderTaskHandle = osThreadNew(encoder, NULL, &EncoderTask_attributes);

  /* creation of CorrectionTask */
  CorrectionTaskHandle = osThreadNew(correction, NULL, &CorrectionTask_attributes);

  /* creation of DirectTask */
  DirectTaskHandle = osThreadNew(direction, NULL, &DirectTask_attributes);

  /* creation of GyroTask */
  GyroTaskHandle = osThreadNew(gyroIMU, NULL, &GyroTask_attributes);

  /* creation of irsense */
  irsenseHandle = osThreadNew(irTask, NULL, &irsense_attributes);

  /* USER CODE BEGIN RTOS_THREADS */
  /* add threads, ... */
  /* USER CODE END RTOS_THREADS */

  /* USER CODE BEGIN RTOS_EVENTS */
  /* add events, ... */
  /* USER CODE END RTOS_EVENTS */

  /* Start scheduler */
  osKernelStart();

  /* We should never get here as control is now taken by the scheduler */
  /* Infinite loop */
  /* USER CODE BEGIN WHILE */
  while (1)
  {
    /* USER CODE END WHILE */

    /* USER CODE BEGIN 3 */
  }
  /* USER CODE END 3 */
}

/**
  * @brief System Clock Configuration
  * @retval None
  */
void SystemClock_Config(void)
{
  RCC_OscInitTypeDef RCC_OscInitStruct = {0};
  RCC_ClkInitTypeDef RCC_ClkInitStruct = {0};

  /** Configure the main internal regulator output voltage
  */
  __HAL_RCC_PWR_CLK_ENABLE();
  __HAL_PWR_VOLTAGESCALING_CONFIG(PWR_REGULATOR_VOLTAGE_SCALE1);

  /** Initializes the RCC Oscillators according to the specified parameters
  * in the RCC_OscInitTypeDef structure.
  */
  RCC_OscInitStruct.OscillatorType = RCC_OSCILLATORTYPE_HSI;
  RCC_OscInitStruct.HSIState = RCC_HSI_ON;
  RCC_OscInitStruct.HSICalibrationValue = RCC_HSICALIBRATION_DEFAULT;
  RCC_OscInitStruct.PLL.PLLState = RCC_PLL_NONE;
  if (HAL_RCC_OscConfig(&RCC_OscInitStruct) != HAL_OK)
  {
    Error_Handler();
  }

  /** Initializes the CPU, AHB and APB buses clocks
  */
  RCC_ClkInitStruct.ClockType = RCC_CLOCKTYPE_HCLK|RCC_CLOCKTYPE_SYSCLK
                              |RCC_CLOCKTYPE_PCLK1|RCC_CLOCKTYPE_PCLK2;
  RCC_ClkInitStruct.SYSCLKSource = RCC_SYSCLKSOURCE_HSI;
  RCC_ClkInitStruct.AHBCLKDivider = RCC_SYSCLK_DIV1;
  RCC_ClkInitStruct.APB1CLKDivider = RCC_HCLK_DIV1;
  RCC_ClkInitStruct.APB2CLKDivider = RCC_HCLK_DIV1;

  if (HAL_RCC_ClockConfig(&RCC_ClkInitStruct, FLASH_LATENCY_0) != HAL_OK)
  {
    Error_Handler();
  }
}

/**
  * @brief ADC1 Initialization Function
  * @param None
  * @retval None
  */
static void MX_ADC1_Init(void)
{

  /* USER CODE BEGIN ADC1_Init 0 */

  /* USER CODE END ADC1_Init 0 */

  ADC_ChannelConfTypeDef sConfig = {0};

  /* USER CODE BEGIN ADC1_Init 1 */

  /* USER CODE END ADC1_Init 1 */

  /** Configure the global features of the ADC (Clock, Resolution, Data Alignment and number of conversion)
  */
  hadc1.Instance = ADC1;
  hadc1.Init.ClockPrescaler = ADC_CLOCK_SYNC_PCLK_DIV8;
  hadc1.Init.Resolution = ADC_RESOLUTION_12B;
  hadc1.Init.ScanConvMode = DISABLE;
  hadc1.Init.ContinuousConvMode = ENABLE;
  hadc1.Init.DiscontinuousConvMode = DISABLE;
  hadc1.Init.ExternalTrigConvEdge = ADC_EXTERNALTRIGCONVEDGE_NONE;
  hadc1.Init.ExternalTrigConv = ADC_SOFTWARE_START;
  hadc1.Init.DataAlign = ADC_DATAALIGN_RIGHT;
  hadc1.Init.NbrOfConversion = 1;
  hadc1.Init.DMAContinuousRequests = ENABLE;
  hadc1.Init.EOCSelection = ADC_EOC_SINGLE_CONV;
  if (HAL_ADC_Init(&hadc1) != HAL_OK)
  {
    Error_Handler();
  }

  /** Configure for the selected ADC regular channel its corresponding rank in the sequencer and its sample time.
  */
  sConfig.Channel = ADC_CHANNEL_11;
  sConfig.Rank = 1;
  sConfig.SamplingTime = ADC_SAMPLETIME_3CYCLES;
  if (HAL_ADC_ConfigChannel(&hadc1, &sConfig) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN ADC1_Init 2 */

  /* USER CODE END ADC1_Init 2 */

}

/**
  * @brief ADC2 Initialization Function
  * @param None
  * @retval None
  */
static void MX_ADC2_Init(void)
{

  /* USER CODE BEGIN ADC2_Init 0 */

  /* USER CODE END ADC2_Init 0 */

  ADC_ChannelConfTypeDef sConfig = {0};

  /* USER CODE BEGIN ADC2_Init 1 */

  /* USER CODE END ADC2_Init 1 */

  /** Configure the global features of the ADC (Clock, Resolution, Data Alignment and number of conversion)
  */
  hadc2.Instance = ADC2;
  hadc2.Init.ClockPrescaler = ADC_CLOCK_SYNC_PCLK_DIV8;
  hadc2.Init.Resolution = ADC_RESOLUTION_12B;
  hadc2.Init.ScanConvMode = DISABLE;
  hadc2.Init.ContinuousConvMode = ENABLE;
  hadc2.Init.DiscontinuousConvMode = DISABLE;
  hadc2.Init.ExternalTrigConvEdge = ADC_EXTERNALTRIGCONVEDGE_NONE;
  hadc2.Init.ExternalTrigConv = ADC_SOFTWARE_START;
  hadc2.Init.DataAlign = ADC_DATAALIGN_RIGHT;
  hadc2.Init.NbrOfConversion = 1;
  hadc2.Init.DMAContinuousRequests = ENABLE;
  hadc2.Init.EOCSelection = ADC_EOC_SINGLE_CONV;
  if (HAL_ADC_Init(&hadc2) != HAL_OK)
  {
    Error_Handler();
  }

  /** Configure for the selected ADC regular channel its corresponding rank in the sequencer and its sample time.
  */
  sConfig.Channel = ADC_CHANNEL_12;
  sConfig.Rank = 1;
  sConfig.SamplingTime = ADC_SAMPLETIME_3CYCLES;
  if (HAL_ADC_ConfigChannel(&hadc2, &sConfig) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN ADC2_Init 2 */

  /* USER CODE END ADC2_Init 2 */

}

/**
  * @brief I2C1 Initialization Function
  * @param None
  * @retval None
  */
static void MX_I2C1_Init(void)
{

  /* USER CODE BEGIN I2C1_Init 0 */

  /* USER CODE END I2C1_Init 0 */

  /* USER CODE BEGIN I2C1_Init 1 */

  /* USER CODE END I2C1_Init 1 */
  hi2c1.Instance = I2C1;
  hi2c1.Init.ClockSpeed = 100000;
  hi2c1.Init.DutyCycle = I2C_DUTYCYCLE_2;
  hi2c1.Init.OwnAddress1 = 0;
  hi2c1.Init.AddressingMode = I2C_ADDRESSINGMODE_7BIT;
  hi2c1.Init.DualAddressMode = I2C_DUALADDRESS_DISABLE;
  hi2c1.Init.OwnAddress2 = 0;
  hi2c1.Init.GeneralCallMode = I2C_GENERALCALL_DISABLE;
  hi2c1.Init.NoStretchMode = I2C_NOSTRETCH_DISABLE;
  if (HAL_I2C_Init(&hi2c1) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN I2C1_Init 2 */

  /* USER CODE END I2C1_Init 2 */

}

/**
  * @brief TIM1 Initialization Function
  * @param None
  * @retval None
  */
static void MX_TIM1_Init(void)
{

  /* USER CODE BEGIN TIM1_Init 0 */

  /* USER CODE END TIM1_Init 0 */

  TIM_ClockConfigTypeDef sClockSourceConfig = {0};
  TIM_MasterConfigTypeDef sMasterConfig = {0};
  TIM_OC_InitTypeDef sConfigOC = {0};
  TIM_BreakDeadTimeConfigTypeDef sBreakDeadTimeConfig = {0};

  /* USER CODE BEGIN TIM1_Init 1 */

  /* USER CODE END TIM1_Init 1 */
  htim1.Instance = TIM1;
  htim1.Init.Prescaler = 160;
  htim1.Init.CounterMode = TIM_COUNTERMODE_UP;
  htim1.Init.Period = 1000;
  htim1.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1;
  htim1.Init.RepetitionCounter = 0;
  htim1.Init.AutoReloadPreload = TIM_AUTORELOAD_PRELOAD_ENABLE;
  if (HAL_TIM_Base_Init(&htim1) != HAL_OK)
  {
    Error_Handler();
  }
  sClockSourceConfig.ClockSource = TIM_CLOCKSOURCE_INTERNAL;
  if (HAL_TIM_ConfigClockSource(&htim1, &sClockSourceConfig) != HAL_OK)
  {
    Error_Handler();
  }
  if (HAL_TIM_PWM_Init(&htim1) != HAL_OK)
  {
    Error_Handler();
  }
  sMasterConfig.MasterOutputTrigger = TIM_TRGO_RESET;
  sMasterConfig.MasterSlaveMode = TIM_MASTERSLAVEMODE_DISABLE;
  if (HAL_TIMEx_MasterConfigSynchronization(&htim1, &sMasterConfig) != HAL_OK)
  {
    Error_Handler();
  }
  sConfigOC.OCMode = TIM_OCMODE_PWM1;
  sConfigOC.Pulse = 0;
  sConfigOC.OCPolarity = TIM_OCPOLARITY_HIGH;
  sConfigOC.OCFastMode = TIM_OCFAST_DISABLE;
  sConfigOC.OCIdleState = TIM_OCIDLESTATE_RESET;
  sConfigOC.OCNIdleState = TIM_OCNIDLESTATE_RESET;
  if (HAL_TIM_PWM_ConfigChannel(&htim1, &sConfigOC, TIM_CHANNEL_4) != HAL_OK)
  {
    Error_Handler();
  }
  sBreakDeadTimeConfig.OffStateRunMode = TIM_OSSR_DISABLE;
  sBreakDeadTimeConfig.OffStateIDLEMode = TIM_OSSI_DISABLE;
  sBreakDeadTimeConfig.LockLevel = TIM_LOCKLEVEL_OFF;
  sBreakDeadTimeConfig.DeadTime = 0;
  sBreakDeadTimeConfig.BreakState = TIM_BREAK_DISABLE;
  sBreakDeadTimeConfig.BreakPolarity = TIM_BREAKPOLARITY_HIGH;
  sBreakDeadTimeConfig.AutomaticOutput = TIM_AUTOMATICOUTPUT_DISABLE;
  if (HAL_TIMEx_ConfigBreakDeadTime(&htim1, &sBreakDeadTimeConfig) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN TIM1_Init 2 */

  /* USER CODE END TIM1_Init 2 */
  HAL_TIM_MspPostInit(&htim1);

}

/**
  * @brief TIM2 Initialization Function
  * @param None
  * @retval None
  */
static void MX_TIM2_Init(void)
{

  /* USER CODE BEGIN TIM2_Init 0 */

  /* USER CODE END TIM2_Init 0 */

  TIM_Encoder_InitTypeDef sConfig = {0};
  TIM_MasterConfigTypeDef sMasterConfig = {0};

  /* USER CODE BEGIN TIM2_Init 1 */

  /* USER CODE END TIM2_Init 1 */
  htim2.Instance = TIM2;
  htim2.Init.Prescaler = 0;
  htim2.Init.CounterMode = TIM_COUNTERMODE_UP;
  htim2.Init.Period = 65535;
  htim2.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1;
  htim2.Init.AutoReloadPreload = TIM_AUTORELOAD_PRELOAD_DISABLE;
  sConfig.EncoderMode = TIM_ENCODERMODE_TI12;
  sConfig.IC1Polarity = TIM_ICPOLARITY_RISING;
  sConfig.IC1Selection = TIM_ICSELECTION_DIRECTTI;
  sConfig.IC1Prescaler = TIM_ICPSC_DIV1;
  sConfig.IC1Filter = 10;
  sConfig.IC2Polarity = TIM_ICPOLARITY_RISING;
  sConfig.IC2Selection = TIM_ICSELECTION_DIRECTTI;
  sConfig.IC2Prescaler = TIM_ICPSC_DIV1;
  sConfig.IC2Filter = 10;
  if (HAL_TIM_Encoder_Init(&htim2, &sConfig) != HAL_OK)
  {
    Error_Handler();
  }
  sMasterConfig.MasterOutputTrigger = TIM_TRGO_RESET;
  sMasterConfig.MasterSlaveMode = TIM_MASTERSLAVEMODE_DISABLE;
  if (HAL_TIMEx_MasterConfigSynchronization(&htim2, &sMasterConfig) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN TIM2_Init 2 */

  /* USER CODE END TIM2_Init 2 */

}

/**
  * @brief TIM3 Initialization Function
  * @param None
  * @retval None
  */
static void MX_TIM3_Init(void)
{

  /* USER CODE BEGIN TIM3_Init 0 */

  /* USER CODE END TIM3_Init 0 */

  TIM_Encoder_InitTypeDef sConfig = {0};
  TIM_MasterConfigTypeDef sMasterConfig = {0};

  /* USER CODE BEGIN TIM3_Init 1 */

  /* USER CODE END TIM3_Init 1 */
  htim3.Instance = TIM3;
  htim3.Init.Prescaler = 0;
  htim3.Init.CounterMode = TIM_COUNTERMODE_UP;
  htim3.Init.Period = 65535;
  htim3.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1;
  htim3.Init.AutoReloadPreload = TIM_AUTORELOAD_PRELOAD_DISABLE;
  sConfig.EncoderMode = TIM_ENCODERMODE_TI12;
  sConfig.IC1Polarity = TIM_ICPOLARITY_RISING;
  sConfig.IC1Selection = TIM_ICSELECTION_DIRECTTI;
  sConfig.IC1Prescaler = TIM_ICPSC_DIV1;
  sConfig.IC1Filter = 10;
  sConfig.IC2Polarity = TIM_ICPOLARITY_RISING;
  sConfig.IC2Selection = TIM_ICSELECTION_DIRECTTI;
  sConfig.IC2Prescaler = TIM_ICPSC_DIV1;
  sConfig.IC2Filter = 10;
  if (HAL_TIM_Encoder_Init(&htim3, &sConfig) != HAL_OK)
  {
    Error_Handler();
  }
  sMasterConfig.MasterOutputTrigger = TIM_TRGO_RESET;
  sMasterConfig.MasterSlaveMode = TIM_MASTERSLAVEMODE_DISABLE;
  if (HAL_TIMEx_MasterConfigSynchronization(&htim3, &sMasterConfig) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN TIM3_Init 2 */

  /* USER CODE END TIM3_Init 2 */

}

/**
  * @brief TIM4 Initialization Function
  * @param None
  * @retval None
  */
static void MX_TIM4_Init(void)
{

  /* USER CODE BEGIN TIM4_Init 0 */

  /* USER CODE END TIM4_Init 0 */

  TIM_ClockConfigTypeDef sClockSourceConfig = {0};
  TIM_MasterConfigTypeDef sMasterConfig = {0};
  TIM_IC_InitTypeDef sConfigIC = {0};

  /* USER CODE BEGIN TIM4_Init 1 */

  /* USER CODE END TIM4_Init 1 */
  htim4.Instance = TIM4;
  htim4.Init.Prescaler = 16-1;
  htim4.Init.CounterMode = TIM_COUNTERMODE_UP;
  htim4.Init.Period = 0xffff - 1;
  htim4.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1;
  htim4.Init.AutoReloadPreload = TIM_AUTORELOAD_PRELOAD_DISABLE;
  if (HAL_TIM_Base_Init(&htim4) != HAL_OK)
  {
    Error_Handler();
  }
  sClockSourceConfig.ClockSource = TIM_CLOCKSOURCE_INTERNAL;
  if (HAL_TIM_ConfigClockSource(&htim4, &sClockSourceConfig) != HAL_OK)
  {
    Error_Handler();
  }
  if (HAL_TIM_IC_Init(&htim4) != HAL_OK)
  {
    Error_Handler();
  }
  sMasterConfig.MasterOutputTrigger = TIM_TRGO_RESET;
  sMasterConfig.MasterSlaveMode = TIM_MASTERSLAVEMODE_DISABLE;
  if (HAL_TIMEx_MasterConfigSynchronization(&htim4, &sMasterConfig) != HAL_OK)
  {
    Error_Handler();
  }
  sConfigIC.ICPolarity = TIM_INPUTCHANNELPOLARITY_RISING;
  sConfigIC.ICSelection = TIM_ICSELECTION_DIRECTTI;
  sConfigIC.ICPrescaler = TIM_ICPSC_DIV1;
  sConfigIC.ICFilter = 0;
  if (HAL_TIM_IC_ConfigChannel(&htim4, &sConfigIC, TIM_CHANNEL_1) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN TIM4_Init 2 */

  /* USER CODE END TIM4_Init 2 */

}

/**
  * @brief TIM8 Initialization Function
  * @param None
  * @retval None
  */
static void MX_TIM8_Init(void)
{

  /* USER CODE BEGIN TIM8_Init 0 */

  /* USER CODE END TIM8_Init 0 */

  TIM_ClockConfigTypeDef sClockSourceConfig = {0};
  TIM_MasterConfigTypeDef sMasterConfig = {0};
  TIM_OC_InitTypeDef sConfigOC = {0};
  TIM_BreakDeadTimeConfigTypeDef sBreakDeadTimeConfig = {0};

  /* USER CODE BEGIN TIM8_Init 1 */

  /* USER CODE END TIM8_Init 1 */
  htim8.Instance = TIM8;
  htim8.Init.Prescaler = 0;
  htim8.Init.CounterMode = TIM_COUNTERMODE_UP;
  htim8.Init.Period = 7199;
  htim8.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1;
  htim8.Init.RepetitionCounter = 0;
  htim8.Init.AutoReloadPreload = TIM_AUTORELOAD_PRELOAD_DISABLE;
  if (HAL_TIM_Base_Init(&htim8) != HAL_OK)
  {
    Error_Handler();
  }
  sClockSourceConfig.ClockSource = TIM_CLOCKSOURCE_INTERNAL;
  if (HAL_TIM_ConfigClockSource(&htim8, &sClockSourceConfig) != HAL_OK)
  {
    Error_Handler();
  }
  if (HAL_TIM_PWM_Init(&htim8) != HAL_OK)
  {
    Error_Handler();
  }
  sMasterConfig.MasterOutputTrigger = TIM_TRGO_RESET;
  sMasterConfig.MasterSlaveMode = TIM_MASTERSLAVEMODE_DISABLE;
  if (HAL_TIMEx_MasterConfigSynchronization(&htim8, &sMasterConfig) != HAL_OK)
  {
    Error_Handler();
  }
  sConfigOC.OCMode = TIM_OCMODE_PWM1;
  sConfigOC.Pulse = 0;
  sConfigOC.OCPolarity = TIM_OCPOLARITY_HIGH;
  sConfigOC.OCNPolarity = TIM_OCNPOLARITY_HIGH;
  sConfigOC.OCFastMode = TIM_OCFAST_DISABLE;
  sConfigOC.OCIdleState = TIM_OCIDLESTATE_RESET;
  sConfigOC.OCNIdleState = TIM_OCNIDLESTATE_RESET;
  if (HAL_TIM_PWM_ConfigChannel(&htim8, &sConfigOC, TIM_CHANNEL_1) != HAL_OK)
  {
    Error_Handler();
  }
  if (HAL_TIM_PWM_ConfigChannel(&htim8, &sConfigOC, TIM_CHANNEL_2) != HAL_OK)
  {
    Error_Handler();
  }
  sBreakDeadTimeConfig.OffStateRunMode = TIM_OSSR_DISABLE;
  sBreakDeadTimeConfig.OffStateIDLEMode = TIM_OSSI_DISABLE;
  sBreakDeadTimeConfig.LockLevel = TIM_LOCKLEVEL_OFF;
  sBreakDeadTimeConfig.DeadTime = 0;
  sBreakDeadTimeConfig.BreakState = TIM_BREAK_DISABLE;
  sBreakDeadTimeConfig.BreakPolarity = TIM_BREAKPOLARITY_HIGH;
  sBreakDeadTimeConfig.AutomaticOutput = TIM_AUTOMATICOUTPUT_DISABLE;
  if (HAL_TIMEx_ConfigBreakDeadTime(&htim8, &sBreakDeadTimeConfig) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN TIM8_Init 2 */

  /* USER CODE END TIM8_Init 2 */

}

/**
  * @brief USART3 Initialization Function
  * @param None
  * @retval None
  */
static void MX_USART3_UART_Init(void)
{

  /* USER CODE BEGIN USART3_Init 0 */

  /* USER CODE END USART3_Init 0 */

  /* USER CODE BEGIN USART3_Init 1 */

  /* USER CODE END USART3_Init 1 */
  huart3.Instance = USART3;
  huart3.Init.BaudRate = 115200;
  huart3.Init.WordLength = UART_WORDLENGTH_8B;
  huart3.Init.StopBits = UART_STOPBITS_1;
  huart3.Init.Parity = UART_PARITY_NONE;
  huart3.Init.Mode = UART_MODE_TX_RX;
  huart3.Init.HwFlowCtl = UART_HWCONTROL_NONE;
  huart3.Init.OverSampling = UART_OVERSAMPLING_16;
  if (HAL_UART_Init(&huart3) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN USART3_Init 2 */

  /* USER CODE END USART3_Init 2 */

}

/**
  * Enable DMA controller clock
  */
static void MX_DMA_Init(void)
{

  /* DMA controller clock enable */
  __HAL_RCC_DMA2_CLK_ENABLE();

  /* DMA interrupt init */
  /* DMA2_Stream0_IRQn interrupt configuration */
  HAL_NVIC_SetPriority(DMA2_Stream0_IRQn, 5, 0);
  HAL_NVIC_EnableIRQ(DMA2_Stream0_IRQn);
  /* DMA2_Stream2_IRQn interrupt configuration */
  HAL_NVIC_SetPriority(DMA2_Stream2_IRQn, 5, 0);
  HAL_NVIC_EnableIRQ(DMA2_Stream2_IRQn);

}

/**
  * @brief GPIO Initialization Function
  * @param None
  * @retval None
  */
static void MX_GPIO_Init(void)
{
  GPIO_InitTypeDef GPIO_InitStruct = {0};

  /* GPIO Ports Clock Enable */
  __HAL_RCC_GPIOE_CLK_ENABLE();
  __HAL_RCC_GPIOC_CLK_ENABLE();
  __HAL_RCC_GPIOA_CLK_ENABLE();
  __HAL_RCC_GPIOD_CLK_ENABLE();
  __HAL_RCC_GPIOB_CLK_ENABLE();

  /*Configure GPIO pin Output Level */
  HAL_GPIO_WritePin(GPIOE, OLED_SCL_Pin|OLED_SDA_Pin|OLED_RST_Pin|OLED_DC_Pin
                          |LED3_Pin|Trig_Pin_Pin, GPIO_PIN_RESET);

  /*Configure GPIO pin Output Level */
  HAL_GPIO_WritePin(GPIOA, AIN2_Pin|AIN1_Pin|BIN1_Pin|BIN2_Pin, GPIO_PIN_RESET);

  /*Configure GPIO pin Output Level */
  HAL_GPIO_WritePin(GPIOD, GPIO_PIN_13, GPIO_PIN_RESET);

  /*Configure GPIO pins : OLED_SCL_Pin OLED_SDA_Pin OLED_RST_Pin OLED_DC_Pin
                           LED3_Pin */
  GPIO_InitStruct.Pin = OLED_SCL_Pin|OLED_SDA_Pin|OLED_RST_Pin|OLED_DC_Pin
                          |LED3_Pin;
  GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
  GPIO_InitStruct.Pull = GPIO_NOPULL;
  GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_VERY_HIGH;
  HAL_GPIO_Init(GPIOE, &GPIO_InitStruct);

  /*Configure GPIO pins : AIN2_Pin AIN1_Pin BIN1_Pin BIN2_Pin */
  GPIO_InitStruct.Pin = AIN2_Pin|AIN1_Pin|BIN1_Pin|BIN2_Pin;
  GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
  GPIO_InitStruct.Pull = GPIO_NOPULL;
  GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_VERY_HIGH;
  HAL_GPIO_Init(GPIOA, &GPIO_InitStruct);

  /*Configure GPIO pin : Trig_Pin_Pin */
  GPIO_InitStruct.Pin = Trig_Pin_Pin;
  GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
  GPIO_InitStruct.Pull = GPIO_NOPULL;
  GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_LOW;
  HAL_GPIO_Init(Trig_Pin_GPIO_Port, &GPIO_InitStruct);

  /*Configure GPIO pin : PD13 */
  GPIO_InitStruct.Pin = GPIO_PIN_13;
  GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
  GPIO_InitStruct.Pull = GPIO_NOPULL;
  GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_LOW;
  HAL_GPIO_Init(GPIOD, &GPIO_InitStruct);

}

/* USER CODE BEGIN 4 */

void HAL_UART_RxCpltCallback(UART_HandleTypeDef *huart)
{
	/*Prevent unused arg compilation warning*/
	UNUSED(huart);
	HAL_UART_Transmit(&huart3, aRxBuffer, 4, 0xFFFFF);

	if (flag==0){
		dir = (char) aRxBuffer[0];
		magnitude = (int)(aRxBuffer[1] - '0') * 100 + (int)(aRxBuffer[2] - '0') * 10 + (int)(aRxBuffer[3] - '0');
//		sprintf(magText, "%4d\0", magnitude);
//		OLED_ShowString(10, 50, magText);
		flag = 1;
	}
	HAL_UART_Receive_IT(&huart3, (uint8_t *) aRxBuffer, 4);
}

void HAL_TIM_IC_CaptureCallback(TIM_HandleTypeDef *htim)
{
	if (htim->Channel == HAL_TIM_ACTIVE_CHANNEL_1)  // if the interrupt source is channel1
	{
		if (Is_First_Captured==0) // if the first value is not captured
		{
			IC_Val1 = HAL_TIM_ReadCapturedValue(htim, TIM_CHANNEL_1); // read the first value
			Is_First_Captured = 1;  // set the first captured as true
			// Now change the polarity to falling edge
			__HAL_TIM_SET_CAPTUREPOLARITY(htim, TIM_CHANNEL_1, TIM_INPUTCHANNELPOLARITY_FALLING);
		}

		else if (Is_First_Captured==1)   // if the first is already captured
		{
			IC_Val2 = HAL_TIM_ReadCapturedValue(htim, TIM_CHANNEL_1);  // read second value
			__HAL_TIM_SET_COUNTER(htim, 0);  // reset the counter

			if (IC_Val2 > IC_Val1)
			{
				Difference = IC_Val2-IC_Val1;
			}

			else if (IC_Val1 > IC_Val2)
			{
				Difference = (65535 - IC_Val1) + IC_Val2;
			}

			Distance = Difference * .034/2;
			Is_First_Captured = 0; // set it back to false

			// set polarity to rising edge
			__HAL_TIM_SET_CAPTUREPOLARITY(htim, TIM_CHANNEL_1, TIM_INPUTCHANNELPOLARITY_RISING);
			__HAL_TIM_DISABLE_IT(&htim4, TIM_IT_CC1);
		}
	}
}

/* USER CODE END 4 */

/* USER CODE BEGIN Header_StartDefaultTask */
/**
  * @brief  Function implementing the defaultTask thread.
  * @param  argument: Not used
  * @retval None
  */
/* USER CODE END Header_StartDefaultTask */
void StartDefaultTask(void *argument)
{
  /* USER CODE BEGIN 5 */

  /* Infinite loop */
  for(;;)
  {
	//HAL_UART_Receive_IT(&huart3, aRxBuffer, 1);
	HAL_GPIO_TogglePin(LED3_GPIO_Port, LED3_Pin);
//	goose(0, 1);

	osDelay(1000);
  }
  /* USER CODE END 5 */
}

/* USER CODE BEGIN Header_encoder */
/**
* @brief Function implementing the EncoderTask thread.
* @param argument: Not used
* @retval None
*/
/* USER CODE END Header_encoder */
void encoder(void *argument)
{
  /* USER CODE BEGIN encoder */
  int cntLA=0,cntRB=0;
  int dirLA,dirRB;

  uint8_t logText[20];

  uint32_t tick = HAL_GetTick();
  /* Infinite loop */
  for(;;)
  {
	  // every 1s, give me the number of pulse
	  // when pulse(diffA) reach 1320 -> 1 revolution

	  if (HAL_GetTick() - tick >= 100) {

		  cntLA = __HAL_TIM_GET_COUNTER(&htim2); // counting down
		  cntRB = __HAL_TIM_GET_COUNTER(&htim3); // counting up

		  if (cntLA > 32768) { // forward
			  dirLA = 1;
			  pulseLA = 65535 - cntLA;
		  } else { //back
			  dirLA = -1;
			  pulseLA = cntLA;
		  }

		  if (cntRB > 32768) { // back
			  dirRB = -1;
			  pulseRB = 65535 - cntRB;
		  } else { // forward
			  dirRB = 1;
			  pulseRB = cntRB;
		  }

	      __HAL_TIM_SET_COUNTER(&htim2, 0);
	      __HAL_TIM_SET_COUNTER(&htim3, 0);
	      speedLA = (21.60 * pulseLA/1540.0)*1000.0/(double)(HAL_GetTick() - tick);
	      speedRB = (21.60 * pulseRB/1550.0) *1000.0/(double)(HAL_GetTick() - tick);

	      tick = HAL_GetTick();
	  }

	  distLA += pulseLA;
	  distRB += pulseRB;

	  travelD = distLA + distRB;
//	sprintf(logText, "global:%5d\0", globalD);
//	OLED_ShowString(10, 20, logText);

	sprintf(logText, "spD%5d-%5d\0",(int) speedLA, (int) speedRB);
	OLED_ShowString(10, 20, logText);

//	sprintf(logText, "globD:%5d\0", travelD);
//	OLED_ShowString(10, 20, logText);

	osDelay(100);

  }
  /* USER CODE END encoder */
}

/* USER CODE BEGIN Header_correction */
/**
* @brief Function implementing the CorrectionTask thread.
* @param argument: Not used
* @retval None
*/
/* USER CODE END Header_correction */
void correction(void *argument)
{
  /* USER CODE BEGIN correction */

//  int newEncVal = 0;
  double tarEnc = 60; // target pwm is about 3500+ for 60 , inner wheel -25
  	  	 	 	 	  // target pwm indoor 30

//  double tarEncA=0, tarEncB=0; // out for debugging

  uint8_t t2 = 'p';
  servoCenter();

  /* Infinite loop */
  for(;;)
  {
	  if (flag) {

		  gyroStart();
		  osDelay(1);
		  total_angle=0;
		  osDelay(1);

		  switch (dir) {
			case 'w':
				moveForward(magnitude -20, tarEnc+20); // speed 40 for outside

				offset = magnitude - (travelD * (21.60 / 1550.0) / 2.0);
				resetDisVal();

				osDelay(200);
				/* Overshoot the offset*/
				moveForward(offset, tarEnc-5); // speed 25
				/* End of overshoot */

				resetDisVal();
				offset = 0;
//				memset(aRxBuffer, 0, 4);
				break;
			case 's':
				moveBackward(magnitude -20, tarEnc+10); // speed 35 for outside

				offset = magnitude - (travelD * (21.60 / 1550.0) / 2.0);
				resetDisVal();

				osDelay(200);
//				/* Overshoot the offset */
				moveBackward(offset, tarEnc-5); // speed 25
				/* End of Overshoot */

				resetDisVal();
				offset = 0;
				break;
			case 'q':
				moveForward(2, tarEnc);
				resetDisVal();

				osDelay(200);

				moveLeft(magnitude, 20, 1); // speed 30 -> 20
				resetDisVal();
				total_angle=0;

				break;
			case 'e':
				moveBackward(2, tarEnc);
				resetDisVal();

				osDelay(200);

				moveRight(magnitude, 20, 1); // speed 20
				resetDisVal();
				total_angle = 0;

				osDelay(200);

				moveBackward(4, tarEnc);
				resetDisVal();

				break;
			case 'a':
				moveLeft(magnitude, 20, -1);
				resetDisVal();

				total_angle=0;
				osDelay(200);

				moveBackward(2, tarEnc);
				resetDisVal();
				break;
			case 'd':
				moveForward(4, tarEnc);
				resetDisVal();

				osDelay(200);

				moveRight(magnitude, 20, -1);
				resetDisVal();

				total_angle=0;
				break;
			case 'y':
				/* moveForward2 */
				moveForward(170, 50);

//				vertDist = (travelD * (21.60 / 1550.0) / 2.0); // maybe just fk it and use ult to measure?
				resetDisVal();
				osDelay(200);

				corrByRev(20, 40);

//				HAL_UART_Transmit(&huart3, (uint8_t *)&t2, 1, 0xFFFF);
				break;
			case 't': // first obst LEFT arrow

//				// turnleft 45 degree of True north
//				compassTurn(45, 0);
//				osDelay(200);
//				resetDisVal();
//
//				// turnRight 25 degree of True north
//				compassTurn(-30,0);
//				osDelay(200);
//				resetDisVal();
//
//				// drive forward with hal_tim_setcompare,
//				toDriveFront();
//				__HAL_TIM_SetCompare(&htim8,TIM_CHANNEL_1,1500);
//				__HAL_TIM_SetCompare(&htim8,TIM_CHANNEL_2,1500);
//				cumErrAng=0;
//				while(1) {
//					goose(0, 1);
//					if (Distance <= 15){ // stop if see second obstacle
//						__HAL_TIM_SetCompare(&htim8,TIM_CHANNEL_1,0);
//						__HAL_TIM_SetCompare(&htim8,TIM_CHANNEL_2,0);
//						break;
//					}
//					osDelay(10);
//				}

				moveLeft(45, 30, 1);
				resetDisVal();
				total_angle = 0;

				osDelay(200);

				moveRight(45, 30, 1);
				resetDisVal();
				total_angle = 0;

				osDelay(200);

				moveForward(10, 40);
				resetDisVal();

				osDelay(200);

				moveRight(45, 30, 1);
				resetDisVal();
				total_angle = 0;

				osDelay(200);

				moveLeft(45, 30, 1);
				resetDisVal();
				total_angle = 0;

				moveForward(150, 50);
				resetDisVal();
				osDelay(200);

				// reverse abit for turning purpose
				corrByRev(20 ,30);

				break;
			case 'u': // first obst RIGHT arrow
//				compassTurn(-45, 0);
//				osDelay(200);
//				resetDisVal();
//
//				compassTurn(30,0);
//				osDelay(200);
//				resetDisVal();
//
//				// drive forward with hal_tim_setcompare, TODO: swap to moveFoward with goose
//				toDriveFront();
//				__HAL_TIM_SetCompare(&htim8,TIM_CHANNEL_1,1500);
//				__HAL_TIM_SetCompare(&htim8,TIM_CHANNEL_2,1500);
//				cumErrAng=0;
//				while(1) {
//					goose(0, 1);
//					if (Distance <= 15){ // stop if see second obstacle
//						__HAL_TIM_SetCompare(&htim8,TIM_CHANNEL_1,0);
//						__HAL_TIM_SetCompare(&htim8,TIM_CHANNEL_2,0);
//						break;
//					}
//					osDelay(10);
//				}

				moveRight(45, 30, 1);
				resetDisVal();
				total_angle = 0;

				osDelay(200);

				moveLeft(45, 30, 1);
				resetDisVal();
				total_angle = 0;

				osDelay(200);

				moveForward(10, 40);
				resetDisVal();

				osDelay(200);

				moveLeft(45, 30, 1);
				resetDisVal();
				total_angle = 0;

				osDelay(200);

				moveRight(45, 30, 1);
				resetDisVal();
				total_angle = 0;

				moveForward(150, 50);
				resetDisVal();
				osDelay(200);

				// reverse abit for turning purpose
				corrByRev(20, 30);


				break;
			case 'g': // second obstacle LEFT
				moveLeft(90, 30, 1);
				resetDisVal();

				osDelay(200);
				total_angle = 0;

				// reverse abit, so dont wide turn
				moveBackward(7, 30);
				resetDisVal();
				osDelay(200);

				// check for any empty space, if empty space turn
				if (IRdistR > 50) {
					moveRight(180, 30, 1);
				} else { // else forward2
					forward2(30, 30);
					resetDisVal();

					osDelay(200);

					moveRight(180, 30, 1);
				}
				resetDisVal();
				total_angle = 0;
				osDelay(200);

				// get into horizontal back
				moveStop(); // why tho
				ogForward(20, 30);
				resetDisVal();

				osDelay(200);

				// apporaching end of sec obs check for any empty space, if empty space turn
				if (IRdistR > 50) {
					moveRight(90, 30, 1);
				} else { // else forward2
					forward2(30, 30);
					resetDisVal();

					osDelay(200);

					moveRight(90, 30, 1);
				}
				resetDisVal();
				total_angle = 0;
				osDelay(200);

				// drive abit toward parking , clear the sec obs
				moveStop();
				ogForward(30, 50);
				resetDisVal();
				osDelay(200);

				// move forward till u see the first obstacle
				moveStop();
				forward3(150, 70, 0); // looking right into the arena
				resetDisVal();

				osDelay(200);

				// drive abit to clear , clear first obs on the way back
				moveStop();
				moveForward(10, 50);
				osDelay(200);

				// turn in to aim at the park
				moveStop();
				moveRight(90, 30, 1);
				resetDisVal();
				total_angle=0;

				osDelay(200);

				// some minor adjustment
				moveBackward(12, 40);

				moveLeft(90, 30, 1);
				resetDisVal();
				total_angle=0;

				// go back to carpark
				finalForward(150, 40);

				break;
			case 'h': // second obstacle RIGHT
				moveRight(90, 30, 1);
				resetDisVal();

				osDelay(200);
				total_angle = 0;

				// reverse abit, so dont wide turn
				moveStop();
				moveBackward(7, 30);
				resetDisVal();
				osDelay(200);

				// check for any empty space, if empty space turn
				if (IRdistL > 50) {
					moveLeft(180, 30, 1);
				} else { // else forward2
					forward2(30, 30);
					resetDisVal();

					osDelay(200);

					moveLeft(180, 30, 1);
				}
				resetDisVal();
				total_angle = 0;
				osDelay(200);

				// get into horizontal back
				moveStop(); // why tho
				ogForward(20, 30);
				resetDisVal();

				osDelay(200);

				// check for any empty space, if empty space turn
				if (IRdistL > 50) {
					moveLeft(90, 30, 1);
				} else { // else forward2
					forward2(30, 30);
					resetDisVal();

					osDelay(200);

					moveLeft(90, 30, 1);
				}
				resetDisVal();
				total_angle = 0;
				osDelay(200);

				// drive abit to the parking, clear sec obs
				ogForward(30, 50);
				resetDisVal();
				osDelay(200);

				// move forward till u see the first obstacle
				moveStop();
				forward3(150, 70, 1); // looking left into the arena
				resetDisVal();

				osDelay(200);

				// drive abit to clear , clear first obs on the way back
				moveStop();
				moveForward(10, 70);
				resetDisVal();

				osDelay(200);

				// turn in to aim at the park
				moveStop();
				moveLeft(90, 30, 1);
				resetDisVal();
				total_angle=0;

				osDelay(200);

				// some minor adjustment
				moveStop();
				moveBackward(12, 40);
				resetDisVal();

				osDelay(200);

				moveRight(90, 30, 1);
				resetDisVal();
				total_angle=0;

				// go back to carpark
				finalForward(150, 40);

				break;
			case 'p':
				// check for any empty space, if empty space turn
				if (IRdistR > 50) {
					moveRight(180, 30, 1);
				} else { // else forward2
					forward2(30, 30);
					resetDisVal();

					osDelay(200);

					moveRight(180, 30, 1);
				}
				resetDisVal();
				total_angle = 0;
				osDelay(200);

				// get into horizontal back

				ogForward(20, 30);
				resetDisVal();
				break;
		  }
			servoCenter();
			resetDisVal();

			gyroStart();
			total_angle = 0;

			flag = 0;

			// return to rpi after we done
			uint8_t ch = 'k';
			HAL_UART_Transmit(&huart3, (uint8_t *)&ch, 1, 0xFFFF);
	  }
    osDelay(10);
  }
  /* USER CODE END correction */
}

/* USER CODE BEGIN Header_direction */
/**
* @brief Function implementing the DirectTask thread.
* @param argument: Not used
* @retval None
*/
/* USER CODE END Header_direction */
void direction(void *argument)
{
  /* USER CODE BEGIN direction */

  /* Infinite loop */
  for(;;)
  {
	  //input
	  OLED_ShowString(10,10, aRxBuffer);

	  // 10, 20 ----- print  speed

	  // 10, 30 ----- print local angle sth like true north
		sprintf(logText, "gAng:%3d A:%2d\0", (int) debugAng, (int) angular_speed);
		OLED_ShowString(10, 30, logText);
//		sprintf(logText, "spd: %3d-%3d\0", (int)tarEncA, (int)tarEncB);
//		OLED_ShowString(10, 30, logText);
//		sprintf(logText, "Aspd: %3d\0", (int) angular_speed);
//		OLED_ShowString(10, 40, logText);
		sprintf(logText, "ir: %4d-%4d\0", (int) IRdistL, (int) IRdistR);
		OLED_ShowString(10, 40, logText);

//		sprintf(logText, "dma: %3d\0", flag);
//		OLED_ShowString(10, 40, logText);

//		sprintf(logText, "trvD:%5d\0", travelD);
//		OLED_ShowString(10, 50, logText);
//		sprintf(logText, "%5d=%5d\0", (int)pwmLA,(int)pwmRB);
//		OLED_ShowString(10, 50, logText);
		sprintf(logText, "ult:%3d v:%3d\0", (int)Distance, (int) vertDist);
		OLED_ShowString(10, 50, logText);
//		sprintf(logText, "totalAng: %3d\0", (int) total_angle);
//		OLED_ShowString(10, 50, logText);

	OLED_Refresh_Gram();
    osDelay(100);
  }
  /* USER CODE END direction */
}

/* USER CODE BEGIN Header_gyroIMU */
/**
* @brief Function implementing the GyroTask thread.
* @param argument: Not used
* @retval None
*/
/* USER CODE END Header_gyroIMU */
void gyroIMU(void *argument)
{
  /* USER CODE BEGIN gyroIMU */
	uint8_t val[2] = {0, 0};
	double gyro_drift = 0;
	char hello[20];
//	int16_t angular_speed = 0;
	uint32_t tick = 0;

	gyro_drift = gyro_Calibrate();
//	uint8_t status = IMU_Initialise(&imu, &hi2c1, &huart3);

	tick = HAL_GetTick();
	osDelayUntil(10);
  /* Infinite loop */
  for(;;)
  {
	  if (HAL_GetTick() - tick >= 100) {

		readByte(0x37, val);
		angular_speed = (val[0] << 8) | val[1];

//		status = IMU_GyroRead(&imu);
//		angular_speed = imu.gyro[2];
//		total_angle += (double)(angular_speed) * (HAL_GetTick() - tick);
//		sprintf(logText, "%d \n\r", (int) imu.gyro[2]);
//		HAL_UART_Transmit(&huart3, logText, sizeof(logText) -1, HAL_MAX_DELAY);

		total_angle += (double)(angular_speed - gyro_drift) * ((HAL_GetTick() - tick) / 16400.0) * 1;
		debugAng += (double)(angular_speed - gyro_drift) * ((HAL_GetTick() - tick) / 16400.0) * 1;

		// prevSpeed = angular_speed;
		if (total_angle >= 720) total_angle = 0;
		if (total_angle <= -720) total_angle = 0;

		tick = HAL_GetTick();
		}
	      osDelay(10);
	      // osDelayUntil(500); //knn why this stop my gyro from working sometime
  }
  /* USER CODE END gyroIMU */
}

/* USER CODE BEGIN Header_irTask */
/**
* @brief Function implementing the irsense thread.
* @param argument: Not used
* @retval None
*/
/* USER CODE END Header_irTask */
void irTask(void *argument)
{
  /* USER CODE BEGIN irTask */
  HAL_ADC_Start_DMA(&hadc1, lengthFront, 4096);
  HAL_ADC_Start_DMA(&hadc2, lengthSide, 4096);


  uint16_t lastFront = 0;
  uint16_t lastSide = 0;

  /* Infinite loop */
  for(;;)
  {
	HCSR04_Read();

//    uint16_t tempA = rightIR;
//    uint16_t tempB = IRdistR;

	rightIR = (lengthSide[0] + lengthSide[1] + lengthSide[2] + lengthSide[3] + lengthSide[4])/5.0;
	IRdistR = 36540 * (1/(double)rightIR) - 2.6616;

	leftIR = (lengthFront[0] + lengthFront[1] + lengthFront[2] + lengthFront[3] + lengthFront[4])/5.0;
	IRdistL = 39272 * (1/(double)leftIR) - 3.8054;

//    lastFront = tempA;
//    lastSide = tempB;

    osDelay(200);
  }
  /* USER CODE END irTask */
}

/**
  * @brief  This function is executed in case of error occurrence.
  * @retval None
  */
void Error_Handler(void)
{
  /* USER CODE BEGIN Error_Handler_Debug */
  /* User can add his own implementation to report the HAL error return state */
  __disable_irq();
  while (1)
  {
  }
  /* USER CODE END Error_Handler_Debug */
}

#ifdef  USE_FULL_ASSERT
/**
  * @brief  Reports the name of the source file and the source line number
  *         where the assert_param error has occurred.
  * @param  file: pointer to the source file name
  * @param  line: assert_param error line source number
  * @retval None
  */
void assert_failed(uint8_t *file, uint32_t line)
{
  /* USER CODE BEGIN 6 */
  /* User can add his own implementation to report the file name and line number,
     ex: printf("Wrong parameters value: file %s on line %d\r\n", file, line) */
  /* USER CODE END 6 */
}
#endif /* USE_FULL_ASSERT */
