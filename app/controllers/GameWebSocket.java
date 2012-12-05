package controllers;

import play.*;
import play.mvc.*;
import play.libs.*;
import play.libs.F.*;
import play.mvc.Http.*;

import static play.libs.F.*;
import static play.libs.F.Matcher.*;
import static play.mvc.Http.WebSocketEvent.*;

import java.util.*;

import models.*;


public class GameWebSocket extends WebSocketController
{
	public static void join(String player, String gameId)
	{
		Game game = Game.get(gameId);
		
		EventStream<Game.Event> eventStream = game.join(player);
		
		while(inbound.isOpen())
		{
			Either<WebSocketEvent, Game.Event> e = await(Promise.waitEither(
				inbound.nextEvent(), 
				eventStream.nextEvent()
			));
			
			//Case: Someone joined the game
			for (Game.Join joined: ClassOf(Game.Join.class).match(e._2))
			{
				if (!player.equals(joined.player))
					outbound.send("join: %s", joined.player);
			}
			
			//Case: New Move Announced
			for (Game.Move moved: ClassOf(Game.Move.class).match(e._2))
			{
				if (!player.equals(moved.player))
				{	
					outbound.send(moved.toString());
					//System.out.println(moveData);
				}
			}
			
			//Case: Move Submitted
			for(String text: TextFrame.and(StartsWith("newMove")).match(e._1))
			{
				//parse text and game.move
				String[] pairs = text.split("&");
				String name = ""; 
				String x = "";
				String y= "";
				String rot = "";
				String thrust = "";
				String color = "";
				String score = "";
				for (String pair: pairs)
				{
					String[] parts = pair.split(": ");
					String key = parts[0];
					String val = parts[1];
					if (key.equals("newMove"))
						name = val;
					else if (key.equals("x"))
						x = val;
					else if (key.equals("y"))
						y = val;
					else if (key.equals("rot"))
						rot = val;
					else if (key.equals("thrust"))
						thrust = val;
					else if (key.equals("color"))
						color = val;
					else if (key.equals("score"))
						score = val;
				}
				game.move(name, x, y, rot, thrust, color, score);
			}
			
			//Case: new bullet fired
			for (String text: TextFrame.and(StartsWith("newBullet")).match(e._1))
			{
				String[] pairs = text.split("&");
				String name = "";
				String x    = "";
				String y    = "";
				String rot  = "";
				
				for (String pair: pairs)
				{
					String[] parts = pair.split(": ");
					String key = parts[0];
					String val = parts[1];
					if (key.equals("newBullet"))
						name = val;
					else if (key.equals("x"))
						x = val;
					else if (key.equals("y"))
						y = val;
					else if (key.equals("rot"))
						rot = val;
				}
				game.fire(name, x, y, rot);
			}
			
			//Case: send bullet event
			for (Game.Bullet bullet : ClassOf(Game.Bullet.class).match(e._2))
			{
				if (!player.equals(bullet.player))
					outbound.send(bullet.toString());
			}
			
		}
	}
}
