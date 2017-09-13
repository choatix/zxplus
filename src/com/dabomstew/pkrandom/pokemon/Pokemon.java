package com.dabomstew.pkrandom.pokemon;

/*----------------------------------------------------------------------------*/
/*--  Pokemon.java - represents an individual Pokemon, and contains         --*/
/*--                 common Pokemon-related functions.                      --*/
/*--                                                                        --*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
/*--  Originally part of "Universal Pokemon Randomizer" by Dabomstew        --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2020.                                 --*/
/*--                                                                        --*/
/*--  The custom code written here is licensed under the terms of the GPL:  --*/
/*--                                                                        --*/
/*--  This program is free software: you can redistribute it and/or modify  --*/
/*--  it under the terms of the GNU General Public License as published by  --*/
/*--  the Free Software Foundation, either version 3 of the License, or     --*/
/*--  (at your option) any later version.                                   --*/
/*--                                                                        --*/
/*--  This program is distributed in the hope that it will be useful,       --*/
/*--  but WITHOUT ANY WARRANTY; without even the implied warranty of        --*/
/*--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the          --*/
/*--  GNU General Public License for more details.                          --*/
/*--                                                                        --*/
/*--  You should have received a copy of the GNU General Public License     --*/
/*--  along with this program. If not, see <http://www.gnu.org/licenses/>.  --*/
/*----------------------------------------------------------------------------*/

