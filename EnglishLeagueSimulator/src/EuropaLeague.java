import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Simulates the UEFA Europa League.
 * This version uses a simplified league phase followed by knockout stages.
 */
public class EuropaLeague {
    private final List<Team> participants;
    private final EuropeanMatchSimulator matchSimulator;
    private final Random random = new Random();

    // Constant for number of league phase matches per team
    private static final int LEAGUE_PHASE_MATCHES = 6; // User specified 6 matches for EL

    public EuropaLeague(List<Team> participants) {
        this.participants = new ArrayList<>(participants); // Create a new list to avoid modifying the original
        this.matchSimulator = new EuropeanMatchSimulator();
        // Reset stats for the new competition
        this.participants.forEach(Team::resetStats);
    }

    public void simulate() {
        System.out.println("\n\n\n--- UEFA EUROPA LEAGUE SIMULATION STARTING ---");

        if (participants.isEmpty()) {
            System.out.println("No teams qualified for the Europa League simulation.");
            return;
        }

        // --- 1. Simplified League Phase (fixed matches per team) ---
        System.out.println("\n--- Simulating Europa League Phase (" + LEAGUE_PHASE_MATCHES + " matches per team) ---");
        List<Match> leaguePhaseFixtures = generateLeaguePhaseFixtures(this.participants, LEAGUE_PHASE_MATCHES);

        // DEBUG: Confirm fixtures generated
        System.out.println("DEBUG: Generated " + leaguePhaseFixtures.size() + " Europa League fixtures.");

        // DEBUG: Confirm entry to match simulation loop
        System.out.println("DEBUG: Simulating Europa League matches...");
        for (Match match : leaguePhaseFixtures) {
            matchSimulator.simulateMatch(match);
        }
        System.out.println("DEBUG: Europa League matches simulation complete.");

        // --- 2. Display League Phase Table and determine qualifiers ---
        participants.sort(Comparator.comparingInt(Team::getPoints).reversed()
                .thenComparingInt(Team::getGoalDifference).reversed()
                .thenComparingInt(Team::getGoalsFor).reversed());

        System.out.println("\n--- EUROPA LEAGUE - LEAGUE PHASE STANDINGS ---");
        System.out
                .println("DEBUG: Attempting to display Europa League table. Participants size: " + participants.size());
        displayLeagueTable(participants); // No labels for Europa League yet

        // Prepare for knockout stage based on new specific structure (16 teams total)
        List<Team> top4DirectQualifiers = new ArrayList<>();
        List<Team> next8PlayoffTeams = new ArrayList<>(); // Ranks 5-12

        // Check if enough teams for this specific structure (need at least 12 teams for
        // ranks 1-12)
        if (participants.size() < 12) {
            System.out.println(
                    "\nNot enough teams to run the specified Europa League knockout structure (need at least 12 participants for ranks 1-12). Skipping knockouts.");
            return;
        }

        // Extract teams based on their sorted league standings
        top4DirectQualifiers.addAll(participants.subList(0, 4)); // Ranks 1-4
        next8PlayoffTeams.addAll(participants.subList(4, 12)); // Ranks 5-12

        System.out.println("\n\n--- EUROPA LEAGUE KNOCKOUT PLAY-OFFS (Ranks 5-12) ---");
        List<Team> playoffWinners = new ArrayList<>();

        // Specific pairings for ranks 5-12 playoff based on their sorted order
        // 5th vs 12th, 6th vs 11th, 7th vs 10th, 8th vs 9th
        // next8PlayoffTeams indices: 0=5th, 1=6th, 2=7th, 3=8th, 4=9th, 5=10th, 6=11th,
        // 7=12th
        playoffWinners.add(matchSimulator.simulateTwoLeggedTie(next8PlayoffTeams.get(0), next8PlayoffTeams.get(7))); // 5th
                                                                                                                     // vs
                                                                                                                     // 12th
        playoffWinners.add(matchSimulator.simulateTwoLeggedTie(next8PlayoffTeams.get(1), next8PlayoffTeams.get(6))); // 6th
                                                                                                                     // vs
                                                                                                                     // 11th
        playoffWinners.add(matchSimulator.simulateTwoLeggedTie(next8PlayoffTeams.get(2), next8PlayoffTeams.get(5))); // 7th
                                                                                                                     // vs
                                                                                                                     // 10th
        playoffWinners.add(matchSimulator.simulateTwoLeggedTie(next8PlayoffTeams.get(3), next8PlayoffTeams.get(4))); // 8th
                                                                                                                     // vs
                                                                                                                     // 9th

        // Check if we got 4 winners from the playoff
        if (playoffWinners.size() != 4) {
            System.out.println("Error: Expected 4 playoff winners from ranks 5-12, but got " + playoffWinners.size()
                    + ". Skipping further knockouts.");
            return;
        }

        List<Team> quarterFinalists = new ArrayList<>();
        quarterFinalists.addAll(top4DirectQualifiers); // Add the 4 direct qualifiers
        quarterFinalists.addAll(playoffWinners); // Add the 4 playoff winners

        // Final check: Ensure we have exactly 8 quarter-finalists before proceeding
        if (quarterFinalists.size() != 8) {
            System.out.println("Error: Unexpected number of quarter-finalists (" + quarterFinalists.size()
                    + "). Expected 8. Skipping further knockouts.");
            return;
        }

        Collections.shuffle(quarterFinalists, random); // Random draw for Quarter-Finals

        System.out.println("\n\n--- EUROPA LEAGUE KNOCKOUT STAGE ---");

        // Dynamically determine starting knockout round (will be Quarter-Finals if 8
        // teams)
        List<Team> currentRoundTeams = new ArrayList<>(quarterFinalists); // Start directly with the 8 Quarter-Finalists

        String roundName; // To be used in the print statement within the loop.

        while (currentRoundTeams.size() > 1) {
            if (currentRoundTeams.size() == 2) { // Final
                System.out.println("\n--- Final ---");
                Team winner = matchSimulator.simulateSingleMatch(currentRoundTeams.get(0), currentRoundTeams.get(1));
                currentRoundTeams.clear();
                currentRoundTeams.add(winner);
            } else if (currentRoundTeams.size() == 4) { // Semi-Finals
                roundName = "Semi-Finals";
                System.out.println("\n--- " + roundName + " ---");
                currentRoundTeams = simulateKnockoutRound(currentRoundTeams);
            } else if (currentRoundTeams.size() == 8) { // Quarter-Finals
                roundName = "Quarter-Finals";
                System.out.println("\n--- " + roundName + " ---");
                currentRoundTeams = simulateKnockoutRound(currentRoundTeams);
            } else { // Fallback for unexpected sizes (e.g., due to byes if fewer than 8, or previous
                     // errors)
                roundName = "Knockout Round (" + currentRoundTeams.size() + " teams)";
                System.out.println("\n--- " + roundName + " ---");
                currentRoundTeams = simulateKnockoutRound(currentRoundTeams);
            }
        }

        // --- 3. The Final ---
        if (currentRoundTeams.size() == 1) {
            System.out.println("\n\n<<<<< " + currentRoundTeams.get(0).name.toUpperCase()
                    + " ARE THE EUROPA LEAGUE CHAMPIONS! >>>>>");
        }
    }

