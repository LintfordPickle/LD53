package lintfordpickle.mailtrain.data;

public class GameState {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private float mFuel;
	private int mCrew;
	private int mCredits;

	// TODO: Trains cars

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public int credits() {
		return mCredits;
	}

	public int crew() {
		return mCrew;
	}

	public float fuel() {
		return mFuel;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameState() {

	}

	public GameState(GameState gameState) {
		mFuel = gameState.fuel();
		mCrew = gameState.crew();
		mCredits = gameState.credits();
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void startNewGame(int pStartingCredits, float startingFuel, int startingCrew) {
		mCredits = pStartingCredits;
		mFuel = startingFuel;
		mCrew = startingCrew;
	}

	public void addCredits(int addAmount) {
		if (addAmount < 0)
			return;
		mCredits += addAmount;
	}

	public void deductCredits(int deductAmount) {
		if (deductAmount > 0)
			return;
		mCredits -= deductAmount;
	}

	public void addCrew(int addAmount) {
		if (addAmount < 0)
			return;
		mCrew += addAmount;
	}

	public void deductCrew(int deductAmount) {
		if (deductAmount > 0)
			return;
		mCrew -= deductAmount;
	}

	public void addFuel(float addAmount) {
		if (addAmount < 0)
			return;
		mCredits += addAmount;
	}

	public void deductFuel(float deductAmount) {
		if (deductAmount > 0)
			return;
		mCredits -= deductAmount;
	}

}
