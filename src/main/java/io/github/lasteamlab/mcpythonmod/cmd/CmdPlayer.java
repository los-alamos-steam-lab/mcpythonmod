package io.github.lasteamlab.mcpythonmod.cmd;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import io.github.lasteamlab.mcpythonmod.MCPythonMod;
import io.github.lasteamlab.mcpythonmod.RemoteSession;

public class CmdPlayer {
    private final String preFix = "player.";
    private RemoteSession session;
	private MCPythonMod plugin;

    public CmdPlayer(RemoteSession session) {
        this.session = session;
		this.plugin = session.plugin;
    }


    public void execute(String command, String[] args) {
		Player player = plugin.getPlayer();
		if (player == null) {
			return;
		}
		
		execute(command, args,  player);
    }

    public void execute(String command, String[] args, String id) {
	    try {
	    	Integer intid = Integer.parseInt(id);  
	    	execute(command, args,  intid);
	    	return;
	    } catch(Exception e) {
	    	Player player = plugin.getPlayer(id);

		    if (player == null) {
				session.send("Player " + id + " is not on server");
				return;
			}
			
			execute(command, args,  player);	    	
	    }
    }

    public void execute(String command, String[] args, Integer id) {
    	Player player = plugin.getPlayer(id);

    	if (player == null) {
			session.send("Player " + id + " is not on server");
			return;
		}
		
		execute(command, args,  player);
    }

	public void execute(String command, String[] args, Player player) {

		if (player == null) {
			session.send("Fail,No player provided.");
			return;
		}
		session.plugin.getLogger().info("executing command with args " + args);

		// player.getType
		if (command.equals("getType")) {

			session.send(player.getType());

			// player.getTile
		} else if (command.equals("getTile")) {

			session.send(session.blockLocationToRelative(player.getLocation()));

			// player.setTile
		} else if (command.equals("setTile")) {
			String x = args[0], y = args[1], z = args[2];

			//get players current location, so when they are moved we will use the same pitch and yaw (rotation)
			Location loc = player.getLocation();
			player.teleport(session.parseRelativeBlockLocation(x, y, z, loc.getPitch(), loc.getYaw()));

			// player.getAbsPos
		} else if (command.equals("getAbsPos")) {

			session.send(player.getLocation());

			// player.setAbsPos
		} else if (command.equals("setAbsPos")) {
			String x = args[0], y = args[1], z = args[2];

			//get players current location, so when they are moved we will use the same pitch and yaw (rotation)
			Location loc = player.getLocation();
			loc.setX(Double.parseDouble(x));
			loc.setY(Double.parseDouble(y));
			loc.setZ(Double.parseDouble(z));
			player.teleport(loc);

			// player.getPos
		} else if (command.equals("getPos")) {

			session.send(session.locationToRelative(player.getLocation()));

			// player.setPos
		} else if (command.equals("setPos")) {
			String x = args[0], y = args[1], z = args[2];

			//get players current location, so when they are moved we will use the same pitch and yaw (rotation)
			Location loc = player.getLocation();
			player.teleport(session.parseRelativeLocation(x, y, z, loc.getPitch(), loc.getYaw()));

			// player.setDirection
		} else if (command.equals("setDirection")) {
			Double x = Double.parseDouble(args[0]);
			Double y = Double.parseDouble(args[1]);
			Double z = Double.parseDouble(args[2]);

			Location loc = player.getLocation();
			loc.setDirection(new Vector(x, y, z));
			player.teleport(loc);

			// player.getDirection
		} else if (command.equals("getDirection")) {

			session.send(player.getLocation().getDirection().toString());

			// player.setRotation
		} else if (command.equals("setRotation")) {
			Float yaw = Float.parseFloat(args[0]);

			Location loc = player.getLocation();
			loc.setYaw(yaw);
			player.teleport(loc);

			// player.getRotation
		} else if (command.equals("getRotation")) {

			float yaw = player.getLocation().getYaw();
			// turn bukkit's 0 - -360 to positive numbers
			if (yaw < 0) yaw = yaw * -1;
			session.send(yaw);

			// player.setPitch
		} else if (command.equals("setPitch")) {
			Float pitch = Float.parseFloat(args[0]);

			Location loc = player.getLocation();
			loc.setPitch(pitch);
			player.teleport(loc);

			// player.getPitch
		} else if (command.equals("getPitch")) {

			session.send(player.getLocation().getPitch());

		} else if (command.equals("getEntities")) {
			int distance = Integer.parseInt(args[0]);
			String entityType = "";

			if (args.length == 2) {
				entityType = args[1];
			}				
			
			session.send(session.getEntities(session.origin.getWorld(), player, distance, entityType));

		// player.removeEntities
		} else if (command.equals("removeEntities")) {
			int distance = Integer.parseInt(args[0]);
			String entityType = "";

			if (args.length == 2) {
				entityType = args[1];
			}				

			session.send(session.removeEntities(session.origin.getWorld(), player, distance, entityType));

		// player.events.block.hits
		} else if (command.equals("events.block.hits")) {
			session.send(session.getBlockHits(player.getEntityId()));
			
		// player.events.chat.posts
		} else if (command.equals("events.chat.posts")) {
			session.send(session.getChatPosts(player.getEntityId()));
			
		// player.events.projectile.hits
		} else if(command.equals("events.projectile.hits")) {
			session.send(session.getProjectileHits(player.getEntityId()));
		
		// player.events.clear
		} else if (command.equals("events.clear")) {
			session.clearEntityEvents(player.getEntityId());
			
            // player.getFoodLevel
        } else if (command.equals("getFoodLevel")) {

            session.send(player.getFoodLevel());

            // player.setFoodLevel
        } else if (command.equals("setFoodLevel")) {
			Integer foodLevel = Integer.parseInt(args[0]);

			player.setFoodLevel(foodLevel);

			// player.getHealth
		} else if(command.equals("getHealth")) {

			session.send(player.getHealth());

			// player.setHealth
		} else if(command.equals("setHealth")){
			Double health = Double.parseDouble(args[0]);

			player.setHealth(health);

            // player.sendTitle
        } else if (command.equals("sendTitle")) {

			String title = args[0];
			String subTitle = args[1];
			Integer fadeIn = Integer.parseInt(args[2]);
			Integer stay = Integer.parseInt(args[3]);
			Integer fadeOut = Integer.parseInt(args[4]);
			player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);

		} else {
			session.plugin.getLogger().warning(preFix + command + " is not supported.");
			session.send("Fail," + preFix + command + " is not supported.");
		}
	}

}
