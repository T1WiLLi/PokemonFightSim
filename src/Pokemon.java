import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Pokemon {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        File pokemonFile = new File("src/Pokemon.txt");

        final double MISS_STRIKE = 0.20d; // %20
        final double CRIT_STRIKE = 0.05d; // %5
        final double ESCAPE = 0.30d; // 30%

        String[][] userPokemon = null; // Pokemon

        String input;

        int gameStrike = 0; // Amount of games won
        int tempPokemonHp;
        int tempPokemonPP;

        boolean hasChoosePokemon = false;
        boolean isPlaying = true;

        playAudio("src/pokemon_song.wav");
        printCredit();
        anythingsToContinue(scanner);

        do {
            gameStrike = 0;
            hasChoosePokemon = false;
            while (!hasChoosePokemon) {
                userPokemon = askUserForPokemon(scanner, pokemonFile);
                clearConsole();
                System.out.println("\n"+userPokemon[0][0] + " has been choosed!");
                printPokemonInfo(userPokemon);

                printSeparator(100);
                System.out.print("\nDo you want to change Pokemon (YES/NO): ");
                input = scanner.nextLine();
                switch (input.toUpperCase()) {
                    case "YES", "Y" -> System.out.println("Let's change it!");
                    case "NO", "N" -> {
                        hasChoosePokemon = true;
                    }
                    default -> {
                        System.out.println("I think you tried to enter something that hasn't been recognized, please try again!");
                        anythingsToContinue(scanner);
                    }
                }
            }
            clearConsole();

            tempPokemonHp = Integer.parseInt(userPokemon[0][1]);
            tempPokemonPP = Integer.parseInt(userPokemon[0][2]);

            while (Integer.parseInt(userPokemon[0][1]) > 0) {
                if (gameStrike % 5 == 0 && gameStrike != 0) {
                    clearConsole();
                    System.out.println("Let's rest a bit!");
                    System.out.println("Your pokemon stats have been restored!");
                    userPokemon[0][1] = String.valueOf(tempPokemonHp);
                    userPokemon[0][2] = String.valueOf(tempPokemonPP);
                    printPokemonInfo(userPokemon);
                    anythingsToContinue(scanner);
                }
                userPokemon = fightLoop(scanner, userPokemon, getPokemonData(pokemonFile, (int)(Math.random() * getAmountOfPokemon(pokemonFile))), MISS_STRIKE, CRIT_STRIKE, ESCAPE);
                clearConsole();
                if (Integer.parseInt(userPokemon[0][1]) > 0) {
                    gameStrike++;
                    System.out.println("Your current win-strike is " + gameStrike);
                    System.out.print("\nDo you want to fight again ? (YES/NO): ");
                    input = scanner.nextLine();
                    if (input.equalsIgnoreCase("no")) {
                        System.out.println("Goodbye!");
                        System.exit(0);
                    }
                }
            }
            System.out.println("You ended up with " + gameStrike + " win");
            System.out.print("Do you want to try again? (YES/NO): ");
            input = scanner.nextLine();
            if (input.equalsIgnoreCase("yes")) {
                continue;
            } else {
                System.out.println("Goodbye!");
                System.exit(0);
            }
        } while (isPlaying);
        scanner.close();
    }
    // ALL FUNCTIONAL GAME FUNCTIONS


    /**
     * Main game loop for the battle between user's Pokemon and enemy Pokemon.
     *
     * @param scanner        The scanner for user input.
     * @param userPokemon    The user's Pokemon.
     * @param enemyPokemon   The enemy Pokemon.
     * @param missStrike    The chance of a miss in percentage.
     * @param critStrike    The chance of a critical strike in percentage.
     * @param escape         The chance of escape in percentage.
     * @return The updated user's Pokemon data.
     */
    static String[][] fightLoop(Scanner scanner, String[][] userPokemon, String[][] enemyPokemon, double missStrike, double critStrike, double escape) {

        int currentAttackPP;
        int currentAttackDmg;

        int userChoice = 0;
        int enemyChoice = 0;

        boolean flee = false;

        while (isBattleOver(userPokemon, enemyPokemon) && !flee) {
            clearConsole();
            printHeading(userPokemon[0][0] + " VS " + enemyPokemon[0][0]);

            System.out.println("Your Pokemon:");
            printPokemonInfo(userPokemon);
            System.out.println("\nEnemy Pokemon:");
            printPokemonInfo(enemyPokemon);

            printSeparator(40);
            System.out.println("\nYour turn to attack!");

            displayAvailableActions(userPokemon);

            userChoice = getUserChoice(scanner);

            switch (userChoice) {
                case 1,2,3,4 -> {
                    currentAttackPP = Integer.parseInt(userPokemon[userChoice][1]);
                    currentAttackDmg = Integer.parseInt(userPokemon[userChoice][2]);

                    if (Integer.parseInt(userPokemon[0][2]) < currentAttackPP) {
                        System.out.println("You don't have enough PP!");
                        scanner.nextLine();
                        anythingsToContinue(scanner);
                        continue;
                    }
                    enemyPokemon[0][1] = String.valueOf(attack(currentAttackDmg, Integer.parseInt(enemyPokemon[0][1]), missStrike, critStrike));
                    userPokemon[0][2] = String.valueOf(Integer.parseInt(userPokemon[0][2]) - currentAttackPP);
                }
                case 5 -> {
                    if (flee(escape)) {
                        System.out.println("You've successfully escaped the fight!");
                        flee = true;
                        
                    } else {
                        System.out.println("You can't escape " + enemyPokemon[0][0] + " !");
                    }
                }
                case 6 -> System.out.println("You do nothings...");
                default -> { 
                    System.out.println("Invalid choice. Try again.");
                    continue;
                }   
            }

            if (flee) {
                break;
            }

            if (Integer.parseInt(enemyPokemon[0][1]) <= 0) {
                System.out.println("You defeated the enemy!");
                continue;
            }

            //Logic for the enemy here... 
            enemyChoice = (int)(Math.random()*6) + 1;

            switch (enemyChoice) {
                case 1,2,3,4 -> {
                    currentAttackPP = Integer.parseInt(enemyPokemon[enemyChoice][1]);
                    currentAttackDmg = Integer.parseInt(enemyPokemon[enemyChoice][2]);

                    if (Integer.parseInt(enemyPokemon[0][2]) < currentAttackPP) {
                        currentAttackPP = Integer.parseInt(enemyPokemon[4][1]);
                        currentAttackDmg = Integer.parseInt(enemyPokemon[4][2]);
                    }
                    System.out.println("\n\nThe enemy uses " + enemyPokemon[enemyChoice][0]);

                    userPokemon[0][1] = String.valueOf(attack(currentAttackDmg, Integer.parseInt(userPokemon[0][1]), missStrike, critStrike));
                    enemyPokemon[0][2] = String.valueOf(Integer.parseInt(enemyPokemon[0][2]) - currentAttackPP);
                }
                case 5 -> {
                    if (flee(escape)) {
                        System.out.println("The " + enemyPokemon[0][0] + " escaped!");
                        flee = true;
                    } else {
                        System.out.println("the " + enemyPokemon[0][0] + " of your enemy tried to escape your " + userPokemon[0][0] + " !");
                    }
                }
                default -> {
                    System.out.println("The enemy choosed to do nothings!");
                }
            }

            if (Integer.parseInt(userPokemon[0][1]) <= 0) {
                System.out.println("Your Pokemon fainted!");
                continue;
            }

            scanner.nextLine();
            anythingsToContinue(scanner);
        }
        scanner.nextLine();
        anythingsToContinue(scanner);
        return userPokemon;
    }

    /**
     * Get status of battle (return or false)
     * 
     * @param userPokemon
     * @param enemyPokemon
     * @return boolean - true or false
     */
    static boolean isBattleOver(String[][] userPokemon, String[][] enemyPokemon) {
        return Integer.parseInt(userPokemon[0][1]) > 0 && Integer.parseInt(enemyPokemon[0][1]) > 0;
    }

    /**
     * Get the user input
     * 
     * @param scanner
     * @return
     */
    static int getUserChoice(Scanner scanner) {
        int userChoice;
        do {
            System.out.print("Please provide the action you want to perform: ");
            while (!scanner.hasNextInt()) {
                System.out.println("You have to provide a numerical number!");
                scanner.next();
            }
            userChoice = scanner.nextInt();
        } while (userChoice < 0 || userChoice > 6);
        return userChoice;
    }

    /**
     * Display the menu options for attacks and other actions.
     *
     * @param pokemon a 2D array containing information about attacks
     */
    static void displayAvailableActions(String[][] userPokemon) {
        System.out.println("Available Actions:");
        System.out.println("1-4. Attack");
        System.out.println("5. Flee");
        System.out.println("6. Do Nothing");
    }


    /**
     * Simulate an attack on a target with a chance to miss or critical strike.
     *
     * @param attack     the base damage of the attack
     * @param health     the current health of the target
     * @param missStrike the probability of the attack missing (0.0 to 1.0)
     * @param critStrike the probability of a critical strike (0.0 to 1.0)
     * @return the updated health of the target after the attack
     */
    static int attack(int attack, int health, double missStrike, double critStrike) {
        if (isMiss(missStrike)) {
            System.out.println("\nAttack missed!");
            return health;
        }

        int damage = calculateDamage(attack, critStrike);
        if (damage / 2 == attack) {
            System.out.println("\nCritical Strike! Damage: " + damage);
        } else {
            System.out.println("\nNormal Attack! Damage: " + damage);
        }
        return health - damage;
    }

    /**
     * Determine if the attack should miss based on the miss probability.
     *
     * @param missStrike the probability of the attack missing (0.0 to 1.0)
     * @return true if the attack should miss, false otherwise
     */
    static boolean isMiss(double missStrike) {
        return new Random().nextDouble() <= missStrike;
    }

    /**
     * Determine if the attack should be a critical strike based on the critical strike probability.
     *
     * @param critStrike the probability of a critical strike (0.0 to 1.0)
     * @return true if the attack should be a critical strike, false otherwise
     */
    static boolean isCrit(double critStrike) {
        return new Random().nextDouble() <= critStrike;
    }

    /**
     * Calculate the damage inflicted by an attack, considering the critical strike chance.
     *
     * @param attack     the base damage of the attack
     * @param critStrike the probability of a critical strike (0.0 to 1.0)
     * @return the calculated damage
     */
    static int calculateDamage(int attack, double critStrike) {
        return isCrit(critStrike) ? (attack * 2) : attack;
    }

    /**
     * Determine whether you can escape or nah
     *
     * @param escape the probability of successfully escaping (0.0 to 1.0)
     * @return true if the character can escape, false otherwise
     */
    static boolean flee(double escape) {
        return (new Random().nextDouble() <= escape);
    }

    /**
     * Wait for the user to press Enter to continue.
     *
     * @param scanner The scanner for user input.
     */
    static void anythingsToContinue(Scanner scanner) {
        System.out.println("\nEnter anything to continue...");
        scanner.nextLine();
    }

    /**
     * Ask the user to choose a Pokemon.
     *
     * @param scanner The scanner for user input.
     * @param file    The file containing Pokemon data.
     * @return The chosen Pokemon's data.
     */
    static String[][] askUserForPokemon(Scanner scanner, File file) {
            clearConsole();
            printPokemonName(pokemonNameReader(file));
            int userIndex = 0;
            boolean isIndexOkay = false;

            while (!isIndexOkay) {
                try {
                    System.out.print("\nPlease provide the index of the Pokemon you wish to have (press '0' to randomize!): ");
                    userIndex = scanner.nextInt();
                    if (userIndex == 0) {
                        userIndex = (int)(Math.random() * getAmountOfPokemon(file)) + 1;
                        isIndexOkay = true;
                    } else if (userIndex > 0 && userIndex < getAmountOfPokemon(file)) {
                        isIndexOkay = true;
                    } else {
                        System.out.println("Please enter a number between 1 and 150 inclusive!");
                    }
                } catch (InputMismatchException e) {
                    scanner.next();
                    System.out.println("Please provide a numerical value");
                }
            }
            scanner.nextLine();
            return getPokemonData(file, userIndex);
        }

    // ALL FILE HANDLING FUNCTIONS


    /**
     * Provides greater scalability throughout the program by eliminating hard-coded values as for the pokemon amount.
     * 
     * @param file The file containing Pokemon names
     * @return The number of lines before encountering the line containing "-name," or -1 if not found.
     */

    static long getAmountOfPokemon(File file) {
        long lines = 0;
        try (LineNumberReader lnr = new LineNumberReader(new FileReader(file))) {
            while (!lnr.readLine().contentEquals("-name")) {
                lines = lnr.getLineNumber();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return lines - 1;
    }


    /**
     * Read the names of Pokemon from a file.
     *
     * @param file The file containing Pokemon names.
     * @return A list of Pokemon names.
     */
    static List<String> pokemonNameReader(File file) {
            List<String> names = new ArrayList<>();

            try (Scanner scanner = new Scanner(file)) {
                boolean readingNames = false;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.equals("-name")) {
                        readingNames = true;
                        continue;
                    }
                    if (readingNames && !line.isEmpty()) {
                        names.add(line);
                    }
                }
                Collections.sort(names); // Sort the names
            } catch (Exception e) {
                e.printStackTrace();
            }
            return names;
        }

    /**
     * Print a list of Pokemon names in a formatted way.
     *
     * @param args The list of Pokemon names.
     */
    static void printPokemonName(List<String> args) { //Print a list of pokemon names in a formated way
        String lineSep = "\u001B[34m+\u001B[33m---------------------\u001B[34m+\u001B[33m---------------------\u001B[34m+\u001B[33m---------------------\u001B[34m+\u001B[33m---------------------\u001B[34m+\u001B[33m---------------------\u001B[34m+\u001B[33m---------------------\u001B[34m+\u001B[33m---------------------\u001B[34m+\u001B[33m---------------------\u001B[34m+\u001B[33m---------------------\u001B[34m+\u001B[33m----------------------\u001B[34m+\u001B[0m";
        for (int i = 0; i < args.size(); i++) {
            if (i%10==0 && i != 0) {
                System.out.print(" \u001B[33m|\u001B[0m");
            }
            
            if(i%10==0) {
                System.out.println("");
                System.out.println(lineSep);
            }
            System.out.printf("\u001B[33m|\u001B[0m %-24s", (i + 1) + " - \u001B[1m" + args.get(i));
        }
        System.out.print(" \u001B[33m|\u001B[0m");
        System.out.println("");
        System.out.println(lineSep);
    }

    /**
     * Get the data for a specific Pokemon by index.
     *
     * @param file  The file containing Pokemon data.
     * @param index The index of the Pokemon.
     * @return The data of the Pokemon.
     */
    static String[][] getPokemonData(File file, int index) { //Return the pokemon choosed by the user
        try (Scanner scanner = new Scanner(file)) {
            List<String> pokemons = new ArrayList<>();
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.equals("-name")) {
                    break;
                }
                if (!line.isEmpty()) {
                    pokemons.add(line);
                }
            }
            if (index >= 1 && index <= pokemons.size()) {
                Collections.sort(pokemons);
                String[] parts = pokemons.get(index - 1).split("->");
                if (parts.length == 5) {
                    String[][] pokemonData = {
                            parts[0].split(":"),
                            parts[1].split(":"),
                            parts[2].split(":"),
                            parts[3].split(":"),
                            parts[4].split(":")
                    };
                    return pokemonData;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ALL UI FUNCTIONS


    /**
     * Clear the console screen.
     */
    static void clearConsole() {
        for(int i = 0; i < 100; i++) {
            System.out.println();
        }
    }

    /**
     * Clear x part of the console.
     * 
     * @param i the specified amount of line to be cleared.
     */
    static void clearPartConsole(int i) {
        for (int j = 0; j < i; j++) {
            System.out.println();
        }
    }

    /**
     * Print a separator line.
     *
     * @param length The length of the separator.
     */
    static void printSeparator(int length) {
        StringBuilder separator = new StringBuilder();
        for (int i = 0; i < length; i++) {
            separator.append("-");
        }
        System.out.println(separator.toString());
    }

    /**
     * Print the heading of the game.
     *
     * @param text The heading text.
     */
    static void printHeading(String text) {
        printSeparator(50);
        System.out.println(text);
        printSeparator(50);
    }

    /**
     * Print any message as if it was typed
     * 
     * @param inMessage
     */
    static void printMessageDelay(String inMessage) {
        char[] message = inMessage.toCharArray();
        for (char c : message) {
            System.out.print(c);
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Print an ASCII title art
     */
    static void printAnimatedASCII() {
        String[] lines = {
            "  _____   ____  _  ________ __  __  ____  _   _               _____  ______ _____                  _   _ _____            _____ _____  ______ ______ _   _ ",
            " |  __ \\ / __ \\| |/ /  ____|  \\/  |/ __ \\| \\ | |   _         |  __ \\|  ____|  __ \\           /\\   | \\ | |  __ \\          / ____|  __ \\|  ____|  ____| \\ | |",
            " | |__) | |  | | ' /| |__  | \\  / | |  | |  \\| |  (_)        | |__) | |__  | |  | |         /  \\  |  \\| | |  | |        | |  __| |__) | |__  | |__  |  \\| |",
            " |  ___/| |  | |  < |  __| | |\\/| | |  | | . ` |             |  _  /|  __| | |  | |        / /\\ \\ | . ` | |  | |        | | |_ |  _  /|  __| |  __| | . ` |",
            " | |    | |__| | . \\| |____| |  | | |__| | |\\  |   _         | | \\ \\| |____| |__| |       / ____ \\| |\\  | |__| |        | |__| | | \\ \\| |____| |____| |\\  |",
            " |_|     \\____/|_|\\_\\______|_|  |_|\\____/|_| \\_|  (_)        |_|  \\_\\______|_____/       /_/    \\_\\_| \\_|_____/          \\_____|_|  \\_\\______|______|_| \\_|"
        };

        for (String line : lines) {
            for (int i = 0; i < line.length(); i++) {
                System.out.print(line.charAt(i));
                try {
                    Thread.sleep(1); //Speed of line
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println();
        }
    }

    /**
     * Print the credit for the game.
     */
    static void printCredit() {
        clearConsole();
        printAnimatedASCII();
        System.out.println("");
        printHeading("TEXT VERSION - A Pokemon Adventure.");
        printMessageDelay("By William Beaudin.");
        printMessageDelay("\nLast version as of 2023-10-04.");
    }

    /**
     * Print the information of a Pokemon.
     *
     * @param pokemonData The Pokemon data.
     */
    static void printPokemonInfo(String[][] array) {
        if (array == null) {
            System.out.println("Invalid Pokémon data format.");
            return;
        }

        System.out.println("\nPokemon name: \u001B[1m" + array[0][0] + "\u001B[0m");
        System.out.println("Pokemon Health: \u001B[31m\u001B[1m\u001B[6m" + array[0][1] + "\u001B[0m");
        System.out.println("Pokemon PP: \u001B[36m\u001B[1m\u001B[6m" + array[0][2] + "\u001B[0m");
        System.out.println("\nAbilities:");

        System.out.println("\u001B[34m+\u001B[33m----\u001B[34m+\u001B[33m--------------------\u001B[34m+\u001B[33m-------\u001B[34m+\u001B[33m--------\u001B[34m+\u001B[33m");
        System.out.printf("\u001B[33m|\u001B[0m %-2s \u001B[33m|\u001B[0m %-18s \u001B[33m|\u001B[0m %-5s \u001B[33m|\u001B[0m %-6s \u001B[33m|\u001B[0m\n", "#", "Name", "Cost", "Damage");
        System.out.println("\u001B[34m+\u001B[33m----\u001B[34m+\u001B[33m--------------------\u001B[34m+\u001B[33m-------\u001B[34m+\u001B[33m--------\u001B[34m+\u001B[33m");

        for (int i = 1; i < array.length; i++) {
            System.out.printf("\u001B[33m|\u001B[0m %-2d \u001B[33m|\u001B[0m %-18s \u001B[33m|\u001B[0m %-5s \u001B[33m|\u001B[0m %-6s \u001B[33m|\u001B[0m\n", i, array[i][0], array[i][1], array[i][2]);
            System.out.println("\u001B[34m+\u001B[33m----\u001B[34m+\u001B[33m--------------------\u001B[34m+\u001B[33m-------\u001B[34m+\u001B[33m--------\u001B[34m+\u001B[33m");
        }
        System.out.println("\u001B[0m");
    }

    //Audio related-function


    /**
     * Play audio from a file.
     *
     * @param filePath The path to the audio file.
     */
    static void playAudio(String filePath) {
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile())) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch (Exception e) {
            System.out.println("Error playing audio: " + e.getMessage());
        }
    }
}