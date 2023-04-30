package lintfordpickle.mailtrain.controllers.world;

import java.io.FileWriter;
import java.io.IOException;

import org.lwjgl.glfw.GLFW;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;

import lintfordpickle.mailtrain.ConstantsGame;
import lintfordpickle.mailtrain.data.world.scenes.ScenePropInstance;
import lintfordpickle.mailtrain.data.world.scenes.Props;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.controllers.core.ResourceController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.geometry.Rectangle;
import net.lintford.library.core.graphics.sprites.spritesheet.SpriteSheetDefinition;
import net.lintford.library.core.input.mouse.IInputProcessor;

public class SceneryController extends BaseController implements IInputProcessor {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Scenery Controller";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private ResourceController mResourceController;
	private SpriteSheetDefinition mWorldSpriteSheet;
	private Props mWorldScenery;

	public int selectedItemIndex = 0;

	public static int getMaxSpriteIndex() {
		return 10;
	}

	public static String getSelectedSpriteName(int pIndex) {
		switch (pIndex) {
		default:
		case 0:
			return "TEXTURETREE00";
		case 1:
			return "TEXTURETREE01";
		case 2:
			return "TEXTURETREE02";
		case 3:
			return "TEXTUREDEPOTNORTH";
		case 4:
			return "TEXTUREDEPOTWEST";
		case 5:
			return "TEXTUREDEPOTSOUTH";
		case 6:
			return "TEXTURETREE03";
		case 7:
			return "TEXTUREDEPOTNORTHEXT";
		case 8:
			return "TEXTUREDEPOTWESTEXT";
		case 9:
			return "TEXTURESIGNALBOX";
		case 10:
			return "TEXTURESIGNALBOXBROKEN";
		}
	}

	private float mMouseLeftCoolDown;

	public boolean isCoolDownElapsed() {
		return mMouseLeftCoolDown <= 0.f;
	}

	public void resetCoolDownTimer() {
		mMouseLeftCoolDown = 200.f; // ms
	}

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public boolean isInitialized() {
		return mWorldScenery != null;
	}

	public Props worldScenery() {
		return mWorldScenery;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public SceneryController(ControllerManager pControllerManager, Props pWorldScenery, int pEntityGroupUid) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupUid);

		mWorldScenery = pWorldScenery;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		final var lControllerManager = pCore.controllerManager();

		mResourceController = (ResourceController) lControllerManager.getControllerByNameRequired(ResourceController.CONTROLLER_NAME, LintfordCore.CORE_ENTITY_GROUP_ID);

		mWorldSpriteSheet = mResourceController.resourceManager().spriteSheetManager().getSpriteSheet("SPRITESHEET_ENVIRONMENT", ConstantsGame.GAME_RESOURCE_GROUP_ID);
	}

	final Rectangle lTempRect = new Rectangle();

	@Override
	public boolean handleInput(LintfordCore pCore) {
		// NO TIME - PRESS AND HOLD ALT TO ENABLE OBJECT PLACEMENT
		final var isInEditMode = pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_RIGHT_ALT);
		if (isInEditMode) {
			if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_F8)) {
				mWorldScenery.mSceneryItems.clear();
				return true;
			}
			if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_DELETE, this)) {
				final int lNumProps = mWorldScenery.mSceneryItems.size();
				final float lMouseWorldPositionX = pCore.gameCamera().getMouseWorldSpaceX();
				final float lMouseWorldPositionY = pCore.gameCamera().getMouseWorldSpaceY();
				for (int i = 0; i < lNumProps; i++) {
					final var lPropInst = mWorldScenery.mSceneryItems.get(i);
					if (lPropInst == null)
						continue;
					lTempRect.set(lPropInst.worldPositionX, lPropInst.worldPositionY, lPropInst.objectWidth, lPropInst.objectHeight);
					if (lTempRect.intersectsAA(lMouseWorldPositionX, lMouseWorldPositionY)) {
						mWorldScenery.mSceneryItems.remove(lPropInst);
						return true;
					}
				}
				return true;
			}
			if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_8, this)) {// toggle left
				selectedItemIndex--;
				if (selectedItemIndex < 0)
					selectedItemIndex = getMaxSpriteIndex();
				System.out.println("Selected item is : " + getSelectedSpriteName(selectedItemIndex));
			}
			if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_9, this)) { // toggle right
				selectedItemIndex++;
				if (selectedItemIndex > getMaxSpriteIndex())
					selectedItemIndex = 0;
				System.out.println("Selected item is : " + getSelectedSpriteName(selectedItemIndex));
			}
			if (pCore.input().mouse().isMouseLeftButtonDownTimed(this)) {
				final float lMouseWorldPositionX = pCore.gameCamera().getMouseWorldSpaceX();
				final float lMouseWorldPositionY = pCore.gameCamera().getMouseWorldSpaceY();

				ScenePropInstance lNewProp = new ScenePropInstance();
				lNewProp.worldPositionX = lMouseWorldPositionX;
				lNewProp.worldPositionY = lMouseWorldPositionY;
				lNewProp.spriteItemName = getSelectedSpriteName(selectedItemIndex);

				final var lSpriteFrame = mWorldSpriteSheet.getSpriteFrame(lNewProp.spriteItemName);
				lNewProp.objectWidth = lSpriteFrame.width();
				lNewProp.objectHeight = lSpriteFrame.height();

				mWorldScenery.mSceneryItems.add(lNewProp);
				System.out.println("Added prop " + lNewProp.spriteItemName + " to world " + lNewProp.worldPositionX + ", " + lNewProp.worldPositionY);

			}
		}
		return super.handleInput(pCore);
	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		mMouseLeftCoolDown -= pCore.appTime().elapsedTimeMilli();
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void loadDefaultScene(String fileName) {
		mWorldScenery = WorldIOController.loadSceneryFromFile(fileName);
	}

	public void saveSceneryScene(String pFilename) {
		FileWriter lWriter = null;
		Gson gson = new Gson();
		try {
			lWriter = new FileWriter(pFilename);
			gson.toJson(mWorldScenery, lWriter);

		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				lWriter.flush();
				lWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean allowKeyboardInput() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allowGamepadInput() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allowMouseInput() {
		// TODO Auto-generated method stub
		return false;
	}

}
