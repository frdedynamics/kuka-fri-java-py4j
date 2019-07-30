package com.mojo.ioaccess;



import java.util.Arrays;
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
    private static CMD_MODES CMD_MODE = CMD_MODES.JOINT_POSITION;
    private static double[] CMD_POSITIONS = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
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
        Logger.getAnonymousLogger().info("IOAccessClient initialized: \n"
                + "joint position command: " + Arrays.toString(CMD_POSITIONS));
    }

    @Override
    public void onStateChange(FRISessionState newState, FRISessionState oldState)
    {
        switch (newState)
        {
            case MONITORING_WAIT:
            case MONITORING_READY:
            case COMMANDING_WAIT:
            case COMMANDING_ACTIVE:
            default:
            {
                break;
            }
        }
    }

    @Override
    public void command()
    {
        switch (CMD_MODE) {
            case JOINT_POSITION:
                getRobotCommand().setJointPosition(CMD_POSITIONS);
                break;

            case JOINT_TORQUE:
                getRobotCommand().setTorque(CMD_TORQUE);
                break;

            default: {
                break;
            }
        }
    }

}
