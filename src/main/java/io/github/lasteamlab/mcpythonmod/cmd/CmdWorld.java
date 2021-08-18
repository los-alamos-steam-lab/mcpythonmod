package io.github.lasteamlab.mcpythonmod.cmd;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import io.github.lasteamlab.mcpythonmod.MCPythonMod;
import io.github.lasteamlab.mcpythonmod.RemoteSession;

import java.util.Collection;
import java.util.List;  // nécessaire pour l'utilisation de List


public class CmdWorld {
	private final String preFix = "world.";
	private RemoteSession session;
	private MCPythonMod plugin;

	public CmdWorld(RemoteSession session) {
		this.session = session;
		this.plugin = session.plugin;
	}

	public void execute(World world, String command, String[] args) {

		// world.getBlock
		if (command.equals("getBlock")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			Block block = world.getBlockAt(loc);
			session.send(block.getType());


			// world.getBlocks
		} else if (command.equals("getBlocks")) {
			Location loc1 = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			Location loc2 = session.parseRelativeBlockLocation(args[3], args[4], args[5]);

			session.send(getBlocks(loc1, loc2));

		} else if (command.equals("getBlockWithData")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			org.bukkit.block.Block block = world.getBlockAt(loc);
			org.bukkit.block.data.BlockData blockdata =  block.getBlockData();
			
			StringBuilder b = new StringBuilder();
			
			// in the case of a banner search for its parameters and add to b
			org.bukkit.block.BlockState blockstate = block.getState();
			if (blockstate instanceof org.bukkit.block.Banner) {
				// parameters of the banner
				org.bukkit.block.Banner banner = (org.bukkit.block.Banner) block.getState();
				List <org.bukkit.block.banner.Pattern> bannerpattern =  banner.getPatterns();
				
				b.append("; Patterns {");
				for (int i=0; i<bannerpattern.size() ; i++) {
					b.append("[");
					b.append(bannerpattern.get(i).getPattern());
					b.append(",");
					//plugin.getLogger().info(listepattern.get(i).getPattern().toString());
					b.append(bannerpattern.get(i).getColor());
					//plugin.getLogger().info(listepattern.get(i).getColor().toString());
					b.append("] ");
				}	
				b.append("}");
			}												
			session.send(blockdata.toString()+b);

			// world.setBlock
		} else if (command.equals("setBlock")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			Material blockType = Material.getMaterial(args[3]);
			updateBlock(world, loc, blockType);

			// world.setBlocks
		} else if (command.equals("setBlocks")) {
			Location loc1 = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			Location loc2 = session.parseRelativeBlockLocation(args[3], args[4], args[5]);
			Material blockType = Material.getMaterial(args[6]); 
			setCuboid(loc1, loc2, blockType);


			// world.getPlayerIds
		} else if (command.equals("getPlayerIds")) {
			StringBuilder bdr = new StringBuilder();
			Collection<? extends Player> players = Bukkit.getOnlinePlayers();
			if (players.size() > 0) {
				for (Player p : players) {
					bdr.append(p.getEntityId());
					bdr.append("|");
				}
				bdr.deleteCharAt(bdr.length() - 1);
				session.send(bdr.toString());
			} else {
				session.send("Fail," + "There are no players in the server.");
			}

			// world.getPlayerId
		} else if (command.equals("getPlayerId")) {
			Player p = plugin.getPlayer(args[0]);
			if (p != null) {
				session.send(p.getEntityId());
			} else {
				plugin.getLogger().info("Player [" + args[0] + "] not found.");
				session.send("Fail, " + "The player not exist");
			}

			// world.getHeight
		} else if (command.equals("getHeight")) {
			session.send(world.getHighestBlockYAt(session.parseRelativeBlockLocation(args[0], "0", args[1])) - session.origin.getBlockY());
			// world.getEntities
		} else if (command.equals("getEntities")) {
			String entityType = args[0];
			session.send(session.getEntities(world, entityType));	// entityType est une chaîne de caractères			
			// world.spawnEntity
		} else if (command.equals("spawnEntity")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			org.bukkit.entity.EntityType typeanimal = org.bukkit.entity.EntityType.valueOf(args[3]);
			Entity entity = world.spawnEntity(loc, typeanimal);
			
			// on vérifie qu'il s'agit d'un animal 				
			if ( entity instanceof org.bukkit.entity.Animals) {
				// s'il y a un argument supplémentaire il indique qu'il s'agit ou non d'un bébé
				org.bukkit.entity.Animals animal = (org.bukkit.entity.Animals) entity;
				int nbr_arg = args.length-4;
				if (nbr_arg == 1) {
					if (args[4].contentEquals("BABY") ){
						animal.setBaby();
					}
				}					
			}

			session.send(entity.getEntityId());

		// world.spawnCat							
		} else if (command.equals("spawnCat")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			org.bukkit.entity.EntityType typeanimal = org.bukkit.entity.EntityType.CAT;
			Entity entity = world.spawnEntity(loc, typeanimal);
			
			// on vérifie qu'il s'agit d'un chat				
			if ( entity instanceof org.bukkit.entity.Cat) {
				// on place les arguments supplémentaires : bébé, couleur pelage, couleur collier
				org.bukkit.entity.Cat chat = (org.bukkit.entity.Cat) entity;
				int nbr_arg = args.length-3;
				if (nbr_arg >= 1) {  // couleur du pelage
					org.bukkit.entity.Cat.Type typeDeChat = org.bukkit.entity.Cat.Type.valueOf(args[3]);
					chat.setCatType(typeDeChat);												
				}
				if (nbr_arg >= 2) { // bébé
					if (args[4].contentEquals("BABY") ){
						chat.setBaby();
					} else {
						chat.setAdult();
					}
				}						
				if (nbr_arg >= 3) { // couleur du collier
					// on apprivoise le chat
					chat.setTamed(true);
					org.bukkit.DyeColor couleur = org.bukkit.DyeColor.valueOf(args[5]);
					chat.setCollarColor(couleur);
					//plugin.getLogger().info("Collier "  + chat.getCollarColor().toString() );
				}	
			}
			
			// on renvoie l'identifiant de l'animal
			session.send(entity.getEntityId());				

				
		// world.spawnHorse							
		} else if (command.equals("spawnHorse")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			org.bukkit.entity.EntityType typeanimal = org.bukkit.entity.EntityType.HORSE;
			Entity entity = world.spawnEntity(loc, typeanimal);
			
			// on vérifie qu'il s'agit d'un cheval			
			if ( entity instanceof org.bukkit.entity.Horse) {
				// on place les arguments supplémentaires : couleur - marques - age (baby ou adult) - domestication - puissance de saut
				org.bukkit.entity.Horse cheval = (org.bukkit.entity.Horse) entity;
				int nbr_arg = args.length-3;
				if (nbr_arg >= 1) {  // couleur de la robe
					org.bukkit.entity.Horse.Color robeDeCheval = org.bukkit.entity.Horse.Color.valueOf(args[3]);
					cheval.setColor(robeDeCheval);	
					//plugin.getLogger().info("robe "  + cheval.getColor().toString() );
				}
				if (nbr_arg >= 2) { // marques de la robe
					org.bukkit.entity.Horse.Style marques = org.bukkit.entity.Horse.Style.valueOf(args[4]);
					cheval.setStyle(marques);
					//plugin.getLogger().info("marques "  + cheval.getStyle().toString() );
				}					
				if (nbr_arg >= 3) { // 
					if (args[5].contentEquals("BABY") ){
						cheval.setBaby();
					} else {
						cheval.setAdult();
					}
				}						
				if (nbr_arg >= 4) { // puissance de saut
					double saut = (double) Double.parseDouble(args[6]) ;
					if (saut  > 2.0 || saut < 0.0)  {
						saut = 1.0;							
					}
					cheval.setJumpStrength(saut);
				}					
				cheval.setTamed(true);
				//plugin.getLogger().info("apprivoise / Tamed :  "  + cheval.isTamed() );
									
			}
			// on renvoie l'identifiant de l'animal
			session.send(entity.getEntityId());	
				
			// world.spawnParrot						
			} else if (command.equals("spawnParrot")) {
				Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
				org.bukkit.entity.EntityType typeanimal = org.bukkit.entity.EntityType.PARROT;
				Entity entity = world.spawnEntity(loc, typeanimal);
				
				// on vérifie qu'il s'agit d'un perroquet				
				if ( entity instanceof org.bukkit.entity.Parrot) {
					// on place les arguments supplémentaires : variant
					org.bukkit.entity.Parrot perroquet = (org.bukkit.entity.Parrot) entity;
					int nbr_arg = args.length-3;
					if (nbr_arg >= 1) {  // couleur du perroquet
						org.bukkit.entity.Parrot.Variant typeDeperroquet = org.bukkit.entity.Parrot.Variant.valueOf(args[3]);
						perroquet.setVariant(typeDeperroquet);												
					}
					if (nbr_arg >= 2) { // 
						if (args[4].contentEquals("BABY") ){
							perroquet.setBaby();
						} else {
							perroquet.setAdult();
						}
					}	
				}					
				// on renvoie l'identifiant de l'animal
				session.send(entity.getEntityId());				

					
			// world.spawnRabbit					
			} else if (command.equals("spawnRabbit")) {
				Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
				org.bukkit.entity.EntityType typeanimal = org.bukkit.entity.EntityType.RABBIT;
				Entity entity = world.spawnEntity(loc, typeanimal);
				
				// on vérifie qu'il s'agit d'un lapin			
				if ( entity instanceof org.bukkit.entity.Rabbit) {
					// on place les arguments supplémentaires : variant
					org.bukkit.entity.Rabbit lapin = (org.bukkit.entity.Rabbit) entity;
					int nbr_arg = args.length-3;
					if (nbr_arg >= 1) {  // pelage
						org.bukkit.entity.Rabbit.Type typeDeLapin = org.bukkit.entity.Rabbit.Type.valueOf(args[3]);
						lapin.setRabbitType(typeDeLapin);												
					}
					if (nbr_arg >= 2) { // 
						if (args[4].contentEquals("BABY") ){
							lapin.setBaby();
						} else {
							lapin.setAdult();
						}
					}	
				}					
				// on renvoie l'identifiant de l'animal
				session.send(entity.getEntityId());						
					

			// world.spawnWolf						
			} else if (command.equals("spawnWolf")) {
				Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
				org.bukkit.entity.EntityType typeanimal = org.bukkit.entity.EntityType.WOLF;
				Entity entity = world.spawnEntity(loc, typeanimal);
				
				// on vérifie qu'il s'agit d'un loup				
				if ( entity instanceof org.bukkit.entity.Wolf) {
					// on place les arguments supplémentaires : bébé,  couleur collier - anima apprivoisé
					org.bukkit.entity.Wolf loup = (org.bukkit.entity.Wolf) entity;
					int nbr_arg = args.length-3;
					if (nbr_arg >= 1) { // bébé
						if (args[3].contentEquals("BABY") ){
							loup.setBaby();
						} else {
							loup.setAdult();
						}
					}						
					if (nbr_arg >= 2) { // couleur du collier
						// le loup est apprivoisé
						loup.setAngry(false);
						loup.setTamed(true);
						org.bukkit.DyeColor couleur = org.bukkit.DyeColor.valueOf(args[4]);
						loup.setCollarColor(couleur);
						//plugin.getLogger().info("Collier "  + loup.getCollarColor().toString() );
					} else { // le loup est sauvage
						loup.setAngry(true);
						loup.setTamed(false);
					}
				}
			
			// on renvoie l'identifiant de l'animal
			session.send(entity.getEntityId());								
					
					
			// world.removeEntity
		} else if (command.equals("removeEntity")) {
			int result = 0;
			for (Entity e : world.getEntities()) {
				if (e.getEntityId() == Integer.parseInt(args[0]))
				{
					e.remove();
					result = 1;
					break;
				}
			}
			session.send(result);

			// world.removeEntities
		} else if (command.equals("removeEntities")) {
			org.bukkit.entity.EntityType entityType = org.bukkit.entity.EntityType.valueOf(args[0]);				
			int removedEntitiesCount = 0;
			for (Entity e : world.getEntities()) {
				if (e.getType() == entityType)
				{
					e.remove();
					removedEntitiesCount++;
				}
			}
			session.send(removedEntitiesCount);

			// world.setEntityName
		} else if (command.equals("setEntityName")) {
			int result = 0;
			for (Entity e : world.getEntities()) {
				if (e.getEntityId() == Integer.parseInt(args[0]))
				{
					e.setCustomName(args[1]);
					e.setCustomNameVisible(true);
					result = 1;
					break;
				}
			}
			session.send(result);				


			// world.getEntityTypes
		} else if (command.equals("getEntityTypes")) {
			StringBuilder bdr = new StringBuilder();				

			for (EntityType entityType : org.bukkit.entity.EntityType.values()) { // values() renvoie un tableau de EntityType
				if ( entityType.isSpawnable() && entityType.isAlive() ) {
					// on transforme le tableau en chaîne de caractères dont les champs sont séparés par une ','
					bdr.append(entityType.toString());
					//plugin.getLogger().info("entityType : " + entityType.toString());
					bdr.append(",");
				}
			}				
			session.send(bdr.toString());


			// world.setSign
		} else if (command.equals("setSign")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			Block thisBlock = world.getBlockAt(loc);

			thisBlock.setType(Material.valueOf(args[3]));

			org.bukkit.block.data.type.Sign s = (org.bukkit.block.data.type.Sign) thisBlock.getBlockData();
			s.setRotation(BlockFace.valueOf(args[4]));
			thisBlock.setBlockData(s);

			BlockState signState = thisBlock.getState();

			if (signState instanceof Sign) {
				Sign sign = (Sign) signState;

				for (int i = 5; i - 5 < 4 && i < args.length; i++) {
					sign.setLine(i - 5, args[i]);
				}
				sign.update();
			}


		} else if (command.equals("setWallSign")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			Block thisBlock = world.getBlockAt(loc);
			thisBlock.setType(Material.valueOf(args[3]));

			WallSign s = (WallSign) thisBlock.getBlockData();
			s.setFacing(BlockFace.valueOf(args[4]));
			thisBlock.setBlockData(s);

			BlockState signState = thisBlock.getState();

			if (signState instanceof Sign) {
				Sign sign = (Sign) signState;

				for (int i = 5; i - 5 < 4 && i < args.length; i++) {
					sign.setLine(i - 5, args[i]);
				}
				sign.update();
			}

		} else if (command.equals("setBed")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			Block thisBlock = world.getBlockAt(loc);
			
			//blockType of bed
			Material blocType = Material.getMaterial(args[3]);
			thisBlock.setType(blocType);

			//part of the Bed "FOOT" or "HEAD"
			org.bukkit.block.data.type.Bed.Part partie = org.bukkit.block.data.type.Bed.Part.valueOf(args[4]);
			org.bukkit.block.data.type.Bed lit = (org.bukkit.block.data.type.Bed) thisBlock.getBlockData();
			lit.setPart(partie);
			
			//facing direction  : NORTH SOUTH WEST EAST
			BlockFace face = BlockFace.valueOf(args[5]);
			lit.setFacing(face);
			thisBlock.setBlockData(lit);

		// 	world setSlab
		} else if (command.equals("setSlab")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			Block thisBlock = world.getBlockAt(loc);
			
			//BlockType Material de la plaque
			Material blocType = Material.getMaterial(args[3]);
			thisBlock.setType(blocType);

			//type de la plaque : BOTTOM, DOUBLE, TOP
			org.bukkit.block.data.type.Slab.Type partie = org.bukkit.block.data.type.Slab.Type.valueOf(args[4]);
			org.bukkit.block.data.type.Slab slab = (org.bukkit.block.data.type.Slab) thisBlock.getBlockData();
			slab.setType(partie);
			
			thisBlock.setBlockData(slab);				
			
		// 	world setBlockDir  - Set Block  BlockData : directional type
		} else if (command.equals("setBlockDir")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			Block thisBlock = world.getBlockAt(loc);
			
			//On attribue le Material au bloc
			org.bukkit.Material matiere = Material.getMaterial(args[3]);
			thisBlock.setType(matiere);
			
			// On crée la chaîne de caractère qui contient la direction facing "esat", "west", ...
			String chaine = "[facing="+args[4].toLowerCase()+"]";
			// affichage de contrôle
			//plugin.getLogger().info(" valeur.toString() : " + chaine);	
			
			// On crée le BlockData databloc avec la matière et l'orientation choisie
			org.bukkit.block.data.BlockData databloc = Bukkit.createBlockData(matiere,chaine);
			
			// on attibue le blocdata au bloc courant
			thisBlock.setBlockData(databloc);
			
			// Affichage de vérification - contrôle du bloc créé
			//org.bukkit.block.BlockState etat = thisBlock.getState();
			//plugin.getLogger().info("BlockState : " + etat.toString());	
			//org.bukkit.block.data.BlockData valeur = etat.getBlockData();
			//plugin.getLogger().info("BlockData : " + valeur.toString());

			
		// 	world setBlockMultiFace  - Set Block  BlockData : MultipleFacing type
		} else if (command.equals("setBlockMultiFace")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			org.bukkit.block.Block thisBlock = world.getBlockAt(loc);
			
			//On attribue le Material au bloc
			org.bukkit.Material matiere = Material.getMaterial(args[3]);
			thisBlock.setType(matiere);
			
			org.bukkit.block.data.BlockData databloc = thisBlock.getBlockData();
			//plugin.getLogger().info("orientation choisie " + databloc.toString());
			
			if ( databloc instanceof org.bukkit.block.data.MultipleFacing) {
				//plugin.getLogger().info("Multifacing bloc");
				org.bukkit.block.data.MultipleFacing multiface = (org.bukkit.block.data.MultipleFacing) databloc;
				
				// récupération des arguments à partir du 4ème
				for ( int i = 4; i-4 < 5 && i < args.length; i++) {
					//facing direction  : NORTH SOUTH WEST EAST UP 
					BlockFace face = BlockFace.valueOf(args[i]);
					//multietat.setFace(face, true);
					((org.bukkit.block.data.MultipleFacing) multiface).setFace(face, true);
					// vérification du choix
					//plugin.getLogger().info("orientation choisie " + args[i]);
			}
			}
			// On crée le BlockData databloc avec la matière et l'orientation choisie
			//org.bukkit.block.data.BlockData databloc = Bukkit.createBlockData(matiere,chaine);
			
			// on attibue le blocdata au bloc courant
			thisBlock.setBlockData(databloc);
			
			// Affichage de vérification - contrôle du bloc créé
			//org.bukkit.block.BlockState etat = thisBlock.getState();
			//plugin.getLogger().info("BlockState : " + etat.toString());	
			//org.bukkit.block.data.BlockData valeur = etat.getBlockData();
			//plugin.getLogger().info("BlockData : " + valeur.toString());


			
		// 	world setBlockRotat  - Set Block  BlockData : Rotatable type
		} else if (command.equals("setBlockRotat")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			org.bukkit.block.Block thisBlock = world.getBlockAt(loc);
			
			//On fixe le Material au bloc
			org.bukkit.Material matiere = Material.getMaterial(args[3]);
			thisBlock.setType(matiere);
			
			org.bukkit.block.data.BlockData databloc = thisBlock.getBlockData();
			//plugin.getLogger().info("rotation choisie " + databloc.toString());
			
			// Orientation du Bloc
			if ( databloc instanceof org.bukkit.block.data.Rotatable) {
				//plugin.getLogger().info("Rotatable bloc");
				org.bukkit.block.data.Rotatable rotation = (org.bukkit.block.data.Rotatable) databloc;
				
				// On paramètre la rotation du bloc
				//plugin.getLogger().info("rotation choisie " + args[4]);
				BlockFace face = BlockFace.valueOf(args[4]);
				((org.bukkit.block.data.Rotatable) rotation).setRotation(face);
			}
			// mise à jour du bloc
			thisBlock.setBlockData(databloc);
			
			// gestion du motif du bloc
			org.bukkit.block.BlockState etatblock = thisBlock.getState();
			if (etatblock instanceof org.bukkit.block.Banner) {
	
				// paramètres de la banière
				org.bukkit.block.Banner banner = (org.bukkit.block.Banner) thisBlock.getState();
				

				// tableau de patterns
				int dim = args.length-5;
				org.bukkit.block.banner.Pattern[] patterns = (new org.bukkit.block.banner.Pattern[dim]);
				
											
				//Motifs en couleur sur la bannière
				int j = 0;
				for ( int i = 5;  i < args.length ; i = i+2) {
					org.bukkit.block.banner.PatternType motif = org.bukkit.block.banner.PatternType.valueOf(args[i]);
					org.bukkit.DyeColor color  = org.bukkit.DyeColor.valueOf(args[i+1]);
					//plugin.getLogger().info(" PatternType : "+ motif.toString() );
					//plugin.getLogger().info(" PatternType : "+ color.toString() );

					patterns[j] = new org.bukkit.block.banner.Pattern(color, motif);
					
					banner.addPattern(patterns[j]);
					j = j+1;
				}	

			
			// Mise à jour du bloc
			banner.update();
			banner.setBlockData(databloc);
			}
							
			// Affichage de vérification - contrôle du bloc créé
			//org.bukkit.block.BlockState etat = thisBlock.getState();
			//plugin.getLogger().info("BlockState : " + etat.toString());	
			//org.bukkit.block.data.BlockData valeur = etat.getBlockData();
			//plugin.getLogger().info("BlockData : " + valeur.toString());				
			
			
		// 	world setBlockOrient  - Set Block de type block.data.Orientable
		} else if (command.equals("setBlockOrient")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			org.bukkit.block.Block thisBlock = world.getBlockAt(loc);
			
			//On attribue le Material au bloc
			org.bukkit.Material matiere = Material.getMaterial(args[3]);
			thisBlock.setType(matiere);
			
			// On crée la chaîne de caractère qui contient l'axe d'orientation x,y ou z
			String chaine = "[axis="+args[4].toLowerCase()+"]";
			// affichage de contrôle
			//plugin.getLogger().info(" valeur.toString() : " + chaine);	
			
			// On crée le BlockData databloc avec la matière et la direction axiale
			org.bukkit.block.data.BlockData databloc = Bukkit.createBlockData(matiere,chaine);
			
			// on attibue le blocdata au bloc courant
			thisBlock.setBlockData(databloc);
			
			// Affichage de vérification - contrôle du bloc créé
			//org.bukkit.block.BlockState etat = thisBlock.getState();
			//plugin.getLogger().info("BlockState : " + etat.toString());	
			//org.bukkit.block.data.BlockData valeur = etat.getBlockData();
			//plugin.getLogger().info("BlockData : " + valeur.toString());
		
			
		// 	world setBlockBisected - Set Block de type block.data.Bisected
		} else if (command.equals("setBlockBisected")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			org.bukkit.block.Block thisBlock = world.getBlockAt(loc);
			
			//On attribue le Material au bloc
			org.bukkit.Material matiere = Material.getMaterial(args[3]);
			thisBlock.setType(matiere);
			
			// On crée la chaîne de caractère qui contient la moitié "half"
			String chaine = "[half="+args[4].toLowerCase()+"]";
			// affichage de contrôle
			//plugin.getLogger().info(" valeur.toString() : " + chaine);	
			
			// On crée le BlockData databloc avec la matière et la direction axiale
			org.bukkit.block.data.BlockData databloc = Bukkit.createBlockData(matiere,chaine);
			
			// on attibue le blocdata au bloc courant
			thisBlock.setBlockData(databloc);
			
			// Affichage de vérification - contrôle du bloc créé
			//org.bukkit.block.BlockState etat = thisBlock.getState();
			//plugin.getLogger().info("BlockState : " + etat.toString());	
			//org.bukkit.block.data.BlockData valeur = etat.getBlockData();
			//plugin.getLogger().info("BlockData : " + valeur.toString());				

			
		// 	world setBlockAge  - Set Block  BlockData : ageable type
		} else if (command.equals("setBlockAge")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			org.bukkit.block.Block thisBlock = world.getBlockAt(loc);
		
			//On attribue le Material au bloc
			org.bukkit.Material matiere = Material.getMaterial(args[3]);
			thisBlock.setType(matiere);
							
			// On crée la chaîne de caractère qui contient l'age
			String chaine = "[age="+args[4]+"]";
			// affichage de contrôle
			//plugin.getLogger().info(" valeur.toString() : " + chaine);	
			
			// On crée le BlockData databloc avec la matière et l'age
			org.bukkit.block.data.BlockData databloc = Bukkit.createBlockData(matiere,chaine);
			
			// on attibue le blocdata au bloc courant
			thisBlock.setBlockData(databloc);
			
			// Affichage de vérification - Mise au point - contrôle du bloc créé
			//org.bukkit.block.BlockState etat = thisBlock.getState();
			//plugin.getLogger().info("BlockState : " + etat.toString());	
			//org.bukkit.block.data.BlockData blockData = etat.getBlockData();
			//plugin.getLogger().info("BlockData : " + blockData.toString());

			//int an = (int) Double.parseDouble(args[4]);
			
			// extraction du bloc data correspondant - attention il faut déclarer :
			//import org.bukkit.block.data.Ageable
			//org.bukkit.block.data.Ageable age = (Ageable)blockData;
			//int ageMax = age.getMaximumAge();
			//age.setAge(an);
			//plugin.getLogger().info("Age max : " + ageMax);
			//plugin.getLogger().info("           ");					

			
		// 	world setBlockSapl  - Set Block  BlockData : sapling type
		} else if (command.equals("setBlockSapl")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			Block thisBlock = world.getBlockAt(loc);
			
			//On attribue le Material au bloc
			org.bukkit.Material matiere = Material.getMaterial(args[3]);
			thisBlock.setType(matiere);
			
			// on fixe la valeur de saple - taille				
			org.bukkit.block.data.type.Sapling blocData = (org.bukkit.block.data.type.Sapling) thisBlock.getBlockData();
			int stage = (int) Double.parseDouble(args[4]);
			blocData.setStage(stage);
			
			// Affichage de vérification - Mise au point - contrôle du bloc créé
			//int stageMax = blocData.getMaximumStage();
			//org.bukkit.block.BlockState etat = thisBlock.getState();
			//plugin.getLogger().info("BlockState : " + etat.toString());	
			//org.bukkit.block.data.BlockData blockData = etat.getBlockData();
			//plugin.getLogger().info("BlockData : " + blockData.toString());
			//plugin.getLogger().info("Stage max : " + stageMax);
			//plugin.getLogger().info("           ");	

			
			// on attibue le blocdata au bloc courant
			thisBlock.setBlockData(blocData);

		// 	world setBlockLevel - Set Block  BlockData : Levelled type
		} else if (command.equals("setBlockLevel")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			Block thisBlock = world.getBlockAt(loc);
			
			//On attribue le Material au bloc
			org.bukkit.Material matiere = Material.getMaterial(args[3]);
			thisBlock.setType(matiere);
			
			// on fixe la valeur de level
			// In the case of water and lava blocks the levels have special meanings: 
			// a level of 0 corresponds to a source block, 
			// 1-7 regular fluid heights,
			// and 8-15 to "falling" fluids. 
			// All falling fluids have the same behaviour, but the level corresponds to that of the block above them,
			//equal to this.level - 8 Note that counterintuitively, an adjusted level of 1 is the highest level, 
			// whilst 7 is the lowest.
			org.bukkit.block.data.Levelled blocData = (org.bukkit.block.data.Levelled) thisBlock.getBlockData();
			int level = (int) Double.parseDouble(args[4]);
			blocData.setLevel(level);
			
			// Affichage de vérification - Mise au point - contrôle du bloc créé
			//int levelMax = blocData.getMaximumLevel();
			//org.bukkit.block.BlockState etat = thisBlock.getState();
			//plugin.getLogger().info("BlockState : " + etat.toString());	
			//org.bukkit.block.data.BlockData blockData = etat.getBlockData();
			//plugin.getLogger().info("BlockData : " + blockData.toString());
			//plugin.getLogger().info("Level max : " + levelMax);
			//plugin.getLogger().info("           ");	
			
			// on attibue le blocdata au bloc courant
			thisBlock.setBlockData(blocData);
			
				
		// 	world setStairs
		} else if (command.equals("setStairs")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			Block thisBlock = world.getBlockAt(loc);
			
			//blockType of stairs
			Material blocType = Material.getMaterial(args[3]);
			thisBlock.setType(blocType);

			org.bukkit.block.data.type.Stairs escalier = (org.bukkit.block.data.type.Stairs) thisBlock.getBlockData();
			
			//facing direction  : NORTH SOUTH WEST EAST
			BlockFace face = BlockFace.valueOf(args[4]);
			escalier.setFacing(face);
			
			//Shape of the stairs : STRAIGHT, INNER_LEFT, INNER_RIGHT, OUTER_LEFT, OUTER_RIGHT
			org.bukkit.block.data.type.Stairs.Shape forme = org.bukkit.block.data.type.Stairs.Shape.valueOf(args[5]);
			escalier.setShape(forme);
			
			//Half of the stairs : BOTTOM, TOP
			org.bukkit.block.data.Bisected.Half half = org.bukkit.block.data.Bisected.Half.valueOf(args[6]);
			escalier.setHalf(half);
								
			thisBlock.setBlockData(escalier);	
			
		// 	world setGate
		} else if (command.equals("setGate")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			Block thisBlock = world.getBlockAt(loc);
			
			//blockType of gate
			Material blocType = Material.getMaterial(args[3]);
			thisBlock.setType(blocType);

			org.bukkit.block.data.type.Gate gate = (org.bukkit.block.data.type.Gate) thisBlock.getBlockData();
			
			//facing direction  : NORTH SOUTH WEST EAST
			BlockFace face = BlockFace.valueOf(args[4]);
			//plugin.getLogger().info("orientation choisie " + args[4]);
			//plugin.getLogger().info("orientations possibles " + gate.getFaces());
			gate.setFacing(face);

			//Attacher au mur
			//plugin.getLogger().info("attachee au mur " + args[5]);
			if (args[5].contentEquals("True")) {
					gate.setInWall(true);	
			}
			else gate.setInWall(false);
									
			//Close door
			gate.setOpen(false);
												
			thisBlock.setBlockData(gate);	

	} else if (command.equals("setPane")) {
		Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
		Block thisBlock = world.getBlockAt(loc);
		
		//blockType of gate
		Material blocType = Material.getMaterial(args[3]);
		thisBlock.setType(blocType);

		org.bukkit.block.data.type.GlassPane pane = (org.bukkit.block.data.type.GlassPane) thisBlock.getBlockData();
		
		for ( int i = 4; i-4 < 4 && i < args.length; i++) {
			//facing direction  : NORTH SOUTH WEST EAST
			BlockFace face = BlockFace.valueOf(args[i]);
			pane.setFace(face, true);
			//plugin.getLogger().info("orientation choisie " + args[i]);
		}
		
		//plugin.getLogger().info("orientations possibles " + pane.getFaces());																		
		thisBlock.setBlockData(pane);					
			
		// 	world setFurnace
		} else if (command.equals("setFurnace")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			Block thisBlock = world.getBlockAt(loc);
			
			//blockType of furnace
			Material blocType = Material.getMaterial(args[3]);
			thisBlock.setType(blocType);

			org.bukkit.block.data.type.Furnace furnace = (org.bukkit.block.data.type.Furnace) thisBlock.getBlockData();
			
			//facing direction  : NORTH SOUTH WEST EAST
			BlockFace face = BlockFace.valueOf(args[4]);
			furnace.setFacing(face);

			//Lightedor not Lighted Furnace
			//plugin.getLogger().info("allume : " + args[5]);
			if (args[5].equals("False")) {
					furnace.setLit(false);	
					//plugin.getLogger().info("allume : " + args[5]);
			}
			else furnace.setLit(true);
			
												
			thisBlock.setBlockData(furnace);
			//plugin.getLogger().info("allume : " + furnace.isLit());


		// 	world setTrapDoor
		} else if (command.equals("setTrapDoor")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			Block thisBlock = world.getBlockAt(loc);
			
			//blockType of gate
			Material blocType = Material.getMaterial(args[3]);
			thisBlock.setType(blocType);

			org.bukkit.block.data.type.TrapDoor trapdoor = (org.bukkit.block.data.type.TrapDoor) thisBlock.getBlockData();
			
			//facing direction qd la trappe est ouverte : NORTH SOUTH WEST EAST
			BlockFace face = BlockFace.valueOf(args[4]);
			trapdoor.setFacing(face);

			// Half : TOP ou BOTTOM
			org.bukkit.block.data.Bisected.Half half =  org.bukkit.block.data.Bisected.Half.valueOf(args[5]);
			trapdoor.setHalf(half);
						
			
			//position ouverte True ou fermée False
			if (args[6].equals("True")) {
					trapdoor.setOpen(true);	
			}
			else trapdoor.setOpen(false);
												
			thisBlock.setBlockData(trapdoor);					
			
			
		// 	world setFence
		} else if (command.equals("setFence")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			Block thisBlock = world.getBlockAt(loc);
			
			//blockType of BlockData : fence
			Material blocType = Material.getMaterial(args[3]);
			thisBlock.setType(blocType);

			org.bukkit.block.data.type.Fence fence = (org.bukkit.block.data.type.Fence) thisBlock.getBlockData();
							
			for ( int i = 4; i-4 < 4 && i < args.length; i++) {
				//facing direction  : NORTH SOUTH WEST EAST
				BlockFace face = BlockFace.valueOf(args[i]);
				fence.setFace(face, true);
				//plugin.getLogger().info("orientation choisie " + args[i]);
			}
							
			thisBlock.setBlockData(fence);								
			
		// 	world setDoor					
		} else if (command.equals("setDoor")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			Block thisBlock = world.getBlockAt(loc);
			
			//blockType of gate
			Material blocType = Material.getMaterial(args[3]);
			thisBlock.setType(blocType);

			org.bukkit.block.data.type.Door porte = (org.bukkit.block.data.type.Door) thisBlock.getBlockData();
			
			//facing direction  : NORTH SOUTH WEST EAST
			BlockFace face = BlockFace.valueOf(args[4]);
			porte.setFacing(face);
			
			//Hinge  : LEFT or RIGHT
			org.bukkit.block.data.type.Door.Hinge lien = org.bukkit.block.data.type.Door.Hinge.valueOf(args[5]);
			porte.setHinge(lien);					

			//Bisected : BOTTOM or TOP
			org.bukkit.block.data.Bisected.Half position = org.bukkit.block.data.Bisected.Half.valueOf(args[6]);
			porte.setHalf(position);																		
			
			//Close door
			porte.setOpen(false);
			
			//Powerable
			porte.setPowered(true);
								
			thisBlock.setBlockData(porte);	
					
		// 	world setChest
		} else if (command.equals("setChest")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			Block thisBlock = world.getBlockAt(loc);
			
			//blockType of Chest
			Material blocType = Material.getMaterial(args[3]);
			thisBlock.setType(blocType);

			org.bukkit.block.data.type.Chest chest = (org.bukkit.block.data.type.Chest) thisBlock.getBlockData();
			
			//chest type  : RIGHT, LEFT, SINGLE
			org.bukkit.block.data.type.Chest.Type chestType = org.bukkit.block.data.type.Chest.Type.valueOf(args[4]);
			chest.setType(chestType);		
									
			//facing direction  : NORTH SOUTH WEST EAST
			BlockFace face = BlockFace.valueOf(args[5]);
			chest.setFacing(face);
			
			thisBlock.setBlockData(chest);							
					
			// world.explode
		} else if (command.equals("createExplosion")) {
			Location loc = session.parseRelativeBlockLocation(args[0], args[1], args[2]);
			Float power = Float.parseFloat(args[3]);

			world.createExplosion(loc, power);

		} else {
			session.plugin.getLogger().warning(preFix + command + " is not supported.");
			session.send("Fail," + preFix + command + " is not supported.");
		}
	}

