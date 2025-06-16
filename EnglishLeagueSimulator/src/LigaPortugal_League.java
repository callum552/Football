import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The main class for the Portuguese Liga Portugal Simulator.
 * This version simulates the 18-team league, a Taça de Portugal, and is tuned
 * to ensure the dominance of the "Big Three".
 *
 * NOTE: This class has been refactored to work with the EuropeanCompetitionSimulator.
 */
public class LigaPortugal_League {
    private final List<Team> teams;
    private final List<Match> fixtures;
    private final MatchSimulator matchSimulator;
    private final Random random = new Random();

    private Team tacaDePortugalWinner;

    // Lists to store qualified teams
    private final List<Team> uclTeams = new ArrayList<>();
    private final List<Team> uelTeams = new ArrayList<>();
    private final List<Team> ueclTeams = new ArrayList<>();

    public LigaPortugal_League() {
        this.teams = new ArrayList<>();
        this.fixtures = new ArrayList<>();
        this.matchSimulator = new MatchSimulator();
    }

    // --- GETTERS FOR EUROPEAN QUALIFIERS ---
    public List<Team> getUclTeams() { return uclTeams; }
    public List<Team> getUelTeams() { return uelTeams; }
    public List<Team> getUeclTeams() { return ueclTeams; }


    public static void main(String[] args) {
        LigaPortugal_League ligaPortugal = new LigaPortugal_League();

        System.out.println("--- Setting up Liga Portugal with real teams ---");
        ligaPortugal.setupTeams();

        System.out.println("\n--- Simulating the Taça de Portugal... ---");
        ligaPortugal.simulateTacaDePortugal();

        System.out.println("\n--- Generating League Fixtures ---");
        ligaPortugal.generateFixtures();

        System.out.println("\n--- Simulating League Season with Dominance Model... ---");
        ligaPortugal.simulateSeason();

        System.out.println("\n--- Determining European Spots ---");
        ligaPortugal.determineEuropeanSpots();

        System.out.println("\n--- FINAL LIGA PORTUGAL TABLE ---");
        ligaPortugal.displayTable();
    }

    public void setupTeams() {
        double initialElo = 1500;
        this.teams.add(new Team("SL Benfica", 92, 85, initialElo + 250));
        this.teams.add(new Team("FC Porto", 91, 84, initialElo + 240));
        this.teams.add(new Team("Sporting CP", 90, 83, initialElo + 230));
        this.teams.add(new Team("SC Braga", 85, 78, initialElo + 150));
        this.teams.add(new Team("Vitoria de Guimaraes", 80, 79, initialElo + 100));
        this.teams.add(new Team("Moreirense", 76, 77, initialElo + 20));
        this.teams.add(new Team("Arouca", 78, 74, initialElo + 10));
        this.teams.add(new Team("Famalicao", 75, 76, initialElo));
        this.teams.add(new Team("Farense", 74, 72, initialElo - 20));
        this.teams.add(new Team("Casa Pia", 70, 75, initialElo - 50));
        this.teams.add(new Team("Gil Vicente", 73, 71, initialElo - 60));
        this.teams.add(new Team("Boavista", 72, 73, initialElo - 70));
        this.teams.add(new Team("Estoril Praia", 74, 70, initialElo - 90));
        this.teams.add(new Team("Estrela da Amadora", 68, 72, initialElo - 150));
        this.teams.add(new Team("Rio Ave", 69, 74, initialElo - 160));
        this.teams.add(new Team("Chaves", 67, 69, initialElo - 200));
        this.teams.add(new Team("Portimonense", 66, 68, initialElo - 220));
        this.teams.add(new Team("Vizela", 65, 67, initialElo - 240));
        System.out.println("18 Liga Portugal teams have been created.");
    }

    public void simulateTacaDePortugal() {
        List<Team> cupTeams = new ArrayList<>(this.teams);
        cupTeams.sort(Comparator.comparingDouble(Team::getEloRating).reversed());
        List<Team> preliminaryTeams = new ArrayList<>(cupTeams.subList(14, 18));
        List<Team> teamsWithByes = new ArrayList<>(cupTeams.subList(0, 14));

        System.out.println("\n** Taça de Portugal Preliminary Round **");
        List<Team> preliminaryWinners = new ArrayList<>();
        for (int i = 0; i < preliminaryTeams.size(); i += 2) {
            preliminaryWinners.add(matchSimulator.simulateSingleMatch(preliminaryTeams.get(i), preliminaryTeams.get(i+1)));
        }

        List<Team> roundOf16Teams = new ArrayList<>(teamsWithByes);
        roundOf16Teams.addAll(preliminaryWinners);

        System.out.println("\n** Taça de Portugal Round of 16 **");
        List<Team> quarterFinalists = simulateKnockoutRound(roundOf16Teams);
        System.out.println("\n** Taça de Portugal Quarter-Finals **");
        List<Team> semiFinalists = simulateKnockoutRound(quarterFinalists);
        System.out.println("\n** Taça de Portugal Semi-Finals **");
        List<Team> finalists = simulateKnockoutRound(semiFinalists);
        System.out.println("\n** Taça de Portugal Final **");
        this.tacaDePortugalWinner = matchSimulator.simulateSingleMatch(finalists.get(0), finalists.get(1));
        System.out.println("\nTaça de Portugal Winner: " + this.tacaDePortugalWinner.getName());
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

        // 1. UCL spots (Top 2 direct, 3rd is qualifier)
        uclTeams.add(this.teams.get(0));
        uclTeams.add(this.teams.get(1));
        uclTeams.add(this.teams.get(2)); // UCL-Q
        qualifiedForEurope.addAll(uclTeams);

        // 2. Taça de Portugal winner gets a UEL spot
        if (!qualifiedForEurope.contains(tacaDePortugalWinner)) {
            uelTeams.add(tacaDePortugalWinner);
            qualifiedForEurope.add(tacaDePortugalWinner);
        }

        // 3. Fill remaining UEL/UECL spots from league table
        int leagueSpotCounter = 3; // Start checking from 4th place

        // Find UEL spot (4th place, or passed down)
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
                if (position <= 2) {
                    qualificationMarker = (position == 1) ? " [C][UCL]" : " [UCL]";
                } else {
                    qualificationMarker = " [UCL-Q]";
                }
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
        System.out.println("Legend: [C] Champions, [UCL] Champions League, [UCL-Q] UCL Qualifiers, [UEL] Europa League, [UECL] Europa Conference League");
        System.out.println("        [RPO] Relegation Play-off, [R] Relegation");
        System.out.println("Cup Winner: [Taça de Portugal: " + this.tacaDePortugalWinner.getName() + "]");
    }
}