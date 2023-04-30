package lintfordpickle.mailtrain.data.trains.definitions;

public class TrainCarDefinition {

	public static final TrainCarDefinition Locomotive00Definition = new Locomotive00Definition();
	public static final TrainCarDefinition EmptyCarriage00Definition = new EmptyCarriage00Definition();

	public boolean isLocomotive;
	public float maxForce;
	public float maxRevForce;

	public float topSpeed = 88;
	public float maxAccel = 100;
	public float maxBrake = 150;

}
