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

public class LobbyWebSocket extends WebSocketController
{
	public static void join(String player, String gameId) 
	{
		Lobby lobby = Lobby.get(gameId);
		
		EventStream<Lobby.Event> eventStream = lobby.join(player);
		
	    while(inbound.isOpen()) 
	    {
	    	Either<WebSocketEvent, Lobby.Event> e = await(Promise.waitEither(
	    		inbound.nextEvent(),
	    		eventStream.nextEvent()
	    	));
	    	
	    	//Case: Someone joined the lobby
	    	for (Lobby.Join joined: ClassOf(Lobby.Join.class).match(e._2))
	    		outbound.send("join: %s", joined.player);
	    	
	    	//Case: Someone Left
	    	for (Lobby.Leave left: ClassOf(Lobby.Leave.class).match(e._2))
	    		outbound.send("leave: %s", left.player);
	    	
	    	//Case: socket closed
	    	for(WebSocketClose closed: SocketClosed.match(e._1))
	    	{
	    		lobby.leave(player);
	    		disconnect();
	    	}
	    	
	    	//Case: Tell clients to join start
	    	for(Lobby.StartGame start : 
	    			ClassOf(Lobby.StartGame.class).match(e._2))
	    	{
	    		outbound.send("start: %s", start.gameId);
	    		disconnect();
	    	}
	    	
	    	//Case: Someone clicks start
	    	for(String message: TextFrame.and(StartsWith("start")).match(e._1))
	    	{
	    		lobby.startGame(gameId);
	    		//outbound.send("start:ok");
	    	}
	    		
	    }
	}
}
