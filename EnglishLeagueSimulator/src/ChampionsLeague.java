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
 * Simulates the UEFA Champions League, from the league phase to the final.
 */
public class ChampionsLeague {
    private final List<Team> participants;
    private final EuropeanMatchSimulator matchSimulator;
    private final Random random = new Random();

    // Constant for number of league phase matches per team
    private static final int LEAGUE_PHASE_MATCHES = 8;

    public ChampionsLeague(List<Team> participants) {
        this.participants = participants;
        this.matchSimulator = new EuropeanMatchSimulator();
        // Reset stats for the new competition
        this.participants.forEach(Team::resetStats);
    }

    public void simulate() {
        System.out.println("\n\n\n--- UEFA CHAMPIONS LEAGUE SIMULATION STARTING ---");

        // --- 1. League Phase (Swiss Model - fixed matches per team) ---
        simulateLeaguePhase();

        // --- 2. Display League Phase Table and determine qualifiers ---
        // DEBUG: Print team stats BEFORE sorting
        System.out.println("\nDEBUG: Teams stats BEFORE sorting:");
        for (Team team : participants) {
            System.out.printf("DEBUG: %-26s | Pts: %-3d | GD: %-3d | GF: %-3d%n",
                    team.name, team.getPoints(), team.getGoalDifference(), team.getGoalsFor());
        }

        participants.sort(Comparator.comparingInt(Team::getPoints).reversed()
                .thenComparingInt(Team::getGoalDifference).reversed()
                .thenComparingInt(Team::getGoalsFor).reversed());

        // DEBUG: Print team stats AFTER sorting
        System.out.println("\nDEBUG: Teams stats AFTER sorting:");
        for (Team team : participants) {
            System.out.printf("DEBUG: %-26s | Pts: %-3d | GD: %-3d | GF: %-3d%n",
                    team.name, team.getPoints(), team.getGoalDifference(), team.getGoalsFor());
        }

        List<Team> top8 = new ArrayList<>();
        List<Team> playoffTeams = new ArrayList<>();

        // Determine top 8 and playoff teams based on current participants list
        if (participants.size() > 0) {
            top8.addAll(participants.subList(0, Math.min(8, participants.size())));
            if (participants.size() > 8) {
                // Teams from 9th position (index 8) up to 24th position (index 23), or end of
                // list if smaller
                playoffTeams.addAll(participants.subList(8, Math.min(24, participants.size())));
            }
        }

        System.out.println("\n--- CHAMPIONS LEAGUE - FINAL LEAGUE PHASE STANDINGS ---");
        System.out.println("\nTop " + top8.size() + " teams qualify directly for Round of 16.");
        if (!playoffTeams.isEmpty()) {
            System.out.println("Teams in positions " + (top8.size() + 1) + "-" + (top8.size() + playoffTeams.size())
                    + " proceed to the Knockout Play-offs.");
        } else if (participants.size() > 8) {
            System.out.println(
                    "No teams available for Knockout Play-offs from positions 9-24 as total participants are less than 9.");
        }

        displayLeagueTable(participants, top8, playoffTeams); // Pass the qualifier lists for labeling

        // Check if enough teams to run the full knockout phases as designed (8 direct +
        // 16 playoff = 24 total)
        // This prevents IndexOutOfBoundsException in subsequent subList operations for
        // knockout stages
        if (top8.size() < 8 || playoffTeams.size() < 16) {
            System.out.println(
                    "\nNot enough teams to run full Champions League knockout stages (need 8 direct + 16 playoff teams). Skipping knockouts.");
            return; // Exit simulation if not enough teams
        }

        // --- 3. Knockout Play-offs ---
        System.out.println("\n\n--- CHAMPIONS LEAGUE KNOCKOUT PLAY-OFFS ---");
        List<Team> playoffWinners = new ArrayList<>();
        // Use the actual playoffTeams (which should be 16 at this point due to the
        // check above)
        // Ensure random pairings
        Collections.shuffle(playoffTeams, random); // Shuffle the actual playoffTeams list
        for (int i = 0; i < 8; i++) {
            Team seeded = playoffTeams.get(i);
            Team unseeded = playoffTeams.get(i + 8);
            playoffWinners.add(matchSimulator.simulateTwoLeggedTie(seeded, unseeded));
        }

        // --- 4. Main Knockout Stage ---
        List<Team> roundOf16 = new ArrayList<>(top8); // Use the actual top8 list
        roundOf16.addAll(playoffWinners);
        Collections.shuffle(roundOf16); // Random draw

        System.out.println("\n\n--- CHAMPIONS LEAGUE KNOCKOUT STAGE ---");
        List<Team> quarterFinalists = simulateKnockoutRound(roundOf16, "Round of 16");
        List<Team> semiFinalists = simulateKnockoutRound(quarterFinalists, "Quarter-Finals");
        List<Team> finalists = simulateKnockoutRound(semiFinalists, "Semi-Finals");

        // --- 5. The Final ---
        if (finalists.size() == 2) {
            System.out.println("\n\n--- CHAMPIONS LEAGUE FINAL ---");
            Team winner = matchSimulator.simulateSingleMatch(finalists.get(0), finalists.get(1));
            System.out.println("\n\n<<<<< " + winner.name.toUpperCase() + " ARE THE CHAMPIONS OF EUROPE! >>>>>");
        } else {
            System.out.println("\nNot enough finalists for Champions League final.");
        }
    }

