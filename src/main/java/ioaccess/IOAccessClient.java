package ioaccess;

import com.kuka.connectivity.fastRobotInterface.clientSDK.clientLBR.LBRClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;


enum CMD_MODES {
    JOINT_POSITION,
    JOINT_TORQUE
}

/**
 * Example client for Fieldbus access.
 */
@SuppressWarnings("unused")
public class IOAccessClient extends LBRClient
{
    private static final Logger blogger = LoggerFactory.getLogger(IOAccessClient.class);

    private static boolean started = false;
    private long startTime;
    private static double secLast;
    private static double direction = -1.0;
    private static double[] measuredJointPositionLast;
    private static CMD_MODES cmdMode = CMD_MODES.JOINT_TORQUE;
    private static double[] cmdPositions;
    private static double[] cmdTorque = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

    /**
     * Public methods for getting data
     * @return
     * maybe return something when stuff is set
     */
    @SuppressWarnings("WeakerAccess")
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

    public double[] getMeasurementsPacket(){
        double[] retVal = new double[16];
        retVal[0] = Double.valueOf(getRobotState().getTimeStampSec());
        retVal[1] = Double.valueOf(getRobotState().getTimeStampNanoSec());
        int joint_index = 0;
        for (int i=2; i<9; i++){
            retVal[i] = getRobotState().getMeasuredJointPosition()[joint_index];
            joint_index = joint_index + 1;
        }
        joint_index = 0;
        for (int i=9; i<16; i++){
            retVal[i] = getRobotState().getMeasuredTorque()[joint_index];
            joint_index = joint_index + 1;
        }

        return retVal;
    }
    public void setCommandedJointPosition(double[] positions) {
        if (cmdPositions != positions){
            blogger.info("New joint position command. Old: {}, new: {}", cmdPositions, positions);

        }
        cmdPositions = positions;
    }

    public void setCommandedJointTorque(double[] torques) {
        if (cmdTorque != torques){
            blogger.info("New joint position command. Old: {}, new: {}", cmdTorque, torques);

        }
        cmdTorque = torques;
    }

    /**
     * end
     */

    IOAccessClient()
    {
        startTime = System.nanoTime();
        blogger.info("IOAccessClient initialized with COMMAND MODE: {},  joint position command: {}, joint torque " +
                "command: {}.", cmdMode, cmdPositions, cmdTorque);

    }

    @Override
    public void onStateChange(FRISessionState newState, FRISessionState oldState)
    {
        switch (newState)
        {
            case MONITORING_WAIT:
            case MONITORING_READY:
                if (!started){
                    measuredJointPositionLast = getRobotState().getMeasuredJointPosition();
                    secLast = (double)getRobotState().getTimeStampSec() +
                            ((double)getRobotState().getTimeStampNanoSec()/1000000000.0);
                    cmdPositions = getRobotState().getCommandedJointPosition();

                    blogger.info("MONITORING READY with measuredJointPosition: {}, time[sec]: {}, cmdPositions: {}.",
                            measuredJointPositionLast, secLast, cmdPositions);
                    started = true;
                }
                break;

            case COMMANDING_WAIT:
            case COMMANDING_ACTIVE:
            default:
                break;
        }
    }

    @Override
    public void waitForCommand(){
        super.waitForCommand();

        if (getRobotState().getClientCommandMode() == ClientCommandMode.TORQUE){
            getRobotCommand().setTorque(cmdTorque);
        }

    }

    @Override
    public void command()
    {
        super.command();

        switch (cmdMode) {
            case JOINT_POSITION:
                getRobotCommand().setJointPosition(cmdPositions);
                break;

            case JOINT_TORQUE:
                if (getRobotState().getClientCommandMode() == ClientCommandMode.TORQUE){

                    double secNow = (double)getRobotState().getTimeStampSec() +
                            ((double)getRobotState().getTimeStampNanoSec()/1000000000.0);
                    double secDelta = secNow - secLast;
                    if (secDelta > 0.006 || secDelta < 0.004){
                        secDelta = 0.005;
                    }
                    secLast = secNow;

                    if ((int)(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()- startTime)) > 5000){
                        direction = -direction;
                        startTime = System.nanoTime();
                    }

                    Arrays.fill(cmdTorque, 0.0);
                    double[] measuredJointPosition = getRobotState().getMeasuredJointPosition();
                    cmdTorque[6] = sweepAxis(secDelta, measuredJointPosition[6], measuredJointPositionLast[6],
                            direction);
                    measuredJointPositionLast = measuredJointPosition;


                    cmdPositions = getRobotState().getCommandedJointPosition();
                    cmdPositions[6] = measuredJointPosition[6] + 0.01;

                    getRobotCommand().setJointPosition(cmdPositions);
                    getRobotCommand().setTorque(cmdTorque);
                }

                break;

            default:
                break;

        }
    }

    private double sweepAxis(double secondsDelta, double measuredJointPosition, double measuredJointPositionLast,
                             double direction){

        double maxTau = 0.7;
        double desired = 90.0;
        double err = (direction*desired) - (measuredJointPosition*180.0/Math.PI);
        double errDt = 0.0 - (measuredJointPosition-measuredJointPositionLast)*180.0/Math.PI/secondsDelta;
        double kp = 0.8;
        double kd = 0.5;

        double tau = kp*err + kd*errDt;

        if (tau > maxTau){
            tau = maxTau;
        }
        if (tau < -maxTau){
            tau = -maxTau;
        }

        return tau;
    }

}
