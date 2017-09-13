package com.dabomstew.pkrandom.pokemon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MovesetTemplate {
    public final String rarity;
    private final String[] templates;
    private final Random random;
    
    private Set<Type> allTypes;
    
    public MovesetTemplate(Random random, String rarity, String[] templates) {
        this.random = random;
        this.rarity = rarity;
        this.templates = templates;
        
        allTypes = new HashSet<Type>();
        allTypes.add(Type.NORMAL);
        allTypes.add(Type.FIGHTING);
        allTypes.add(Type.FLYING);
        allTypes.add(Type.BUG);
        allTypes.add(Type.ROCK);
        allTypes.add(Type.GROUND);
        allTypes.add(Type.POISON);
        allTypes.add(Type.WATER);
        allTypes.add(Type.GRASS);
        allTypes.add(Type.FIRE);
        allTypes.add(Type.ICE);
        allTypes.add(Type.ELECTRIC);
        allTypes.add(Type.PSYCHIC);
        allTypes.add(Type.GHOST);
        allTypes.add(Type.DRAGON);
        allTypes.add(Type.DARK);
        allTypes.add(Type.STEEL);
    }

    private Move generateMove(String template, List<Move> allMoves, Type forceType) {
        List<Move> matchingMoves = new ArrayList<Move>();
        for(Move move : allMoves) {
            if(matches(template, move) && (forceType == null || move.type.equals(forceType))) {
                matchingMoves.add(move);
            }
        }
        if(matchingMoves.size() == 0) {
            return null;
        }
        return matchingMoves.get(random.nextInt(matchingMoves.size()));
    }
    
    public Move[] generate(Pokemon pk, List<Move> allMoves) {
        Move[] moves = new Move[4];
        Set<Type> typesLeft = new HashSet<Type>(this.allTypes);

        // System.out.println(pk.name);
        for(int i = 0; i < 4; i++) {
            boolean forceDamaging = templates[i].endsWith("dm");
            boolean forceStab = forceDamaging && random.nextDouble() < 0.6;
            Move move = null;
            if(forceStab) {
                List<Type> allowedTypes = new ArrayList<Type>();
                if(pk.primaryType != null && typesLeft.contains(pk.primaryType)) {
                    allowedTypes.add(pk.primaryType);
                }
                if(pk.secondaryType != null && typesLeft.contains(pk.secondaryType)) {
                    allowedTypes.add(pk.secondaryType);
                }
                if(allowedTypes.size() > 0) {
                    Type moveType = allowedTypes.get(random.nextInt(allowedTypes.size()));
                    move = generateMove(templates[i], allMoves, moveType);
                    
                    for(int k = 0; move == null && k < 10; k++) {
                        move = generateMove(templates[i], allMoves, moveType);
                    }
                }
            }
            
            while(move == null) {
                if(forceDamaging) {
                    List<Type> typeList = new ArrayList(typesLeft);
                    Type moveType = typeList.get(random.nextInt(typeList.size()));
                    move = generateMove(templates[i], allMoves, moveType);
                } else {
                    move = generateMove(templates[i], allMoves, null);
                }
            }
            
            if(forceDamaging) {
                typesLeft.remove(move.type);
            }
            moves[i] = move;
            // System.out.println(move.name);
        }
        // System.out.println();

        return moves;
    }
    
    private static boolean contains(Move move, String[] candidates) {
        String name = move.name.toLowerCase();
        for(String candidate : candidates) {
            if(name.equals(candidate)) return true;
        }
        return false;
    }
    
    private static boolean matches(String template, Move move) {
        if(move == null) return false;
        if(move.name.toLowerCase().equals("struggle")) return false;

        // Specific move reference
        if(!template.startsWith(".")) {
            return template.toLowerCase().equals(move.name.toLowerCase());
        }

        if(template.equals(".any")) {
            return true;
        } else if(template.equals(".dm")) {
            return move.isDamaging();
        } else if(template.equals(".pdm")) {
            return move.isDamaging() && !move.isSpecial();
        } else if(template.equals(".sdm")) {
            return move.isDamaging() && move.isSpecial();
        } else if(template.equals(".priority")) {
            return contains(move, new String[] 
                    {"mach punch", "extremespeed", "quick attack"});
        } else if(template.equals(".antighost")) {
            return move.isDamaging() && (
                    move.type.equals(Type.DARK) || move.type.equals(Type.GHOST) || move.type.equals(Type.GROUND) || move.type.equals(Type.PSYCHIC)
                    );
        } else if(template.equals(".status")) {
            return contains(move, new String[] 
                    {"spore", "thunder wave", "toxic", "confuse ray", "glare", "sacred fire", "sludge bomb"});
        } else if(template.equals(".antisetup")) {
            return contains(move, new String[] 
                    {"haze", "perish song"});
        } else if(template.equals(".antispeed")) {
            return contains(move, new String[] 
                    {"icy wind", "scary face", "thunder wave", "glare", "agility"});
        } else if(template.equals(".heal")) {
            return contains(move, new String[] 
                    {"recover", "milk drink", "rest"});
        } else if(template.equals(".screen")) {
            return contains(move, new String[] 
                    {"reflect", "light screen"});
        } else if(template.equals(".darude")) {
            return contains(move, new String[] 
                    {"sandstorm", "leech seed", "toxic", "sacred fire", "sludge bomb"});
        } else if(template.equals(".statlower")) {
            return contains(move, new String[] 
                    {"screech", "mud slap", "sand attack", "charm", "crunch", "icy wind", "scary face", "smokescreen"});
        } else if(template.equals(".defsetup")) {
            return contains(move, new String[] 
                    {"barrier", "amnesia", "curse"});
        } else {
            System.out.println("Could not resolve template " + template);
            return true;
        }
    }

    @Override
    public String toString() {
        return "MovesetTemplate [" + templates[0] + " " + templates[1] + " " + templates[2] + " " + templates[3] + "]";
    }
}
