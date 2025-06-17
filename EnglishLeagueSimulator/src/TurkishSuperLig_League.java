import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The main class for the Turkish Süper Lig Simulator.
 * This version simulates the 18-team league, a simple Turkish Cup,
 * and features accurate European and relegation qualification rules.
 */
public class TurkishSuperLig_League {
    private final List<Team> teams;
    private final List<Match> fixtures;
    private final MatchSimulator matchSimulator;
    private final Random random = new Random();

    private Team turkishCupWinner;

    // Lists to store qualified teams
    private final List<Team> uclTeams = new ArrayList<>();
    private final List<Team> uelTeams = new ArrayList<>();
    private final List<Team> ueclTeams = new ArrayList<>();

    public TurkishSuperLig_League() {
        this.teams = new ArrayList<>();
        this.fixtures = new ArrayList<>();
        this.matchSimulator = new MatchSimulator();
    }

    // --- GETTERS FOR EUROPEAN QUALIFIERS ---
    public List<Team> getUclTeams() {
        return uclTeams;
    }

    public List<Team> getUelTeams() {
        return uelTeams;
    }

    public List<Team> getUeclTeams() {
        return ueclTeams;
    }

    public static void main(String[] args) {
        TurkishSuperLig_League turkishSuperLig = new TurkishSuperLig_League();

        System.out.println("--- Setting up Turkish Süper Lig with real teams ---");
        turkishSuperLig.setupTeams();

        System.out.println("\n--- Simulating the Turkish Cup... ---");
        turkishSuperLig.simulateTurkishCup();

        System.out.println("\n--- Generating League Fixtures ---");
        turkishSuperLig.generateFixtures();

        System.out.println("\n--- Simulating League Season with Dominance Model... ---");
        turkishSuperLig.simulateSeason();

        System.out.println("\n--- Determining European Spots ---");
        turkishSuperLig.determineEuropeanSpots();

        System.out.println("\n--- FINAL TURKISH SÜPER LIG TABLE ---");
        turkishSuperLig.displayTable();
    }

    public void setupTeams() {
        double initialElo = 1500;
        this.teams.add(new Team("Galatasaray", 87, 83, initialElo + 180));
        this.teams.add(new Team("Fenerbahçe", 86, 82, initialElo + 170));
        this.teams.add(new Team("Beşiktaş", 83, 80, initialElo + 140));
        this.teams.add(new Team("Trabzonspor", 81, 79, initialElo + 100));
        this.teams.add(new Team("İstanbul Başakşehir", 79, 77, initialElo + 50));
        this.teams.add(new Team("Adana Demirspor", 77, 75, initialElo + 20));
        this.teams.add(new Team("Konyaspor", 75, 74, initialElo));
        this.teams.add(new Team("Sivasspor", 74, 73, initialElo - 10));
        this.teams.add(new Team("Antalyaspor", 73, 72, initialElo - 20));
        this.teams.add(new Team("Kayserispor", 72, 71, initialElo - 30));
        this.teams.add(new Team("Gaziantep FK", 71, 70, initialElo - 40));
        this.teams.add(new Team("Alanyaspor", 70, 69, initialElo - 50));
        this.teams.add(new Team("Fatih Karagümrük", 69, 68, initialElo - 60));
        this.teams.add(new Team("Hatayspor", 68, 67, initialElo - 70));
        this.teams.add(new Team("Ankaragücü", 67, 66, initialElo - 80));
        this.teams.add(new Team("Pendikspor", 66, 65, initialElo - 90));
        this.teams.add(new Team("Samsunspor", 65, 64, initialElo - 100));
        this.teams.add(new Team("Çaykur Rizespor", 64, 63, initialElo - 110));
        System.out.println("18 Turkish Süper Lig teams have been created.");
    }

    public void simulateTurkishCup() {
        List<Team> cupTeams = new ArrayList<>(this.teams);
        Collections.shuffle(cupTeams, random);

        cupTeams.sort(Comparator.comparingDouble(Team::getEloRating).reversed());
        List<Team> preliminaryTeams = new ArrayList<>(cupTeams.subList(14, 18)); // Example: lower 4 teams play
                                                                                 // preliminary
        List<Team> teamsWithByes = new ArrayList<>(cupTeams.subList(0, 14)); // Top 14 get byes

        System.out.println("\n** Turkish Cup Preliminary Round **");
        List<Team> preliminaryWinners = new ArrayList<>();
        for (int i = 0; i < preliminaryTeams.size(); i += 2) {
            preliminaryWinners
                    .add(matchSimulator.simulateSingleMatch(preliminaryTeams.get(i), preliminaryTeams.get(i + 1)));
        }

        List<Team> roundOf16Teams = new ArrayList<>(teamsWithByes);
        roundOf16Teams.addAll(preliminaryWinners);

        System.out.println("\n** Turkish Cup Round of 16 **");
        List<Team> quarterFinalists = simulateKnockoutRound(roundOf16Teams);
        System.out.println("\n** Turkish Cup Quarter-Finals **");
        List<Team> semiFinalists = simulateKnockoutRound(quarterFinalists);
        System.out.println("\n** Turkish Cup Semi-Finals **");
        List<Team> finalists = simulateKnockoutRound(semiFinalists);
        System.out.println("\n** Turkish Cup Final **");
        this.turkishCupWinner = matchSimulator.simulateSingleMatch(finalists.get(0), finalists.get(1));
        System.out.println("\nTurkish Cup Winner: " + this.turkishCupWinner.getName());
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

        // 1. UCL spots (Champion and Runner-up for qualifiers)
        uclTeams.add(this.teams.get(0)); // Champion
        uclTeams.add(this.teams.get(1)); // Runner-up
        qualifiedForEurope.addAll(uclTeams);

        // 2. Turkish Cup winner gets a UEL spot
        if (!qualifiedForEurope.contains(turkishCupWinner)) {
            uelTeams.add(turkishCupWinner);
            qualifiedForEurope.add(turkishCupWinner);
        }

        // 3. Fill remaining UEL/UECL spots from league table
        int leagueSpotCounter = 2; // Start checking from 3rd place

        // Find UEL spot (3rd place, or passed down if cup winner is higher)
        while (uelTeams.size() < 1 && leagueSpotCounter < this.teams.size()) {
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
                qualificationMarker = (position <= 2) ? " [UCL-Q]" : ""; // Both top 2 are UCL Qualifiers
                if (position == 1)
                    qualificationMarker = " [C]" + qualificationMarker;
            } else if (uelTeams.contains(team)) {
                qualificationMarker = " [UEL]";
            } else if (ueclTeams.contains(team)) {
                qualificationMarker = " [UECL]";
            }

            if (position >= 17) { // Assuming 2 direct relegations
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
        System.out.println(
                "Legend: [C] Champions, [UCL-Q] UCL Qualifiers, [UEL] Europa League, [UECL] Europa Conference League, [R] Relegation");
        System.out.println("Cup Winner: [Turkish Cup: " + this.turkishCupWinner.getName() + "]");
    }
}