    private void simulateLeaguePhase() {
        System.out.println("\n--- Simulating League Phase (" + LEAGUE_PHASE_MATCHES + " matches per team) ---");
        List<Match> leaguePhaseFixtures = generateLeaguePhaseFixtures(this.participants, LEAGUE_PHASE_MATCHES);

        for (Match match : leaguePhaseFixtures) {
            matchSimulator.simulateMatch(match);
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
        Collections.shuffle(teamsNeedingMatches, random); // Randomize initial processing order

        // Max iterations to ensure all matches are attempted to be scheduled
        // It's a generous limit; actual iterations will be much fewer if scheduling is
        // successful.
        int safetyBreak = teams.size() * matchesPerTeam * (teams.size() > 1 ? teams.size() : 1) * 2;

        while (!teamsNeedingMatches.isEmpty() && safetyBreak > 0) {
            safetyBreak--; // Decrement safety break each iteration

            Team currentTeam = teamsNeedingMatches.remove(0); // Take a team from the front

            // If currentTeam already has enough matches, add it back to end if other teams
            // still need matches
            if (gamesScheduledCount.get(currentTeam) >= matchesPerTeam) {
                teamsNeedingMatches.add(currentTeam); // Put back, as it's done for now
                continue; // Move to the next iteration of the while loop
            }

            List<Team> potentialOpponents = new ArrayList<>(teams); // Pool of all teams
            Collections.shuffle(potentialOpponents, random); // Randomize search order for opponent

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
                } else { // Fallback to random assignment if no clear home/away need
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

            if (foundMatchThisIteration) {
                // If a match was found, and the currentTeam still needs more games, add it back
                // to the end
                if (gamesScheduledCount.get(currentTeam) < matchesPerTeam) {
                    teamsNeedingMatches.add(currentTeam);
                }
            } else {
                // If no match was found for currentTeam in this iteration, it means it's stuck
                // for now.
                // Add it back to the end to try again in a future iteration.
                teamsNeedingMatches.add(currentTeam);
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

        Collections.shuffle(fixtures, random); // Randomize order of generated fixtures
        return fixtures;
    }

    private List<Team> simulateKnockoutRound(List<Team> teams, String roundName) {
        System.out.println("\n\n--- " + roundName + " ---");
        List<Team> winners = new ArrayList<>();
        Collections.shuffle(teams, random); // Ensure random pairings

        // Handle odd number of teams: one team gets a bye
        if (teams.size() % 2 != 0 && teams.size() > 1) {
            Team byeTeam = teams.remove(teams.size() - 1); // Remove the last team
            winners.add(byeTeam); // Add it directly to winners for the next round
            System.out.println(byeTeam.getName() + " receives a bye to the next round.");
        }

        for (int i = 0; i < teams.size(); i += 2) {
            winners.add(matchSimulator.simulateTwoLeggedTie(teams.get(i), teams.get(i + 1)));
        }
        return winners;
    }

    private void displayLeagueTable(List<Team> tableTeams, List<Team> top8Qualifiers, List<Team> playoffQualifiers) {
        System.out.println("Pos | Team                     | P  | W  | D  | L  | GF | GA | GD  | Pts | Qualification");
        System.out.println("-----------------------------------------------------------------------------------------");
        int pos = 1;
        for (Team team : tableTeams) {
            String qualificationMarker = "";
            if (top8Qualifiers.contains(team)) {
                qualificationMarker = " [R16]"; // Directly qualifies for Round of 16
            } else if (playoffQualifiers.contains(team)) {
                qualificationMarker = " [PO]"; // Qualifies for Knockout Play-offs
            }

            System.out.printf("%-3d | %-26s | %-2d | %-2d | %-2d | %-2d | %-2d | %-2d | %-3d | %-3d%s%n",
                    pos++,
                    team.name,
                    team.gamesPlayed,
                    team.wins,
                    team.draws,
                    team.losses,
                    team.goalsFor,
                    team.goalsAgainst,
                    team.goalDifference,
                    team.points,
                    qualificationMarker);
        }
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("Legend: [R16] Qualifies directly for Round of 16, [PO] Qualifies for Knockout Play-offs");
    }
}