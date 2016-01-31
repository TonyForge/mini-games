package com.tforgegames.arcade;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tforgegames.minigames.MiniGame;
import com.tforgegames.minigames.MiniGameGlobalProperties;

public class Arcade extends MiniGame {

	public Stage stage;
	
	public Arcade(MiniGameGlobalProperties properties) {
		super(properties);

		stage = new Stage(new FitViewport(640,480,camera2d),batch);
	}
	
	public void render(float delta) {
		super.render(delta);
		
		if (isRunning)
		{
			stage.act();
			if (Gdx.input.isTouched())
			{
				//Gdx.input.getX()
				Vector2 v = new Vector2(Gdx.input.getX()-camera2d.viewportWidth/2,Gdx.input.getY()-camera2d.viewportHeight/2);
				v.setLength(500);
				camera2d.translate(v.x*delta, -v.y*delta);
			}
			
			camera2d.update();
			stage.draw();
		}
	}
	
	public void end() {
		stage.dispose();
	}

	public void loadResources() {
		//assets.load("treasure-hunter-tiles.png", Texture.class);
	}
	
	public void unloadResources() {
		//assets.unload("treasure-hunter-tiles.png");
	}
	
	public void resize(int width, int height) {
		super.resize(width, height);
		stage.getViewport().update(width, height, true);
	}
}