    private List<Match> generateLeaguePhaseFixtures(List<Team> teams, int matchesPerTeam) {
        List<Match> fixtures = new ArrayList<>();
        Map<Team, Integer> gamesScheduledCount = new HashMap<>();
        Map<Team, Set<Team>> opponentsPlayed = new HashMap<>();
        Map<Team, Integer> homeGamesCount = new HashMap<>();
        Map<Team, Integer> awayGamesCount = new HashMap<>();

        for (Team team : teams) {
            gamesScheduledCount.put(team, 0);
            opponentsPlayed.put(team, new HashSet<>());
            homeGamesCount.put(team, 0);
            awayGamesCount.put(team, 0);
        }

        List<Team> teamsNeedingMatches = new ArrayList<>(teams); // Working copy of teams that still need matches
        Collections.shuffle(teamsNeedingMatches, random); // Initial shuffle

        int safetyBreak = teams.size() * matchesPerTeam * (teams.size() > 1 ? teams.size() : 1) * 5; // Large safety
                                                                                                     // limit

        int loopCounter = 0; // To track iterations for periodic reshuffle

        while (true) {
            // Sort teams by how many games they still need (ascending)
            // This prioritizes teams that are "stuck" or have the fewest matches scheduled.
            teamsNeedingMatches.sort(Comparator.comparingInt(gamesScheduledCount::get));

            Team currentTeam = null;
            for (Team t : teamsNeedingMatches) {
                if (gamesScheduledCount.get(t) < matchesPerTeam) {
                    currentTeam = t;
                    break;
                }
            }

            if (currentTeam == null) { // All teams have their matches
                break;
            }

            safetyBreak--;
            if (safetyBreak <= 0) {
                System.out.println(
                        "Warning: Maximum scheduling attempts reached. Some teams might not have all their matches due to complex constraints.");
                break; // Exit loop if stuck
            }

            List<Team> potentialOpponents = new ArrayList<>(teams); // Pool of all teams
            Collections.shuffle(potentialOpponents, random); // Randomize search for opponent

            boolean foundMatchThisIteration = false;
            for (Team opponent : potentialOpponents) {
                // Skip if: same team, already played, or opponent already has enough matches
                if (currentTeam.equals(opponent) || opponentsPlayed.get(currentTeam).contains(opponent) ||
                        gamesScheduledCount.get(opponent) >= matchesPerTeam) {
                    continue;
                }

                // Found a valid pair: currentTeam vs opponent
                Team homeTeam, awayTeam;
                int desiredHomeGames = matchesPerTeam / 2;
                int desiredAwayGames = matchesPerTeam - desiredHomeGames;

                // Prioritize balancing home/away games for both teams
                boolean currentTeamNeedsHome = homeGamesCount.get(currentTeam) < desiredHomeGames;
                boolean currentTeamNeedsAway = awayGamesCount.get(currentTeam) < desiredAwayGames;
                boolean opponentNeedsHome = homeGamesCount.get(opponent) < desiredHomeGames;
                boolean opponentNeedsAway = awayGamesCount.get(opponent) < desiredAwayGames;

                if (currentTeamNeedsHome && opponentNeedsAway) {
                    homeTeam = currentTeam;
                    awayTeam = opponent;
                } else if (opponentNeedsHome && currentTeamNeedsAway) {
                    homeTeam = opponent;
                    awayTeam = currentTeam;
                } else if (currentTeamNeedsHome) { // Prioritize currentTeam's home need
                    homeTeam = currentTeam;
                    awayTeam = opponent;
                } else if (opponentNeedsHome) { // Prioritize opponent's home need
                    homeTeam = opponent;
                    awayTeam = currentTeam;
                } else if (currentTeamNeedsAway) { // Prioritize currentTeam's away need
                    homeTeam = opponent;
                    awayTeam = currentTeam;
                } else if (opponentNeedsAway) { // Prioritize opponent's away need
                    homeTeam = currentTeam;
                    awayTeam = opponent;
                } else { // Fallback random if no clear home/away need
                    if (random.nextBoolean()) {
                        homeTeam = currentTeam;
                        awayTeam = opponent;
                    } else {
                        homeTeam = opponent;
                        awayTeam = currentTeam;
                    }
                }

                fixtures.add(new Match(homeTeam, awayTeam));
                opponentsPlayed.get(currentTeam).add(opponent);
                opponentsPlayed.get(opponent).add(currentTeam);
                gamesScheduledCount.put(currentTeam, gamesScheduledCount.get(currentTeam) + 1);
                gamesScheduledCount.put(opponent, gamesScheduledCount.get(opponent) + 1);
                homeGamesCount.put(homeTeam, homeGamesCount.get(homeTeam) + 1);
                awayGamesCount.put(awayTeam, awayGamesCount.get(awayTeam) + 1);
                foundMatchThisIteration = true;
                break; // Found a match, break from searching for opponent for currentTeam
            }

            if (!foundMatchThisIteration) {
                // If currentTeam couldn't find ANY opponent in this pass, it means it's stuck
                // for now.
                // It will be re-prioritized in the next iteration.
            }
        }

        // Final check for unscheduled matches and debug prints
        System.out.println("\nDEBUG: Final scheduled games per team (from generateLeaguePhaseFixtures):");
        for (Team team : teams) {
            System.out.printf("DEBUG: %-26s | Scheduled: %-2d | Home: %-2d | Away: %-2d%n",
                    team.getName(), gamesScheduledCount.get(team), homeGamesCount.get(team), awayGamesCount.get(team));
            if (gamesScheduledCount.get(team) < matchesPerTeam) {
                System.out.println("Warning: " + team.getName() + " only scheduled " + gamesScheduledCount.get(team)
                        + " of " + matchesPerTeam + " matches.");
            }
        }

        Collections.shuffle(fixtures, random);
        return fixtures;
    }