	// create a cuboid of lots of blocks
	private void setCuboid(Location pos1, Location pos2, Material blockType) {
		int minX, maxX, minY, maxY, minZ, maxZ;
		World world = pos1.getWorld();
		minX = pos1.getBlockX() < pos2.getBlockX() ? pos1.getBlockX() : pos2.getBlockX();
		maxX = pos1.getBlockX() >= pos2.getBlockX() ? pos1.getBlockX() : pos2.getBlockX();
		minY = pos1.getBlockY() < pos2.getBlockY() ? pos1.getBlockY() : pos2.getBlockY();
		maxY = pos1.getBlockY() >= pos2.getBlockY() ? pos1.getBlockY() : pos2.getBlockY();
		minZ = pos1.getBlockZ() < pos2.getBlockZ() ? pos1.getBlockZ() : pos2.getBlockZ();
		maxZ = pos1.getBlockZ() >= pos2.getBlockZ() ? pos1.getBlockZ() : pos2.getBlockZ();

		for (int x = minX; x <= maxX; ++x) {
			for (int z = minZ; z <= maxZ; ++z) {
				for (int y = minY; y <= maxY; ++y) {
					updateBlock(world, x, y, z, blockType);
				}
			}
		}
	}

	// get a cuboid of lots of blocks
	private String getBlocks(Location pos1, Location pos2) {
		StringBuilder blockData = new StringBuilder();

		int minX, maxX, minY, maxY, minZ, maxZ;
		World world = pos1.getWorld();
		minX = pos1.getBlockX() < pos2.getBlockX() ? pos1.getBlockX() : pos2.getBlockX();
		maxX = pos1.getBlockX() >= pos2.getBlockX() ? pos1.getBlockX() : pos2.getBlockX();
		minY = pos1.getBlockY() < pos2.getBlockY() ? pos1.getBlockY() : pos2.getBlockY();
		maxY = pos1.getBlockY() >= pos2.getBlockY() ? pos1.getBlockY() : pos2.getBlockY();
		minZ = pos1.getBlockZ() < pos2.getBlockZ() ? pos1.getBlockZ() : pos2.getBlockZ();
		maxZ = pos1.getBlockZ() >= pos2.getBlockZ() ? pos1.getBlockZ() : pos2.getBlockZ();

		for (int y = minY; y <= maxY; ++y) {
			for (int x = minX; x <= maxX; ++x) {
				for (int z = minZ; z <= maxZ; ++z) {
					blockData.append(world.getBlockAt(x, y, z).getType().name() + ",");
				}
			}
		}

		return blockData.substring(0, blockData.length() > 0 ? blockData.length() - 1 : 0);    // We don't want last comma
	}

	// updates a block
	private void updateBlock(World world, Location loc, Material blockType) {
		Block thisBlock = world.getBlockAt(loc);
		updateBlock(thisBlock, blockType);
	}
	
	private void updateBlock(World world, int x, int y, int z, Material blockType) {
		Block thisBlock = world.getBlockAt(x,y,z);
		updateBlock(thisBlock, blockType);
	}
	
	private void updateBlock(Block thisBlock, Material blockType) {
		// check to see if the block is different - otherwise leave it 
		thisBlock.setType(blockType);
		if ((thisBlock.getType() != blockType)) {
			thisBlock.setType(blockType);
		}
	}


}
