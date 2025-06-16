import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Simulates the UEFA Champions League, from the league phase to the final.
 */
public class ChampionsLeague {
    private final List<Team> participants;
    private final EuropeanMatchSimulator matchSimulator;

    public ChampionsLeague(List<Team> participants) {
        this.participants = participants;
        this.matchSimulator = new EuropeanMatchSimulator();
        // Reset stats for the new competition
        this.participants.forEach(Team::resetStats);
    }

    public void simulate() {
        System.out.println("\n\n\n--- UEFA CHAMPIONS LEAGUE SIMULATION STARTING ---");

        // --- 1. League Phase (Swiss Model) ---
        simulateLeaguePhase();

        // --- 2. Display League Phase Table and determine qualifiers ---
        participants.sort(Comparator.comparingInt(Team::getPoints).reversed()
                .thenComparingInt(Team::getGoalDifference).reversed()
                .thenComparingInt(Team::getGoalsFor).reversed());

        System.out.println("\n--- CHAMPIONS LEAGUE - FINAL LEAGUE PHASE STANDINGS ---");
        displayLeagueTable(participants);

        List<Team> top8 = new ArrayList<>(participants.subList(0, 8));
        List<Team> playoffTeams = new ArrayList<>(participants.subList(8, 24));
        System.out.println("\nTop 8 teams qualify directly for Round of 16.");
        System.out.println("Teams 9-24 proceed to the Knockout Play-offs.");


        // --- 3. Knockout Play-offs ---
        System.out.println("\n\n--- CHAMPIONS LEAGUE KNOCKOUT PLAY-OFFS ---");
        List<Team> playoffWinners = new ArrayList<>();
        // Seeded (9-16) vs Unseeded (17-24)
        for (int i = 0; i < 8; i++) {
            Team seeded = playoffTeams.get(i);
            Team unseeded = playoffTeams.get(i + 8);
            playoffWinners.add(matchSimulator.simulateTwoLeggedTie(seeded, unseeded));
        }

        // --- 4. Main Knockout Stage ---
        List<Team> roundOf16 = new ArrayList<>(top8);
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
        }
    }

    private void simulateLeaguePhase() {
        System.out.println("\n--- Simulating League Phase (8 matches per team) ---");
        // Simplified: Each team plays 8 random opponents (4 home, 4 away)
        for(Team team : participants) {
            List<Team> opponents = new ArrayList<>(participants);
            opponents.remove(team);
            Collections.shuffle(opponents);

            for(int i = 0; i < 4; i++) { // 4 home games
                matchSimulator.simulateSingleMatch(team, opponents.get(i), true);
            }
            for(int i = 4; i < 8; i++) { // 4 away games
                matchSimulator.simulateSingleMatch(opponents.get(i), team, true);
            }
        }
    }

    private List<Team> simulateKnockoutRound(List<Team> teams, String roundName) {
        System.out.println("\n\n--- " + roundName + " ---");
        List<Team> winners = new ArrayList<>();
        Collections.shuffle(teams);
        for (int i = 0; i < teams.size(); i += 2) {
            winners.add(matchSimulator.simulateTwoLeggedTie(teams.get(i), teams.get(i + 1)));
        }
        return winners;
    }

    private void displayLeagueTable(List<Team> tableTeams) {
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
