import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The main class for the Dutch Eredivisie Simulator.
 * This version simulates the 18-team league, a simplified KNVB Beker, and the post-season
 * European play-offs.
 *
 * NOTE: This class has been refactored to work with the EuropeanCompetitionSimulator.
 */
public class Eredivisie_League {
    private final List<Team> teams;
    private final List<Match> fixtures;
    private final EuropeanMatchSimulator matchSimulator; // Use the European simulator for two-legged ties
    private final Random random = new Random();

    private Team knvbBekerWinner;
    private Team ueclPlayoffWinner;

    // Lists to store qualified teams
    private final List<Team> uclTeams = new ArrayList<>();
    private final List<Team> uelTeams = new ArrayList<>();
    private final List<Team> ueclTeams = new ArrayList<>();
    private final List<Team> ueclPlayoffTeams = new ArrayList<>();


    public Eredivisie_League() {
        this.teams = new ArrayList<>();
        this.fixtures = new ArrayList<>();
        this.matchSimulator = new EuropeanMatchSimulator();
    }

    // --- GETTERS FOR EUROPEAN QUALIFIERS ---
    public List<Team> getUclTeams() { return uclTeams; }
    public List<Team> getUelTeams() { return uelTeams; }
    public List<Team> getUeclTeams() { return ueclTeams; }


    public static void main(String[] args) {
        Eredivisie_League eredivisie = new Eredivisie_League();

        System.out.println("--- Setting up Eredivisie with real teams ---");
        eredivisie.setupTeams();

        System.out.println("\n--- Simulating the KNVB Beker... ---");
        eredivisie.simulateKNVBBeker();

        System.out.println("\n--- Generating League Fixtures ---");
        eredivisie.generateFixtures();

        System.out.println("\n--- Simulating League Season with Dominance Model... ---");
        eredivisie.simulateSeason();

        System.out.println("\n--- Simulating European Play-offs... ---");
        eredivisie.simulateUECLPlayoffs();

        System.out.println("\n--- Determining European Spots ---");
        eredivisie.determineEuropeanSpots();

        System.out.println("\n--- FINAL EREDIVISIE TABLE ---");
        eredivisie.displayTable();
    }

    public void setupTeams() {
        double initialElo = 1500;
        this.teams.add(new Team("PSV Eindhoven", 92, 84, initialElo + 240));
        this.teams.add(new Team("Feyenoord", 90, 85, initialElo + 220));
        this.teams.add(new Team("Ajax", 88, 80, initialElo + 200));
        this.teams.add(new Team("FC Twente", 85, 82, initialElo + 160));
        this.teams.add(new Team("AZ Alkmaar", 84, 79, initialElo + 150));
        this.teams.add(new Team("FC Utrecht", 80, 78, initialElo + 80));
        this.teams.add(new Team("Sparta Rotterdam", 77, 79, initialElo + 50));
        this.teams.add(new Team("NEC Nijmegen", 78, 76, initialElo + 40));
        this.teams.add(new Team("Go Ahead Eagles", 75, 75, initialElo));
        this.teams.add(new Team("Fortuna Sittard", 72, 77, initialElo - 20));
        this.teams.add(new Team("Heerenveen", 76, 73, initialElo - 30));
        this.teams.add(new Team("PEC Zwolle", 74, 71, initialElo - 50));
        this.teams.add(new Team("Heracles Almelo", 70, 74, initialElo - 100));
        this.teams.add(new Team("Almere City", 68, 72, initialElo - 120));
        this.teams.add(new Team("RKC Waalwijk", 69, 70, initialElo - 150));
        this.teams.add(new Team("Excelsior", 67, 68, initialElo - 200));
        this.teams.add(new Team("FC Volendam", 65, 69, initialElo - 220));
        this.teams.add(new Team("Willem II", 64, 66, initialElo - 240));
        System.out.println("18 Eredivisie teams have been created.");
    }

    public void simulateKNVBBeker() {
        List<Team> cupTeams = new ArrayList<>(this.teams);
        cupTeams.sort(Comparator.comparingDouble(Team::getEloRating).reversed());
        List<Team> preliminaryTeams = new ArrayList<>(cupTeams.subList(14, 18));
        List<Team> teamsWithByes = new ArrayList<>(cupTeams.subList(0, 14));

        System.out.println("\n** KNVB Beker Preliminary Round **");
        List<Team> preliminaryWinners = new ArrayList<>();
        for (int i = 0; i < preliminaryTeams.size(); i += 2) {
            preliminaryWinners.add(matchSimulator.simulateSingleMatch(preliminaryTeams.get(i), preliminaryTeams.get(i+1)));
        }

        List<Team> roundOf16Teams = new ArrayList<>(teamsWithByes);
        roundOf16Teams.addAll(preliminaryWinners);

        System.out.println("\n** KNVB Beker Round of 16 **");
        List<Team> quarterFinalists = simulateKnockoutRound(roundOf16Teams);
        System.out.println("\n** KNVB Beker Quarter-Finals **");
        List<Team> semiFinalists = simulateKnockoutRound(quarterFinalists);
        System.out.println("\n** KNVB Beker Semi-Finals **");
        List<Team> finalists = simulateKnockoutRound(semiFinalists);
        System.out.println("\n** KNVB Beker Final **");
        this.knvbBekerWinner = matchSimulator.simulateSingleMatch(finalists.get(0), finalists.get(1));
        System.out.println("\nKNVB Beker Winner: " + this.knvbBekerWinner.getName());
    }

