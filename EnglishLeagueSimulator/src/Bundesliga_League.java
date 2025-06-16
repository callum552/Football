import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The main class for the German Bundesliga Simulator.
 * This version simulates the 18-team league, a simple DFB-Pokal, and features
 * the correct, Bundesliga-specific tie-breaker rules.
 *
 * NOTE: This class has been refactored to work with the EuropeanCompetitionSimulator.
 */
public class Bundesliga_League {
    private final List<Team> teams;
    private final List<Match> fixtures;
    private final MatchSimulator matchSimulator;
    private final Random random = new Random();

    private Team dfbPokalWinner;

    // Lists to store qualified teams
    private final List<Team> uclTeams = new ArrayList<>();
    private final List<Team> uelTeams = new ArrayList<>();
    private final List<Team> ueclTeams = new ArrayList<>();

    public Bundesliga_League() {
        this.teams = new ArrayList<>();
        this.fixtures = new ArrayList<>();
        this.matchSimulator = new MatchSimulator();
    }

    // --- GETTERS FOR EUROPEAN QUALIFIERS ---
    public List<Team> getUclTeams() { return uclTeams; }
    public List<Team> getUelTeams() { return uelTeams; }
    public List<Team> getUeclTeams() { return ueclTeams; }

    public static void main(String[] args) {
        Bundesliga_League bundesliga = new Bundesliga_League();

        System.out.println("--- Setting up the Bundesliga with real teams ---");
        bundesliga.setupTeams();

        System.out.println("\n--- Simulating the DFB-Pokal... ---");
        bundesliga.simulateDFBPokal();

        System.out.println("\n--- Generating League Fixtures ---");
        bundesliga.generateFixtures();

        System.out.println("\n--- Simulating League Season with Dominance Model... ---");
        bundesliga.simulateSeason();

        System.out.println("\n--- Determining European Spots ---");
        bundesliga.determineEuropeanSpots();

        System.out.println("\n--- FINAL BUNDESLIGA TABLE ---");
        bundesliga.displayTable();
    }

    public void setupTeams() {
        double initialElo = 1500;
        this.teams.add(new Team("Bayern Munich", 98, 88, initialElo + 280));
        this.teams.add(new Team("Borussia Dortmund", 89, 82, initialElo + 180));
        this.teams.add(new Team("RB Leipzig", 88, 84, initialElo + 170));
        this.teams.add(new Team("Bayer Leverkusen", 87, 81, initialElo + 160));
        this.teams.add(new Team("Eintracht Frankfurt", 84, 79, initialElo + 100));
        this.teams.add(new Team("SC Freiburg", 80, 80, initialElo + 50));
        this.teams.add(new Team("Union Berlin", 78, 83, initialElo + 40));
        this.teams.add(new Team("VfL Wolfsburg", 81, 78, initialElo + 30));
        this.teams.add(new Team("Borussia M'gladbach", 82, 75, initialElo + 20));
        this.teams.add(new Team("TSG Hoffenheim", 83, 74, initialElo + 10));
        this.teams.add(new Team("Mainz 05", 77, 77, initialElo));
        this.teams.add(new Team("FC Koln", 76, 79, initialElo - 20));
        this.teams.add(new Team("Werder Bremen", 79, 72, initialElo - 40));
        this.teams.add(new Team("FC Augsburg", 74, 76, initialElo - 60));
        this.teams.add(new Team("VfB Stuttgart", 75, 71, initialElo - 100));
        this.teams.add(new Team("VfL Bochum", 70, 73, initialElo - 150));
        this.teams.add(new Team("Hertha BSC", 68, 70, initialElo - 200));
        this.teams.add(new Team("Schalke 04", 67, 68, initialElo - 220));
        System.out.println("18 Bundesliga teams have been created.");
    }

    public void simulateDFBPokal() {
        List<Team> cupTeams = new ArrayList<>(this.teams);
        Collections.shuffle(cupTeams, random);

        cupTeams.sort(Comparator.comparingDouble(Team::getEloRating).reversed());
        List<Team> preliminaryTeams = new ArrayList<>(cupTeams.subList(14, 18));
        List<Team> teamsWithByes = new ArrayList<>(cupTeams.subList(0, 14));

        System.out.println("\n** DFB-Pokal Preliminary Round **");
        List<Team> preliminaryWinners = new ArrayList<>();
        for (int i = 0; i < preliminaryTeams.size(); i += 2) {
            preliminaryWinners.add(matchSimulator.simulateSingleMatch(preliminaryTeams.get(i), preliminaryTeams.get(i+1)));
        }

        List<Team> roundOf16Teams = new ArrayList<>(teamsWithByes);
        roundOf16Teams.addAll(preliminaryWinners);

        System.out.println("\n** DFB-Pokal Round of 16 **");
        List<Team> quarterFinalists = simulateKnockoutRound(roundOf16Teams);
        System.out.println("\n** DFB-Pokal Quarter-Finals **");
        List<Team> semiFinalists = simulateKnockoutRound(quarterFinalists);
        System.out.println("\n** DFB-Pokal Semi-Finals **");
        List<Team> finalists = simulateKnockoutRound(semiFinalists);
        System.out.println("\n** DFB-Pokal Final **");
        this.dfbPokalWinner = matchSimulator.simulateSingleMatch(finalists.get(0), finalists.get(1));
        System.out.println("\nDFB-Pokal Winner: " + this.dfbPokalWinner.getName());
    }

