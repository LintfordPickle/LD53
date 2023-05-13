package lintfordpickle.mailtrain.data.scene.trains.definitions;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.data.scene.trains.TrainCar;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.batching.SpriteBatch;
import net.lintford.library.core.graphics.sprites.spritesheet.SpriteSheetDefinition;
import net.lintford.library.core.maths.RandomNumbers;

public class CannonCarriageDefinition extends TrainCarDefinition {

	public class CannonCarriageStateData extends TrainInstanceStateData {

		public boolean mFireLeftGun;
		public float mFireGunCooldown;

	}

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final int TURRET_COOLDOWN = 800; // ms

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public CannonCarriageDefinition() {

		carriageBaseSpriteFrameName = "TEXTURECANNON";

		isLocomotive = false;
		maxForce = 0;
		maxRevForce = 0;
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	@Override
	public TrainInstanceStateData getInstanceStateDataInstance() {
		return new CannonCarriageStateData();
	}

	@Override
	public void updateCarriageInstance(LintfordCore core, TrainCar instance) {
		super.updateCarriageInstance(core, instance);

		final var stateData = (CannonCarriageStateData) instance.instanceStateData();

		stateData.mFireGunCooldown -= core.gameTime().elapsedTimeMilli();

		if (core.input().mouse().isMouseLeftButtonDown() && stateData.mFireGunCooldown < 0.f) {
			if (instance.trainCallbackListener() != null) {
				
				final var lx = (instance.rearAxle.worldPositionX + instance.frontAxle.worldPositionX) * .5f;
				final var ly = (instance.rearAxle.worldPositionY + instance.frontAxle.worldPositionY) * .5f;

				final var lMouseX = core.gameCamera().getMouseWorldSpaceX();
				final var lMouseY = core.gameCamera().getMouseWorldSpaceY();

				final var lVectorX = lMouseX - lx;
				final var lVectorY = lMouseY - ly;

				final var lAngle = (float) Math.atan2(lVectorY, lVectorX);
				
				final var r90 = Math.toRadians(90);
				final var lSignum = stateData.mFireLeftGun ? -1.f : 1.f;
				final var lox = (float) Math.cos(lAngle - r90) * 5.f * lSignum;
				final var loy = (float) Math.sin(lAngle - r90) * 5.f * lSignum;

				instance.trainCallbackListener().shootScanlineProjectile(1, lx + lox, ly + loy, lAngle, 600);
			}

			stateData.mFireLeftGun = !stateData.mFireLeftGun;
			stateData.mFireGunCooldown = TURRET_COOLDOWN + RandomNumbers.random(-50, 50);
		}

	}

	@Override
	public void drawCarriageInstance(LintfordCore core, SpriteSheetDefinition trainSpriteSheet, SpriteBatch spriteBatch, TrainCar instance) {

		final var stateData = (CannonCarriageStateData) instance.instanceStateData();

		final var lTurretSpriteFrame = trainSpriteSheet.getSpriteFrame("TEXTURECANNONTURRET");
		final float lAlpha = ConstantsGame.DEBUG_DRAW_AXLE_POINTS ? 0.7f : 1f;
		final var lWhiteWithAlpha = ColorConstants.getWhiteWithAlpha(lAlpha);

		// --

		final var lDestX = (instance.rearAxle.worldPositionX + instance.frontAxle.worldPositionX) * .5f;
		final var lDestY = (instance.rearAxle.worldPositionY + instance.frontAxle.worldPositionY) * .5f;
		final var lDestW = 22;
		final var lDestH = 5;

		final var lMouseX = core.gameCamera().getMouseWorldSpaceX();
		final var lMouseY = core.gameCamera().getMouseWorldSpaceY();

		final var lVectorX = lMouseX - lDestX;
		final var lVectorY = lMouseY - lDestY;

		final var lAngle = (float) Math.atan2(lVectorY, lVectorX);
		final var lRotX = 0;
		final var lRotY = 0;

		final var r90 = Math.toRadians(90);
		final var c = (float) Math.cos(lAngle);
		final var s = (float) Math.sin(lAngle);
		final var lTimeInBackPhase = 350.f; // ms

		spriteBatch.begin(core.gameCamera());

		{ // left turret
			final var lox = (float) Math.cos(lAngle - r90) * 5.f;
			final var loy = (float) Math.sin(lAngle - r90) * 5.f;

			final var lIsLeftGunInBackPhase = stateData.mFireLeftGun && (stateData.mFireGunCooldown > TURRET_COOLDOWN - lTimeInBackPhase);

			final float lBackPhaseOffsetX = lIsLeftGunInBackPhase ? (float) c * 5.f : 0.f;
			final float lBackPhaseOffsetY = lIsLeftGunInBackPhase ? (float) s * 5.f : 0.f;

			spriteBatch.drawAroundCenter(trainSpriteSheet.texture(), lTurretSpriteFrame, lDestX + lox - lBackPhaseOffsetX, lDestY + loy - lBackPhaseOffsetY, lDestW, lDestH, -0.3f, lAngle, lRotX, lRotY, 1,
					lWhiteWithAlpha);
		}

		{ // right
			final var lox = (float) Math.cos(lAngle - r90) * -5.f;
			final var loy = (float) Math.sin(lAngle - r90) * -5.f;

			final var lIsLeftGunInBackPhase = !stateData.mFireLeftGun && (stateData.mFireGunCooldown > TURRET_COOLDOWN - lTimeInBackPhase);

			final float lOffsetX = lIsLeftGunInBackPhase ? (float) Math.cos(lAngle) * 5.f : 0.f;
			final float lOffsetY = lIsLeftGunInBackPhase ? (float) Math.sin(lAngle) * 5.f : 0.f;

			spriteBatch.drawAroundCenter(trainSpriteSheet.texture(), lTurretSpriteFrame, lDestX + lox - lOffsetX, lDestY + loy - lOffsetY, lDestW, lDestH, -0.3f, lAngle, lRotX, lRotY, 1, lWhiteWithAlpha);
		}

		spriteBatch.end();

	}
}
