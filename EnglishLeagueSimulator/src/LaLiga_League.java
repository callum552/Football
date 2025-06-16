import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The main class for the Spanish LaLiga Simulator.
 * This version simulates the 20-team league, a simple Copa del Rey, and features
 * accurate European and relegation qualification rules.
 *
 * NOTE: This class has been refactored to work with the EuropeanCompetitionSimulator.
 */
public class LaLiga_League {
    private final List<Team> teams;
    private final List<Match> fixtures;
    private final MatchSimulator matchSimulator;
    private final Random random = new Random();

    private Team copaDelReyWinner;

    // Lists to store qualified teams
    private final List<Team> uclTeams = new ArrayList<>();
    private final List<Team> uelTeams = new ArrayList<>();
    private final List<Team> ueclTeams = new ArrayList<>();


    public LaLiga_League() {
        this.teams = new ArrayList<>();
        this.fixtures = new ArrayList<>();
        this.matchSimulator = new MatchSimulator();
    }

    // --- GETTERS FOR EUROPEAN QUALIFIERS ---
    public List<Team> getUclTeams() { return uclTeams; }
    public List<Team> getUelTeams() { return uelTeams; }
    public List<Team> getUeclTeams() { return ueclTeams; }


    public static void main(String[] args) {
        LaLiga_League laLiga = new LaLiga_League();

        System.out.println("--- Setting up LaLiga with real teams ---");
        laLiga.setupTeams();

        System.out.println("\n--- Simulating the Copa del Rey... ---");
        laLiga.simulateCopaDelRey();

        System.out.println("\n--- Generating League Fixtures ---");
        laLiga.generateFixtures();

        System.out.println("\n--- Simulating League Season with Dominance Model... ---");
        laLiga.simulateSeason();

        System.out.println("\n--- Determining European Spots ---");
        laLiga.determineEuropeanSpots();

        System.out.println("\n--- FINAL LALIGA TABLE ---");
        laLiga.displayTable();
    }

    public void setupTeams() {
        double initialElo = 1500;
        this.teams.add(new Team("Real Madrid", 97, 89, initialElo + 250));
        this.teams.add(new Team("Barcelona", 94, 85, initialElo + 220));
        this.teams.add(new Team("Atletico Madrid", 90, 87, initialElo + 190));
        this.teams.add(new Team("Athletic Bilbao", 85, 83, initialElo + 140));
        this.teams.add(new Team("Girona", 84, 78, initialElo + 120));
        this.teams.add(new Team("Real Sociedad", 84, 82, initialElo + 100));
        this.teams.add(new Team("Real Betis", 82, 80, initialElo + 90));
        this.teams.add(new Team("Villarreal", 83, 76, initialElo + 80));
        this.teams.add(new Team("Valencia", 79, 81, initialElo + 20));
        this.teams.add(new Team("Getafe", 75, 80, initialElo));
        this.teams.add(new Team("Osasuna", 77, 78, initialElo - 10));
        this.teams.add(new Team("Sevilla", 80, 75, initialElo - 20));
        this.teams.add(new Team("Alaves", 74, 77, initialElo - 50));
        this.teams.add(new Team("Celta Vigo", 76, 74, initialElo - 70));
        this.teams.add(new Team("Mallorca", 72, 79, initialElo - 90));
        this.teams.add(new Team("Las Palmas", 73, 75, initialElo - 110));
        this.teams.add(new Team("Rayo Vallecano", 75, 72, initialElo - 130));
        this.teams.add(new Team("Real Valladolid", 68, 70, initialElo - 200));
        this.teams.add(new Team("Cadiz", 65, 73, initialElo - 220));
        this.teams.add(new Team("Almeria", 67, 69, initialElo - 240));
        System.out.println("20 LaLiga teams have been created.");
    }

    public void simulateCopaDelRey() {
        List<Team> cupTeams = new ArrayList<>(this.teams);
        System.out.println("\n** Copa del Rey Preliminary Round **");
        List<Team> roundOf16Teams = getRoundOf16Teams(cupTeams);
        System.out.println("\n** Copa del Rey Round of 16 **");
        List<Team> quarterFinalists = simulateKnockoutRound(roundOf16Teams);
        System.out.println("\n** Copa del Rey Quarter-Finals **");
        List<Team> semiFinalists = simulateKnockoutRound(quarterFinalists);
        System.out.println("\n** Copa del Rey Semi-Finals **");
        List<Team> finalists = simulateKnockoutRound(semiFinalists);
        System.out.println("\n** Copa del Rey Final **");
        this.copaDelReyWinner = matchSimulator.simulateSingleMatch(finalists.get(0), finalists.get(1));
        System.out.println("\nCopa del Rey Winner: " + this.copaDelReyWinner.getName());
    }

    private List<Team> getRoundOf16Teams(List<Team> cupTeams) {
        Collections.shuffle(cupTeams, random);
        cupTeams.sort(Comparator.comparingDouble(Team::getEloRating).reversed());
        List<Team> preliminaryRoundTeams = new ArrayList<>(cupTeams.subList(12, 20));
        List<Team> teamsWithByes = new ArrayList<>(cupTeams.subList(0, 12));
        List<Team> preliminaryWinners = new ArrayList<>();
        for (int i = 0; i < preliminaryRoundTeams.size(); i += 2) {
            preliminaryWinners.add(matchSimulator.simulateSingleMatch(preliminaryRoundTeams.get(i), preliminaryRoundTeams.get(i+1)));
        }
        List<Team> roundOf16Teams = new ArrayList<>(teamsWithByes);
        roundOf16Teams.addAll(preliminaryWinners);
        Collections.shuffle(roundOf16Teams, random);
        return roundOf16Teams;
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

        // 2. Copa del Rey winner gets a UEL spot
        if (!qualifiedForEurope.contains(copaDelReyWinner)) {
            uelTeams.add(copaDelReyWinner);
            qualifiedForEurope.add(copaDelReyWinner);
        }

        // 3. Fill remaining UEL/UECL spots from league table
        int leagueSpotCounter = 4; // Start checking from 5th place

        // Find UEL spot (5th place, or 6th if cup winner is top 4)
        while (uelTeams.size() < 2 && leagueSpotCounter < this.teams.size()) {
            Team team = this.teams.get(leagueSpotCounter);
            if (!qualifiedForEurope.contains(team)) {
                uelTeams.add(team);
                qualifiedForEurope.add(team);
            }
            leagueSpotCounter++;
        }

        // Find UECL spot (next available league position)
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
        // Sorting is handled in determineEuropeanSpots
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

            if (position >= 18) {
                qualificationMarker += " [R]";
            }

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
        System.out.println("Legend: [C] Champions, [UCL] Champions League, [UEL] Europa League, [UECL] Europa Conference League, [R] Relegation");
        System.out.println("Cup Winner: [Copa del Rey: " + this.copaDelReyWinner.getName() + "]");
    }
}