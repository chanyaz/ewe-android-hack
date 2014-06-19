package com.expedia.bookings.invaders;

/**
* Created by jdrotos on 6/17/14.
*/
public interface IInvaderGameListener {
	public void onScoreUpdate(int score);
	public void onAvailableLivesUpdate(int livesRemaining);
	public void onGameWon();
	public void onGameLost();
	public void onGameStarted();
	public void onGamePaused();
	public void onGameResumed();
	public void onGameReset();
}