    private List<Team> simulateKnockoutRound(List<Team> teamsInRound) {
        List<Team> winners = new ArrayList<>();
        Collections.shuffle(teamsInRound, random); // Ensure random pairings

        // Handle odd number of teams: one team gets a bye
        if (teamsInRound.size() % 2 != 0 && teamsInRound.size() > 1) {
            Team byeTeam = teamsInRound.remove(teamsInRound.size() - 1); // Remove the last team
            winners.add(byeTeam); // Add it directly to winners for the next round
            System.out.println(byeTeam.getName() + " receives a bye to the next round.");
        }

        for (int i = 0; i < teamsInRound.size(); i += 2) {
            // Use two-legged tie simulation for knockout rounds
            winners.add(matchSimulator.simulateTwoLeggedTie(teamsInRound.get(i), teamsInRound.get(i + 1)));
        }
        return winners;
    }

    private void displayLeagueTable(List<Team> tableTeams) {
        System.out.println("DEBUG: displayLeagueTable called with " + tableTeams.size() + " teams.");
        System.out.println("Pos | Team                     | P  | W  | D  | L  | GF | GA | GD  | Pts");
        System.out.println("-------------------------------------------------------------------------");
        int pos = 1;
        for (Team team : tableTeams) {
            System.out.printf("%-3d | %-26s | %-2d | %-2d | %-2d | %-2d | %-2d | %-2d | %-3d | %-3d%n",
                    pos++,
                    team.name,
                    team.gamesPlayed,
                    team.wins,
                    team.draws,
                    team.losses,
                    team.goalsFor,
                    team.goalsAgainst,
                    team.goalDifference,
                    team.points);
        }
        System.out.println("-------------------------------------------------------------------------");
    }
}