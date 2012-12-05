package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {

    public static void index() {
        render();
    }
    
    public static void waitForPlayers(String name, String gameId)
    {
    	render(name, gameId);
    }
    
    public static void gamePage(String name, String gameId)
    {
    	render(name, gameId);
    }

}