    private List<Team> simulateKnockoutRound(List<Team> teamsInRound) {
        List<Team> winners = new ArrayList<>();
        Collections.shuffle(teamsInRound, random);
        for (int i = 0; i < teamsInRound.size(); i += 2) {
            winners.add(matchSimulator.simulateSingleMatch(teamsInRound.get(i), teamsInRound.get(i + 1)));
        }
        return winners;
    }

    public void generateFixtures() {
        for (Team homeTeam : this.teams) {
            for (Team awayTeam : this.teams) {
                if (!homeTeam.equals(awayTeam)) {
                    this.fixtures.add(new Match(homeTeam, awayTeam));
                }
            }
        }
        System.out.println(this.fixtures.size() + " league matches have been scheduled.");
    }

    public void simulateSeason() {
        for (Match match : this.fixtures) {
            matchSimulator.simulateMatch(match);
        }
    }

    public void determineEuropeanSpots() {
        this.teams.sort(Comparator.comparingInt(Team::getPoints).reversed()
                .thenComparingInt(Team::getGoalDifference).reversed()
                .thenComparingInt(Team::getGoalsFor).reversed());

        Set<Team> qualifiedForEurope = new HashSet<>();

        // 1. UCL spots (Top 4)
        for (int i = 0; i < 4; i++) {
            uclTeams.add(this.teams.get(i));
        }
        qualifiedForEurope.addAll(uclTeams);

        // 2. DFB-Pokal winner gets a UEL spot
        if (!qualifiedForEurope.contains(dfbPokalWinner)) {
            uelTeams.add(dfbPokalWinner);
            qualifiedForEurope.add(dfbPokalWinner);
        }

        // 3. Fill remaining UEL/UECL spots from league table
        int leagueSpotCounter = 4; // Start checking from 5th place
        while (uelTeams.size() < 2 && leagueSpotCounter < this.teams.size()) {
            Team team = this.teams.get(leagueSpotCounter);
            if (!qualifiedForEurope.contains(team)) {
                uelTeams.add(team);
                qualifiedForEurope.add(team);
            }
            leagueSpotCounter++;
        }

        // UECL spot
        while (ueclTeams.isEmpty() && leagueSpotCounter < this.teams.size()) {
            Team team = this.teams.get(leagueSpotCounter);
            if (!qualifiedForEurope.contains(team)) {
                ueclTeams.add(team);
                qualifiedForEurope.add(team);
            }
            leagueSpotCounter++;
        }
    }

    public void displayTable() {
        // Final sort is done in determineEuropeanSpots, so we just display here
        System.out.println("Pos | Team                     | P  | W  | D  | L  | GF | GA | GD  | Pts | Elo ");
        System.out.println("------------------------------------------------------------------------------------");

        int position = 1;
        for (Team team : this.teams) {
            String teamDisplayName = team.getName();
            String qualificationMarker = "";

            if (uclTeams.contains(team)) {
                qualificationMarker = (position == 1) ? " [C][UCL]" : " [UCL]";
            } else if (uelTeams.contains(team)) {
                qualificationMarker = " [UEL]";
            } else if (ueclTeams.contains(team)) {
                qualificationMarker = " [UECL]";
            }

            if (position == 16) qualificationMarker += " [RPO]";
            else if (position >= 17) qualificationMarker += " [R]";

            teamDisplayName += qualificationMarker.trim();

            System.out.printf("%-3d | %-26s | %-2d | %-2d | %-2d | %-2d | %-2d | %-2d | %-3d | %-3d | %.0f%n",
                    position++,
                    teamDisplayName,
                    team.gamesPlayed,
                    team.wins,
                    team.draws,
                    team.losses,
                    team.goalsFor,
                    team.goalsAgainst,
                    team.goalDifference,
                    team.getPoints(),
                    team.getEloRating());
        }

        System.out.println("------------------------------------------------------------------------------------");
        System.out.println("Legend: [C] Champions, [UCL] Champions League, [UEL] Europa League, [UECL] Europa Conference League");
        System.out.println("        [RPO] Relegation Play-off, [R] Relegation");
        System.out.println("Cup Winner: [DFB-Pokal: " + this.dfbPokalWinner.getName() + "]");
    }
}