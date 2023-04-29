package lintfordpickle.mailtrain.data.trains;

public class TrainHitch {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public final TrainCar parentCar;
	public TrainHitch connectedTo;
	public boolean isHitchable;
	public boolean isOpen;
	public boolean isDamaged;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrainHitch(TrainCar pParentTrainCar) {
		parentCar = pParentTrainCar;

	}
}
