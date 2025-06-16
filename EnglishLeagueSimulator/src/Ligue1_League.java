import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The main class for the French Ligue 1 Simulator.
 * This version simulates the 18-team league, a Coupe de France, and features
 * accurate European and relegation qualification rules.
 *
 * NOTE: This class has been refactored to work with the EuropeanCompetitionSimulator.
 */
public class Ligue1_League {
    private final List<Team> teams;
    private final List<Match> fixtures;
    private final MatchSimulator matchSimulator;
    private final Random random = new Random();

    private Team coupeDeFranceWinner;

    // Lists to store qualified teams
    private final List<Team> uclTeams = new ArrayList<>();
    private final List<Team> uelTeams = new ArrayList<>();
    private final List<Team> ueclTeams = new ArrayList<>();


    public Ligue1_League() {
        this.teams = new ArrayList<>();
        this.fixtures = new ArrayList<>();
        this.matchSimulator = new MatchSimulator();
    }

    // --- GETTERS FOR EUROPEAN QUALIFIERS ---
    public List<Team> getUclTeams() { return uclTeams; }
    public List<Team> getUelTeams() { return uelTeams; }
    public List<Team> getUeclTeams() { return ueclTeams; }


    public static void main(String[] args) {
        Ligue1_League ligue1 = new Ligue1_League();

        System.out.println("--- Setting up Ligue 1 with real teams ---");
        ligue1.setupTeams();

        System.out.println("\n--- Simulating the Coupe de France... ---");
        ligue1.simulateCoupeDeFrance();

        System.out.println("\n--- Generating League Fixtures ---");
        ligue1.generateFixtures();

        System.out.println("\n--- Simulating League Season with Dominance Model... ---");
        ligue1.simulateSeason();

        System.out.println("\n--- Determining European Spots ---");
        ligue1.determineEuropeanSpots();

        System.out.println("\n--- FINAL LIGUE 1 TABLE ---");
        ligue1.displayTable();
    }

    public void setupTeams() {
        double initialElo = 1500;
        this.teams.add(new Team("Paris Saint-Germain", 98, 86, initialElo + 300));
        this.teams.add(new Team("AS Monaco", 88, 80, initialElo + 180));
        this.teams.add(new Team("Marseille", 86, 82, initialElo + 160));
        this.teams.add(new Team("Lille", 85, 84, initialElo + 150));
        this.teams.add(new Team("Lyon", 84, 81, initialElo + 130));
        this.teams.add(new Team("Nice", 79, 85, initialElo + 90));
        this.teams.add(new Team("Rennes", 83, 78, initialElo + 80));
        this.teams.add(new Team("Lens", 81, 80, initialElo + 70));
        this.teams.add(new Team("Reims", 78, 79, initialElo + 20));
        this.teams.add(new Team("Strasbourg", 77, 76, initialElo));
        this.teams.add(new Team("Toulouse", 76, 77, initialElo - 10));
        this.teams.add(new Team("Montpellier", 79, 74, initialElo - 30));
        this.teams.add(new Team("Nantes", 74, 75, initialElo - 80));
        this.teams.add(new Team("Le Havre", 70, 78, initialElo - 100));
        this.teams.add(new Team("Brest", 75, 73, initialElo - 120));
        this.teams.add(new Team("Metz", 68, 71, initialElo - 200));
        this.teams.add(new Team("Lorient", 71, 69, initialElo - 220));
        this.teams.add(new Team("Clermont Foot", 66, 70, initialElo - 240));
        System.out.println("18 Ligue 1 teams have been created.");
    }

    public void simulateCoupeDeFrance() {
        List<Team> cupTeams = new ArrayList<>(this.teams);
        cupTeams.sort(Comparator.comparingDouble(Team::getEloRating).reversed());
        List<Team> preliminaryTeams = new ArrayList<>(cupTeams.subList(14, 18));
        List<Team> teamsWithByes = new ArrayList<>(cupTeams.subList(0, 14));

        System.out.println("\n** Coupe de France Preliminary Round **");
        List<Team> preliminaryWinners = new ArrayList<>();
        for (int i = 0; i < preliminaryTeams.size(); i += 2) {
            preliminaryWinners.add(matchSimulator.simulateSingleMatch(preliminaryTeams.get(i), preliminaryTeams.get(i+1)));
        }

        List<Team> roundOf16Teams = new ArrayList<>(teamsWithByes);
        roundOf16Teams.addAll(preliminaryWinners);

        System.out.println("\n** Coupe de France Round of 16 **");
        List<Team> quarterFinalists = simulateKnockoutRound(roundOf16Teams);
        System.out.println("\n** Coupe de France Quarter-Finals **");
        List<Team> semiFinalists = simulateKnockoutRound(quarterFinalists);
        System.out.println("\n** Coupe de France Semi-Finals **");
        List<Team> finalists = simulateKnockoutRound(semiFinalists);
        System.out.println("\n** Coupe de France Final **");
        this.coupeDeFranceWinner = matchSimulator.simulateSingleMatch(finalists.get(0), finalists.get(1));
        System.out.println("\nCoupe de France Winner: " + this.coupeDeFranceWinner.getName());
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

        // 2. Coupe de France winner gets a UEL spot
        if (!qualifiedForEurope.contains(coupeDeFranceWinner)) {
            uelTeams.add(coupeDeFranceWinner);
            qualifiedForEurope.add(coupeDeFranceWinner);
        }

        // 3. Fill remaining UEL/UECL spots from league table
        int leagueSpotCounter = 4; // Start checking from 5th place

        // Find UEL spot (5th place, or passed down)
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
        System.out.println("Cup Winner: [Coupe de France: " + this.coupeDeFranceWinner.getName() + "]");
    }
}