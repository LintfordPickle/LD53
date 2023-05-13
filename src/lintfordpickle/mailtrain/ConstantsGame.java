package lintfordpickle.mailtrain;

import net.lintford.library.core.entities.BaseEntity;
import net.lintford.library.core.storage.FileUtils;

public class ConstantsGame {

	// ---------------------------------------------
	// Setup
	// ---------------------------------------------

	public static final String FOOTER_TEXT = "(c) 2023 LintfordPickle";

	public static final String APPLICATION_NAME = "Mailtrain";
	public static final String WINDOW_TITLE = "Maintrain";

	public static final float ASPECT_RATIO = 16.f / 9.f;

	public static final int GAME_CANVAS_WIDTH = 960;
	public static final int GAME_CANVAS_HEIGHT = 540;

	public static final int GAME_RESOURCE_GROUP_ID = BaseEntity.getEntityNumber();

	public static final boolean PREVIEW_MODE = true;

	// ---------------------------------------------
	// Values
	// ---------------------------------------------

	public static final String WORLD_BASE_DIRECTORY = "res" + FileUtils.FILE_SEPARATOR + "def" + FileUtils.FILE_SEPARATOR;
	public static final String WORLD_DIRECTORY = WORLD_BASE_DIRECTORY; // + "worlds" + FileUtils.FILE_SEPARATOR;
	public static final String SCENES_REL_DIRECTORY = "scenes" + FileUtils.FILE_SEPARATOR;

	// ---------------------------------------------
	// Audio
	// ---------------------------------------------

	public static final boolean SOUNDS_ENABLED = false;

	// ---------------------------------------------
	// Debug
	// ---------------------------------------------

	public static final boolean IS_DEBUG_MODE = true;
	public static final boolean CAMERA_DEBUG_MODE = true;
	public static final boolean SKIP_MAIN_MENU_ON_STARTUP = true;
	public static final boolean ESCAPE_RESTART_MAIN_SCENE = false;

	public static final boolean DEBUG_FORCE_NO_CARRIAGES = false;
	public static final boolean DEBUG_EDITOR_IN_GAME = true;

	// TRAIN
	public static final boolean DEBUG_DRAW_PLAYER_TRAIN_STATS = true;
	public static final boolean DEBUG_DRAW_AXLE_POINTS = true;
	public static final boolean DEBUG_DRAW_TRAIN_DEBUG_INFO = false;

	// Outputs debug information about acceleration, velocity and movement speed.
	public static final boolean DEBUG_DRAW_TRAIN_DEBUG_SPEED_INFO = true;
	public static final boolean DEBUG_DRAW_TRAIN_CARS_DEBUG_INFO = false;

}