package lintfordpickle.mailtrain;

import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.glClearColor;

import org.lwjgl.opengl.GL11;

import lintfordpickle.mailtrain.screens.MainMenu;
import lintfordpickle.mailtrain.screens.MenuBackgroundScreen;
import net.lintford.library.GameInfo;
import net.lintford.library.ResourceLoader;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.maths.RandomNumbers;
import net.lintford.library.screenmanager.ScreenManager;

public class MailtrainGame extends LintfordCore {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	protected int mEntityGroupID;

	protected ResourceLoader mGameResourceLoader;
	protected ScreenManager mScreenManager;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public ScreenManager screenManager() {
		return mScreenManager;
	}

	public MailtrainGame(GameInfo gameInfo, String[] args) {
		super(gameInfo, args);

		mEntityGroupID = RandomNumbers.RANDOM.nextInt();
		mIsFixedTimeStep = true;

		mScreenManager = new ScreenManager(this);
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	protected void showStartUpLogo(long pWindowHandle) {
		glClearColor(1.0f, 0f, 0f, 1f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		glfwSwapBuffers(pWindowHandle);
	}

	@Override
	protected void onInitializeApp() {
		super.onInitializeApp();

		if (ConstantsGame.SKIP_MAIN_MENU_ON_STARTUP) {

		}

		mScreenManager.addScreen(new MenuBackgroundScreen(mScreenManager));
		mScreenManager.addScreen(new MainMenu(mScreenManager));
		mScreenManager.initialize();
	}

	@Override
	protected void onLoadResources() {
		super.onLoadResources();

		mGameResourceLoader = new GameResourceLoader(mResourceManager, config().display());

		mGameResourceLoader.loadResources(mResourceManager);
		mGameResourceLoader.setMinimumTimeToShowLogosMs(ConstantsGame.IS_DEBUG_MODE ? 0 : 1000);
		mGameResourceLoader.loadResourcesInBackground(this);

		mScreenManager.loadResources(mResourceManager);
	}

	@Override
	protected void onUnloadResources() {
		super.onUnloadResources();

		mScreenManager.unloadResources();
	}

	@Override
	protected void onHandleInput() {
		super.onHandleInput();

		gameCamera().handleInput(this);
		mScreenManager.handleInput(this);
	}

	@Override
	protected void onUpdate() {
		super.onUpdate();

		mScreenManager.update(this);
	}

	@Override
	protected void onDraw() {
		super.onDraw();

		mScreenManager.draw(this);
	}

}
