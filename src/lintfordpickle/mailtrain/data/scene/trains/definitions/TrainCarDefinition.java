package lintfordpickle.mailtrain.data.scene.trains.definitions;

import lintfordpickle.mailtrain.data.scene.trains.TrainCar;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.graphics.batching.SpriteBatch;
import net.lintford.library.core.graphics.sprites.spritesheet.SpriteSheetDefinition;

public class TrainCarDefinition {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final TrainCarDefinition Locomotive00Definition = new LocomotiveDefinition();
	public static final TrainCarDefinition EmptyCarriage00Definition = new EmptyCarriageDefinition();
	public static final TrainCarDefinition Cannon00Definition = new CannonCarriageDefinition();

	public abstract class TrainInstanceStateData {

	}

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public TrainInstanceStateData getInstanceStateDataInstance() {
		return null;
	}

	public String carriageBaseSpriteFrameName; // TODO: Currently all within the train spritesheet

	public boolean isLocomotive;
	public float maxForce;
	public float maxRevForce;

	public float topSpeed = 88;
	public float maxAccel = 100;
	public float maxBrake = 150;

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void updateCarriageInstance(LintfordCore core, TrainCar instance) {

	}

	// TODO: definition accessing sprite batches ain't chill
	public void drawCarriageInstance(LintfordCore core, SpriteSheetDefinition trainSpriteSheet, SpriteBatch spriteBatch, TrainCar instance) {

	}

}
