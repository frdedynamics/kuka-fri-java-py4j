package com.mojo.ioaccess;



import java.util.concurrent.TimeUnit;

import com.kuka.connectivity.fastRobotInterface.clientSDK.clientLBR.LBRClient;

/**
 * Example client for Fieldbus access.
 */
public class IOAccessClient extends LBRClient
{
    private long startTime;
    private static final double ANALOG_MAX = 1;
    private static final double ANALOG_MIN = 0;
    private static final long DIGITAL_MAX = 60;
    private static final long DIGITAL_MIN = 0;

    private static final double STEP = 0.1;

    private static boolean ioPrev = false;

    /**
     * Public methods for getting data
     * @return
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

    /**
     * end
     */

    public IOAccessClient()
    {
        startTime = System.nanoTime();
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
    public void monitor()
    {
        getAndSetExample();
    }

    @Override
    public void waitForCommand()
    {
        getAndSetExample();
    }

    @Override
    public void command()
    {
        getAndSetExample();
    }

    private void getAndSetExample()
    {
        // LIMIT
        double tempAnalog = getRobotState().getAnalogIOValue("FRI.Out_Analog_Deci_Seconds");
        if (tempAnalog > ANALOG_MAX)
        {
            getRobotCommand().setAnalogIOValue("FRI.Out_Analog_Deci_Seconds", ANALOG_MAX);
        }
        else if (tempAnalog < ANALOG_MIN)
        {
            getRobotCommand().setAnalogIOValue("FRI.Out_Analog_Deci_Seconds", ANALOG_MIN);
        }

        long tempDigital = getRobotState().getDigitalIOValue("FRI.Out_Integer_Seconds");
        if (tempDigital > DIGITAL_MAX)
        {
            getRobotCommand().setDigitalIOValue("FRI.Out_Integer_Seconds", DIGITAL_MAX);
        }
        else if (tempDigital < DIGITAL_MIN)
        {
            getRobotCommand().setDigitalIOValue("FRI.Out_Integer_Seconds", DIGITAL_MIN);
        }

        boolean isEnabled = getRobotState().getBooleanIOValue("FRI.In_Bool_Clock_Enabled");

        boolean ioNow = getRobotState().getBooleanIOValue("MediaFlange.InputX3Pin10");
        if (ioNow != ioPrev){
            System.out.println("gripper change..");
        }
        ioPrev = ioNow;

        if (isEnabled)
        {
            long now = System.nanoTime();
            long difference = now - startTime;
            int milliSecDiff = (int) (TimeUnit.NANOSECONDS.toMillis(difference));
            if (milliSecDiff >= 100)
            {
                double analogValue = getRobotState().getAnalogIOValue("FRI.Out_Analog_Deci_Seconds");
                analogValue = analogValue + STEP;
                if (analogValue < ANALOG_MAX)
                {
                    getRobotCommand().setAnalogIOValue("FRI.Out_Analog_Deci_Seconds", analogValue);
                }
                else
                {
                    getRobotCommand().setAnalogIOValue("FRI.Out_Analog_Deci_Seconds", ANALOG_MIN);

                    long digitalValue = getRobotState().getDigitalIOValue("FRI.Out_Integer_Seconds") + 1;
                    if (digitalValue < DIGITAL_MAX)
                    {
                        getRobotCommand().setDigitalIOValue("FRI.Out_Integer_Seconds", digitalValue);
                    }
                    else
                    {
                        getRobotCommand().setDigitalIOValue("FRI.Out_Integer_Seconds", DIGITAL_MIN);
                    }
                }
                startTime = now;
            }
        }
    }
}