import com.dabomstew.pkrandom.constants.Species;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Pokemon implements Comparable<Pokemon> {

    public String name;
    public int number;

    public String formeSuffix = "";
    public Pokemon baseForme = null;
    public int formeNumber = 0;
    public int cosmeticForms = 0;
    public int formeSpriteIndex = 0;
    public boolean actuallyCosmetic = false;
    public List<Integer> realCosmeticFormNumbers = new ArrayList<>();

    public Type primaryType, secondaryType;

    public int hp, attack, defense, spatk, spdef, speed, special;

    public int ability1, ability2, ability3;

    public int catchRate, expYield;

    public int guaranteedHeldItem, commonHeldItem, rareHeldItem, darkGrassHeldItem;

    public int genderRatio;

    public int frontSpritePointer, picDimensions;

    public int callRate;

    public ExpCurve growthCurve;

    public double percentRandomizedBuffPercent = 1;

    public List<Evolution> evolutionsFrom = new ArrayList<Evolution>();
    public List<Evolution> evolutionsTo = new ArrayList<Evolution>();

    public List<MegaEvolution> megaEvolutionsFrom = new ArrayList<>();
    public List<MegaEvolution> megaEvolutionsTo = new ArrayList<>();

    protected List<Integer> shuffledStatsOrder;

    public int extra1;

    // A flag to use for things like recursive stats copying.
    // Must not rely on the state of this flag being preserved between calls.
    public boolean temporaryFlag;

    public Pokemon() {
        shuffledStatsOrder = Arrays.asList(0, 1, 2, 3, 4, 5);
    }

    public void shuffleStats(Random random) {
        Collections.shuffle(shuffledStatsOrder, random);
        applyShuffledOrderToStats();
    }

    public void copyShuffledStatsUpEvolution(Pokemon evolvesFrom) {
        // If stats were already shuffled once, un-shuffle them
        shuffledStatsOrder = Arrays.asList(
                shuffledStatsOrder.indexOf(0),
                shuffledStatsOrder.indexOf(1),
                shuffledStatsOrder.indexOf(2),
                shuffledStatsOrder.indexOf(3),
                shuffledStatsOrder.indexOf(4),
                shuffledStatsOrder.indexOf(5));
        applyShuffledOrderToStats();
        shuffledStatsOrder = evolvesFrom.shuffledStatsOrder;
        applyShuffledOrderToStats();
    }

    protected void applyShuffledOrderToStats() {
        List<Integer> stats = Arrays.asList(hp, attack, defense, spatk, spdef, speed);

        // Copy in new stats
        hp = stats.get(shuffledStatsOrder.get(0));
        attack = stats.get(shuffledStatsOrder.get(1));
        defense = stats.get(shuffledStatsOrder.get(2));
        spatk = stats.get(shuffledStatsOrder.get(3));
        spdef = stats.get(shuffledStatsOrder.get(4));
        speed = stats.get(shuffledStatsOrder.get(5));
    }

    public void randomizeStatsWithinBST(Random random) {
        if (number == Species.shedinja) {
            // Shedinja is horribly broken unless we restrict him to 1HP.
            int bst = bst() - 51;

            // Make weightings
            double atkW = random.nextDouble(), defW = random.nextDouble();
            double spaW = random.nextDouble(), spdW = random.nextDouble(), speW = random.nextDouble();

            double totW = atkW + defW + spaW + spdW + speW;

            hp = 1;
            attack = (int) Math.max(1, Math.round(atkW / totW * bst)) + 10;
            defense = (int) Math.max(1, Math.round(defW / totW * bst)) + 10;
            spatk = (int) Math.max(1, Math.round(spaW / totW * bst)) + 10;
            spdef = (int) Math.max(1, Math.round(spdW / totW * bst)) + 10;
            speed = (int) Math.max(1, Math.round(speW / totW * bst)) + 10;
        } else {
            // Minimum 20 HP, 10 everything else
            int bst = bst() - 70;

            // Make weightings
            double hpW = random.nextDouble(), atkW = random.nextDouble(), defW = random.nextDouble();
            double spaW = random.nextDouble(), spdW = random.nextDouble(), speW = random.nextDouble();

            double totW = hpW + atkW + defW + spaW + spdW + speW;

            hp = (int) Math.max(1, Math.round(hpW / totW * bst)) + 20;
            attack = (int) Math.max(1, Math.round(atkW / totW * bst)) + 10;
            defense = (int) Math.max(1, Math.round(defW / totW * bst)) + 10;
            spatk = (int) Math.max(1, Math.round(spaW / totW * bst)) + 10;
            spdef = (int) Math.max(1, Math.round(spdW / totW * bst)) + 10;
            speed = (int) Math.max(1, Math.round(speW / totW * bst)) + 10;
        }

        // Check for something we can't store
        if (hp > 255 || attack > 255 || defense > 255 || spatk > 255 || spdef > 255 || speed > 255) {
            // re roll
            randomizeStatsWithinBST(random);
        }

    }

    private int pickNewBST(Random random) {
        int minBST, maxBST;
        int fromDepth = this.evosFromDepth();
        int toDepth = this.evosToDepth();
        // pick new bst based on observed ranges for different poke types
        if (isLegendary()) {
            minBST = 580;
            maxBST = 720;
        } else if (fromDepth == 0 && toDepth == 0) {
            // solo poke
            minBST = 175;
            maxBST = 600;
        } else if (fromDepth >= 2 && toDepth == 0) {
            // first stage of 3+
            minBST = 175;
            maxBST = 365;
        } else if (fromDepth == 1 && toDepth == 0) {
            // first stage of 2
            minBST = 175;
            maxBST = 435;
        } else if (toDepth >= 1 && fromDepth >= 1) {
            // middle stage of 3+
            minBST = 205;
            maxBST = 465;
        } else {
            // last stage of 2+
            minBST = 395;
            maxBST = 600;
        }
        return (int) Math.round(minBST + random.nextDouble() * (maxBST - minBST));
    }

    private void scaleStatsToNewBST(int newBST) {
        double bstMult = (newBST - 70.0) / (bst() - 70.0);
        if (number == 292) {
            bstMult = (newBST * 5.0 / 6 - 50.0) / (bst() - 51.0);
        } else {
            hp = (int) Math.min(255, (20 + Math.round((hp - 20) * bstMult)));
        }

        attack = (int) Math.min(255, (10 + Math.round((attack - 10) * bstMult)));
        defense = (int) Math.min(255, (10 + Math.round((defense - 10) * bstMult)));
        spatk = (int) Math.min(255, (10 + Math.round((spatk - 10) * bstMult)));
        spdef = (int) Math.min(255, (10 + Math.round((spdef - 10) * bstMult)));
        speed = (int) Math.min(255, (10 + Math.round((speed - 10) * bstMult)));

        special = (int) Math.ceil((spatk + spdef) / 2.0f);
    }

    private void randomizeStatsWithinNewBST(Random random, int newBST) {
        if (number == 292) {
            int allocatablePoints = newBST * 5 / 6 - 50;

            // Make weightings
            do {
                double atkW = random.nextDouble(), defW = random.nextDouble();
                double spaW = random.nextDouble(), spdW = random.nextDouble(), speW = random.nextDouble();

                double totW = atkW + defW + spaW + spdW + speW;

                hp = 1;
                attack = (int) Math.max(1, Math.round(atkW / totW * allocatablePoints)) + 10;
                defense = (int) Math.max(1, Math.round(defW / totW * allocatablePoints)) + 10;
                spatk = (int) Math.max(1, Math.round(spaW / totW * allocatablePoints)) + 10;
                spdef = (int) Math.max(1, Math.round(spdW / totW * allocatablePoints)) + 10;
                speed = (int) Math.max(1, Math.round(speW / totW * allocatablePoints)) + 10;
            } while (hp > 255 || attack > 255 || defense > 255 || spatk > 255 || spdef > 255 || speed > 255);
        } else {
            // Minimum 20 HP, 10 everything else
            int allocatablePoints = newBST - 70;

            do {
                // Make weightings
                double hpW = random.nextDouble(), atkW = random.nextDouble(), defW = random.nextDouble();
                double spaW = random.nextDouble(), spdW = random.nextDouble(), speW = random.nextDouble();

                double totW = hpW + atkW + defW + spaW + spdW + speW;

                hp = (int) Math.max(1, Math.round(hpW / totW * allocatablePoints)) + 20;
                attack = (int) Math.max(1, Math.round(atkW / totW * allocatablePoints)) + 10;
                defense = (int) Math.max(1, Math.round(defW / totW * allocatablePoints)) + 10;
                spatk = (int) Math.max(1, Math.round(spaW / totW * allocatablePoints)) + 10;
                spdef = (int) Math.max(1, Math.round(spdW / totW * allocatablePoints)) + 10;
                speed = (int) Math.max(1, Math.round(speW / totW * allocatablePoints)) + 10;
            } while (hp > 255 || attack > 255 || defense > 255 || spatk > 255 || spdef > 255 || speed > 255);
        }

        special = (int) Math.ceil((spatk + spdef) / 2.0f);
    }

    public void randomizeBST(Random random, boolean dontRandomizeRatio) {
        int newBST = pickNewBST(random);
        if (dontRandomizeRatio) {
            scaleStatsToNewBST(newBST);
        } else {
            randomizeStatsWithinNewBST(random, newBST);
        }
    }

    public void randomizeBSTPerc(Random random, int percent, boolean dontRandomizeRatio) {
        double modifier = 1;
        if (random.nextBoolean()) {
            modifier = 1 + ((percent / 100.0f) * random.nextDouble());
        } else {
            modifier = 1 - ((percent / 100.0f) * random.nextDouble());
        }
        if ((bst() * modifier) < 180) {
            modifier = 180 / bst();
        }
        if (modifier <= 0) {
            modifier = 1;
        }
        percentRandomizedBuffPercent = modifier;
        int effectiveNewBST = (int) Math.round(bstForPowerLevels() * modifier);

        if (dontRandomizeRatio) {
            scaleStatsToNewBST(effectiveNewBST);
        } else {
            randomizeStatsWithinNewBST(random, effectiveNewBST);
        }
    }

    public void equalizeBST(Random random, boolean dontRandomizeRatio) {
        if (dontRandomizeRatio) {
            scaleStatsToNewBST(420);
        } else {
            randomizeStatsWithinNewBST(random, 420);
        }
    }

    public void percentRaiseStatFloorUpEvolution(Random random, boolean dontRandomizeRatio, Pokemon evolvesFrom) {
        percentRandomizedBuffPercent = evolvesFrom.percentRandomizedBuffPercent;
        double statRatio = evolvesFrom.percentRandomizedBuffPercent;

        int effectiveNewBST = (int) Math.round(bstForPowerLevels() * statRatio);

        if (dontRandomizeRatio) {
            scaleStatsToNewBST(effectiveNewBST);
        } else {
            randomizeStatsWithinNewBST(random, effectiveNewBST);
        }
    }

    public void copyRandomizedStatsUpEvolution(Pokemon evolvesFrom) {
        double ourBST = bst();
        double theirBST = evolvesFrom.bst();

        double bstRatio = ourBST / theirBST;

        hp = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.hp * bstRatio)));
        attack = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.attack * bstRatio)));
        defense = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.defense * bstRatio)));
        speed = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.speed * bstRatio)));
        spatk = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.spatk * bstRatio)));
        spdef = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.spdef * bstRatio)));
    }

    public void assignNewStatsForEvolution(Pokemon evolvesFrom, Random random) {

        double ourBST = bst();
        double theirBST = evolvesFrom.bst();

        double bstDiff = ourBST - theirBST;

        // Make weightings
        double hpW = random.nextDouble(), atkW = random.nextDouble(), defW = random.nextDouble();
        double spaW = random.nextDouble(), spdW = random.nextDouble(), speW = random.nextDouble();

        double totW = hpW + atkW + defW + spaW + spdW + speW;

        double hpDiff = Math.round((hpW / totW) * bstDiff);
        double atkDiff = Math.round((atkW / totW) * bstDiff);
        double defDiff = Math.round((defW / totW) * bstDiff);
        double spaDiff = Math.round((spaW / totW) * bstDiff);
        double spdDiff = Math.round((spdW / totW) * bstDiff);
        double speDiff = Math.round((speW / totW) * bstDiff);

        hp = (int) Math.min(255, Math.max(1, evolvesFrom.hp + hpDiff));
        attack = (int) Math.min(255, Math.max(1, evolvesFrom.attack + atkDiff));
        defense = (int) Math.min(255, Math.max(1, evolvesFrom.defense + defDiff));
        speed = (int) Math.min(255, Math.max(1, evolvesFrom.speed + speDiff));
        spatk = (int) Math.min(255, Math.max(1, evolvesFrom.spatk + spaDiff));
        spdef = (int) Math.min(255, Math.max(1, evolvesFrom.spdef + spdDiff));
    }

    public void randomizeBSTSetAmountAbovePreevo(Random random, Pokemon evolvesFrom, boolean dontRandomizeRatio) {
        int newBST = evolvesFrom.bstForPowerLevels() + 95;
        if (dontRandomizeRatio) {
            scaleStatsToNewBST(newBST);
        } else {
            randomizeStatsWithinNewBST(random, newBST);
        }
    }

    public void copyRandomizedBSTUpEvolution(Random random, Pokemon evolvesFrom, boolean fixedAmount) {
        int newBST = fixedAmount ? evolvesFrom.bstForPowerLevels() + 95 : pickNewBST(random);
        // quick and easy method to copy preevo's stat ratios with a new BST
        scaleStatsToNewBST(newBST);
        copyRandomizedStatsUpEvolution(evolvesFrom);
    }

    public void copyEqualizedStatsUpEvolution(Pokemon evolvesFrom) {
        hp = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.hp)));
        attack = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.attack)));
        defense = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.defense)));
        speed = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.speed)));
        spatk = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.spatk)));
        spdef = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.spdef)));

        special = (int) Math.ceil((spatk + spdef) / 2.0f);
    }

    public int bst() {
        return hp + attack + defense + spatk + spdef + speed;
    }

    public boolean isRunnable() {
        if(evolutionsFrom.size() > 0) return false;

        int scaled_attack = attack;

        // Normal attackers have a bigger effective attack stat
        if(primaryType.equals(Type.NORMAL) || (secondaryType != null && secondaryType.equals(Type.NORMAL)))
            scaled_attack = (int) (scaled_attack * 1.2);

        // Pure power, huge power
        if(number == 308 || number == 184)
            scaled_attack *= 2;

        // Truant
        if(number == 289) {
            return false;
        }

        int power = Math.max(scaled_attack, spatk);

        // Mixed attackers get a power boost because they have a better early game
        if(attack >= 80 && spatk >= 80) {
            power += 10;
        }

        if(power >= 95 && speed >= 100) return true;
        if(power >= 100 && speed >= 80) return true;
        if(power >= 105 && speed >= 70) return true;
        if(power >= 110 && speed >= 65) return true;
        if(power >= 120 && speed >= 60) return true;
        if(power >= 130 && speed >= 55) return true;

        return false;
    }

    public int bstForPowerLevels() {
        // Take into account Shedinja's purposefully nerfed HP
        if (number == Species.shedinja) {
            return (attack + defense + spatk + spdef + speed) * 6 / 5;
        } else {
            return hp + attack + defense + spatk + spdef + speed;
        }
    }

    public double getAttackSpecialAttackRatio() {
        return (double)attack / ((double)attack + (double)spatk);
    }

    public int getBaseNumber() {
        Pokemon base = this;
        while (base.baseForme != null) {
            base = base.baseForme;
        }
        return base.number;
    }

    public void copyBaseFormeBaseStats(Pokemon baseForme) {
        hp = baseForme.hp;
        attack = baseForme.attack;
        defense = baseForme.defense;
        speed = baseForme.speed;
        spatk = baseForme.spatk;
        spdef = baseForme.spdef;
    }

    public void copyBaseFormeAbilities(Pokemon baseForme) {
        ability1 = baseForme.ability1;
        ability2 = baseForme.ability2;
        ability3 = baseForme.ability3;
    }

    public void copyBaseFormeEvolutions(Pokemon baseForme) {
        evolutionsFrom = baseForme.evolutionsFrom;
    }

    public int getSpriteIndex() {
        return formeNumber == 0 ? number : formeSpriteIndex + formeNumber - 1;
    }

    public String fullName() {
        return name + formeSuffix;
    }

    @Override
    public String toString() {
        return "Pokemon [name=" + name + formeSuffix + ", number=" + number + ", primaryType=" + primaryType
                + ", secondaryType=" + secondaryType + ", hp=" + hp + ", attack=" + attack + ", defense=" + defense
                + ", spatk=" + spatk + ", spdef=" + spdef + ", speed=" + speed + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + number;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pokemon other = (Pokemon) obj;
        return number == other.number;
    }

    @Override
    public int compareTo(Pokemon o) {
        return number - o.number;
    }

    private static final List<Integer> legendaries = Arrays.asList(Species.articuno, Species.zapdos, Species.moltres,
            Species.mewtwo, Species.mew, Species.raikou, Species.entei, Species.suicune, Species.lugia, Species.hoOh,
            Species.celebi, Species.regirock, Species.regice, Species.registeel, Species.latias, Species.latios,
            Species.kyogre, Species.groudon, Species.rayquaza, Species.jirachi, Species.deoxys, Species.uxie,
            Species.mesprit, Species.azelf, Species.dialga, Species.palkia, Species.heatran, Species.regigigas,
            Species.giratina, Species.cresselia, Species.phione, Species.manaphy, Species.darkrai, Species.shaymin,
            Species.arceus, Species.victini, Species.cobalion, Species.terrakion, Species.virizion, Species.tornadus,
            Species.thundurus, Species.reshiram, Species.zekrom, Species.landorus, Species.kyurem, Species.keldeo,
            Species.meloetta, Species.genesect, Species.xerneas, Species.yveltal, Species.zygarde, Species.diancie,
            Species.hoopa, Species.volcanion, Species.typeNull, Species.silvally, Species.tapuKoko, Species.tapuLele,
            Species.tapuBulu, Species.tapuFini, Species.cosmog, Species.cosmoem, Species.solgaleo, Species.lunala,
            Species.necrozma, Species.magearna, Species.marshadow, Species.zeraora);

    private static final List<Integer> strongLegendaries = Arrays.asList(Species.mewtwo, Species.lugia, Species.hoOh,
            Species.kyogre, Species.groudon, Species.rayquaza, Species.dialga, Species.palkia, Species.regigigas,
            Species.giratina, Species.arceus, Species.reshiram, Species.zekrom, Species.kyurem, Species.xerneas,
            Species.yveltal, Species.cosmog, Species.cosmoem, Species.solgaleo, Species.lunala);

    private static final List<Integer> ultraBeasts = Arrays.asList(Species.nihilego, Species.buzzwole, Species.pheromosa,
            Species.xurkitree, Species.celesteela, Species.kartana, Species.guzzlord, Species.poipole, Species.naganadel,
            Species.stakataka, Species.blacephalon);

    public boolean isLegendary() {
        return formeNumber == 0 ? legendaries.contains(this.number) : legendaries.contains(this.baseForme.number);
    }

    public boolean isStrongLegendary() {
        return formeNumber == 0 ? strongLegendaries.contains(this.number) : strongLegendaries.contains(this.baseForme.number);
    }

    // This method can only be used in contexts where alt formes are NOT involved; otherwise, some alt formes
    // will be considered as Ultra Beasts in SM.
    // In contexts where formes are involved, use "if (ultraBeastList.contains(...))" instead,
    // assuming "checkPokemonRestrictions" has been used at some point beforehand.
    public boolean isUltraBeast() {
        return ultraBeasts.contains(this.number);
    }

    public int getCosmeticFormNumber(int num) {
        return realCosmeticFormNumbers.isEmpty() ? num : realCosmeticFormNumbers.get(num);
    }

    public int evosFromDepth() {
        if (evolutionsFrom.isEmpty()) {
            return 0;
        }
        int md = 0;
        for (Evolution ef : evolutionsFrom) {
            md = Math.max(md, ef.to.evosFromDepth());
        }
        return md + 1;
    }

    public int evosToDepth() {
        if (evolutionsTo.isEmpty()) {
            return 0;
        }
        int md = 0;
        for (Evolution ef : evolutionsTo) {
            md = Math.max(md, ef.from.evosToDepth());
        }
        return md + 1;
    }

}