    private List<Team> simulateKnockoutRound(List<Team> teamsInRound) {
        List<Team> winners = new ArrayList<>();
        Collections.shuffle(teamsInRound, random);
        for (int i = 0; i < teamsInRound.size(); i += 2) {
            winners.add(matchSimulator.simulateSingleMatch(teamsInRound.get(i), teamsInRound.get(i + 1)));
        }
        return winners;
    }

    public void simulateUECLPlayoffs() {
        this.teams.sort(Comparator.comparingInt(Team::getPoints).reversed()
                .thenComparingInt(Team::getGoalDifference).reversed()
                .thenComparingInt(Team::getGoalsFor).reversed());

        // Teams from 5th to 8th enter the playoffs
        for (int i = 4; i < 8 && i < this.teams.size(); i++) {
            ueclPlayoffTeams.add(this.teams.get(i));
        }

        if (ueclPlayoffTeams.size() < 4) {
            System.out.println("\nNot enough teams for European Play-offs.");
            return;
        }

        System.out.println("\n** European Play-off Semi-Finals **");
        Team final1Winner = matchSimulator.simulateTwoLeggedTie(ueclPlayoffTeams.get(0), ueclPlayoffTeams.get(3)); // 5th vs 8th
        Team final2Winner = matchSimulator.simulateTwoLeggedTie(ueclPlayoffTeams.get(1), ueclPlayoffTeams.get(2)); // 6th vs 7th

        System.out.println("\n** European Play-off Final **");
        this.ueclPlayoffWinner = matchSimulator.simulateTwoLeggedTie(final1Winner, final2Winner);
        System.out.println("\nUECL Play-off Winner: " + this.ueclPlayoffWinner.getName());
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
        // Table is already sorted from simulateUECLPlayoffs
        Set<Team> qualifiedForEurope = new HashSet<>();

        // 1. UCL
        uclTeams.add(this.teams.get(0)); // Champion
        uclTeams.add(this.teams.get(1)); // Runner-up
        uclTeams.add(this.teams.get(2)); // Qualifier
        qualifiedForEurope.addAll(uclTeams);

        // 2. UEL
        if (!qualifiedForEurope.contains(knvbBekerWinner)) {
            uelTeams.add(knvbBekerWinner);
            qualifiedForEurope.add(knvbBekerWinner);
        }

        // If cup winner spot is not used, it passes to the league
        int leagueSpotCounter = 3; // Start checking from 4th place
        while (uelTeams.isEmpty() && leagueSpotCounter < this.teams.size()) {
            Team team = this.teams.get(leagueSpotCounter);
            if (!qualifiedForEurope.contains(team)) {
                uelTeams.add(team);
                qualifiedForEurope.add(team);
            }
            leagueSpotCounter++;
        }

        // 3. UECL
        if (this.ueclPlayoffWinner != null && !qualifiedForEurope.contains(ueclPlayoffWinner)) {
            ueclTeams.add(this.ueclPlayoffWinner);
        }
    }


    public void displayTable() {
        // Final sort of the table
        this.teams.sort(Comparator.comparingInt(Team::getPoints).reversed()
                .thenComparingInt(Team::getGoalDifference).reversed()
                .thenComparingInt(Team::getGoalsFor).reversed());

        System.out.println("Pos | Team                     | P  | W  | D  | L  | GF | GA | GD  | Pts | Elo ");
        System.out.println("------------------------------------------------------------------------------------");

        int position = 1;
        for (Team team : this.teams) {
            String teamDisplayName = team.getName();
            String qualificationMarker = "";

            if (uclTeams.contains(team)) {
                qualificationMarker = (position <= 2) ? " [UCL]" : " [UCL-Q]";
                if(position == 1) qualificationMarker = " [C]" + qualificationMarker;
            } else if (uelTeams.contains(team)) {
                qualificationMarker = " [UEL]";
            } else if (ueclTeams.contains(team)) {
                qualificationMarker = " [UECL]";
            } else if (ueclPlayoffTeams.contains(team)) {
                qualificationMarker = " [UECL-PO]";
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
        System.out.println("Legend: [C] Champions, [UCL] Champions League, [UCL-Q] UCL Qualifiers, [UEL] Europa League");
        System.out.println("        [UECL] Conference League (Play-off Winner), [UECL-PO] Conference League Play-offs");
        System.out.println("        [RPO] Relegation Play-off, [R] Relegation");
        System.out.println("Cup Winner: [KNVB Beker: " + this.knvbBekerWinner.getName() + "]");
        if(this.ueclPlayoffWinner != null){
            System.out.println("UECL Play-off Winner: " + this.ueclPlayoffWinner.getName());
        }
    }
}