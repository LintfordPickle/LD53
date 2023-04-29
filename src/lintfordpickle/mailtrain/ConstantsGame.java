package lintfordpickle.mailtrain;

import net.lintford.library.core.entity.BaseEntity;

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

	// ---------------------------------------------
	// Debug
	// ---------------------------------------------

	public static final boolean IS_DEBUG_MODE = true;
	public static final boolean SKIP_MAIN_MENU_ON_STARTUP = true;
	public static final boolean ESCAPE_RESTART_MAIN_SCENE = false;
}
