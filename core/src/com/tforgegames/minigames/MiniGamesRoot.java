package com.tforgegames.minigames;

import treasurehunter.TreasureHunter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MiniGamesRoot extends Game {
	
	private SpriteBatch batch;
	private AssetManager assets;
	
	Texture img;
	
	private MiniGame currentGame = null;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		assets = new AssetManager();
		
		img = new Texture("badlogic.jpg");
	}

	@Override
	public void render () {
		if (currentGame == null)
		{
			Gdx.gl.glClearColor(1, 1, 0, 0);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			batch.begin();
			batch.draw(img, 0, 0);
			batch.end();
			
			if (Gdx.input.isTouched())
			{
				MiniGameGlobalProperties properties = new MiniGameGlobalProperties();
				properties.batch = batch;
				properties.assets = assets;
				
				properties.screenWidth=Gdx.graphics.getWidth();
				properties.screenHeight=Gdx.graphics.getHeight();
				
				
				Launch(new TreasureHunter(properties));
			}
		}
		else
		{
			super.render();
		}
	}
	
	public void Launch(MiniGame game)
	{
		currentGame = game;
		setScreen(game);
	}
	
	public void dispose()
	{
		super.dispose();
		
		assets.dispose();
		batch.dispose();
		
		img.dispose();
	}
}
