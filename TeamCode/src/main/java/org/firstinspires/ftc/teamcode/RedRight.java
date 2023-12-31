package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;

@Autonomous(name = "Red Right")
@Config
public class RedRight extends LinearOpMode {

    public static Pose2d STARTING_POSITION = new Pose2d(37, -60, Math.toRadians(90));
    public static int JUNCTION_LEVEL = 1;
    public static int ELEVATOR_HOLD_ITERATIONS = 20;
    public static double ARM_POSITION = 0.985;

    public static int STEP1_STRAFE_RIGHT = 33;
    public static int STEP2_FORWARD = 42;
    public static int STEP3_STRAFE_LEFT = 5;
    public static int STEP4_STRAFE_RIGHT = 2;
    public static int STEP5_FORWARD = 8;

    public static int PARKING_ONE_STRAFE_LEFT = 52;
    public static int PARKING_TWO_STRAFE_LEFT = 25;
    public static int PARKING_THREE_STRAFE_RIGHT = 2;

    public static int PARKING_BACK = 12;


    @Override
    public void runOpMode() throws InterruptedException {

        AprilTagSignalDetector detector = new AprilTagSignalDetector(hardwareMap, telemetry, false);
        detector.startDetection();

        SampleMecanumDrive drivetrain = new SampleMecanumDrive(hardwareMap);
        drivetrain.setPoseEstimate(STARTING_POSITION);

        Elevator elevator = new Elevator(hardwareMap);
        Arm arm = new Arm(hardwareMap);
        Grabber grabber = new Grabber(hardwareMap);
        grabber.pickup();

        Trajectory step1_strafeRight = drivetrain.trajectoryBuilder(STARTING_POSITION).strafeRight(STEP1_STRAFE_RIGHT).build();
        Trajectory step2_forward = drivetrain.trajectoryBuilder(step1_strafeRight.end()).forward(STEP2_FORWARD).build();
        Trajectory step3_strafeLeft = drivetrain.trajectoryBuilder(step2_forward.end()).strafeLeft(STEP3_STRAFE_LEFT).build();
        Trajectory step4_strafeRight = drivetrain.trajectoryBuilder(step3_strafeLeft.end()).strafeRight(STEP4_STRAFE_RIGHT).build();
        Trajectory step5_forward = drivetrain.trajectoryBuilder(step4_strafeRight.end()).forward(STEP5_FORWARD).build();

        while (opModeInInit()) {
            telemetry.addLine("Parking position is " + detector.getSignalPosition());
            telemetry.update();
        }

        waitForStart();

        elevator.goToLevel(0);

        //scan here to get parking position
        int parkingPosition = detector.getSignalPosition();
        telemetry.log().add("Parking position is " + parkingPosition);

        //Step 1 - Strafe Right
        drivetrain.followTrajectory(step1_strafeRight);

        //Step 2 - Forward
        drivetrain.followTrajectory(step2_forward);

        //Step 3 - Strafe Left
        drivetrain.followTrajectory(step3_strafeLeft);

        //step-4 elevator up
        elevator.goToLevel(JUNCTION_LEVEL);
        sleep(1000);
        elevator.holdElevator(ELEVATOR_HOLD_ITERATIONS);

        //step5 - move arm to a dropping position
        arm.move(ARM_POSITION);

        elevator.holdElevator(ELEVATOR_HOLD_ITERATIONS);

        //sto6 - grabber releases the cone
        grabber.release();
        elevator.holdElevator(20);

        //step7 - move arm to home position
        arm.goHome();

        //step 8 - move elevator to home
        sleep(1000);
        elevator.goToHome();

        drivetrain.followTrajectory(step4_strafeRight);
        drivetrain.followTrajectory(step5_forward);

        parkRobot(parkingPosition, drivetrain);
    }

    private void parkRobot(int parkingPosition, SampleMecanumDrive drivetrain) {

        Trajectory park_strafe = null;

        if (parkingPosition == 1) {
            park_strafe = drivetrain.trajectoryBuilder(drivetrain.getPoseEstimate()).strafeLeft(PARKING_ONE_STRAFE_LEFT).build();
        } else if (parkingPosition == 2) {
            park_strafe = drivetrain.trajectoryBuilder(drivetrain.getPoseEstimate()).strafeLeft(PARKING_TWO_STRAFE_LEFT).build();
        } else {
            park_strafe = drivetrain.trajectoryBuilder(drivetrain.getPoseEstimate()).strafeRight(PARKING_THREE_STRAFE_RIGHT).build();
        }

        drivetrain.followTrajectory(park_strafe);

        Trajectory parkingBack = drivetrain.trajectoryBuilder(drivetrain.getPoseEstimate()).back(PARKING_BACK).build();
        drivetrain.followTrajectory(parkingBack);
    }
}