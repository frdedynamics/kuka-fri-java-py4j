package com.mojo.ioaccess;



import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.kuka.connectivity.fastRobotInterface.clientSDK.clientLBR.LBRClient;

enum CMD_MODES {
    JOINT_POSITION,
    JOINT_TORQUE
}
/**
 * Example client for Fieldbus access.
 */
public class IOAccessClient extends LBRClient
{
    private static boolean STARTED = false;
    private long _startTime;
    private static double SEC_LAST;
    private static double DIRECTION = -1.0;
    private static double[] MEASURED_JOINT_POSITION_LAST;
    private static CMD_MODES CMD_MODE = CMD_MODES.JOINT_TORQUE;
    private static double[] CMD_POSITIONS;
    private static double[] CMD_TORQUE = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

    /**
     * Public methods for getting data
     * @return
     * maybe return something when stuff is set
     */
    public boolean getTrueBool() {
        return true;
    }

    public int[] getTimeStampSecNanoSec() {
        return new int[] {getRobotState().getTimeStampSec(), getRobotState().getTimeStampNanoSec()};
    }

    public boolean getMediaFlangeInputX3Pin10() {
        return getRobotState().getBooleanIOValue("MediaFlange.InputX3Pin10");
    }

    public double[] getMeasuredJointPosition() {
        return getRobotState().getMeasuredJointPosition();
    }

    public double[] getCommandedJointPosition() {
        return getRobotState().getCommandedJointPosition();
    }

    public double[] getIpoJointPosition() {
        return getRobotState().getIpoJointPosition();
    }

    public double[] getMeasuredTorque() {
        return getRobotState().getMeasuredTorque();
    }

    public double[] getCommandedTorque() {
        return getRobotState().getCommandedTorque();
    }

    public double[] getExternalTorque() {
        return getRobotState().getExternalTorque();
    }


    public void setCommandedJointPosition(double[] positions) {
        if (CMD_POSITIONS != positions){
            Logger.getAnonymousLogger().info("new joint position command: \n"
                    + "old: " + Arrays.toString(CMD_POSITIONS) + "\n"
                    + "new: " + Arrays.toString(positions));
        }
        CMD_POSITIONS = positions;
    }

    public void setCommandedJointTorque(double[] torques) {
        if (CMD_TORQUE != torques){
            Logger.getAnonymousLogger().info("new joint torque command: \n"
                    + "old: " + Arrays.toString(CMD_TORQUE) + "\n"
                    + "new: " + Arrays.toString(torques));
        }
        CMD_TORQUE = torques;
    }

    /**
     * end
     */

    IOAccessClient()
    {
        _startTime = System.nanoTime();
        Logger.getAnonymousLogger().info("IOAccessClient initialized: \n"
                + "COMMAND MODE: " + CMD_MODE.toString() + "\n"
                + "joint position command: " + Arrays.toString(CMD_POSITIONS) + "\n"
                + "joint torque command: " + Arrays.toString(CMD_TORQUE) + "\n");
    }

    @Override
    public void onStateChange(FRISessionState newState, FRISessionState oldState)
    {
        switch (newState)
        {
            case MONITORING_WAIT:
            case MONITORING_READY:
                if (!STARTED){
                    MEASURED_JOINT_POSITION_LAST = getRobotState().getMeasuredJointPosition();
                    SEC_LAST = (double)getRobotState().getTimeStampSec() +
                            ((double)getRobotState().getTimeStampNanoSec()/1000000000.0);
                    CMD_POSITIONS = getRobotState().getCommandedJointPosition();
                    Logger.getAnonymousLogger().info("MONIITORNG READY with: \n" +
                            "MEASURED_JOINT_POSITION " + Arrays.toString(MEASURED_JOINT_POSITION_LAST) + "\n" +
                            "TIME " + SEC_LAST + "\n" +
                            "CMD_POSITIONS " + Arrays.toString(CMD_POSITIONS) + "\n");
                    STARTED = true;
                }

            case COMMANDING_WAIT:
            case COMMANDING_ACTIVE:
            default:
            {
                break;
            }
        }
    }

    @Override
    public void waitForCommand(){
        super.waitForCommand();

        if (getRobotState().getClientCommandMode() == ClientCommandMode.TORQUE){
            getRobotCommand().setTorque(CMD_TORQUE);
        }

    }

    @Override
    public void command()
    {
        super.command();

        switch (CMD_MODE) {
            case JOINT_POSITION:
                getRobotCommand().setJointPosition(CMD_POSITIONS);
                break;

            case JOINT_TORQUE:
                if (getRobotState().getClientCommandMode() == ClientCommandMode.TORQUE){

                    double SEC_NOW = (double)getRobotState().getTimeStampSec() +
                            ((double)getRobotState().getTimeStampNanoSec()/1000000000.0);
                    double SEC_DELTA = SEC_NOW - SEC_LAST;
                    if (SEC_DELTA > 0.006 || SEC_DELTA < 0.004){
                        SEC_DELTA = 0.005;
                    }
                    SEC_LAST = SEC_NOW;

                    if ((int)(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-_startTime)) > 5000){
                        DIRECTION = -DIRECTION;
                        _startTime = System.nanoTime();
                    }

                    Arrays.fill(CMD_TORQUE, 0.0);
                    double[] measured_joint_position = getRobotState().getMeasuredJointPosition();
                    CMD_TORQUE[6] = sweepAxis(SEC_DELTA, measured_joint_position[6], MEASURED_JOINT_POSITION_LAST[6],
                            DIRECTION);
                    MEASURED_JOINT_POSITION_LAST = measured_joint_position;


                    CMD_POSITIONS = getRobotState().getCommandedJointPosition();
                    CMD_POSITIONS[6] = measured_joint_position[6] + 0.01;

                    getRobotCommand().setJointPosition(CMD_POSITIONS);
                    getRobotCommand().setTorque(CMD_TORQUE);
                }

                break;

            default: {
                break;
            }
        }
    }

    private double sweepAxis(double sec_delta, double measured_joint_position, double measured_joint_position_last,
                             double direction){

        double max_tau = 0.7;
        double desired = 90.0;
        double err = (direction*desired) - (measured_joint_position*180.0/Math.PI);
        double err_dt = 0.0 - (measured_joint_position-measured_joint_position_last)*180.0/Math.PI/sec_delta;
        double Kp = 0.8;
        double Kd = 0.5;

        double tau = Kp*err + Kd*err_dt;

        if (tau > max_tau){
            tau = max_tau;
        }
        if (tau < -max_tau){
            tau = -max_tau;
        }

        return tau;
    }

}
